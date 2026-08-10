package org.keycloak.services.client.query;

import org.keycloak.models.mapper.ClientModelMappers;
import org.keycloak.representations.admin.v2.BaseClientRepresentation;
import org.keycloak.representations.admin.v2.OIDCClientRepresentation;

/**
 * 将查询字段路径解析为 {@link BaseClientRepresentation} 上的值。
 */
public class FieldResolver {

    private static final ClientModelMappers MAPPERS = new ClientModelMappers();

    /** 字段路径是否为可查询的已知字段（含 {@code auth.method} 特例）。 */
    public static boolean isKnownField(String fieldPath) {
        if ("auth.method".equals(fieldPath)) {
            return true;
        }
        return MAPPERS.isKnownField(fieldPath);
    }

    /** 解析字段路径对应的值；未知或不可用时返回 {@code null}。 */
    public static Object resolve(String fieldPath, BaseClientRepresentation client) {
        if ("auth.method".equals(fieldPath)) {
            if (client instanceof OIDCClientRepresentation oidc && oidc.getAuth() != null) {
                return oidc.getAuth().getMethod();
            }
            return null;
        }
        return MAPPERS.resolveFieldValue(fieldPath, client);
    }
}
