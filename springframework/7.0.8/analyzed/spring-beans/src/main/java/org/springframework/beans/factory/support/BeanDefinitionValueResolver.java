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

package org.springframework.beans.factory.support;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiFunction;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.NamedBeanHolder;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Bean 工厂实现中使用的辅助类，将 Bean 定义对象中的值解析为
 * 实际应用到目标 Bean 实例上的值。
 *
 * <p>在 {@link AbstractBeanFactory} 和普通
 * {@link org.springframework.beans.factory.config.BeanDefinition} 对象上操作。
 * 由 {@link AbstractAutowireCapableBeanFactory} 使用。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Stephane Nicoll
 * @since 1.2
 * @see AbstractAutowireCapableBeanFactory
 */
public class BeanDefinitionValueResolver {

	/** 用于解析 Bean 引用的自动装配 Bean 工厂。 */
	private final AbstractAutowireCapableBeanFactory beanFactory;

	/** 当前正在装配的 Bean 名称。 */
	private final String beanName;

	/** 当前正在装配的 Bean 定义。 */
	private final BeanDefinition beanDefinition;

	/** 用于类型转换（如 {@link TypedStringValue}）的类型转换器。 */
	private final TypeConverter typeConverter;


	/**
	 * 为给定的 BeanFactory 和 BeanDefinition 创建 BeanDefinitionValueResolver，
	 * 使用指定的 {@link TypeConverter}。
	 * @param beanFactory the BeanFactory to resolve against
	 * @param beanName the name of the bean that we work on
	 * @param beanDefinition the BeanDefinition of the bean that we work on
	 * @param typeConverter the TypeConverter to use for resolving TypedStringValues
	 */
	public BeanDefinitionValueResolver(AbstractAutowireCapableBeanFactory beanFactory, String beanName,
			BeanDefinition beanDefinition, TypeConverter typeConverter) {

		this.beanFactory = beanFactory;
		this.beanName = beanName;
		this.beanDefinition = beanDefinition;
		this.typeConverter = typeConverter;
	}

	/**
	 * 为给定的 BeanFactory 和 BeanDefinition 创建 BeanDefinitionValueResolver，
	 * 使用默认的 {@link TypeConverter}。
	 * @param beanFactory the BeanFactory to resolve against
	 * @param beanName the name of the bean that we work on
	 * @param beanDefinition the BeanDefinition of the bean that we work on
	 */
	public BeanDefinitionValueResolver(AbstractAutowireCapableBeanFactory beanFactory, String beanName,
			BeanDefinition beanDefinition) {

		this.beanFactory = beanFactory;
		this.beanName = beanName;
		this.beanDefinition = beanDefinition;
		BeanWrapper beanWrapper = new BeanWrapperImpl();
		beanFactory.initBeanWrapper(beanWrapper);
		this.typeConverter = beanWrapper;
	}


