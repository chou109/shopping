package com.neuedu.system.demoai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuedu.system.demoai.entity.AIChatConversation;
import com.neuedu.system.demoai.entity.AIChatMessage;
import com.neuedu.system.demoai.repository.AIChatConversationRepository;
import com.neuedu.system.demoai.repository.AIChatMessageRepository;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AIChatConversationServiceTest {
    @Test
    public void createsANewOpaqueConversationWhenRequestedKeyIsNotOwned() {
        AIChatConversationRepository conversations = proxy(AIChatConversationRepository.class, (instance, method, args) -> {
            if ("findByConversationKeyAndUserId".equals(method.getName())) return Optional.empty();
            if ("saveAndFlush".equals(method.getName())) {
                AIChatConversation conversation = (AIChatConversation) args[0];
                conversation.setId(7L);
                return conversation;
            }
            return defaultValue(method.getReturnType());
        });
        AIChatConversationService service = new AIChatConversationService(conversations, proxy(AIChatMessageRepository.class,
                (instance, method, args) -> defaultValue(method.getReturnType())), new ObjectMapper());

        AIChatConversationService.Opened opened = service.open("7f4e8888-8d6e-40d9-a6e6-cb9ebf295615", 99L);

        assertNotEquals("7f4e8888-8d6e-40d9-a6e6-cb9ebf295615", opened.getKey());
        assertEquals(Long.valueOf(7L), opened.getId());
        assertEquals(0, opened.getHistory().size());
    }

    @Test
    public void restoresRecentHistoryInChronologicalOrder() {
        List<AIChatMessage> newestFirst = new ArrayList<>();
        for (int id = 12; id >= 1; id--) {
            AIChatMessage message = new AIChatMessage();
            message.setId((long) id);
            message.setRole(id % 2 == 0 ? "assistant" : "user");
            message.setContent("message-" + id);
            newestFirst.add(message);
        }
        AIChatMessageRepository messages = proxy(AIChatMessageRepository.class, (instance, method, args) -> {
            if ("findByConversationIdOrderByIdDesc".equals(method.getName())) return newestFirst;
            return defaultValue(method.getReturnType());
        });
        AIChatConversationService service = new AIChatConversationService(proxy(AIChatConversationRepository.class,
                (instance, method, args) -> defaultValue(method.getReturnType())), messages, new ObjectMapper());

        List<com.fasterxml.jackson.databind.node.ObjectNode> history = service.load(23L);

        assertEquals(12, history.size());
        assertEquals("message-1", history.get(0).path("content").asText());
        assertEquals("message-12", history.get(11).path("content").asText());
    }

    @Test
    public void refusesToSaveMessagesForAConversationNotOwnedByTheUser() {
        AIChatConversationRepository conversations = proxy(AIChatConversationRepository.class,
                (instance, method, args) -> "findByConversationKeyAndUserId".equals(method.getName()) ? Optional.empty() : defaultValue(method.getReturnType()));
        AIChatConversationService service = new AIChatConversationService(conversations,
                proxy(AIChatMessageRepository.class, (instance, method, args) -> defaultValue(method.getReturnType())), new ObjectMapper());

        assertFalse(service.saveExchange("7f4e8888-8d6e-40d9-a6e6-cb9ebf295615", 55L, "question", "answer"));
    }

    @Test
    public void clearsOnlyTheAuthenticatedUsersConversation() {
        AIChatConversation conversation = new AIChatConversation(); conversation.setId(31L);
        AIChatConversationRepository conversations = proxy(AIChatConversationRepository.class, (instance, method, args) -> {
            if ("findByConversationKeyAndUserId".equals(method.getName())) {
                assertEquals("7f4e8888-8d6e-40d9-a6e6-cb9ebf295615", args[0]);
                assertEquals(Long.valueOf(55L), args[1]);
                return Optional.of(conversation);
            }
            return defaultValue(method.getReturnType());
        });
        AIChatMessageRepository messages = proxy(AIChatMessageRepository.class,
                (instance, method, args) -> "deleteByConversationId".equals(method.getName()) ? null : defaultValue(method.getReturnType()));
        AIChatConversationService service = new AIChatConversationService(conversations, messages, new ObjectMapper());

        assertTrue(service.clear("7f4e8888-8d6e-40d9-a6e6-cb9ebf295615", 55L));
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static Object defaultValue(Class<?> type) {
        if (Optional.class.isAssignableFrom(type)) return Optional.empty();
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        return null;
    }
}
