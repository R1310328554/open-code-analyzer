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

package org.springframework.jmx.export.metadata;

import org.jspecify.annotations.Nullable;

/**
 * 指示将给定 Bean 属性暴露为 JMX 属性的元数据。
 * 仅当用于 JavaBean getter 或 setter 时有效。
 *
 * @author Rob Harrop
 * @since 1.2
 * @see org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler
 * @see org.springframework.jmx.export.MBeanExporter
 */
public class ManagedAttribute extends AbstractJmxAttribute {

	/**
	 * 空属性占位实例。
	 */
	public static final ManagedAttribute EMPTY = new ManagedAttribute();


	private @Nullable Object defaultValue;

	private @Nullable String persistPolicy;

	private int persistPeriod = -1;


	/**
	 * 设置该属性的默认值。
	 */
	public void setDefaultValue(@Nullable Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * 返回该属性的默认值。
	 */
	public @Nullable Object getDefaultValue() {
		return this.defaultValue;
	}

	public void setPersistPolicy(@Nullable String persistPolicy) {
		this.persistPolicy = persistPolicy;
	}

	public @Nullable String getPersistPolicy() {
		return this.persistPolicy;
	}

	public void setPersistPeriod(int persistPeriod) {
		this.persistPeriod = persistPeriod;
	}

	public int getPersistPeriod() {
		return this.persistPeriod;
	}

}
