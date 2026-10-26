package com.neuedu.system.demoai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neuedu.common.core.domain.model.LoginUser;
import com.neuedu.web.service.TokenService;
import com.neuedu.system.demoai.assistant.GogoAssistantService;
import com.neuedu.system.demoai.service.AIChatConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ai/chat")
public class AIChatWebSocket {
    private static final Map<Session, ChatState> STATES = new ConcurrentHashMap<>();
    private static ObjectMapper mapper = new ObjectMapper();
    private static GogoAssistantService assistantService;
    private static TokenService tokenService;
    private static AIChatConversationService conversationService;

    @Autowired public void setAssistantService(GogoAssistantService service) { AIChatWebSocket.assistantService = service; }
    @Autowired public void setTokenService(TokenService service) { AIChatWebSocket.tokenService = service; }
    @Autowired public void setConversationService(AIChatConversationService service) { AIChatWebSocket.conversationService = service; }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            ChatRequest request = mapper.readValue(message, ChatRequest.class);
            String question = request.getQuestion() == null ? "" : request.getQuestion().trim();
            LoginUser loginUser = tokenService == null ? null : tokenService.getLoginUserFromToken(request.getToken());
            Long userId = loginUser == null ? null : loginUser.getUserId();
            if ("clear".equals(request.getAction())) {
                ChatState state = stateFor(session, userId);
                if (conversationService != null && state.conversationKey != null && userId != null) {
                    try { conversationService.clear(state.conversationKey, userId); }
                    catch (Exception e) { log.debug("清理 AI 会话记录失败，错误类型={}", e.getClass().getSimpleName()); }
                }
                state.conversationKey = null;
                state.history.clear();
                sendPayload(session, "conversation", "");
                sendPayload(session, "history", Collections.emptyList());
                send(session, "end", "");
                return;
            }
            if ("resume".equals(request.getAction())) {
                ChatState state = stateFor(session, userId);
                resumeConversation(session, request.getConversationId(), userId, state);
                send(session, "end", "");
                return;
            }
            if (question.isEmpty()) { send(session, "stream", "请告诉我您想咨询什么。"); send(session, "end", ""); return; }
            if (question.length() > 1000) { send(session, "error", "问题最多支持 1000 个字符。"); return; }
            if (isOrderQuery(question) && loginUser == null) {
                send(session, "stream", "为保护订单信息，请先登录；登录后我可以只查询您自己的订单状态。");
                send(session, "handoff", ""); send(session, "end", ""); return;
            }
            ChatState state = stateFor(session, userId);
            AIChatConversationService.Opened opened = openConversation(request.getConversationId(), userId, state);
            if (opened != null && opened.getKey() != null) {
                state.conversationKey = opened.getKey();
                sendPayload(session, "conversation", opened.getKey());
            }
            List<ObjectNode> history = opened == null ? new ArrayList<>(state.history) : opened.getHistory();
            StringBuilder answer = new StringBuilder();
            assistantService.answer(question, request.getProductId(), loginUser, history,
                    text -> { answer.append(text); send(session, "stream", text); }, sources -> sendPayload(session, "sources", sources),
                    result -> sendPayload(session, "tool_result", result),
                    required -> { if (required) send(session, "handoff", ""); });
            if (opened != null && opened.getKey() != null && conversationService != null) {
                try { conversationService.saveExchange(opened.getKey(), userId, question, answer.toString()); }
                catch (Exception e) { log.debug("保存 AI 会话记录失败，错误类型={}", e.getClass().getSimpleName()); }
            } else {
                appendFallbackHistory(state, question, answer.toString());
            }
            send(session, "end", "");
        } catch (Exception e) {
            log.warn("客服消息处理失败，会话ID={}，错误类型={}", session.getId(), e.getClass().getSimpleName());
            send(session, "stream", "客服服务暂时不可用，请稍后重试或联系人工客服。");
            send(session, "handoff", ""); send(session, "end", "");
        }
    }

    private boolean isOrderQuery(String question) { return question.matches(".*(订单|物流|发货状态|我的购买).*" ); }
    private void send(Session session, String type, String content) { Map<String, Object> payload=new HashMap<>(); payload.put("type",type); payload.put("content",content); sendPayload(session,payload); }
    private void sendPayload(Session session, String type, Object content) { Map<String,Object> payload=new HashMap<>(); payload.put("type",type); payload.put("content",content); sendPayload(session,payload); }
    private void sendPayload(Session session, Object payload) { try { if (session != null && session.isOpen()) session.getBasicRemote().sendText(mapper.writeValueAsString(payload)); } catch(Exception e) { log.debug("发送客服消息失败，会话ID={}", session == null ? "unknown" : session.getId()); } }

    @OnClose public void onClose(Session session) { STATES.remove(session); }
    @OnError public void onError(Session session, Throwable error) { log.debug("客服WebSocket关闭，会话ID={}", session == null ? "unknown" : session.getId()); STATES.remove(session); }

    private void resumeConversation(Session session, String requestedKey, Long userId, ChatState state) {
        AIChatConversationService.Opened opened = openConversation(requestedKey, userId, state);
        if (opened != null && opened.getKey() != null) {
            state.conversationKey = opened.getKey();
            state.history = opened.getHistory();
            sendPayload(session, "conversation", opened.getKey());
            sendPayload(session, "history", toHistoryPayload(opened.getHistory()));
        } else {
            state.conversationKey = null;
            state.history.clear();
            sendPayload(session, "conversation", "");
            sendPayload(session, "history", Collections.emptyList());
        }
    }

    private AIChatConversationService.Opened openConversation(String requestedKey, Long userId, ChatState state) {
        if (userId == null || conversationService == null) return null;
        try { return conversationService.open(requestedKey, userId); }
        catch (Exception e) {
            log.debug("持久化 AI 会话不可用，使用连接内历史，错误类型={}", e.getClass().getSimpleName());
            state.conversationKey = null;
            return null;
        }
    }

    private ChatState stateFor(Session session, Long userId) {
        ChatState state = STATES.computeIfAbsent(session, key -> new ChatState(userId));
        if (!Objects.equals(state.userId, userId)) {
            state = new ChatState(userId);
            STATES.put(session, state);
        }
        return state;
    }

    private void appendFallbackHistory(ChatState state, String question, String answer) {
        ObjectNode user = mapper.createObjectNode(); user.put("role", "user"); user.put("content", question); state.history.add(user);
        if (answer != null && !answer.trim().isEmpty()) {
            ObjectNode assistant = mapper.createObjectNode(); assistant.put("role", "assistant"); assistant.put("content", answer); state.history.add(assistant);
        }
        while (state.history.size() > 12) state.history.remove(0);
    }

    private List<Map<String, String>> toHistoryPayload(List<ObjectNode> history) {
        List<Map<String, String>> output = new ArrayList<>();
        for (ObjectNode item : history) {
            Map<String, String> message = new LinkedHashMap<>();
            message.put("role", item.path("role").asText()); message.put("content", item.path("content").asText());
            output.add(message);
        }
        return output;
    }

    private static class ChatState {
        final Long userId;
        String conversationKey;
        List<ObjectNode> history = Collections.synchronizedList(new ArrayList<>());
        ChatState(Long userId) { this.userId = userId; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChatRequest {
        private String question;
        private Long productId;
        private String token;
        private String conversationId;
        private String action;
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getConversationId() { return conversationId; }
        public void setConversationId(String conversationId) { this.conversationId = conversationId; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
    }
}
