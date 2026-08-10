package org.keycloak.testframework.remote.providers.runonserver;

/**
 * 包装 {@link FetchOnServer} 并声明期望结果类型的适配器。
 * <p>
 * 测试框架在反序列化远程返回值时，通过 {@link #getResultClass()} 确定目标类型。
 */
public interface FetchOnServerWrapper<T> {

    /** @return 待远程执行并取回结果的 {@link FetchOnServer} 实例 */
    FetchOnServer getRunOnServer();

    /** @return 远程返回值的期望 Java 类型 */
    Class<T> getResultClass();

}
