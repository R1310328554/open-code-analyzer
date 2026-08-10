package org.keycloak.validation.jakarta;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotNull;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

/**
 * 通过 Hibernate Validator constraint validator payload 传递给约束校验器的上下文对象。
 * <p>
 * Record 组件标注 Jakarta Validation，以便文档工具（如 Admin API v2 OpenAPI
 * {@code ValidationAnnotationScanner}）在 schema 描述中展示与表示类型相同的约束。
 * <p>
 * 自定义约束校验器可将 {@link jakarta.validation.ConstraintValidatorContext} 解包为
 * {@link org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext} 并读取 payload：
 * 
 * <pre>{@code
 * @Override
 * public boolean isValid(String value, ConstraintValidatorContext context) {
 *     ValidationContext validationContext = ValidationContext.unwrap(context);
 *
 *     KeycloakSession session = validationContext.getSession();
 *     RealmModel realm = validationContext.getRealm();
 *     // ... use session and realm for validation logic
 * }
 * }</pre>
 *
 * @param session Keycloak 会话（必填）
 * @param realm 校验所针对的领域模型（必填）
 */
public record ValidationContext(@NotNull KeycloakSession session, @NotNull RealmModel realm) {

    public ValidationContext {
        if (session == null) {
            throw new IllegalArgumentException("session cannot be null");
        }
        if (realm == null) {
            throw new IllegalArgumentException("realm cannot be null");
        }
    }

    /** 从 {@link ConstraintValidatorContext} 解包出 {@link ValidationContext} payload。 */
    public static ValidationContext unwrap(ConstraintValidatorContext context) {
        HibernateConstraintValidatorContext hibernateContext = context
                .unwrap(HibernateConstraintValidatorContext.class);
        return hibernateContext.getConstraintValidatorPayload(ValidationContext.class);
    }

}
