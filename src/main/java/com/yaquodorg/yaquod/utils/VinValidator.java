package com.yaquodorg.yaquod.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class VinValidator implements ConstraintValidator<ValidVIN, String> {

    private static final String VIN_REGEX = "^[A-HJ-NPR-Z0-9]{17}$";

    private static final int[] VIN_WEIGHTS = {
            8, 7, 6, 5, 4, 3, 2, 10,
            0, 9, 8, 7, 6, 5, 4, 3, 2
    };

    private static final String VIN_CHARS = "0123456789.ABCDEFGH..JKLMN.P.R..STUVWXYZ";

    @Override
    public boolean isValid(String vin, ConstraintValidatorContext ctx) {
        if (vin == null || !vin.matches(VIN_REGEX)) return false;
        return checkChecksum(vin);
    }

    private boolean checkChecksum(String vin) {
        int sum = 0;

        for (int i = 0; i < 17; i++) {
            char c = vin.charAt(i);
            int value = transliterate(c);
            sum += value * VIN_WEIGHTS[i];
        }

        int remainder = sum % 11;
        char expectedCheckDigit = remainder == 10 ? 'X' : Character.forDigit(remainder, 10);

        return vin.charAt(8) == expectedCheckDigit;
    }

    private int transliterate(char c) {
        int index = VIN_CHARS.indexOf(c);
        return index % 10;
    }
}
