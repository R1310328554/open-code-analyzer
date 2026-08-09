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

package org.springframework.beans;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.util.ObjectUtils;

/**
 * 标准 {@link BeanInfo} 对象的装饰器（例如由 {@link Introspector#getBeanInfo(Class)} 创建），
 * 用于发现并注册静态和/或非 void 返回的 setter 方法。例如：
 *
 * <pre class="code">
 * public class Bean {
 *
 *     private Foo foo;
 *
 *     public Foo getFoo() {
 *         return this.foo;
 *     }
 *
 *     public Bean setFoo(Foo foo) {
 *         this.foo = foo;
 *         return this;
 *     }
 * }</pre>
 *
 * 标准 JavaBeans {@code Introspector} 会发现 {@code getFoo} 读方法，
 * 但会跳过 {@code #setFoo(Foo)} 写方法，因为其非 void 返回签名不符合 JavaBeans 规范。
 * {@code ExtendedBeanInfo} 则会识别并包含它。这样可在 Spring {@code <beans>} XML 中
 * 使用「构建器」或方法链风格的 setter 签名。
 * {@link #getPropertyDescriptors()} 返回被包装 {@code BeanInfo} 中的全部现有属性描述符，
 * 以及为非 void 返回 setter 新增的描述符。标准（非索引）与
 * <a href="https://docs.oracle.com/javase/tutorial/javabeans/writing/properties.html">
 * 索引属性</a> 均完整支持。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see #ExtendedBeanInfo(BeanInfo)
 * @see ExtendedBeanInfoFactory
 * @see CachedIntrospectionResults
 */
class ExtendedBeanInfo implements BeanInfo {

	private static final Log logger = LogFactory.getLog(ExtendedBeanInfo.class);

	/** 被包装的原始 BeanInfo。 */
	private final BeanInfo delegate;

	/** 本地维护的属性描述符集合（含从 delegate 复制及后续发现的写方法）。 */
	private final Set<PropertyDescriptor> propertyDescriptors = new TreeSet<>(new PropertyDescriptorComparator());


	/**
	 * 包装给定 {@link BeanInfo}：将其现有属性描述符复制到本地，
	 * 每个包装为自定义 {@link SimpleIndexedPropertyDescriptor 索引}
	 * 或 {@link SimplePropertyDescriptor 非索引} {@code PropertyDescriptor} 变体，
	 * 以绕过 JDK 默认的弱/软引用管理；然后在其方法描述符中搜索非 void 返回的写方法，
	 * 为每个找到的方法更新或创建对应的 {@link PropertyDescriptor}。
	 * @param delegate 被包装的 {@code BeanInfo}，不会被修改
	 * @see #getPropertyDescriptors()
	 */
	public ExtendedBeanInfo(BeanInfo delegate) {
		this.delegate = delegate;
		for (PropertyDescriptor pd : delegate.getPropertyDescriptors()) {
			try {
				this.propertyDescriptors.add(pd instanceof IndexedPropertyDescriptor indexedPd ?
						new SimpleIndexedPropertyDescriptor(indexedPd) :
						new SimplePropertyDescriptor(pd));
			}
			catch (IntrospectionException ex) {
				// 可能只是未遵循 JavaBeans 模式的方法
				if (logger.isDebugEnabled()) {
					logger.debug("Ignoring invalid bean property '" + pd.getName() + "': " + ex.getMessage());
				}
			}
		}
		MethodDescriptor[] methodDescriptors = delegate.getMethodDescriptors();
		if (methodDescriptors != null) {
			for (Method method : findCandidateWriteMethods(methodDescriptors)) {
				try {
					handleCandidateWriteMethod(method);
				}
				catch (IntrospectionException ex) {
					// 仅查找候选，多余的可以忽略
					if (logger.isDebugEnabled()) {
						logger.debug("Ignoring candidate write method [" + method + "]: " + ex.getMessage());
					}
				}
			}
		}
	}


	/** 从方法描述符中筛选候选写方法并排序。 */
	private List<Method> findCandidateWriteMethods(MethodDescriptor[] methodDescriptors) {
		List<Method> matches = new ArrayList<>();
		for (MethodDescriptor methodDescriptor : methodDescriptors) {
			Method method = methodDescriptor.getMethod();
			if (isCandidateWriteMethod(method)) {
				matches.add(method);
			}
		}
		// 对非 void 返回写方法排序，避免 Class#getMethods 非确定性排序的副作用
		// 出于历史原因，自然排序顺序取反
		// 参见 https://github.com/spring-projects/spring-framework/issues/14744
		matches.sort(Comparator.comparing(Method::toString).reversed());
		return matches;
	}

