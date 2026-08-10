package org.keycloak.validation.jakarta;

import java.util.Set;
import java.util.function.Function;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidatorFactory;

/**
 * {@link JakartaValidatorProvider} 的 Hibernate Validator 实现，支持向自定义约束校验器传递 {@link ValidationContext}。
 * <p>
 * 含 {@link org.keycloak.models.KeycloakSession} 与 {@link org.keycloak.models.RealmModel} 的
 * {@link ValidationContext} 作为 constraint validator payload，供校验器访问 Keycloak 运行时上下文。
 *
 * @see ValidationContext
 */
public class HibernateValidatorProvider implements JakartaValidatorProvider {

    private final Validator validator;

    /**
     * 创建校验器提供者，使自定义约束校验器可访问 {@link ValidationContext}。
     *
     * @param context 含 session 与 realm 的校验上下文
     */
    public HibernateValidatorProvider(ValidationContext context) {
        ValidatorFactory factory = CDI.current().select(ValidatorFactory.class).get();
        this.validator = factory.unwrap(HibernateValidatorFactory.class)
                .usingContext()
                .constraintValidatorPayload(context)
                .getValidator();
    }

    /** 按指定校验组校验对象，失败时抛出 {@link ConstraintViolationException}。 */
    @Override
    public <T> void validate(T object, Class<?>... groups) throws ConstraintViolationException {
        var errors = validator.validate(object, groups);
        if (!errors.isEmpty()) {
            throw new ConstraintViolationException(errors);
        }
    }

    /** 使用自定义函数执行校验，失败时抛出 {@link ConstraintViolationException}。 */
    @Override
    public void validate(Function<Validator, Set<ConstraintViolation<?>>> validation) throws ConstraintViolationException {
        var errors = validation.apply(getValidator());
        if (!errors.isEmpty()) {
            throw new ConstraintViolationException(errors);
        }
    }

    /** 返回底层 Jakarta {@link Validator} 实例。 */
    @Override
    public Validator getValidator() {
        return validator;
    }

}
