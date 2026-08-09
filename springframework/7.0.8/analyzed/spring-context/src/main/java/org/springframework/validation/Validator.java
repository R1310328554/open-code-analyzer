/*
 * Copyright 2002-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.validation;

import java.util.function.BiConsumer;

/**
 * 应用特定对象的校验器。
 *
 * <p>本接口与任何基础设施或上下文完全解耦；
 * 即不局限于校验 Web 层、数据访问层或其他层中的对象。
 * 因此可在应用的任意层使用，支持将校验逻辑作为一等公民独立封装。
 *
 * <p>Implementations can be created via the static factory methods
 * {@link #forInstanceOf(Class, BiConsumer)} or
 * {@link #forType(Class, BiConsumer)}.
 * 以下是一个简单但完整的 {@code Validator}，校验 {@code UserLogin} 实例的
 * 各 {@link String} 属性非空（不为 {@code null} 且不全为空白），
 * 且存在的密码长度至少为 {@code 'MINIMUM_PASSWORD_LENGTH'} 字符。
 *
 * <pre class="code">Validator userLoginValidator = Validator.forInstance(UserLogin.class, (login, errors) -> {
 *   ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userName", "field.required");
 *   ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "field.required");
 *   if (login.getPassword() != null
 *         &amp;&amp; login.getPassword().trim().length() &lt; MINIMUM_PASSWORD_LENGTH) {
 *      errors.rejectValue("password", "field.min.length",
 *            new Object[]{Integer.valueOf(MINIMUM_PASSWORD_LENGTH)},
 *            "The password must be at least [" + MINIMUM_PASSWORD_LENGTH + "] characters in length.");
 *   }
 * });</pre>
 *
 * <p>另请参阅 Spring 参考手册中对 {@code Validator} 接口
 * 及其在企业应用中角色的更完整讨论。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Toshiaki Maki
 * @author Arjen Poutsma
 * @see SmartValidator
 * @see Errors
 * @see ValidationUtils
 * @see DataBinder#setValidator
 */
public interface Validator {

	/**
	 * 本 {@link Validator} 能否 {@link #validate(Object, Errors) 校验}
	 * 所提供 {@code clazz} 的实例？
	 * <p>本方法<i>通常</i>这样实现：
	 * <pre class="code">return Foo.class.isAssignableFrom(clazz);</pre>
	 * （其中 {@code Foo} 是要 {@link #validate(Object, Errors) 校验} 的
	 * 实际对象实例的类（或超类）。）
	 * @param clazz 询问本 {@link Validator} 能否 {@link #validate(Object, Errors) 校验} 的 {@link Class}
	 * @return 若本 {@link Validator} 确实能 {@link #validate(Object, Errors) 校验}
	 * 所提供 {@code clazz} 的实例则为 {@code true}
	 */
	boolean supports(Class<?> clazz);

	/**
	 * 校验给定 {@code target} 对象，其 {@link Class} 须使 {@link #supports(Class)} 方法
	 * 通常已返回（或将返回）{@code true}。
	 * <p>所提供的 {@link Errors errors} 实例可用于报告任何校验错误，
	 * 通常作为本校验器参与的大型绑定过程的一部分。
	 * 绑定错误通常已在本调用之前预先注册到 {@link Errors errors} 实例。
	 * @param target 要校验的对象
	 * @param errors 校验过程的上下文状态
	 * @see ValidationUtils
	 */
	void validate(Object target, Errors errors);

	/**
	 * 单独校验给定 {@code target} 对象。
	 * <p>委托通用 {@link #validate(Object, Errors)} 方法。
	 * The returned {@link Errors errors} instance can be used to report
	 * any resulting validation errors for the specific target object, for example,
	 * {@code if (validator.validateObject(target).hasErrors()) ...} or
	 * {@code validator.validateObject(target).failOnError(IllegalStateException::new));}.
	 * <p>注意：此校验调用在使用 {@link Errors} 实现时有限制，尤其不支持嵌套路径。
	 * 若不足，请使用支持绑定的 {@link Errors} 实现（如 {@link BeanPropertyBindingResult}）
	 * 调用常规 {@link #validate(Object, Errors)} 方法。
	 * @param target the object that is to be validated
	 * @return 给定对象校验产生的错误
	 * @since 6.1
	 * @see SimpleErrors
	 */
	default Errors validateObject(Object target) {
		Errors errors = new SimpleErrors(target);
		validate(target, errors);
		return errors;
	}


	/**
	 * 返回 {@code Validator}，检查目标对象
	 * {@linkplain Class#isAssignableFrom(Class) 是否为} {@code targetClass} 的实例，
	 * 若是则应用给定 {@code delegate} 填充 {@link Errors}。
	 * <p>For instance:
	 * <pre class="code">Validator passwordEqualsValidator = Validator.forInstanceOf(PasswordResetForm.class, (form, errors) -> {
	 *   if (!Objects.equals(form.getPassword(), form.getConfirmPassword())) {
	 * 	   errors.rejectValue("confirmPassword",
	 * 	         "PasswordEqualsValidator.passwordResetForm.password",
	 * 	         "password and confirm password must be same.");
	 * 	   }
	 * 	 });</pre>
	 * @param targetClass 返回校验器支持的类
	 * @param delegate 若目标对象为 T 类型实例则调用的函数
	 * @param <T> 目标对象类型
	 * @return 创建的 {@code Validator}
	 * @since 6.1
	 */
	static <T> Validator forInstanceOf(Class<T> targetClass, BiConsumer<T, Errors> delegate) {
		return new TypedValidator<>(targetClass, targetClass::isAssignableFrom, delegate);
	}

	/**
	 * 返回 {@code Validator}，检查目标对象的类是否与 {@code targetClass} 完全相同，
	 * 若是则应用给定 {@code delegate} 填充 {@link Errors}。
	 * <p>For instance:
	 * <pre class="code">Validator passwordEqualsValidator = Validator.forType(PasswordResetForm.class, (form, errors) -> {
	 *   if (!Objects.equals(form.getPassword(), form.getConfirmPassword())) {
	 * 	   errors.rejectValue("confirmPassword",
	 * 	         "PasswordEqualsValidator.passwordResetForm.password",
	 * 	         "password and confirm password must be same.");
	 * 	   }
	 * 	 });</pre>
	 * @param targetClass 返回校验器支持的确切类（不含子类）
	 * @param delegate function invoked with the target object, if it is an
	 * instance of type T
	 * @param <T> the target object type
	 * @return the created {@code Validator}
	 * @since 6.1
	 */
	static <T> Validator forType(Class<T> targetClass, BiConsumer<T, Errors> delegate) {
		return new TypedValidator<>(targetClass, targetClass::equals, delegate);
	}

}
