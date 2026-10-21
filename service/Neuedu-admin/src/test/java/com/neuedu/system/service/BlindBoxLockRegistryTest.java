package com.neuedu.system.service;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class BlindBoxLockRegistryTest {
    @Test
    public void reusesAStableLockForTheSameUser() {
        BlindBoxLockRegistry locks = new BlindBoxLockRegistry(512);

        assertSame(locks.lockFor(42L), locks.lockFor(42L));
        assertEquals(512, locks.stripeCount());
    }

    @Test
    public void distributesDifferentUsersAcrossIndependentLocks() {
        BlindBoxLockRegistry locks = new BlindBoxLockRegistry(512);

        assertNotSame(locks.lockFor(42L), locks.lockFor(43L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMissingUserIds() {
        new BlindBoxLockRegistry(512).lockFor(null);
    }
}
