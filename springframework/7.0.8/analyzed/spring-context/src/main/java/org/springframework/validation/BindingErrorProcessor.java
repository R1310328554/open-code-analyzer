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

import org.springframework.beans.PropertyAccessException;

/**
 * 处理 {@code DataBinder} 缺失字段错误，并将 {@code PropertyAccessException}
 * 翻译为 {@code FieldError} 的策略接口。
 *
 * <p>错误处理器可插拔，可按需定制错误处理方式；
 * 典型场景下提供了默认实现。
 *
 * <p>注意：自 Spring 2.0 起，本接口基于给定 BindingResult 操作，
 * 以兼容任意绑定策略（bean 属性、直接字段访问等）。
 * 仍可接收 BindException 参数（因 BindException 也实现 BindingResult），
 * 但不再直接在其上操作。
 *
 * @author Alef Arendsen
 * @author Juergen Hoeller
 * @since 1.2
 * @see DataBinder#setBindingErrorProcessor
 * @see DefaultBindingErrorProcessor
 * @see BindingResult
 * @see BindException
 */
public interface BindingErrorProcessor {

	/**
	 * 将缺失字段错误应用到给定 BindException。
	 * <p>通常为缺失的必填字段创建字段错误。
	 * @param missingField 绑定过程中缺失的字段
	 * @param bindingResult 要添加错误的 Errors 对象。
	 * 可添加多个错误，甚至忽略该错误。
	 * {@code BindingResult} 提供 {@code resolveMessageCodes} 等便捷工具解析错误码。
	 * @see BeanPropertyBindingResult#addError
	 * @see BeanPropertyBindingResult#resolveMessageCodes
	 */
	void processMissingFieldError(String missingField, BindingResult bindingResult);

	/**
	 * 将给定 {@code PropertyAccessException} 翻译为注册到给定 {@code Errors} 实例的适当错误。
	 * <p>注意：可用错误类型有 {@code FieldError} 与 {@code ObjectError}。
	 * 通常创建字段错误，但某些情况下可能希望创建全局 {@code ObjectError}。
	 * @param ex 要翻译的 {@code PropertyAccessException}
	 * @param bindingResult 要添加错误的 Errors 对象。
	 * 可添加多个错误，甚至忽略该错误。
	 * {@code BindingResult} 提供 {@code resolveMessageCodes} 等便捷工具解析错误码。
	 * @see Errors
	 * @see FieldError
	 * @see ObjectError
	 * @see MessageCodesResolver
	 * @see BeanPropertyBindingResult#addError
	 * @see BeanPropertyBindingResult#resolveMessageCodes
	 */
	void processPropertyAccessException(PropertyAccessException ex, BindingResult bindingResult);

}