	/** 判断方法是否为候选写方法（set*、public、非 void 或 static、1 或 2 个参数）。 */
	public static boolean isCandidateWriteMethod(Method method) {
		String methodName = method.getName();
		int nParams = method.getParameterCount();
		return (methodName.length() > 3 && methodName.startsWith("set") && Modifier.isPublic(method.getModifiers()) &&
				(!void.class.isAssignableFrom(method.getReturnType()) || Modifier.isStatic(method.getModifiers())) &&
				(nParams == 1 || (nParams == 2 && int.class == method.getParameterTypes()[0])));
	}

	/** 将候选写方法注册为属性描述符（普通或索引属性）。 */
	private void handleCandidateWriteMethod(Method method) throws IntrospectionException {
		int nParams = method.getParameterCount();
		String propertyName = propertyNameFor(method);
		Class<?> propertyType = method.getParameterTypes()[nParams - 1];
		PropertyDescriptor existingPd = findExistingPropertyDescriptor(propertyName, propertyType);
		if (nParams == 1) {
			if (existingPd == null) {
				this.propertyDescriptors.add(new SimplePropertyDescriptor(propertyName, null, method));
			}
			else {
				existingPd.setWriteMethod(method);
			}
		}
		else if (nParams == 2) {
			if (existingPd == null) {
				this.propertyDescriptors.add(
						new SimpleIndexedPropertyDescriptor(propertyName, null, null, null, method));
			}
			else if (existingPd instanceof IndexedPropertyDescriptor indexedPd) {
				indexedPd.setIndexedWriteMethod(method);
			}
			else {
				// 将普通属性升级为索引属性描述符
				this.propertyDescriptors.remove(existingPd);
				this.propertyDescriptors.add(new SimpleIndexedPropertyDescriptor(
						propertyName, existingPd.getReadMethod(), existingPd.getWriteMethod(), null, method));
			}
		}
		else {
			throw new IllegalArgumentException("Write method must have exactly 1 or 2 parameters: " + method);
		}
	}

	/** 按属性名与类型查找已存在的 PropertyDescriptor。 */
	private @Nullable PropertyDescriptor findExistingPropertyDescriptor(String propertyName, Class<?> propertyType) {
		for (PropertyDescriptor pd : this.propertyDescriptors) {
			final Class<?> candidateType;
			final String candidateName = pd.getName();
			if (pd instanceof IndexedPropertyDescriptor indexedPd) {
				candidateType = indexedPd.getIndexedPropertyType();
				if (candidateName.equals(propertyName) &&
						(candidateType.equals(propertyType) || candidateType.equals(propertyType.componentType()))) {
					return pd;
				}
			}
			else {
				candidateType = pd.getPropertyType();
				if (candidateName.equals(propertyName) &&
						(candidateType.equals(propertyType) || propertyType.equals(candidateType.componentType()))) {
					return pd;
				}
			}
		}
		return null;
	}

	/** 从 setter 方法名推导属性名（去掉 set 前缀并首字母小写）。 */
	private String propertyNameFor(Method method) {
		return Introspector.decapitalize(method.getName().substring(3));
	}


	/**
	 * 返回被包装 {@link BeanInfo} 中的 {@link PropertyDescriptor PropertyDescriptors} 集合，
	 * 以及构造期间发现的每个非 void 返回 setter 方法对应的 {@code PropertyDescriptors}。
	 * @see #ExtendedBeanInfo(BeanInfo)
	 */
	@Override
	public PropertyDescriptor[] getPropertyDescriptors() {
		return this.propertyDescriptors.toArray(PropertyDescriptorUtils.EMPTY_PROPERTY_DESCRIPTOR_ARRAY);
	}

	@Override
	public BeanInfo[] getAdditionalBeanInfo() {
		return this.delegate.getAdditionalBeanInfo();
	}

	@Override
	public BeanDescriptor getBeanDescriptor() {
		return this.delegate.getBeanDescriptor();
	}

	@Override
	public int getDefaultEventIndex() {
		return this.delegate.getDefaultEventIndex();
	}

	@Override
	public int getDefaultPropertyIndex() {
		return this.delegate.getDefaultPropertyIndex();
	}

