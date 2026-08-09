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

import org.springframework.jmx.support.MetricType;
import org.springframework.util.Assert;

/**
 * 指示将给定 Bean 属性暴露为 JMX 属性的元数据，
 * 并附带描述符属性表明该属性为指标（metric）。
 * 仅当用于 JavaBean getter 时有效。
 *
 * @author Jennifer Hickey
 * @since 3.0
 * @see org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler
 */
public class ManagedMetric extends AbstractJmxAttribute {

	private @Nullable String category;

	private @Nullable String displayName;

	private MetricType metricType = MetricType.GAUGE;

	private int persistPeriod = -1;

	private @Nullable String persistPolicy;

	private @Nullable String unit;


	/**
	 * 该指标的类别（例如吞吐量、性能、利用率）。
	 */
	public void setCategory(@Nullable String category) {
		this.category = category;
	}

	/**
	 * The category of this metric (ex. throughput, performance, utilization).
	 */
	public @Nullable String getCategory() {
		return this.category;
	}

	/**
	 * 该指标的显示名称。
	 */
	public void setDisplayName(@Nullable String displayName) {
		this.displayName = displayName;
	}

	/**
	 * A display name for this metric.
	 */
	public @Nullable String getDisplayName() {
		return this.displayName;
	}

	/**
	 * 描述该指标值随时间变化的特性。
	 */
	public void setMetricType(MetricType metricType) {
		Assert.notNull(metricType, "MetricType must not be null");
		this.metricType = metricType;
	}

	/**
	 * A description of how this metric's values change over time.
	 */
	public MetricType getMetricType() {
		return this.metricType;
	}

	/**
	 * 该指标的持久化周期。
	 */
	public void setPersistPeriod(int persistPeriod) {
		this.persistPeriod = persistPeriod;
	}

	/**
	 * The persist period for this metric.
	 */
	public int getPersistPeriod() {
		return this.persistPeriod;
	}

	/**
	 * 该指标的持久化策略。
	 */
	public void setPersistPolicy(@Nullable String persistPolicy) {
		this.persistPolicy = persistPolicy;
	}

	/**
	 * The persist policy for this metric.
	 */
	public @Nullable String getPersistPolicy() {
		return this.persistPolicy;
	}

	/**
	 * 测量值的预期单位。
	 */
	public void setUnit(@Nullable String unit) {
		this.unit = unit;
	}

	/**
	 * The expected unit of measurement values.
	 */
	public @Nullable String getUnit() {
		return this.unit;
	}

}
