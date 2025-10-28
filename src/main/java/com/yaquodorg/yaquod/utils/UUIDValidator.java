package com.yaquodorg.yaquod.utils;

import java.util.UUID;
import java.util.regex.Pattern;

public class UUIDValidator {

    // Strict UUID v1–v5 format
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$"
    );

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        // Check format with regex
        if (!UUID_PATTERN.matcher(value).matches()) {
            return false;
        }

        // Verify it’s parsable
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