	@Override
	public EventSetDescriptor[] getEventSetDescriptors() {
		return this.delegate.getEventSetDescriptors();
	}

	@Override
	public Image getIcon(int iconKind) {
		return this.delegate.getIcon(iconKind);
	}

	@Override
	public MethodDescriptor[] getMethodDescriptors() {
		return this.delegate.getMethodDescriptors();
	}


	/**
	 * 简单的 {@link PropertyDescriptor} 实现，绕过 JDK 弱引用管理。
	 */
	static class SimplePropertyDescriptor extends PropertyDescriptor {

		private @Nullable Method readMethod;

		private @Nullable Method writeMethod;

		private @Nullable Class<?> propertyType;

		private @Nullable Class<?> propertyEditorClass;

		public SimplePropertyDescriptor(PropertyDescriptor original) throws IntrospectionException {
			this(original.getName(), original.getReadMethod(), original.getWriteMethod());
			PropertyDescriptorUtils.copyNonMethodProperties(original, this);
		}

		public SimplePropertyDescriptor(String propertyName, @Nullable Method readMethod, Method writeMethod)
				throws IntrospectionException {

			super(propertyName, null, null);
			this.readMethod = readMethod;
			this.writeMethod = writeMethod;
			this.propertyType = PropertyDescriptorUtils.findPropertyType(readMethod, writeMethod);
		}

		@Override
		public @Nullable Method getReadMethod() {
			return this.readMethod;
		}

		@Override
		public void setReadMethod(@Nullable Method readMethod) {
			this.readMethod = readMethod;
		}

		@Override
		public @Nullable Method getWriteMethod() {
			return this.writeMethod;
		}

		@Override
		public void setWriteMethod(@Nullable Method writeMethod) {
			this.writeMethod = writeMethod;
		}

		@Override
		public @Nullable Class<?> getPropertyType() {
			if (this.propertyType == null) {
				try {
					this.propertyType = PropertyDescriptorUtils.findPropertyType(this.readMethod, this.writeMethod);
				}
				catch (IntrospectionException ex) {
					// 忽略，与 PropertyDescriptor#getPropertyType 行为一致
				}
			}
			return this.propertyType;
		}

		@Override
		public @Nullable Class<?> getPropertyEditorClass() {
			return this.propertyEditorClass;
		}

		@Override
		public void setPropertyEditorClass(@Nullable Class<?> propertyEditorClass) {
			this.propertyEditorClass = propertyEditorClass;
		}

		@Override
		public boolean equals(@Nullable Object other) {
			return (this == other || (other instanceof PropertyDescriptor that &&
					PropertyDescriptorUtils.equals(this, that)));
		}

		@Override
		public int hashCode() {
			return Objects.hash(getReadMethod(), getWriteMethod());
		}

		@Override
		public String toString() {
			return String.format("%s[name=%s, propertyType=%s, readMethod=%s, writeMethod=%s]",
					getClass().getSimpleName(), getName(), getPropertyType(), this.readMethod, this.writeMethod);
		}
	}


	/**
	 * 简单的 {@link IndexedPropertyDescriptor} 实现，绕过 JDK 弱引用管理。
	 */
	static class SimpleIndexedPropertyDescriptor extends IndexedPropertyDescriptor {

		private @Nullable Method readMethod;

		private @Nullable Method writeMethod;

		private @Nullable Class<?> propertyType;

		private @Nullable Method indexedReadMethod;

		private @Nullable Method indexedWriteMethod;

		private @Nullable Class<?> indexedPropertyType;

		private @Nullable Class<?> propertyEditorClass;

		public SimpleIndexedPropertyDescriptor(IndexedPropertyDescriptor original) throws IntrospectionException {
			this(original.getName(), original.getReadMethod(), original.getWriteMethod(),
					original.getIndexedReadMethod(), original.getIndexedWriteMethod());
			PropertyDescriptorUtils.copyNonMethodProperties(original, this);
		}

		public SimpleIndexedPropertyDescriptor(String propertyName, @Nullable Method readMethod,
				@Nullable Method writeMethod, @Nullable Method indexedReadMethod, Method indexedWriteMethod)
				throws IntrospectionException {

			super(propertyName, null, null, null, null);
			this.readMethod = readMethod;
			this.writeMethod = writeMethod;
			this.propertyType = PropertyDescriptorUtils.findPropertyType(readMethod, writeMethod);
			this.indexedReadMethod = indexedReadMethod;
			this.indexedWriteMethod = indexedWriteMethod;
			this.indexedPropertyType = PropertyDescriptorUtils.findIndexedPropertyType(
					propertyName, this.propertyType, indexedReadMethod, indexedWriteMethod);
		}