	/**
	 * 给定一个 PropertyValue，返回解析后的值，必要时解析对工厂中其他 Bean 的引用。
	 * 值可能是：
	 * <li>A BeanDefinition, which leads to the creation of a corresponding
	 * new bean instance. Singleton flags and names of such "inner beans"
	 * are always ignored: Inner beans are anonymous prototypes.
	 * <li>A RuntimeBeanReference, which must be resolved.
	 * <li>A ManagedList. This is a special collection that may contain
	 * RuntimeBeanReferences or Collections that will need to be resolved.
	 * <li>A ManagedSet. May also contain RuntimeBeanReferences or
	 * Collections that will need to be resolved.
	 * <li>A ManagedMap. In this case the value may be a RuntimeBeanReference
	 * or Collection that will need to be resolved.
	 * <li>An ordinary object or {@code null}, in which case it's left alone.
	 * @param argName the name of the argument that the value is defined for
	 * @param value the value object to resolve
	 * @return the resolved object
	 */
	public @Nullable Object resolveValueIfNecessary(Object argName, @Nullable Object value) {
		// 逐一检查每个值，判断是否需要解析对另一个 Bean 的运行时引用
		if (value instanceof RuntimeBeanReference ref) {
			return resolveReference(argName, ref);
		}
		else if (value instanceof RuntimeBeanNameReference ref) {
			String refName = ref.getBeanName();
			refName = String.valueOf(doEvaluate(refName));
			if (!this.beanFactory.containsBean(refName)) {
				throw new BeanDefinitionStoreException(
						"Invalid bean name '" + refName + "' in bean reference for " + argName);
			}
			return refName;
		}
		else if (value instanceof BeanDefinitionHolder bdHolder) {
			// 解析 BeanDefinitionHolder：包含带名称和别名的 BeanDefinition
			return resolveInnerBean(bdHolder.getBeanName(), bdHolder.getBeanDefinition(),
					(name, mbd) -> resolveInnerBeanValue(argName, name, mbd));
		}
		else if (value instanceof BeanDefinition bd) {
			return resolveInnerBean(null, bd,
					(name, mbd) -> resolveInnerBeanValue(argName, name, mbd));
		}
		else if (value instanceof DependencyDescriptor dependencyDescriptor) {
			Set<String> autowiredBeanNames = new LinkedHashSet<>(2);
			Object result = this.beanFactory.resolveDependency(
					dependencyDescriptor, this.beanName, autowiredBeanNames, this.typeConverter);
			for (String autowiredBeanName : autowiredBeanNames) {
				if (this.beanFactory.containsBean(autowiredBeanName)) {
					this.beanFactory.registerDependentBean(autowiredBeanName, this.beanName);
				}
			}
			return result;
		}
		else if (value instanceof ManagedArray managedArray) {
			// 可能需要解析其中的运行时引用
			Class<?> elementType = managedArray.resolvedElementType;
			if (elementType == null) {
				String elementTypeName = managedArray.getElementTypeName();
				if (StringUtils.hasText(elementTypeName)) {
					try {
						elementType = ClassUtils.forName(elementTypeName, this.beanFactory.getBeanClassLoader());
						managedArray.resolvedElementType = elementType;
					}
					catch (Throwable ex) {
						// 在异常信息中展示上下文，便于定位问题
						throw new BeanCreationException(
								this.beanDefinition.getResourceDescription(), this.beanName,
								"Error resolving array type for " + argName, ex);
					}
				}
				else {
					elementType = Object.class;
				}
			}
			return resolveManagedArray(argName, (List<?>) value, elementType);
		}
		else if (value instanceof ManagedList<?> managedList) {
			// 可能需要解析其中的运行时引用
			return resolveManagedList(argName, managedList);
		}
		else if (value instanceof ManagedSet<?> managedSet) {
			// 可能需要解析其中的运行时引用
			return resolveManagedSet(argName, managedSet);
		}
		else if (value instanceof ManagedMap<?, ?> managedMap) {
			// 可能需要解析其中的运行时引用
			return resolveManagedMap(argName, managedMap);
		}
		else if (value instanceof ManagedProperties original) {
			// 将 ManagedProperties 转换为标准 Properties，并解析键值中的表达式
			Properties copy = new Properties();
			original.forEach((propKey, propValue) -> {
				if (propKey instanceof TypedStringValue typedStringValue) {
					propKey = evaluate(typedStringValue);
				}
				if (propValue instanceof TypedStringValue typedStringValue) {
					propValue = evaluate(typedStringValue);
				}
				if (propKey == null || propValue == null) {
					throw new BeanCreationException(
							this.beanDefinition.getResourceDescription(), this.beanName,
							"Error converting Properties key/value pair for " + argName + ": resolved to null");
				}
				copy.put(propKey, propValue);
			});
			return copy;
		}
		else if (value instanceof TypedStringValue typedStringValue) {
			// 将字符串值转换为目标类型
			Object valueObject = evaluate(typedStringValue);
			try {
				Class<?> resolvedTargetType = resolveTargetType(typedStringValue);
				if (resolvedTargetType != null) {
					return this.typeConverter.convertIfNecessary(valueObject, resolvedTargetType);
				}
				else {
					return valueObject;
				}
			}
			catch (Throwable ex) {
				// 在异常信息中展示上下文，便于定位问题
				throw new BeanCreationException(
						this.beanDefinition.getResourceDescription(), this.beanName,
						"Error converting typed String value for " + argName, ex);
			}
		}
		else if (value instanceof NullBean) {
			return null;
		}
		else {
			return evaluate(value);
		}
	}

	/**
	 * 解析内部 Bean 定义，并对其合并后的 Bean 定义调用指定的 {@code resolver}。
	 * @param innerBeanName the inner bean name (or {@code null} to assign one)
	 * @param innerBd the inner raw bean definition
	 * @param resolver the function to invoke to resolve
	 * @param <T> the type of the resolution
	 * @return a resolved inner bean, as a result of applying the {@code resolver}
	 * @since 6.0
	 */
	public <T> T resolveInnerBean(@Nullable String innerBeanName, BeanDefinition innerBd,
			BiFunction<String, RootBeanDefinition, T> resolver) {

		String nameToUse = (innerBeanName != null ? innerBeanName : "(inner bean)" +
				BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR + ObjectUtils.getIdentityHexString(innerBd));
		return resolver.apply(nameToUse,
				this.beanFactory.getMergedBeanDefinition(nameToUse, innerBd, this.beanDefinition));
	}

