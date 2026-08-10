package org.keycloak.representations.admin.v2.validators;

import org.keycloak.representations.admin.v2.RepresentationWithUuid;
import org.keycloak.validation.jakarta.ValidationContext;

/**
 * 供 {@link UuidUnmodifiedValidator} 使用：查询 UUID 是否已存在，并从类型化表示中获取已持久化资源的 UUID。
 *
 * @author Vaclav Muzikar <vmuzikar@ibm.com>
 */
public interface UuidProvider {
    /** 校验 realm 中是否已存在给定 UUID 的资源。 */
    boolean uuidExists(ValidationContext context, String uuid);
    /** 按表示中的别名（如 clientId）查找已持久化资源的 UUID。 */
    String getPersistedUuid(ValidationContext context, RepresentationWithUuid representation);
}
