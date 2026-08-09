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

package org.springframework.context.annotation;

import org.springframework.beans.factory.parsing.Location;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.core.type.MethodMetadata;

/**
 * {@link Configuration @Configuration} 类方法的基类。
 *
 * @author Chris Beams
 * @since 3.1
 */
abstract class ConfigurationMethod {

	/** 方法元数据。 */
	protected final MethodMetadata metadata;

	/** 所属配置类。 */
	protected final ConfigurationClass configurationClass;


	/**
	 * 构造配置方法描述。
	 * @param metadata 方法元数据
	 * @param configurationClass 所属配置类
	 */
	public ConfigurationMethod(MethodMetadata metadata, ConfigurationClass configurationClass) {
		this.metadata = metadata;
		this.configurationClass = configurationClass;
	}


	/** 返回方法元数据。 */
	public MethodMetadata getMetadata() {
		return this.metadata;
	}

	/** 返回所属配置类。 */
	public ConfigurationClass getConfigurationClass() {
		return this.configurationClass;
	}

	/** 返回该方法在配置类资源中的位置信息。 */
	public Location getResourceLocation() {
		return new Location(this.configurationClass.getResource(), this.metadata);
	}

	/** 校验方法定义；子类可覆盖以报告具体问题。 */
	void validate(ProblemReporter problemReporter) {
	}

	@Override
	public String toString() {
		return String.format("[%s:name=%s,declaringClass=%s]",
				getClass().getSimpleName(), getMetadata().getMethodName(), getMetadata().getDeclaringClassName());
	}

}
