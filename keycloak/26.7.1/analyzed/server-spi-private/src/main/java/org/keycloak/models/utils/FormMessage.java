/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.models.utils;

import java.util.Arrays;

/**
 * 表单消息（如错误提示），绑定字段名与国际化消息键。
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class FormMessage {

	/** 全局消息（不绑定具体表单字段）时 {@link #field} 使用的常量值。 */
	
	public static final String GLOBAL = "global";

	private String field;
	private String message;
	private Object[] parameters;

	public FormMessage() {
	}

	/**
	 * 创建带格式化参数的表单消息。
	 * 
	 * @param field 目标字段；为 {@code null} 时使用 {@link #GLOBAL}
	 * @param message 国际化消息键
	 * @param parameters 消息格式化参数
	 */
	public FormMessage(String field, String message, Object... parameters) {
		this(field, message);
		this.parameters = parameters;
	}

    public FormMessage(String message, Object...parameters) {
        this(null, message, parameters);
    }
	
	/**
     * 创建无格式化参数的表单消息。
     * 
     * @param field 目标字段；为 {@code null} 时使用 {@link #GLOBAL}
     * @param message 国际化消息键
     */
    public FormMessage(String field, String message) {
        super();
        if (field == null)
            field = GLOBAL;
        this.field = field;
        this.message = message;
    }

	/** @return 消息绑定的表单字段名 */
	public String getField() {
		return field;
	}

	/** @return 国际化消息键 */
	public String getMessage() {
		return message;
	}

	/** @return 消息格式化参数数组 */
	public Object[] getParameters() {
		return parameters;
	}

	@Override
	public String toString() {
		return "FormMessage [field=" + field + ", message=" + message + ", parameters=" + Arrays.toString(parameters) + "]";
	}

}
