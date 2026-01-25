package com.yaquodorg.yaquod.utils;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = VinValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidVIN {
    String message() default "Invalid VIN number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
