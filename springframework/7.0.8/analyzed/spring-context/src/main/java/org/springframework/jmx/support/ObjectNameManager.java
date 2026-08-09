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

package org.springframework.jmx.support;

import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * 创建 {@link javax.management.ObjectName} 实例的辅助类。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see javax.management.ObjectName#getInstance(String)
 */
public final class ObjectNameManager {

	private ObjectNameManager() {
	}


	/**
	 * 获取与给定名称对应的 {@link ObjectName} 实例。
	 * @param name {@code ObjectName} 或 {@code String} 格式的名称
	 * @return {@code ObjectName} 实例
	 * @throws MalformedObjectNameException 对象名无效时
	 * @see ObjectName#ObjectName(String)
	 * @see ObjectName#getInstance(String)
	 */
	public static ObjectName getInstance(Object name) throws MalformedObjectNameException {
		if (name instanceof ObjectName objectName) {
			return objectName;
		}
		if (!(name instanceof String text)) {
			throw new MalformedObjectNameException("Invalid ObjectName value type [" +
					name.getClass().getName() + "]: only ObjectName and String supported.");
		}
		return getInstance(text);
	}

	/**
	 * 获取与给定名称对应的 {@code ObjectName} 实例。
	 * @param objectName {@code String} 格式的 {@code ObjectName}
	 * @return {@code ObjectName} 实例
	 * @throws MalformedObjectNameException 对象名无效时
	 * @see ObjectName#ObjectName(String)
	 * @see ObjectName#getInstance(String)
	 */
	public static ObjectName getInstance(String objectName) throws MalformedObjectNameException {
		return ObjectName.getInstance(objectName);
	}

	/**
	 * 获取指定域及单个键值属性对应的 {@code ObjectName} 实例。
	 * @param domainName {@code ObjectName} 的域名
	 * @param key 单个属性的键
	 * @param value 单个属性的值
	 * @return {@code ObjectName} 实例
	 * @throws MalformedObjectNameException 对象名无效时
	 * @see ObjectName#ObjectName(String, String, String)
	 * @see ObjectName#getInstance(String, String, String)
	 */
	public static ObjectName getInstance(String domainName, String key, String value)
			throws MalformedObjectNameException {

		return ObjectName.getInstance(domainName, key, value);
	}

	/**
	 * 获取指定域名及键/值属性表对应的 {@code ObjectName} 实例。
	 * @param domainName {@code ObjectName} 的域名
	 * @param properties {@code ObjectName} 的属性表
	 * @return {@code ObjectName} 实例
	 * @throws MalformedObjectNameException 对象名无效时
	 * @see ObjectName#ObjectName(String, java.util.Hashtable)
	 * @see ObjectName#getInstance(String, java.util.Hashtable)
	 */
	public static ObjectName getInstance(String domainName, Hashtable<String, String> properties)
			throws MalformedObjectNameException {

		return ObjectName.getInstance(domainName, properties);
	}

}
