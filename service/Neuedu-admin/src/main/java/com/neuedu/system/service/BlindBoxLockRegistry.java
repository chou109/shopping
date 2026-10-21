package com.neuedu.system.service;

/**
 * Provides a fixed pool of locks for serializing blind-box orders by user.
 * Reusing stripes avoids retaining one lock object for every user that ever participates.
 */
public final class BlindBoxLockRegistry {
    private final Object[] locks;

    public BlindBoxLockRegistry(int stripeCount) {
        if (stripeCount < 1) {
            throw new IllegalArgumentException("stripeCount must be positive");
        }
        locks = new Object[stripeCount];
        for (int index = 0; index < stripeCount; index++) {
            locks[index] = new Object();
        }
    }

    public Object lockFor(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        return locks[Math.floorMod(Long.hashCode(userId), locks.length)];
    }

    int stripeCount() {
        return locks.length;
    }
}
