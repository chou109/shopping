package com.neuedu.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neuedu.system.controller.ProductCommentController;
import com.neuedu.system.domain.ProductComment;
import com.neuedu.system.mapper.ProductCommentMapper;
import com.neuedu.system.service.impl.ProductCommentServiceImpl;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ProductCommentAnonymousPrivacyTest {
    @Test
    public void publicReviewListOmitsIdentityFieldsForAnonymousAuthors() {
        ProductComment anonymous = comment(1L, true);
        ProductComment identified = comment(2L, false);
        ProductCommentMapper mapper = proxy(ProductCommentMapper.class, (method, args) -> {
            if ("selectProductCommentPage".equals(method)) return Arrays.asList(anonymous, identified);
            if ("selectLikedCommentIds".equals(method)) return Collections.emptyList();
            return defaultValue(method);
        });
        ProductCommentServiceImpl service = new ProductCommentServiceImpl(mapper, null, null);

        List<ProductComment> result = service.getProductComments(7L, null, null);

        assertNull(result.get(0).getUserId());
        assertEquals("匿名用户", result.get(0).getNickName());
        assertNull(result.get(0).getUserAvatar());
        assertEquals(Long.valueOf(21L), result.get(1).getUserId());
        assertEquals("User21", result.get(1).getNickName());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void pagedPublicCommentTreeOmitsIdentityForAnonymousRootsAndReplies() {
        ProductComment root = comment(1L, true);
        root.setParentId(0L);
        ProductComment reply = comment(2L, true);
        reply.setParentId(1L);
        Page<ProductComment> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(root));
        page.setTotal(1);

        ProductCommentController controller = new ProductCommentController(proxy(IProductCommentService.class,
                (method, args) -> {
                    if ("page".equals(method)) return page;
                    if ("list".equals(method)) return Collections.singletonList(reply);
                    if ("getLikedCommentIds".equals(method)) return Collections.emptyList();
                    return defaultValue(method);
                }));

        Map<String, Object> data = (Map<String, Object>) controller.getCommentTreePage(7L, 1, 10).get("data");
        List<Map<String, Object>> roots = (List<Map<String, Object>>) data.get("list");
        Map<String, Object> rootNode = roots.get(0);
        Map<String, Object> replyNode = ((List<Map<String, Object>>) rootNode.get("children")).get(0);

        assertEquals("匿名用户", rootNode.get("userName"));
        assertEquals("匿名用户", replyNode.get("userName"));
        assertNull(rootNode.get("userId"));
        assertNull(replyNode.get("userId"));
        assertNull(rootNode.get("userAvatar"));
        assertNull(replyNode.get("userAvatar"));
    }

    private static ProductComment comment(Long id, boolean anonymous) {
        ProductComment comment = new ProductComment();
        comment.setCommentId(id);
        comment.setProductId(7L);
        comment.setUserId(id + 19L);
        comment.setIsAnonymous(anonymous ? 1 : 0);
        comment.setNickName("User" + (id + 19L));
        comment.setUserAvatar("/avatar/" + id + ".png");
        return comment;
    }

    private interface MapperCall {
        Object invoke(String methodName, Object[] args);
    }

    private static <T> T proxy(Class<T> type, MapperCall call) {
        InvocationHandler handler = (instance, method, args) -> call.invoke(method.getName(), args);
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }

    private static Object defaultValue(String method) {
        if ("toString".equals(method)) return "comment test mapper";
        return null;
    }
}
