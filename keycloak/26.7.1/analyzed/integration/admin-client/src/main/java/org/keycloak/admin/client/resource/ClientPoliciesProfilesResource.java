package org.keycloak.admin.client.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.keycloak.representations.idm.ClientProfilesRepresentation;

/**
 * 领域客户端配置档案（Client Profiles）集合的管理 REST 资源。
 * <p>
 * 客户端配置档案定义客户端应满足的安全基线，
 * 策略可引用档案以批量约束客户端行为。
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public interface ClientPoliciesProfilesResource {

    /**
     * 获取领域客户端配置档案。
     *
     * @param includeGlobalProfiles 是否包含服务器级全局配置档案
     * @return 客户端配置档案表示对象
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ClientProfilesRepresentation getProfiles(@QueryParam("include-global-profiles") Boolean includeGlobalProfiles);

    /**
     * 更新领域内的客户端配置档案。
     * <p>
     * {@code globalProfiles} 字段会被忽略，全局配置档案不可通过此接口修改。
     *
     * @param clientProfiles 待更新的客户端配置档案
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    void updateProfiles(final ClientProfilesRepresentation clientProfiles);
}
