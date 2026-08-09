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

package org.springframework.beans.factory.config;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.core.MethodParameter;
import org.springframework.core.Nullness;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.ObjectUtils;

/**
 * 描述即将被注入的特定依赖。
 * 封装构造器参数、方法参数或字段，提供对其元数据的统一访问。
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
@SuppressWarnings("serial")
public class DependencyDescriptor extends InjectionPoint implements Serializable {

	/** 声明类（字段或方法/构造器所在类）。 */
	private final Class<?> declaringClass;

	/** 方法名称（字段依赖时为 {@code null}）。 */
	private @Nullable String methodName;

	/** 参数类型数组（字段依赖时为 {@code null}）。 */
	private Class<?> @Nullable [] parameterTypes;

	/** 参数索引（字段依赖时无意义）。 */
	private int parameterIndex;

	/** 字段名称（方法/构造器参数依赖时为 {@code null}）。 */
	private @Nullable String fieldName;

	/** 是否必需（注入点级别）。 */
	private final boolean required;

	/** 是否"急切"（用于类型匹配时是否急切解析候选 Bean）。 */
	private final boolean eager;

	/** 嵌套层级（用于泛型嵌套类型）。 */
	private int nestingLevel = 1;

	/** 包含此依赖的具体类（可能是子类）。 */
	private @Nullable Class<?> containingClass;

	/** 可解析类型（延迟构建，transient）。 */
	private transient volatile @Nullable ResolvableType resolvableType;

	/** 类型描述符（延迟构建，transient）。 */
	private transient volatile @Nullable TypeDescriptor typeDescriptor;


	/**
	 * 为方法或构造器参数创建新描述符。
	 * 将依赖视为"急切"。
	 * @param methodParameter 要封装的 MethodParameter
	 * @param required 依赖是否必需
	 */
	public DependencyDescriptor(MethodParameter methodParameter, boolean required) {
		this(methodParameter, required, true);
	}

	/**
	 * 为方法或构造器参数创建新描述符。
	 * @param methodParameter 要封装的 MethodParameter
	 * @param required 依赖是否必需
	 * @param eager 此依赖是否"急切"，即是否为类型匹配急切解析候选目标 Bean
	 */
	public DependencyDescriptor(MethodParameter methodParameter, boolean required, boolean eager) {
		super(methodParameter);

		this.declaringClass = methodParameter.getDeclaringClass();
		if (methodParameter.getMethod() != null) {
			this.methodName = methodParameter.getMethod().getName();
		}
		this.parameterTypes = methodParameter.getExecutable().getParameterTypes();
		this.parameterIndex = methodParameter.getParameterIndex();
		this.containingClass = methodParameter.getContainingClass();
		this.required = required;
		this.eager = eager;
	}

	/**
	 * 为字段创建新描述符。
	 * 将依赖视为"急切"。
	 * @param field 要封装的字段
	 * @param required 依赖是否必需
	 */
	public DependencyDescriptor(Field field, boolean required) {
		this(field, required, true);
	}

	/**
	 * 为字段创建新描述符。
	 * @param field 要封装的字段
	 * @param required 依赖是否必需
	 * @param eager 此依赖是否"急切"，即是否为类型匹配急切解析候选目标 Bean
	 */
	public DependencyDescriptor(Field field, boolean required, boolean eager) {
		super(field);

		this.declaringClass = field.getDeclaringClass();
		this.fieldName = field.getName();
		this.required = required;
		this.eager = eager;
	}

	/**
	 * 拷贝构造器。
	 * @param original 要复制的原始描述符
	 */
	public DependencyDescriptor(DependencyDescriptor original) {
		super(original);

		this.declaringClass = original.declaringClass;
		this.methodName = original.methodName;
		this.parameterTypes = original.parameterTypes;
		this.parameterIndex = original.parameterIndex;
		this.fieldName = original.fieldName;
		this.required = original.required;
		this.eager = original.eager;
		this.nestingLevel = original.nestingLevel;
		this.containingClass = original.containingClass;
	}


	/**
	 * 返回此依赖是否必需。
	 * <p>可选语义源自 Java 的 {@link java.util.Optional}、
	 * 参数级 {@code @Nullable} 注解的任何变体（如 JSpecify、JSR-305 或 FindBugs 注解集），
	 * 或 Kotlin 语言级别的可空类型声明。
	 */
	public boolean isRequired() {
		if (!this.required) {
			return false;
		}

		if (this.field != null) {
			return !(this.field.getType() == Optional.class || Nullness.forField(this.field) == Nullness.NULLABLE);
		}
		else {
			return !obtainMethodParameter().isOptional();
		}
	}

	/**
	 * 返回此依赖是否"急切"，即是否为类型匹配急切解析候选目标 Bean。
	 */
	public boolean isEager() {
		return this.eager;
	}

	/**
	 * 解析指定的非唯一场景：默认抛出 {@link NoUniqueBeanDefinitionException}。
	 * <p>子类可覆盖此方法以选择其中一个实例，或通过返回 {@code null} 完全放弃结果。
	 * @param type 请求的 Bean 类型
	 * @param matchingBeans 已为给定类型预选出的 Bean 名称与对应 Bean 实例的映射
	 *（限定符等已应用）
	 * @return 要继续使用的 Bean 实例，或 {@code null} 表示无
	 * @throws BeansException 非唯一场景为致命错误时
	 * @since 5.1
	 */
	public @Nullable Object resolveNotUnique(ResolvableType type, Map<String, Object> matchingBeans) throws BeansException {
		throw new NoUniqueBeanDefinitionException(type, matchingBeans.keySet());
	}

	/**
	 * 针对给定工厂解析此依赖的快捷方式，例如考虑某些预解析信息。
	 * <p>解析算法在遍历所有 Bean 进行常规类型匹配之前，会先尝试通过本方法解析快捷方式。
	 * 子类可覆盖此方法，在仍能获得 {@link InjectionPoint} 暴露等能力的同时，
	 * 基于预缓存信息提升解析性能。
	 * @param beanFactory 关联工厂
	 * @return 快捷方式结果（若有），否则为 {@code null}
	 * @throws BeansException 无法获取快捷方式时
	 * @since 4.3.1
	 */
	public @Nullable Object resolveShortcut(BeanFactory beanFactory) throws BeansException {
		return null;
	}

	/**
	 * 将此依赖匹配算法的候选结果 Bean 名称解析为给定工厂中的 Bean 实例。
	 * <p>默认实现调用 {@link BeanFactory#getBean(String, Class)}。
	 * 子类可提供额外参数或其他自定义。
	 * @param beanName Bean 名称，作为此依赖的候选匹配结果
	 * @param requiredType 期望的 Bean 类型（用于断言）
	 * @param beanFactory 关联工厂
	 * @return Bean 实例（永不为 {@code null}）
	 * @throws BeansException 无法获取 Bean 时
	 * @since 4.3.2
	 * @see BeanFactory#getBean(String)
	 */
	public Object resolveCandidate(String beanName, Class<?> requiredType, BeanFactory beanFactory)
			throws BeansException {

		try {
			// 需要为 SmartFactoryBean 提供所需类型
			return beanFactory.getBean(beanName, requiredType);
		}
		catch (BeanNotOfRequiredTypeException ex) {
			// 可能是 null Bean...
			return beanFactory.getBean(beanName);
		}
	}


	/**
	 * 增加此描述符的嵌套层级。
	 */
	public void increaseNestingLevel() {
		this.nestingLevel++;
		this.resolvableType = null;
		if (this.methodParameter != null) {
			this.methodParameter = this.methodParameter.nested();
		}
	}

	/**
	 * 可选地设置包含此依赖的具体类。
	 * 该类可能与声明参数/字段的类不同，可能是其子类，并可能替换类型变量。
	 * @since 4.0
	 */
	public void setContainingClass(Class<?> containingClass) {
		this.containingClass = containingClass;
		this.resolvableType = null;
		if (this.methodParameter != null) {
			this.methodParameter = this.methodParameter.withContainingClass(containingClass);
		}
	}

	/**
	 * 为封装的参数/字段构建 {@link ResolvableType} 对象。
	 * @since 4.0
	 */
	public ResolvableType getResolvableType() {
		ResolvableType resolvableType = this.resolvableType;
		if (resolvableType == null) {
			resolvableType = (this.field != null ?
					ResolvableType.forField(this.field, this.nestingLevel, this.containingClass) :
					ResolvableType.forMethodParameter(obtainMethodParameter()));
			this.resolvableType = resolvableType;
		}
		return resolvableType;
	}

	/**
	 * 为封装的参数/字段构建 {@link TypeDescriptor} 对象。
	 * @since 5.1.4
	 */
	public TypeDescriptor getTypeDescriptor() {
		TypeDescriptor typeDescriptor = this.typeDescriptor;
		if (typeDescriptor == null) {
			typeDescriptor = (this.field != null ?
					new TypeDescriptor(getResolvableType(), getDependencyType(), getAnnotations()) :
					new TypeDescriptor(obtainMethodParameter()));
			this.typeDescriptor = typeDescriptor;
		}
		return typeDescriptor;
	}

	/**
	 * 返回是否允许回退匹配。
	 * <p>默认返回 {@code false}，但可覆盖为 {@code true}，
	 * 以向 {@link org.springframework.beans.factory.support.AutowireCandidateResolver}
	 * 表明回退匹配也可接受。
	 * @since 4.0
	 */
	public boolean fallbackMatchAllowed() {
		return false;
	}

	/**
	 * 返回用于回退匹配的此描述符变体。
	 * @since 4.0
	 * @see #fallbackMatchAllowed()
	 */
	public DependencyDescriptor forFallbackMatch() {
		return new DependencyDescriptor(this) {
			@Override
			public boolean fallbackMatchAllowed() {
				return true;
			}
			@Override
			public boolean usesStandardBeanLookup() {
				return true;
			}
		};
	}

	/**
	 * 为底层方法参数（若有）初始化参数名发现。
	 * <p>本方法此时并不实际尝试获取参数名；仅允许在应用调用
	 * {@link #getDependencyName()}（若有）时发现参数名。
	 */
	public void initParameterNameDiscovery(@Nullable ParameterNameDiscoverer parameterNameDiscoverer) {
		if (this.methodParameter != null) {
			this.methodParameter.initParameterNameDiscovery(parameterNameDiscoverer);
		}
	}

	/**
	 * 确定封装的参数/字段的名称。
	 * @return 声明的名称（若无法解析则可能为 {@code null}）
	 */
	public @Nullable String getDependencyName() {
		return (this.field != null ? this.field.getName() : obtainMethodParameter().getParameterName());
	}

	/**
	 * 确定封装的参数/字段的声明（非泛型）类型。
	 * @return 声明的类型（永不为 {@code null}）
	 */
	public Class<?> getDependencyType() {
		if (this.field != null) {
			if (this.nestingLevel > 1) {
				Class<?> clazz = getResolvableType().getRawClass();
				return (clazz != null ? clazz : Object.class);
			}
			else {
				return this.field.getType();
			}
		}
		else {
			return obtainMethodParameter().getNestedParameterType();
		}
	}

	/**
	 * 确定此依赖是否支持延迟解析，例如通过额外代理。
	 * 默认为 {@code true}。
	 * @since 6.1.2
	 * @see org.springframework.beans.factory.support.AutowireCandidateResolver#getLazyResolutionProxyIfNecessary
	 */
	public boolean supportsLazyResolution() {
		return true;
	}

	/**
	 * 确定此描述符在 {@link #resolveCandidate(String, Class, BeanFactory)} 中
	 * 是否使用标准 Bean 查找，从而有资格进行工厂级快捷解析。
	 * <p>默认情况下，{@code DependencyDescriptor} 类本身使用标准 Bean 查找，
	 * 但子类可覆盖。若子类覆盖其他方法但保留标准 Bean 查找，
	 * 可覆盖本方法返回 {@code true}。
	 * @since 6.2
	 * @see #resolveCandidate(String, Class, BeanFactory)
	 */
	public boolean usesStandardBeanLookup() {
		return (getClass() == DependencyDescriptor.class);
	}


	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (!super.equals(other)) {
			return false;
		}
		return (other instanceof DependencyDescriptor otherDesc && this.required == otherDesc.required &&
				this.eager == otherDesc.eager && this.nestingLevel == otherDesc.nestingLevel &&
				this.containingClass == otherDesc.containingClass);
	}

	@Override
	public int hashCode() {
		return (31 * super.hashCode() + ObjectUtils.nullSafeHashCode(this.containingClass));
	}


	//---------------------------------------------------------------------
	// Serialization support
	//---------------------------------------------------------------------

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// 依赖默认序列化；反序列化后仅初始化状态
		ois.defaultReadObject();

		// 恢复反射句柄（遗憾的是不可序列化）
		try {
			if (this.fieldName != null) {
				this.field = this.declaringClass.getDeclaredField(this.fieldName);
			}
			else {
				if (this.methodName != null) {
					this.methodParameter = new MethodParameter(
							this.declaringClass.getDeclaredMethod(this.methodName, this.parameterTypes), this.parameterIndex);
				}
				else {
					this.methodParameter = new MethodParameter(
							this.declaringClass.getDeclaredConstructor(this.parameterTypes), this.parameterIndex);
				}
				for (int i = 1; i < this.nestingLevel; i++) {
					this.methodParameter = this.methodParameter.nested();
				}
			}
		}
		catch (Throwable ex) {
			throw new IllegalStateException("Could not find original class structure", ex);
		}
	}

}
