package org.keycloak.testframework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注入 {@link com.sun.net.httpserver.HttpServer}，用于在测试用 Mock HTTP 服务器上
 * 注册或注销额外上下文。通常仅由 supplier 使用，测试代码不应直接使用。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectHttpServer {

}
