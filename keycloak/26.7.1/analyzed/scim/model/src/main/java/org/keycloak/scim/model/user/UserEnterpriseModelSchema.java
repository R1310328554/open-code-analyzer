package org.keycloak.scim.model.user;


import org.keycloak.models.KeycloakSession;
import org.keycloak.scim.resource.schema.attribute.Attribute;

import static org.keycloak.scim.resource.Scim.ENTERPRISE_USER_SCHEMA;

/**
 * SCIM Enterprise User 扩展 schema，对应 {@link org.keycloak.scim.resource.Scim#ENTERPRISE_USER_SCHEMA}。
 * <p>暴露企业用户扩展字段，非内部 schema，可通过 /Schemas 发现。</p>
 */
public final class UserEnterpriseModelSchema extends UserExtensionModelSchema {

    /** 以 Enterprise User schema URN 构造。 */
    public UserEnterpriseModelSchema(KeycloakSession session) {
        super(session, ENTERPRISE_USER_SCHEMA);
    }

    @Override
    public String getId() {
        return ENTERPRISE_USER_SCHEMA;
    }

    @Override
    public String getName() {
        return "EnterpriseUser";
    }

    @Override
    public String getDescription() {
        return "Enterprise User";
    }

    /** Enterprise schema 对外可见。 */
    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    protected boolean hasSchema(String attributeName) {
        return getId().equals(Attribute.getSchema(attributeName));
    }
}
