package com.neuedu.system.service;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BlindBoxPrizePolicyTest {
    @Test
    public void selectsMainShopForFirstEightyFivePercent() {
        assertEquals(Long.valueOf(10L), BlindBoxPrizePolicy.preferredShop(0, 0));
        assertEquals(Long.valueOf(10L), BlindBoxPrizePolicy.preferredShop(84, 0));
    }

    @Test
    public void selectsBackupShopForNextFourteenPercent() {
        assertEquals(Long.valueOf(11L), BlindBoxPrizePolicy.preferredShop(85, 0));
        assertEquals(Long.valueOf(11L), BlindBoxPrizePolicy.preferredShop(98, 0));
    }

    @Test
    public void distributesFinalPercentAcrossConfiguredShops() {
        assertEquals(Long.valueOf(6L), BlindBoxPrizePolicy.preferredShop(99, 0));
        assertEquals(Long.valueOf(14L), BlindBoxPrizePolicy.preferredShop(99, 6));
    }
}
