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

/**
 * 可用于自定义嵌入式数据库的 {@link EmbeddedDatabaseConfigurer} 委托。
 * @author Stephane Nicoll
 * @since 6.2
 */
public class EmbeddedDatabaseConfigurerDelegate extends AbstractEmbeddedDatabaseConfigurer {

	/** 目标相关状态（`target`）。 */
	private final EmbeddedDatabaseConfigurer target;

	/**
	 * 创建 `EmbeddedDatabaseConfigurerDelegate` 的新实例。
	 */
	public EmbeddedDatabaseConfigurerDelegate(EmbeddedDatabaseConfigurer target) {
		this.target = target;
	}

	/**
	 * 方法 `configureConnectionProperties`：完成本类中与「configure Connection Properties」相关的职责。
	 */
	@Override
	public void configureConnectionProperties(ConnectionProperties properties, String databaseName) {
		this.target.configureConnectionProperties(properties, databaseName);
	}

}
