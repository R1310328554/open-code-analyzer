package org.keycloak.validation.jakarta;

import java.util.Set;
import java.util.function.Function;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

/**
 * Jakarta Bean Validation 的提供者接口。
 * <p>
 * 实现可通过 Hibernate Validator 的 constraint validator payload 机制向自定义约束校验器提供 {@link ValidationContext}，以便访问 Keycloak 运行时上下文（session、realm）。
 *
 * @see ValidationContext
 * @see HibernateValidatorProvider
 */
public interface JakartaValidatorProvider {

    /**
     * 使用指定校验组校验给定对象。
     *
     * @param object 待校验对象
     * @param groups 应用的校验组
     * @param <T> 对象类型
     * @throws ConstraintViolationException 校验失败时
     */
    <T> void validate(T object, Class<?>... groups) throws ConstraintViolationException;

    /**
     * 通过接收 {@link Validator} 的自定义函数执行校验。
     *
     * @param validation 执行校验并返回约束违规集合的函数
     * @throws ConstraintViolationException 校验失败时
     */
    void validate(Function<Validator, Set<ConstraintViolation<?>>> validation) throws ConstraintViolationException;

    /**
     * 返回底层 Jakarta {@link Validator} 实例。
     *
     * @return 校验器实例
     */
    Validator getValidator();
}
