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

package org.springframework.jmx.export.assembler;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Objects;

import javax.management.Descriptor;
import javax.management.MBeanParameterInfo;
import javax.management.modelmbean.ModelMBeanNotificationInfo;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.metadata.InvalidMetadataException;
import org.springframework.jmx.export.metadata.JmxAttributeSource;
import org.springframework.jmx.export.metadata.JmxMetadataUtils;
import org.springframework.jmx.export.metadata.ManagedAttribute;
import org.springframework.jmx.export.metadata.ManagedMetric;
import org.springframework.jmx.export.metadata.ManagedNotification;
import org.springframework.jmx.export.metadata.ManagedOperation;
import org.springframework.jmx.export.metadata.ManagedOperationParameter;
import org.springframework.jmx.export.metadata.ManagedResource;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * {@link MBeanInfoAssembler} 接口的实现，从源码级元数据读取管理接口信息。
 *
 * <p>通过 {@link JmxAttributeSource} 策略接口读取元数据，支持多种实现。
 * Spring 内置基于注解的实现：{@code AnnotationJmxAttributeSource}。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Jennifer Hickey
 * @since 1.2
 * @see #setAttributeSource
 * @see org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource
 */
public class MetadataMBeanInfoAssembler extends AbstractReflectiveMBeanInfoAssembler
		implements AutodetectCapableMBeanInfoAssembler, InitializingBean {

	/** 读取 JMX 元数据的策略源。 */
	private @Nullable JmxAttributeSource attributeSource;


	/**
	 * 创建 MetadataMBeanInfoAssembler，须通过 {@link #setAttributeSource} 配置。
	 */
	public MetadataMBeanInfoAssembler() {
	}

	/**
	 * 使用给定 JmxAttributeSource 创建 MetadataMBeanInfoAssembler。
	 * @param attributeSource 要使用的 JmxAttributeSource
	 */
	public MetadataMBeanInfoAssembler(JmxAttributeSource attributeSource) {
		Assert.notNull(attributeSource, "JmxAttributeSource must not be null");
		this.attributeSource = attributeSource;
	}


	/**
	 * 设置用于从 Bean 类读取元数据的 JmxAttributeSource 实现。
	 * @see org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource
	 */
	public void setAttributeSource(JmxAttributeSource attributeSource) {
		Assert.notNull(attributeSource, "JmxAttributeSource must not be null");
		this.attributeSource = attributeSource;
	}

	@Override
	public void afterPropertiesSet() {
		if (this.attributeSource == null) {
			throw new IllegalArgumentException("Property 'attributeSource' is required");
		}
	}

	private JmxAttributeSource obtainAttributeSource() {
		Assert.state(this.attributeSource != null, "No JmxAttributeSource set");
		return this.attributeSource;
	}


	/**
	 * 若遇到 JDK 动态代理则抛出 IllegalArgumentException。元数据只能从目标类与 CGLIB 代理读取。
	 */
	@Override
	protected void checkManagedBean(Object managedBean) throws IllegalArgumentException {
		if (AopUtils.isJdkDynamicProxy(managedBean)) {
			throw new IllegalArgumentException(
					"MetadataMBeanInfoAssembler does not support JDK dynamic proxies - " +
					"export the target beans directly or use CGLIB proxies instead");
		}
	}

	/**
	 * 用于 Bean 自动检测：检查 Bean 类是否带有 ManagedResource 属性。若有，则将其纳入注册列表。
	 * @param beanClass Bean 的类
	 * @param beanName Bean 在 BeanFactory 中的名称
	 */
	@Override
	public boolean includeBean(Class<?> beanClass, String beanName) {
		return (obtainAttributeSource().getManagedResource(getClassToExpose(beanClass)) != null);
	}

	/**
	 * 对属性访问器进行纳入投票。
	 * @param method 访问器方法
	 * @param beanKey beans 映射中与该 MBean 关联的键
	 * @return 方法是否带有相应元数据
	 */
	@Override
	protected boolean includeReadAttribute(Method method, String beanKey) {
		return hasManagedAttribute(method) || hasManagedMetric(method);
	}

	/**
	 * 对属性修改器进行纳入投票。
	 * @param method 修改器方法
	 * @param beanKey beans 映射中与该 MBean 关联的键
	 * @return 方法是否带有相应元数据
	 */
	@Override
	protected boolean includeWriteAttribute(Method method, String beanKey) {
		return hasManagedAttribute(method);
	}

	/**
	 * 对操作进行纳入投票。
	 * @param method 操作方法
	 * @param beanKey beans 映射中与该 MBean 关联的键
	 * @return 方法是否带有相应元数据
	 */
	@Override
	protected boolean includeOperation(Method method, String beanKey) {
		PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method);
		return (pd != null && hasManagedAttribute(method)) || hasManagedOperation(method);
	}

	/**
	 * 检查给定 Method 是否带有 ManagedAttribute 属性。
	 */
	private boolean hasManagedAttribute(Method method) {
		return (obtainAttributeSource().getManagedAttribute(method) != null);
	}

	/**
	 * 检查给定 Method 是否带有 ManagedMetric 属性。
	 */
	private boolean hasManagedMetric(Method method) {
		return (obtainAttributeSource().getManagedMetric(method) != null);
	}

	/**
	 * 检查给定 Method 是否带有 ManagedOperation 属性。
	 * @param method 待检查的方法
	 */
	private boolean hasManagedOperation(Method method) {
		return (obtainAttributeSource().getManagedOperation(method) != null);
	}


	/**
	 * 从源码级元数据读取受管资源描述；找不到时返回空 String。
	 */
	@Override
	protected String getDescription(Object managedBean, String beanKey) {
		ManagedResource mr = obtainAttributeSource().getManagedResource(getClassToExpose(managedBean));
		return (mr != null ? mr.getDescription() : "");
	}

	/**
	 * 为与属性描述符对应的属性创建描述。优先使用 getter 或 setter 元数据中的描述，否则使用属性名。
	 */
	@Override
	protected String getAttributeDescription(PropertyDescriptor propertyDescriptor, String beanKey) {
		Method readMethod = propertyDescriptor.getReadMethod();
		Method writeMethod = propertyDescriptor.getWriteMethod();

		ManagedAttribute getter =
				(readMethod != null ? obtainAttributeSource().getManagedAttribute(readMethod) : null);
		ManagedAttribute setter =
				(writeMethod != null ? obtainAttributeSource().getManagedAttribute(writeMethod) : null);

		if (getter != null && StringUtils.hasText(getter.getDescription())) {
			return getter.getDescription();
		}
		else if (setter != null && StringUtils.hasText(setter.getDescription())) {
			return setter.getDescription();
		}

		ManagedMetric metric = (readMethod != null ? obtainAttributeSource().getManagedMetric(readMethod) : null);
		if (metric != null && StringUtils.hasText(metric.getDescription())) {
			return metric.getDescription();
		}

		return propertyDescriptor.getDisplayName();
	}

	/**
	 * 从元数据获取给定 Method 的描述；元数据中无描述时使用方法名。
	 */
	@Override
	protected String getOperationDescription(Method method, String beanKey) {
		PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method);
		if (pd != null) {
			ManagedAttribute ma = obtainAttributeSource().getManagedAttribute(method);
			if (ma != null && StringUtils.hasText(ma.getDescription())) {
				return ma.getDescription();
			}
			ManagedMetric metric = obtainAttributeSource().getManagedMetric(method);
			if (metric != null && StringUtils.hasText(metric.getDescription())) {
				return metric.getDescription();
			}
			return method.getName();
		}
		else {
			ManagedOperation mo = obtainAttributeSource().getManagedOperation(method);
			if (mo != null && StringUtils.hasText(mo.getDescription())) {
				return mo.getDescription();
			}
			return method.getName();
		}
	}

	/**
	 * 从方法上附带的 ManagedOperationParameter 属性读取 MBeanParameterInfo。未找到属性时返回空数组。
	 */
	@Override
	protected MBeanParameterInfo[] getOperationParameters(Method method, String beanKey) {
		@Nullable ManagedOperationParameter[] params = obtainAttributeSource().getManagedOperationParameters(method);
		if (ObjectUtils.isEmpty(params)) {
			return super.getOperationParameters(method, beanKey);
		}

		MBeanParameterInfo[] parameterInfo = new MBeanParameterInfo[params.length];
		Class<?>[] methodParameters = method.getParameterTypes();
		for (int i = 0; i < params.length; i++) {
			ManagedOperationParameter param = Objects.requireNonNull(params[i]);
			parameterInfo[i] =
					new MBeanParameterInfo(param.getName(), methodParameters[i].getName(), param.getDescription());
		}
		return parameterInfo;
	}

	/**
	 * 从受管资源 Class 的 ManagedNotification 元数据生成对应的 ModelMBeanNotificationInfo 元数据。
	 */
	@Override
	protected ModelMBeanNotificationInfo[] getNotificationInfo(Object managedBean, String beanKey) {
		@Nullable ManagedNotification[] notificationAttributes =
				obtainAttributeSource().getManagedNotifications(getClassToExpose(managedBean));
		ModelMBeanNotificationInfo[] notificationInfos =
				new ModelMBeanNotificationInfo[notificationAttributes.length];

		for (int i = 0; i < notificationAttributes.length; i++) {
			ManagedNotification attribute = notificationAttributes[i];
			notificationInfos[i] = JmxMetadataUtils.convertToModelMBeanNotificationInfo(Objects.requireNonNull(attribute));
		}

		return notificationInfos;
	}

	/**
	 * 将 ManagedResource 属性中的描述符字段添加到 MBean 描述符，包括 currencyTimeLimit、persistPolicy、persistPeriod、persistLocation 与 persistName（若元数据中存在）。
	 */
	@Override
	protected void populateMBeanDescriptor(Descriptor desc, Object managedBean, String beanKey) {
		ManagedResource mr = obtainAttributeSource().getManagedResource(getClassToExpose(managedBean));
		if (mr == null) {
			throw new InvalidMetadataException(
					"No ManagedResource attribute found for class: " + getClassToExpose(managedBean));
		}

		applyCurrencyTimeLimit(desc, mr.getCurrencyTimeLimit());

		if (mr.isLog()) {
			desc.setField(FIELD_LOG, "true");
		}
		if (StringUtils.hasLength(mr.getLogFile())) {
			desc.setField(FIELD_LOG_FILE, mr.getLogFile());
		}

		if (StringUtils.hasLength(mr.getPersistPolicy())) {
			desc.setField(FIELD_PERSIST_POLICY, mr.getPersistPolicy());
		}
		if (mr.getPersistPeriod() >= 0) {
			desc.setField(FIELD_PERSIST_PERIOD, Integer.toString(mr.getPersistPeriod()));
		}
		if (StringUtils.hasLength(mr.getPersistName())) {
			desc.setField(FIELD_PERSIST_NAME, mr.getPersistName());
		}
		if (StringUtils.hasLength(mr.getPersistLocation())) {
			desc.setField(FIELD_PERSIST_LOCATION, mr.getPersistLocation());
		}
	}

	/**
	 * 将 ManagedAttribute 或 ManagedMetric 属性中的描述符字段添加到属性描述符。
	 */
	@Override
	protected void populateAttributeDescriptor(
			Descriptor desc, @Nullable Method getter, @Nullable Method setter, String beanKey) {

		if (getter != null) {
			ManagedMetric metric = obtainAttributeSource().getManagedMetric(getter);
			if (metric != null) {
				populateMetricDescriptor(desc, metric);
				return;
			}
		}

		ManagedAttribute gma = (getter != null ? obtainAttributeSource().getManagedAttribute(getter) : null);
		ManagedAttribute sma = (setter != null ? obtainAttributeSource().getManagedAttribute(setter) : null);
		populateAttributeDescriptor(desc,
				(gma != null ? gma : ManagedAttribute.EMPTY),
				(sma != null ? sma : ManagedAttribute.EMPTY));
	}

	private void populateAttributeDescriptor(Descriptor desc, ManagedAttribute gma, ManagedAttribute sma) {
		applyCurrencyTimeLimit(desc, resolveIntDescriptor(gma.getCurrencyTimeLimit(), sma.getCurrencyTimeLimit()));

		Object defaultValue = resolveObjectDescriptor(gma.getDefaultValue(), sma.getDefaultValue());
		desc.setField(FIELD_DEFAULT, defaultValue);

		String persistPolicy = resolveStringDescriptor(gma.getPersistPolicy(), sma.getPersistPolicy());
		if (StringUtils.hasLength(persistPolicy)) {
			desc.setField(FIELD_PERSIST_POLICY, persistPolicy);
		}
		int persistPeriod = resolveIntDescriptor(gma.getPersistPeriod(), sma.getPersistPeriod());
		if (persistPeriod >= 0) {
			desc.setField(FIELD_PERSIST_PERIOD, Integer.toString(persistPeriod));
		}
	}

	private void populateMetricDescriptor(Descriptor desc, ManagedMetric metric) {
		applyCurrencyTimeLimit(desc, metric.getCurrencyTimeLimit());

		if (StringUtils.hasLength(metric.getPersistPolicy())) {
			desc.setField(FIELD_PERSIST_POLICY, metric.getPersistPolicy());
		}
		if (metric.getPersistPeriod() >= 0) {
			desc.setField(FIELD_PERSIST_PERIOD, Integer.toString(metric.getPersistPeriod()));
		}

		if (StringUtils.hasLength(metric.getDisplayName())) {
			desc.setField(FIELD_DISPLAY_NAME, metric.getDisplayName());
		}

		if (StringUtils.hasLength(metric.getUnit())) {
			desc.setField(FIELD_UNITS, metric.getUnit());
		}

		if (StringUtils.hasLength(metric.getCategory())) {
			desc.setField(FIELD_METRIC_CATEGORY, metric.getCategory());
		}

		desc.setField(FIELD_METRIC_TYPE, metric.getMetricType().toString());
	}

	/**
	 * 将 ManagedAttribute 属性中的描述符字段添加到操作描述符，主要是 currencyTimeLimit（若元数据中存在）。
	 */
	@Override
	protected void populateOperationDescriptor(Descriptor desc, Method method, String beanKey) {
		ManagedOperation mo = obtainAttributeSource().getManagedOperation(method);
		if (mo != null) {
			applyCurrencyTimeLimit(desc, mo.getCurrencyTimeLimit());
		}
	}

	/**
	 * 在 getter 与 setter 两个 int 值中选取属性描述符应使用的值。通常仅一方为非负值；若两者皆非负，取较大者。
	 * @param getter getter 关联的 int 值
	 * @param setter setter 关联的 int 值
	 */
	private int resolveIntDescriptor(int getter, int setter) {
		return Math.max(getter, setter);
	}

	/**
	 * 根据 getter 与 setter 附带的值确定描述符值；两者皆有值时优先 getter。
	 * @param getter getter 关联的 Object 值
	 * @param setter setter 关联的 Object 值
	 * @return 描述符应使用的 Object 值
	 */
	private @Nullable Object resolveObjectDescriptor(@Nullable Object getter, @Nullable Object setter) {
		return (getter != null ? getter : setter);
	}

	/**
	 * 根据 getter 与 setter 附带的值确定描述符值；两者皆有值时优先 getter。
	 * @param getter getter 关联的 String 值
	 * @param setter setter 关联的 String 值
	 * @return 描述符应使用的 String 值
	 */
	private @Nullable String resolveStringDescriptor(@Nullable String getter, @Nullable String setter) {
		return (StringUtils.hasLength(getter) ? getter : setter);
	}

}