	/**
	 * 如有必要，将给定值作为表达式求值。
	 * @param value the candidate value (may be an expression)
	 * @return the resolved value
	 */
	protected @Nullable Object evaluate(TypedStringValue value) {
		Object result = doEvaluate(value.getValue());
		if (!ObjectUtils.nullSafeEquals(result, value.getValue())) {
			value.setDynamic();
		}
		return result;
	}

	/**
	 * 如有必要，将给定值作为表达式求值。
	 * @param value the original value (may be an expression)
	 * @return the resolved value if necessary, or the original value
	 */
	protected @Nullable Object evaluate(@Nullable Object value) {
		if (value instanceof String str) {
			return doEvaluate(str);
		}
		else if (value instanceof String[] values) {
			boolean actuallyResolved = false;
			@Nullable Object[] resolvedValues = new Object[values.length];
			for (int i = 0; i < values.length; i++) {
				String originalValue = values[i];
				Object resolvedValue = doEvaluate(originalValue);
				if (resolvedValue != originalValue) {
					actuallyResolved = true;
				}
				resolvedValues[i] = resolvedValue;
			}
			return (actuallyResolved ? resolvedValues : values);
		}
		else {
			return value;
		}
	}

	/**
	 * 如有必要，将给定字符串值作为表达式求值。
	 * @param value the original value (may be an expression)
	 * @return the resolved value if necessary, or the original String value
	 */
	private @Nullable Object doEvaluate(@Nullable String value) {
		return this.beanFactory.evaluateBeanDefinitionString(value, this.beanDefinition);
	}

	/**
	 * 解析给定 TypedStringValue 中的目标类型。
	 * @param value the TypedStringValue to resolve
	 * @return the resolved target type (or {@code null} if none specified)
	 * @throws ClassNotFoundException if the specified type cannot be resolved
	 * @see TypedStringValue#resolveTargetType
	 */
	protected @Nullable Class<?> resolveTargetType(TypedStringValue value) throws ClassNotFoundException {
		if (value.hasTargetType()) {
			return value.getTargetType();
		}
		return value.resolveTargetType(this.beanFactory.getBeanClassLoader());
	}

	/**
	 * 解析对工厂中另一个 Bean 的引用。
	 */
	private @Nullable Object resolveReference(Object argName, RuntimeBeanReference ref) {
		try {
			Object bean;
			Class<?> beanType = ref.getBeanType();
			String resolvedName = String.valueOf(doEvaluate(ref.getBeanName()));
			if (ref.isToParent()) {
				BeanFactory parent = this.beanFactory.getParentBeanFactory();
				if (parent == null) {
					throw new BeanCreationException(
							this.beanDefinition.getResourceDescription(), this.beanName,
							"Cannot resolve reference to bean " + ref +
									" in parent factory: no parent factory available");
				}
				if (beanType != null) {
					bean = (parent.containsBean(resolvedName) ?
							parent.getBean(resolvedName, beanType) : parent.getBean(beanType));
				}
				else {
					bean = parent.getBean(resolvedName);
				}
			}
			else {
				if (beanType != null) {
					if (this.beanFactory.containsBean(resolvedName)) {
						bean = this.beanFactory.getBean(resolvedName, beanType);
					}
					else {
						NamedBeanHolder<?> namedBean = this.beanFactory.resolveNamedBean(beanType);
						bean = namedBean.getBeanInstance();
						resolvedName = namedBean.getBeanName();
					}
				}
				else {
					bean = this.beanFactory.getBean(resolvedName);
				}
				this.beanFactory.registerDependentBean(resolvedName, this.beanName);
			}
			if (bean instanceof NullBean) {
				bean = null;
			}
			return bean;
		}
		catch (BeansException ex) {
			throw new BeanCreationException(
					this.beanDefinition.getResourceDescription(), this.beanName,
					"Cannot resolve reference to bean '" + ref.getBeanName() + "' while setting " + argName, ex);
		}
	}

