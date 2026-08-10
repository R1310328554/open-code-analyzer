package org.keycloak.testframework.mail.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 向测试字段注入 {@link org.keycloak.testframework.mail.MailServer}，用于接收 Keycloak 发出的邮件。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectMailServer { }
