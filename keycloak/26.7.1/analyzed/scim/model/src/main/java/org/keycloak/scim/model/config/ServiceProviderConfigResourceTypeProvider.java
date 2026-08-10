package org.keycloak.scim.model.config;

import java.util.List;
import java.util.stream.Stream;

import org.keycloak.common.util.Time;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.Model;
import org.keycloak.scim.protocol.ForbiddenException;
import org.keycloak.scim.protocol.request.SearchRequest;
import org.keycloak.scim.resource.config.ServiceProviderConfig;
import org.keycloak.scim.resource.config.ServiceProviderConfig.AuthenticationScheme;
import org.keycloak.scim.resource.config.ServiceProviderConfig.BulkSupport;
import org.keycloak.scim.resource.config.ServiceProviderConfig.FilterSupport;
import org.keycloak.scim.resource.config.ServiceProviderConfig.Supported;
import org.keycloak.scim.resource.schema.ModelSchema;
import org.keycloak.scim.resource.spi.ScimResourceTypeProvider;
import org.keycloak.scim.resource.spi.SingletonResourceTypeProvider;

import static org.keycloak.scim.resource.Scim.hasDiscoveryEndpointPermission;

/**
 * {@link ServiceProviderConfig} 单例资源类型的 SCIM 提供者。
 * <p>返回 Keycloak SCIM 服务的能力声明，包括认证方案、过滤支持与 Patch 支持等。</p>
 */
public class ServiceProviderConfigResourceTypeProvider implements SingletonResourceTypeProvider<ServiceProviderConfig> {

    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;

    /**
     * 构造提供者。
     *
     * @param session Keycloak 会话
     */
    public ServiceProviderConfigResourceTypeProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public ServiceProviderConfig getSingleton() {
        ServiceProviderConfig config = new ServiceProviderConfig();

        config.setId("");
        config.setBulk(new BulkSupport());
        config.setPatch(Supported.TRUE);
        config.setEtag(Supported.FALSE);
        config.setAuthenticationSchemes(List.of());
        config.setChangePassword(Supported.FALSE);
        config.setCreatedTimestamp(Time.currentTimeMillis());
        config.setSort(Supported.FALSE);
        config.setFilter(getFilterSupport());
        config.setAuthenticationSchemes(getAuthenticationSchemes());

        return config;
    }

    /** 构造过滤能力声明。 */
    private FilterSupport getFilterSupport() {
        FilterSupport filter = new FilterSupport();

        filter.setSupported(true);
        filter.setMaxResults(ScimResourceTypeProvider.DEFAULT_MAX_RESULTS);

        return filter;
    }

    /** 构造支持的 OAuth Bearer Token 认证方案列表。 */
    private List<AuthenticationScheme> getAuthenticationSchemes() {
        AuthenticationScheme scheme = new AuthenticationScheme();

        scheme.setName("OAuth Bearer Token");
        scheme.setDescription("Authentication scheme using the OAuth Bearer Token standard");
        scheme.setSpecUri("https://tools.ietf.org/html/rfc6750");
        scheme.setType("oauthbearertoken");

        return List.of(scheme);
    }

    @Override
    public Stream<ServiceProviderConfig> getAll(SearchRequest searchRequest) {
        if (hasDiscoveryEndpointPermission(session)) {
            return Stream.of(getSingleton());
        }
        throw new ForbiddenException();
    }

    @Override
    public Class<ServiceProviderConfig> getResourceType() {
        return ServiceProviderConfig.class;
    }

    @Override
    public String getSchema() {
        return ServiceProviderConfig.SCHEMA;
    }

    @Override
    public <M extends Model> List<ModelSchema<M, ServiceProviderConfig>> getSchemas() {
        return List.of();
    }
}
