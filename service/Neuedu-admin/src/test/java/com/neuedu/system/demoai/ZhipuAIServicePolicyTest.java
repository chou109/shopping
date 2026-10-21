package com.neuedu.system.demoai;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ZhipuAIServicePolicyTest {
    private final ZhipuAIService service = new ZhipuAIService();

    @Test
    public void onlyDescribesThePaymentMethodTheStoreImplements() {
        String response = service.simpleChat("支持哪些支付方式？", "");

        assertTrue(response.contains("余额支付"));
        assertTrue(response.contains("尚未接入"));
        assertFalse(response.contains("支持支付宝"));
        assertFalse(response.contains("支持微信支付"));
        assertFalse(response.contains("支持银行卡"));
    }

    @Test
    public void doesNotInventShippingOrReturnGuarantees() {
        String shippingResponse = service.simpleChat("物流多久能到？", "");
        String returnResponse = service.simpleChat("支持七天无理由退货吗？", "");

        assertTrue(shippingResponse.contains("尚未接入承运商轨迹"));
        assertFalse(shippingResponse.contains("3-7天"));
        assertTrue(returnResponse.contains("没有统一的无理由退货"));
        assertFalse(returnResponse.contains("7天无理由退货"));
    }

    @Test
    public void doesNotClaimUnconfiguredPromotions() {
        String response = service.simpleChat("有什么优惠活动吗？", "");

        assertTrue(response.contains("没有统一的优惠或满减查询服务"));
        assertFalse(response.contains("满减、折扣"));
    }

    @Test
    public void neverUsesAClientSuppliedUserIdToLookUpOrders() {
        String response = service.handleOrderQuery("查看订单", 12345L);

        assertEquals("为保护订单信息，请登录后前往“我的订单”查看。当前客服会话不提供订单查询。", response);
    }
}
