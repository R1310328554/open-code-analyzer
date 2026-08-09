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
 * 特定数据库的自定义 {@link SQLExceptionTranslator} 实例的注册表。
 * @author Thomas Risberg
 * @since 3.1.1
 */
public class CustomSQLExceptionTranslatorRegistrar implements InitializingBean {

	/**
	 * 映射注册表以保存自定义翻译器特定数据库。键是 {@link org.springframework.jdbc.support.SQLErrorCodesFactory} 中定义
	 * 的数据库产品名称。
	 */
	private final Map<String, SQLExceptionTranslator> translators = new HashMap<>();


	/**
	 * {@link SQLExceptionTranslator} 引用映射的设置器，其中键必须是 {@code sql-error-codes.xml} 文件中定义的数据库名称。 
	 * <p>请注意，除非数据库名称匹配，否则任何现有的翻译器都将保留，此时新的翻译器将替换现有的翻译器。
	 */
	public void setTranslators(Map<String, SQLExceptionTranslator> translators) {
		this.translators.putAll(translators);
	}

	/**
	 * 在…之后回调：Properties Set（方法 `afterPropertiesSet`）。
	 */
	@Override
	public void afterPropertiesSet() {
		this.translators.forEach((dbName, translator) ->
				CustomSQLExceptionTranslatorRegistry.getInstance().registerTranslator(dbName, translator));
	}

}
