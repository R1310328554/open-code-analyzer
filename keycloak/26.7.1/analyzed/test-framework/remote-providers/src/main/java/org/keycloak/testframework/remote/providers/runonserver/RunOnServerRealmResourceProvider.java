package org.keycloak.testframework.remote.providers.runonserver;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.util.JsonSerialization;

/**
 * 测试用 Realm 资源端点，在 Keycloak 服务器内反序列化并执行 {@link RunOnServer} 或 {@link FetchOnServer}。
 * <p>
 * 客户端将 Base64 编码的 Java 对象 POST 到本端点；成功时返回 JSON 结果或空，失败时返回编码后的异常。
 */
public class RunOnServerRealmResourceProvider implements RealmResourceProvider {

    private final KeycloakSession session;
    private final RunOnServerRealmResourceProviderFactory factory;

    /**
     * @param session 当前 Keycloak 会话
     * @param factory 提供测试类加载器的工厂
     */
    public RunOnServerRealmResourceProvider(KeycloakSession session, RunOnServerRealmResourceProviderFactory factory) {
        this.session = session;
        this.factory = factory;
    }

    /** {@inheritDoc} 本提供者即 JAX-RS 资源根。 */
    @Override
    public Object getResource() {
        return this;
    }

    /** {@inheritDoc} 无额外资源需释放。 */
    @Override
    public void close() {
    }

    @POST
    @Path("/")
    @Consumes(MediaType.TEXT_PLAIN + ";charset=utf-8")
    @Produces(MediaType.TEXT_PLAIN + ";charset=utf-8")
    /**
     * 接收序列化任务并在服务器端执行。
     *
     * @param runOnServer Base64 编码的 {@link RunOnServer} 或 {@link FetchOnServer} 对象
     * @param executionId 测试执行标识，用于选择 {@link TestClassLoader}
     * @return {@link FetchOnServer} 结果的 JSON 字符串、{@code null}，或 {@link SerializationUtil#encodeException(Throwable)} 输出
     */
    public String runOnServer(String runOnServer, @QueryParam("executionId") String executionId) {
        try {
            ClassLoader classLoader = factory.getTestClassLoader(executionId);
            Object o = SerializationUtil.decode(runOnServer, classLoader);
            if (o instanceof FetchOnServer f) {
                Object result = f.run(session);
                return result != null ? JsonSerialization.writeValueAsString(result) : null;
            } else if (o instanceof RunOnServer r) {
                r.run(session);
                return null;
            } else {
                throw new IllegalArgumentException("Can't handle serialized class: " + o.getClass().getName());
            }
        } catch (Throwable t) {
            return SerializationUtil.encodeException(t);
        }
    }

}
