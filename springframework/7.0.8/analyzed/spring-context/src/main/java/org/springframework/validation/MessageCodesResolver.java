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

import org.jspecify.annotations.Nullable;

/**
 * 从校验错误码构建消息码的策略接口。
 * DataBinder 用它为 ObjectError 与 FieldError 构建 codes 列表。
 *
 * <p>生成的消息码对应 MessageSourceResolvable 的 codes
 * （由 ObjectError 与 FieldError 实现）。
 *
 * @author Juergen Hoeller
 * @since 1.0.1
 * @see DataBinder#setMessageCodesResolver
 * @see ObjectError
 * @see FieldError
 * @see org.springframework.context.MessageSourceResolvable#getCodes()
 */
public interface MessageCodesResolver {

	/**
	 * 为给定错误码与对象名构建消息码。
	 * 用于构建 ObjectError 的 codes 列表。
	 * @param errorCode 用于拒绝对象的错误码
	 * @param objectName 对象名称
	 * @return 要使用的消息码
	 */
	String[] resolveMessageCodes(String errorCode, String objectName);

	/**
	 * 为给定错误码与字段规范构建消息码。
	 * 用于构建 FieldError 的 codes 列表。
	 * @param errorCode 用于拒绝值的错误码
	 * @param objectName 对象名称
	 * @param field 字段名
	 * @param fieldType 字段类型（无法确定时可为 {@code null}）
	 * @return 要使用的消息码
	 */
	String[] resolveMessageCodes(String errorCode, String objectName, String field, @Nullable Class<?> fieldType);

}
