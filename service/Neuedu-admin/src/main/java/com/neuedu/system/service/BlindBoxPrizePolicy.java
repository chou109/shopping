package com.neuedu.system.service;

import java.util.Arrays;
import java.util.List;

/**
 * 盲盒奖品店铺分档规则。
 */
public final class BlindBoxPrizePolicy {
    private static final List<Long> OTHER_SHOPS = Arrays.asList(6L, 7L, 8L, 9L, 12L, 13L, 14L);

    private BlindBoxPrizePolicy() {
    }

    public static Long preferredShop(int roll, int otherShopIndex) {
        if (roll < 0 || roll >= 100) {
            throw new IllegalArgumentException("roll 必须在 0 到 99 之间");
        }
        if (roll < 85) {
            return 10L;
        }
        if (roll < 99) {
            return 11L;
        }
        if (otherShopIndex < 0 || otherShopIndex >= OTHER_SHOPS.size()) {
            throw new IllegalArgumentException("其他店铺索引超出范围");
        }
        return OTHER_SHOPS.get(otherShopIndex);
    }
}
