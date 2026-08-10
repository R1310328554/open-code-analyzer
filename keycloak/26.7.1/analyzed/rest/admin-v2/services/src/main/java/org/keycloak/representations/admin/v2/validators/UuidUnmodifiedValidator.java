package org.keycloak.representations.admin.v2.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.keycloak.representations.admin.v2.BaseClientRepresentation;
import org.keycloak.representations.admin.v2.RepresentationWithUuid;
import org.keycloak.representations.admin.v2.validation.UuidUnmodified;
import org.keycloak.validation.jakarta.ValidationContext;

/**
 * {@link org.keycloak.representations.admin.v2.validation.UuidUnmodified} 约束校验器：客户端未提供 UUID，或提供的 UUID 与已持久化值一致（更新场景）。
 * <p>
 * 另校验提供的 UUID 在系统中不存在，防止重命名后通过 PUT 重复创建；适用于 PUT 创建。
 * <p>
 * 假定资源除 UUID 外还有唯一别名（如 name 或 clientId）用于定位。
 *
 * @author Vaclav Muzikar <vmuzikar@ibm.com>
 */
public class UuidUnmodifiedValidator implements ConstraintValidator<UuidUnmodified, RepresentationWithUuid> {

    @Override
    public boolean isValid(RepresentationWithUuid representation, ConstraintValidatorContext context) {
        Class<?> type = representation.getClass();
        UuidProvider uuidProvider = null;
        if (BaseClientRepresentation.class.isAssignableFrom(type)) {
            uuidProvider = new ClientUuidProvider();
        } else {
            throw new AssertionError("No UuidProvider defined for " + type);
        }
        
        String providedUuid = representation.getUuid();
        if (providedUuid == null || providedUuid.isEmpty()) { // 未提供 UUID，无需校验
            return true;
        }

        ValidationContext validationContext = ValidationContext.unwrap(context);
        String persistedUuid = uuidProvider.getPersistedUuid(validationContext, representation);

        if (persistedUuid != null) { // 资源已存在
            if (persistedUuid.equals(providedUuid)) {
                return true;
            }
        } else if (!uuidProvider.uuidExists(validationContext, providedUuid)) { // PUT 创建：UUID 未被占用（排除仅重命名）
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("uuid")
                .addConstraintViolation();
        return false;
    }
}