		@Override
		public @Nullable Method getReadMethod() {
			return this.readMethod;
		}

		@Override
		public void setReadMethod(@Nullable Method readMethod) {
			this.readMethod = readMethod;
		}

		@Override
		public @Nullable Method getWriteMethod() {
			return this.writeMethod;
		}

		@Override
		public void setWriteMethod(@Nullable Method writeMethod) {
			this.writeMethod = writeMethod;
		}

		@Override
		public @Nullable Class<?> getPropertyType() {
			if (this.propertyType == null) {
				try {
					this.propertyType = PropertyDescriptorUtils.findPropertyType(this.readMethod, this.writeMethod);
				}
				catch (IntrospectionException ex) {
					// 忽略，与 IndexedPropertyDescriptor#getPropertyType 行为一致
				}
			}
			return this.propertyType;
		}

		@Override
		public @Nullable Method getIndexedReadMethod() {
			return this.indexedReadMethod;
		}

		@Override
		public void setIndexedReadMethod(@Nullable Method indexedReadMethod) throws IntrospectionException {
			this.indexedReadMethod = indexedReadMethod;
		}

		@Override
		public @Nullable Method getIndexedWriteMethod() {
			return this.indexedWriteMethod;
		}

		@Override
		public void setIndexedWriteMethod(@Nullable Method indexedWriteMethod) throws IntrospectionException {
			this.indexedWriteMethod = indexedWriteMethod;
		}

		@Override
		public @Nullable Class<?> getIndexedPropertyType() {
			if (this.indexedPropertyType == null) {
				try {
					this.indexedPropertyType = PropertyDescriptorUtils.findIndexedPropertyType(
							getName(), getPropertyType(), this.indexedReadMethod, this.indexedWriteMethod);
				}
				catch (IntrospectionException ex) {
					// 忽略，与 IndexedPropertyDescriptor#getIndexedPropertyType 行为一致
				}
			}
			return this.indexedPropertyType;
		}

		@Override
		public @Nullable Class<?> getPropertyEditorClass() {
			return this.propertyEditorClass;
		}

		@Override
		public void setPropertyEditorClass(@Nullable Class<?> propertyEditorClass) {
			this.propertyEditorClass = propertyEditorClass;
		}

		/*
		 * 参见 java.beans.IndexedPropertyDescriptor#equals
		 */
		@Override
		public boolean equals(@Nullable Object other) {
			return (this == other || (other instanceof IndexedPropertyDescriptor that &&
					ObjectUtils.nullSafeEquals(getIndexedReadMethod(), that.getIndexedReadMethod()) &&
					ObjectUtils.nullSafeEquals(getIndexedWriteMethod(), that.getIndexedWriteMethod()) &&
					ObjectUtils.nullSafeEquals(getIndexedPropertyType(), that.getIndexedPropertyType()) &&
					PropertyDescriptorUtils.equals(this, that)));
		}

		@Override
		public int hashCode() {
			return Objects.hash(getReadMethod(), getWriteMethod(),
					getIndexedReadMethod(), getIndexedWriteMethod());
		}

		@Override
		public String toString() {
			return String.format("%s[name=%s, propertyType=%s, indexedPropertyType=%s, " +
							"readMethod=%s, writeMethod=%s, indexedReadMethod=%s, indexedWriteMethod=%s]",
					getClass().getSimpleName(), getName(), getPropertyType(), getIndexedPropertyType(),
					this.readMethod, this.writeMethod, this.indexedReadMethod, this.indexedWriteMethod);
		}
	}


	/**
	 * 按字母数字顺序排序 PropertyDescriptor，模拟
	 * {@link java.beans.BeanInfo#getPropertyDescriptors()} 的行为。
	 * @see ExtendedBeanInfo#propertyDescriptors
	 */
	static class PropertyDescriptorComparator implements Comparator<PropertyDescriptor> {

		@Override
		public int compare(PropertyDescriptor desc1, PropertyDescriptor desc2) {
			return desc1.getName().compareTo(desc2.getName());
		}
	}

}
