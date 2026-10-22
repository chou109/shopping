package com.neuedu.system.demoai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neuedu.common.core.domain.model.LoginUser;
import com.neuedu.web.service.TokenService;
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
    private static final Map<Session, List<ObjectNode>> HISTORY = new ConcurrentHashMap<>();
    private static ObjectMapper mapper = new ObjectMapper();
    private static ZhipuAIService assistantService;
    private static TokenService tokenService;

    @Autowired public void setAssistantService(ZhipuAIService service) { AIChatWebSocket.assistantService = service; }
    @Autowired public void setTokenService(TokenService service) { AIChatWebSocket.tokenService = service; }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            ChatRequest request = mapper.readValue(message, ChatRequest.class);
            String question = request.getQuestion() == null ? "" : request.getQuestion().trim();
            if (question.isEmpty()) { send(session, "stream", "请告诉我您想咨询什么。"); send(session, "end", ""); return; }
            if (question.length() > 1000) { send(session, "error", "问题最多支持 1000 个字符。"); return; }
            LoginUser loginUser = tokenService == null ? null : tokenService.getLoginUserFromToken(request.getToken());
            if (isOrderQuery(question) && loginUser == null) {
                send(session, "stream", "为保护订单信息，请先登录；登录后我可以只查询您自己的订单状态。");
                send(session, "handoff", ""); send(session, "end", ""); return;
            }
            List<ObjectNode> history = HISTORY.computeIfAbsent(session, key -> Collections.synchronizedList(new ArrayList<>()));
            assistantService.answer(question, request.getProductId(), loginUser, history,
                    text -> send(session, "stream", text), sources -> sendPayload(session, "sources", sources),
                    required -> { if (required) send(session, "handoff", ""); });
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

    @OnClose public void onClose(Session session) { HISTORY.remove(session); }
    @OnError public void onError(Session session, Throwable error) { log.debug("客服WebSocket关闭，会话ID={}", session == null ? "unknown" : session.getId()); HISTORY.remove(session); }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChatRequest {
        private String question;
        private Long productId;
        private String token;
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
}
