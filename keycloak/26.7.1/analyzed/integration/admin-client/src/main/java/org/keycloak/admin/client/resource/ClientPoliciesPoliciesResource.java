package org.keycloak.admin.client.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.keycloak.representations.idm.ClientPoliciesRepresentation;

/**
 * 领域客户端策略（Client Policies）集合的管理 REST 资源。
 * <p>
 * 客户端策略用于在 OAuth/OIDC 流程中强制执行安全与合规规则，
 * 例如限制客户端认证方式、令牌格式等。
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public interface ClientPoliciesPoliciesResource {

    /** 获取领域内配置的客户端策略（不含全局策略）。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ClientPoliciesRepresentation getPolicies();

    /**
     * 获取领域客户端策略。
     *
     * @param includeGlobalPolicies 是否包含服务器级全局客户端策略；自 Keycloak 25 起可用，旧版本忽略此参数
     * @return 客户端策略表示对象
     * @since Keycloak server 26.7.0
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ClientPoliciesRepresentation getPolicies(@QueryParam("include-global-policies") Boolean includeGlobalPolicies);

    /** 更新领域内的客户端策略配置。 */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    void updatePolicies(final ClientPoliciesRepresentation clientPolicies);
}
