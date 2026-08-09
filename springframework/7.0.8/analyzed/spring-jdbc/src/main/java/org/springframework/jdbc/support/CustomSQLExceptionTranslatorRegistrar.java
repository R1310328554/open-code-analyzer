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

package org.springframework.jdbc.support;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

/**
 * 特定数据库自定义 {@link SQLExceptionTranslator} 实例的注册器。
 *
 * @author Thomas Risberg
 * @since 3.1.1
 */
public class CustomSQLExceptionTranslatorRegistrar implements InitializingBean {

	/**
	 * 保存特定数据库自定义翻译器的映射注册表。
	 * 键为 {@link org.springframework.jdbc.support.SQLErrorCodesFactory} 中定义的数据库产品名称。
	 */
	private final Map<String, SQLExceptionTranslator> translators = new HashMap<>();


	/**
	 * 设置 {@link SQLExceptionTranslator} 引用映射，键必须为 {@code sql-error-codes.xml} 中定义的数据库名称。
	 * <p>已有翻译器将保留，除非数据库名称匹配时新翻译器将替换现有翻译器。
	 */
	public void setTranslators(Map<String, SQLExceptionTranslator> translators) {
		this.translators.putAll(translators);
	}

	@Override
	public void afterPropertiesSet() {
		this.translators.forEach((dbName, translator) ->
				CustomSQLExceptionTranslatorRegistry.getInstance().registerTranslator(dbName, translator));
	}

}