	/**
	 * 解析内部 Bean 定义并创建实例。
	 * @param argName the name of the argument that the inner bean is defined for
	 * @param innerBeanName the name of the inner bean
	 * @param mbd the merged bean definition for the inner bean
	 * @return the resolved inner bean instance
	 */
	private @Nullable Object resolveInnerBeanValue(Object argName, String innerBeanName, RootBeanDefinition mbd) {
		try {
			// 检查给定 Bean 名称是否唯一；若不唯一则递增计数器直至唯一
			String actualInnerBeanName = innerBeanName;
			if (mbd.isSingleton()) {
				actualInnerBeanName = adaptInnerBeanName(innerBeanName);
			}
			this.beanFactory.registerContainedBean(actualInnerBeanName, this.beanName);
			// 确保内部 Bean 所依赖的 Bean 已初始化
			String[] dependsOn = mbd.getDependsOn();
			if (dependsOn != null) {
				for (String dependsOnBean : dependsOn) {
					this.beanFactory.registerDependentBean(dependsOnBean, actualInnerBeanName);
					this.beanFactory.getBean(dependsOnBean);
				}
			}
			// 实际创建内部 Bean 实例
			Object innerBean = this.beanFactory.createBean(actualInnerBeanName, mbd, null);
			if (innerBean instanceof FactoryBean<?> factoryBean) {
				boolean synthetic = mbd.isSynthetic();
				innerBean = this.beanFactory.getObjectFromFactoryBean(
						factoryBean, null, actualInnerBeanName, !synthetic);
			}
			if (innerBean instanceof NullBean) {
				innerBean = null;
			}
			return innerBean;
		}
		catch (BeansException ex) {
			throw new BeanCreationException(
					this.beanDefinition.getResourceDescription(), this.beanName,
					"Cannot create inner bean '" + innerBeanName + "' " +
					(mbd.getBeanClassName() != null ? "of type [" + mbd.getBeanClassName() + "] " : "") +
					"while setting " + argName, ex);
		}
	}

	/**
	 * 检查给定 Bean 名称是否唯一。若不唯一，则添加递增计数器直至名称唯一。
	 * @param innerBeanName the original name for the inner bean
	 * @return the adapted name for the inner bean
	 */
	private String adaptInnerBeanName(String innerBeanName) {
		String actualInnerBeanName = innerBeanName;
		int counter = 0;
		String prefix = innerBeanName + BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR;
		while (this.beanFactory.isBeanNameInUse(actualInnerBeanName)) {
			counter++;
			actualInnerBeanName = prefix + counter;
		}
		return actualInnerBeanName;
	}

	/**
	 * 对托管数组中的每个元素，必要时解析引用。
	 */
	private Object resolveManagedArray(Object argName, List<?> ml, Class<?> elementType) {
		Object resolved = Array.newInstance(elementType, ml.size());
		for (int i = 0; i < ml.size(); i++) {
			Array.set(resolved, i, resolveValueIfNecessary(new KeyedArgName(argName, i), ml.get(i)));
		}
		return resolved;
	}

	/**
	 * 对托管列表中的每个元素，必要时解析引用。
	 */
	private List<?> resolveManagedList(Object argName, List<?> ml) {
		List<Object> resolved = new ArrayList<>(ml.size());
		for (int i = 0; i < ml.size(); i++) {
			resolved.add(resolveValueIfNecessary(new KeyedArgName(argName, i), ml.get(i)));
		}
		return resolved;
	}

	/**
	 * 对托管集合中的每个元素，必要时解析引用。
	 */
	private Set<?> resolveManagedSet(Object argName, Set<?> ms) {
		Set<Object> resolved = CollectionUtils.newLinkedHashSet(ms.size());
		int i = 0;
		for (Object m : ms) {
			resolved.add(resolveValueIfNecessary(new KeyedArgName(argName, i), m));
			i++;
		}
		return resolved;
	}

	/**
	 * 对托管 Map 中的每个键值，必要时解析引用。
	 */
	private Map<?, ?> resolveManagedMap(Object argName, Map<?, ?> mm) {
		Map<Object, Object> resolved = CollectionUtils.newLinkedHashMap(mm.size());
		mm.forEach((key, value) -> {
			Object resolvedKey = resolveValueIfNecessary(argName, key);
			Object resolvedValue = resolveValueIfNecessary(new KeyedArgName(argName, key), value);
			resolved.put(resolvedKey, resolvedValue);
		});
		return resolved;
	}


	/**
	 * 用于延迟构建 toString 的持有者类。
	 */
	private static class KeyedArgName {

		/** 参数名称。 */
		private final Object argName;

		/** 集合或 Map 中的键/索引。 */
		private final Object key;

		public KeyedArgName(Object argName, Object key) {
			this.argName = argName;
			this.key = key;
		}

		@Override
		public String toString() {
			return this.argName + " with key " + BeanWrapper.PROPERTY_KEY_PREFIX +
					this.key + BeanWrapper.PROPERTY_KEY_SUFFIX;
		}
	}

}
