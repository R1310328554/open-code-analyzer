package org.keycloak.testframework.remote.providers.timeoffset;

import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.common.util.Time;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

/**
 * 测试用 Realm 资源，读写 Keycloak {@link Time} 全局偏移量。
 * <p>
 * 可选同步调整 Infinispan 缓存时间服务，使令牌过期等场景与偏移后的时钟一致。
 */
public class TimeOffSetRealmResourceProvider implements RealmResourceProvider {

    private final KeycloakSession session;
    private final String KEY_OFFSET = "offset";
    private final String CACHES = "caches";

    /** @param session 当前 Keycloak 会话，供 Infinispan 时间切换使用 */
    public TimeOffSetRealmResourceProvider(KeycloakSession session) {
        this.session = session;
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

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    /** @return 当前 {@link Time#getOffset()} 的 JSON 表示 */
    public Response getTimeOffset() {
        int offset = Time.getOffset();
        var time = Map.of(KEY_OFFSET, offset);
        return Response.ok(time).build();
    }

    @PUT
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * 设置时间偏移；请求体可含 {@code offset} 与可选 {@code caches} 标志。
     *
     * @param time 含 {@code offset}（整数秒）及可选 {@code caches}（是否同步 Infinispan）的映射
     * @return 成功时 200，缺少 {@code offset} 时 400
     */
    public Response setTimeOffset(Map<String, Object> time) {
        if (!time.containsKey(KEY_OFFSET)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        int timeOffset = (Integer) time.get(KEY_OFFSET);
        Time.setOffset(timeOffset);

        boolean caches = time.containsKey(CACHES) ? (Boolean) time.get(CACHES) : false;
        if (caches) {
            if (timeOffset > 0) {
                InfinispanTimeUtil.enableTestingTimeService(session);
            } else {
                InfinispanTimeUtil.disableTestingTimeService(session);
            }
        }

        return Response.ok().header("Content-Type", MediaType.APPLICATION_JSON).build();
    }
}
