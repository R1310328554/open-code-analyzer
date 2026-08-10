package org.keycloak.testframework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注入 {@link org.apache.http.client.HttpClient}，供测试内发起 HTTP 请求。
 * 也可选用 {@link InjectSimpleHttp}，其 API 更简洁。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectHttpClient {

    /** 是否自动跟随 HTTP 重定向。 */
    boolean followRedirects() default true;

}
