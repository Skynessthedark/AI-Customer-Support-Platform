package com.ai_customer_support.client.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.ai_customer_support.client.validation.FileValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;


@Documented
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileValidator.class)
public @interface ValidFile {
    String message() default "Invalid file submitted.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
