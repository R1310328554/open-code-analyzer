package org.keycloak.representations.admin.v2.validators;

import java.util.Optional;

import org.keycloak.models.ClientModel;
import org.keycloak.representations.admin.v2.BaseClientRepresentation;
import org.keycloak.representations.admin.v2.RepresentationWithUuid;
import org.keycloak.validation.jakarta.ValidationContext;

/**
 * @author Vaclav Muzikar <vmuzikar@ibm.com>
 * Admin API v2 客户端 UUID 的 {@link UuidProvider} 实现：按 realm 查询客户端 ID/UUID。
 */
public class ClientUuidProvider implements UuidProvider {
    @Override
    /** 校验 realm 中是否存在给定 UUID 的客户端。 */
    @Override
    public boolean uuidExists(ValidationContext context, String uuid) {
        return Optional.ofNullable(context.realm().getClientById(uuid)).isPresent();
    }

    /** 按 clientId 查找已持久化客户端的 UUID。 */
    @Override
    public String getPersistedUuid(ValidationContext context, RepresentationWithUuid representation) {
        String clientId = ((BaseClientRepresentation) representation).getClientId();
        return Optional.ofNullable(context.realm().getClientByClientId(clientId)).map(ClientModel::getId).orElse(null);
    }
}
