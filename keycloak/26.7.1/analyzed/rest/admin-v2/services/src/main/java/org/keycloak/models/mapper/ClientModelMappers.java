package org.keycloak.models.mapper;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.keycloak.representations.admin.v2.BaseClientRepresentation;
import org.keycloak.representations.admin.v2.OIDCClientRepresentation;
import org.keycloak.representations.admin.v2.SAMLClientRepresentation;

/**
 * 按协议（OIDC/SAML）选择 {@link BaseClientModelMapper} 的注册表，提供字段解析与投影辅助方法。
 */
public class ClientModelMappers {

    private final Map<String, BaseClientModelMapper<?>> mappers;

    public ClientModelMappers() {
        // TODO: this may be done via discovery later
        mappers = Map.of(OIDCClientRepresentation.PROTOCOL, new OIDCClientModelMapper(),
                SAMLClientRepresentation.PROTOCOL, new SAMLClientModelMapper());
    }

    /** 任一协议映射器是否注册了该字段名。 */
    public boolean isKnownField(String name) {
        return mappers.values().stream().anyMatch(f -> f.fields.containsKey(name));
    }

    /** 按表示的 protocol 解析指定字段的当前值。 */
    public Object resolveFieldValue(String name, BaseClientRepresentation rep) {
        String protocol = rep.getProtocol();
        var mapper = protocol != null ? mappers.get(protocol) : null;
        if (mapper != null) {
            var field = mapper.fields.get(name);
            if (field != null) {
                return field.getValue(rep);
            }
        }
        return null;
    }

    public void applyProjection(BaseClientRepresentation rep, Set<String> includeFields) {
        String protocol = rep.getProtocol();
        var mapper = protocol != null ? mappers.get(protocol) : null;
        if (mapper != null) {
            mapper.applyProjection(rep, includeFields);
        }
    }

    /** 按协议名获取对应映射器。 */
    public Optional<BaseClientModelMapper<?>> getMapper(String protocol) {
        return Optional.ofNullable(mappers.get(protocol));
    }

}
