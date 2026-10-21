package com.neuedu.system.controller;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DashboardControllerTest {
    @Test
    public void statsCardsDoNotExposeUncalculatedTrendPercentages() {
        Map<String, Object> card = DashboardController.createStatsCard(
                "总商品数", "12", "ShoppingCart", "linear-gradient(135deg, #1890ff, #36cfc9)");

        assertEquals("总商品数", card.get("title"));
        assertEquals("12", card.get("value"));
        assertEquals("ShoppingCart", card.get("icon"));
        assertEquals("linear-gradient(135deg, #1890ff, #36cfc9)", card.get("color"));
        assertFalse(card.containsKey("trend"));
    }
}
