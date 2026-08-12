package com.company.chatplatform.common.core.util;

import java.security.SecureRandom;
import java.util.UUID;

public class UUIDv7Utils {
    private static final SecureRandom RANDOM = new SecureRandom();

    private UUIDv7Utils() {}

    public static UUID generate() {
        long timestamp = System.currentTimeMillis();
        
        long msb = (timestamp & 0xFFFFFFFFFFFFL) << 16;
        msb |= (0x7L << 12); // Version 7
        msb |= (RANDOM.nextInt(0x1000) & 0x0FFF);

        long lsb = (0x2L << 62); // Variant 1 (RFC 4122)
        lsb |= (RANDOM.nextLong() & 0x3FFFFFFFFFFFFFFFL);

        return new UUID(msb, lsb);
    }

    public static String generateString() {
        return generate().toString();
    }
}
