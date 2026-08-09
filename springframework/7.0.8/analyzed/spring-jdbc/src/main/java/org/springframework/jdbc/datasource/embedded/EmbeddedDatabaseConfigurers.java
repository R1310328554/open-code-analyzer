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

package org.springframework.jdbc.datasource.embedded;

import java.util.function.UnaryOperator;

import org.springframework.util.Assert;

/**
 * 将常见 {@linkplain EmbeddedDatabaseType 嵌入式数据库类型}
 * 映射到 {@link EmbeddedDatabaseConfigurer} 策略。
 *
 * @author Keith Donald
 * @author Oliver Gierke
 * @author Sam Brannen
 * @author Stephane Nicoll
 * @since 6.2
 */
public abstract class EmbeddedDatabaseConfigurers {

	/**
	 * 返回给定嵌入式数据库类型对应的配置器实例。
	 * @param type {@linkplain EmbeddedDatabaseType 嵌入式数据库类型}
	 * @return 配置器实例
	 * @throws IllegalStateException 指定数据库类型的驱动不可用时
	 */
	public static EmbeddedDatabaseConfigurer getConfigurer(EmbeddedDatabaseType type) {
		Assert.notNull(type, "EmbeddedDatabaseType is required");
		try {
			return switch (type) {
				case HSQL -> HsqlEmbeddedDatabaseConfigurer.getInstance();
				case H2 -> H2EmbeddedDatabaseConfigurer.getInstance();
				case DERBY -> DerbyEmbeddedDatabaseConfigurer.getInstance();
			};
		}
		catch (ClassNotFoundException | NoClassDefFoundError ex) {
			throw new IllegalStateException("Driver for test database type [" + type + "] is not available", ex);
		}
	}

	/**
	 * 自定义给定嵌入式数据库类型的默认配置器。
	 * <p>{@code customizer} 通常使用
	 * {@link EmbeddedDatabaseConfigurerDelegate} 按需定制。
	 * @param type {@linkplain EmbeddedDatabaseType 嵌入式数据库类型}
	 * @param customizer 基于默认配置器返回定制结果的函数
	 * @return 定制后的配置器实例
	 * @throws IllegalStateException 指定数据库类型的驱动不可用时
	 */
	public static EmbeddedDatabaseConfigurer customizeConfigurer(
			EmbeddedDatabaseType type, UnaryOperator<EmbeddedDatabaseConfigurer> customizer) {

		EmbeddedDatabaseConfigurer defaultConfigurer = getConfigurer(type);
		return customizer.apply(defaultConfigurer);
	}

}
