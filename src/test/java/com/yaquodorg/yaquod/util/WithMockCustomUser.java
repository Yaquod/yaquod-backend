package com.yaquodorg.yaquod.util;

import com.yaquodorg.yaquod.entity.Role;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(
    factory = CustomSecurityContextFactory.class,
    setupBefore = TestExecutionEvent.TEST_EXECUTION)
public @interface WithMockCustomUser {
  String email() default "test@example.com";

  Role role() default Role.CLIENT;
}
