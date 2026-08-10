package org.keycloak.testframework.remote.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Test;

/**
 * 标记测试方法在 Keycloak 服务器进程内执行。
 * <p>
 * 组合 {@link org.junit.jupiter.api.Test}，使远程测试框架将方法体调度到服务端运行。
 */
@Test
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestOnServer {
}
