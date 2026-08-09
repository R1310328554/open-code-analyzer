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

package org.springframework.beans.factory.aot;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import org.springframework.aot.hint.ExecutableMode;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionValueResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.InstanceSupplier;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.support.SimpleInstantiationStrategy;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.function.ThrowingBiFunction;
import org.springframework.util.function.ThrowingFunction;
import org.springframework.util.function.ThrowingSupplier;

/**
 * 专用的 {@link InstanceSupplier}，提供用于实例化底层 Bean 实例的工厂
 * {@link Method}（若有）。必要时透明地处理 {@link AutowiredArguments} 的解析。
 * 通常在 AOT 处理后的应用中作为基于反射注入的针对性替代方案使用。
 *
 * <p>若未提供 {@code generator}，则使用反射实例化 Bean 实例，
 * 并贡献完整的 {@link ExecutableMode#INVOKE 调用}提示。支持多种生成器回调风格：
 * <ul>
 * <li>接受 {@code registeredBean} 和已解析 {@code arguments} 的函数，
 * 适用于需要解析参数的可执行对象。会添加
 * {@link ExecutableMode#INTROSPECT 内省}提示以便读取参数注解。</li>
 * <li>仅接受 {@code registeredBean} 的函数，适用于无需解析参数的较简单情形。</li>
 * <li>可使用方法引用的供应器。</li>
 * </ul>
 * 生成器回调处理受检异常，调用方无需自行处理。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @since 6.0
 * @param <T> 本供应器提供的实例类型
 * @see AutowiredArguments
 */
public final class BeanInstanceSupplier<T> extends AutowiredElementResolver implements InstanceSupplier<T> {

	/** 可执行对象（构造器或工厂方法）的查找策略。 */
	private final ExecutableLookup lookup;

	/** 无需参数解析的生成器函数。 */
	private final @Nullable ThrowingFunction<RegisteredBean, T> generatorWithoutArguments;

	/** 需要参数解析的生成器双函数。 */
	private final @Nullable ThrowingBiFunction<RegisteredBean, AutowiredArguments, T> generatorWithArguments;

	/** 直接按 Bean 名称注入的参数快捷方式。 */
	private final String @Nullable [] shortcutBeanNames;


	private BeanInstanceSupplier(ExecutableLookup lookup,
			@Nullable ThrowingFunction<RegisteredBean, T> generatorWithoutArguments,
			@Nullable ThrowingBiFunction<RegisteredBean, AutowiredArguments, T> generatorWithArguments,
			String @Nullable [] shortcutBeanNames) {

		this.lookup = lookup;
		this.generatorWithoutArguments = generatorWithoutArguments;
		this.generatorWithArguments = generatorWithArguments;
		this.shortcutBeanNames = shortcutBeanNames;
	}


	/**
	 * 创建为指定 Bean 构造器解析参数的 {@link BeanInstanceSupplier}。
	 * @param <T> 提供的实例类型
	 * @param parameterTypes 构造器参数类型
	 * @return 新的 {@link BeanInstanceSupplier} 实例
	 */
	public static <T> BeanInstanceSupplier<T> forConstructor(Class<?>... parameterTypes) {
		Assert.notNull(parameterTypes, "'parameterTypes' must not be null");
		Assert.noNullElements(parameterTypes, "'parameterTypes' must not contain null elements");
		return new BeanInstanceSupplier<>(new ConstructorLookup(parameterTypes), null, null, null);
	}

	/**
	 * 创建为指定工厂方法解析参数的 {@link BeanInstanceSupplier}。
	 * @param <T> 提供的实例类型
	 * @param declaringClass 声明工厂方法的类
	 * @param methodName 工厂方法名
	 * @param parameterTypes 工厂方法参数类型
	 * @return 新的 {@link BeanInstanceSupplier} 实例
	 */
	public static <T> BeanInstanceSupplier<T> forFactoryMethod(
			Class<?> declaringClass, String methodName, Class<?>... parameterTypes) {

		Assert.notNull(declaringClass, "'declaringClass' must not be null");
		Assert.hasText(methodName, "'methodName' must not be empty");
		Assert.notNull(parameterTypes, "'parameterTypes' must not be null");
		Assert.noNullElements(parameterTypes, "'parameterTypes' must not contain null elements");
		return new BeanInstanceSupplier<>(
				new FactoryMethodLookup(declaringClass, methodName, parameterTypes),
				null, null, null);
	}


	ExecutableLookup getLookup() {
		return this.lookup;
	}

	/**
	 * 返回使用指定 {@code generator} 双函数实例化底层 Bean 的新
	 * {@link BeanInstanceSupplier} 实例。
	 * @param generator 使用 {@link RegisteredBean} 和已解析的
	 * {@link AutowiredArguments} 实例化底层 Bean 的 {@link ThrowingBiFunction}
	 * @return 带有指定生成器的新 {@link BeanInstanceSupplier} 实例
	 */
	public BeanInstanceSupplier<T> withGenerator(ThrowingBiFunction<RegisteredBean, AutowiredArguments, T> generator) {
		Assert.notNull(generator, "'generator' must not be null");
		return new BeanInstanceSupplier<>(this.lookup, null, generator, this.shortcutBeanNames);
	}

	/**
	 * 返回使用指定 {@code generator} 函数实例化底层 Bean 的新
	 * {@link BeanInstanceSupplier} 实例。
	 * @param generator 使用 {@link RegisteredBean} 实例化底层 Bean 的
	 * {@link ThrowingFunction}
	 * @return 带有指定生成器的新 {@link BeanInstanceSupplier} 实例
	 */
	public BeanInstanceSupplier<T> withGenerator(ThrowingFunction<RegisteredBean, T> generator) {
		Assert.notNull(generator, "'generator' must not be null");
		return new BeanInstanceSupplier<>(this.lookup, generator, null, this.shortcutBeanNames);
	}

	/**
	 * 返回对特定参数使用直接 Bean 名称注入快捷方式的新
	 * {@link BeanInstanceSupplier} 实例。
	 * @param beanNames 用作快捷方式的 Bean 名称（与构造器或工厂方法参数对齐）
	 * @return 使用给定快捷 Bean 名称的新 {@link BeanInstanceSupplier} 实例
	 * @since 6.2
	 */
	public BeanInstanceSupplier<T> withShortcut(String... beanNames) {
		return new BeanInstanceSupplier<>(
				this.lookup, this.generatorWithoutArguments, this.generatorWithArguments, beanNames);
	}


	@SuppressWarnings("unchecked")
	@Override
	public T get(RegisteredBean registeredBean) {
		Assert.notNull(registeredBean, "'registeredBean' must not be null");
		if (this.generatorWithoutArguments != null) {
			Executable executable = getFactoryMethodForGenerator();
			return invokeBeanSupplier(executable, () -> this.generatorWithoutArguments.apply(registeredBean));
		}
		else if (this.generatorWithArguments != null) {
			Executable executable = getFactoryMethodForGenerator();
			AutowiredArguments arguments = resolveArguments(registeredBean,
					executable != null ? executable : this.lookup.get(registeredBean));
			return invokeBeanSupplier(executable, () -> this.generatorWithArguments.apply(registeredBean, arguments));
		}
		else {
			// 无自定义生成器时，通过反射解析参数并实例化
			Executable executable = this.lookup.get(registeredBean);
			@Nullable Object[] arguments = resolveArguments(registeredBean, executable).toArray();
			return invokeBeanSupplier(executable, () -> (T) instantiate(registeredBean, executable, arguments));
		}
	}

	@Override
	public @Nullable Method getFactoryMethod() {
		// 缓存工厂方法检索结果，供限定符内省等使用
		if (this.lookup instanceof FactoryMethodLookup factoryMethodLookup) {
			return factoryMethodLookup.get();
		}
		return null;
	}

	private @Nullable Method getFactoryMethodForGenerator() {
		// 避免在完整配置类之外不必要地暴露 currentlyInvokedFactoryMethod
		if (this.lookup instanceof FactoryMethodLookup factoryMethodLookup &&
				factoryMethodLookup.declaringClass.getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR)) {
			return factoryMethodLookup.get();
		}
		return null;
	}

	private T invokeBeanSupplier(@Nullable Executable executable, ThrowingSupplier<T> beanSupplier) {
		if (executable instanceof Method method) {
			return SimpleInstantiationStrategy.instantiateWithFactoryMethod(method, beanSupplier);
		}
		return beanSupplier.get();
	}

	/**
	 * 为指定的已注册 Bean 解析参数。
	 * @param registeredBean 已注册的 Bean
	 * @return 已解析的构造器或工厂方法参数
	 */
	AutowiredArguments resolveArguments(RegisteredBean registeredBean) {
		Assert.notNull(registeredBean, "'registeredBean' must not be null");
		return resolveArguments(registeredBean, this.lookup.get(registeredBean));
	}

	private AutowiredArguments resolveArguments(RegisteredBean registeredBean, Executable executable) {
		int parameterCount = executable.getParameterCount();
		@Nullable Object[] resolved = new Object[parameterCount];
		Assert.isTrue(this.shortcutBeanNames == null || this.shortcutBeanNames.length == resolved.length,
				() -> "'shortcuts' must contain " + resolved.length + " elements");

		ValueHolder[] argumentValues = resolveArgumentValues(registeredBean, executable);
		Set<String> autowiredBeanNames = new LinkedHashSet<>(resolved.length * 2);
		// 内部类构造器的第一个参数为外部类实例，跳过
		int startIndex = (executable instanceof Constructor<?> constructor &&
				ClassUtils.isInnerClass(constructor.getDeclaringClass())) ? 1 : 0;
		for (int i = startIndex; i < parameterCount; i++) {
			MethodParameter parameter = getMethodParameter(executable, i);
			DependencyDescriptor descriptor = new DependencyDescriptor(parameter, true);
			String shortcut = (this.shortcutBeanNames != null ? this.shortcutBeanNames[i] : null);
			if (shortcut != null) {
				descriptor = new ShortcutDependencyDescriptor(descriptor, shortcut);
			}
			ValueHolder argumentValue = argumentValues[i];
			resolved[i] = resolveAutowiredArgument(
					registeredBean, descriptor, argumentValue, autowiredBeanNames);
		}
		registerDependentBeans(registeredBean.getBeanFactory(), registeredBean.getBeanName(), autowiredBeanNames);

		return AutowiredArguments.of(resolved);
	}

	private MethodParameter getMethodParameter(Executable executable, int index) {
		if (executable instanceof Constructor<?> constructor) {
			return new MethodParameter(constructor, index);
		}
		if (executable instanceof Method method) {
			return new MethodParameter(method, index);
		}
		throw new IllegalStateException("Unsupported executable: " + executable.getClass().getName());
	}

	private ValueHolder[] resolveArgumentValues(RegisteredBean registeredBean, Executable executable) {
		Parameter[] parameters = executable.getParameters();
		ValueHolder[] resolved = new ValueHolder[parameters.length];
		RootBeanDefinition beanDefinition = registeredBean.getMergedBeanDefinition();
		if (beanDefinition.hasConstructorArgumentValues() &&
				registeredBean.getBeanFactory() instanceof AbstractAutowireCapableBeanFactory beanFactory) {
			BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(
					beanFactory, registeredBean.getBeanName(), beanDefinition, beanFactory.getTypeConverter());
			ConstructorArgumentValues values = resolveConstructorArguments(
					valueResolver, beanDefinition.getConstructorArgumentValues());
			Set<ValueHolder> usedValueHolders = CollectionUtils.newHashSet(parameters.length);
			for (int i = 0; i < parameters.length; i++) {
				Class<?> parameterType = parameters[i].getType();
				String parameterName = (parameters[i].isNamePresent() ? parameters[i].getName() : null);
				ValueHolder valueHolder = values.getArgumentValue(
						i, parameterType, parameterName, usedValueHolders);
				if (valueHolder != null) {
					resolved[i] = valueHolder;
					usedValueHolders.add(valueHolder);
				}
			}
		}
		return resolved;
	}

	private ConstructorArgumentValues resolveConstructorArguments(
			BeanDefinitionValueResolver valueResolver, ConstructorArgumentValues constructorArguments) {

		ConstructorArgumentValues resolvedConstructorArguments = new ConstructorArgumentValues();
		for (Map.Entry<Integer, ConstructorArgumentValues.ValueHolder> entry : constructorArguments.getIndexedArgumentValues().entrySet()) {
			resolvedConstructorArguments.addIndexedArgumentValue(entry.getKey(), resolveArgumentValue(valueResolver, entry.getValue()));
		}
		for (ConstructorArgumentValues.ValueHolder valueHolder : constructorArguments.getGenericArgumentValues()) {
			resolvedConstructorArguments.addGenericArgumentValue(resolveArgumentValue(valueResolver, valueHolder));
		}
		return resolvedConstructorArguments;
	}

	private ValueHolder resolveArgumentValue(BeanDefinitionValueResolver resolver, ValueHolder valueHolder) {
		if (valueHolder.isConverted()) {
			return valueHolder;
		}
		Object value = resolver.resolveValueIfNecessary("constructor argument", valueHolder.getValue());
		ValueHolder resolvedHolder = new ValueHolder(value, valueHolder.getType(), valueHolder.getName());
		resolvedHolder.setSource(valueHolder);
		return resolvedHolder;
	}

	private @Nullable Object resolveAutowiredArgument(RegisteredBean registeredBean, DependencyDescriptor descriptor,
			@Nullable ValueHolder argumentValue, Set<String> autowiredBeanNames) {

		TypeConverter typeConverter = registeredBean.getBeanFactory().getTypeConverter();
		if (argumentValue != null) {
			return (argumentValue.isConverted() ? argumentValue.getConvertedValue() :
					typeConverter.convertIfNecessary(argumentValue.getValue(),
							descriptor.getDependencyType(), descriptor.getMethodParameter()));
		}
		try {
			return registeredBean.resolveAutowiredArgument(descriptor, typeConverter, autowiredBeanNames);
		}
		catch (BeansException ex) {
			throw new UnsatisfiedDependencyException(null, registeredBean.getBeanName(), descriptor, ex);
		}
	}

	private Object instantiate(RegisteredBean registeredBean, Executable executable, @Nullable Object[] args) {
		if (executable instanceof Constructor<?> constructor) {
			if (registeredBean.getBeanFactory() instanceof DefaultListableBeanFactory dlbf &&
					registeredBean.getMergedBeanDefinition().hasMethodOverrides()) {
				return dlbf.getInstantiationStrategy().instantiate(registeredBean.getMergedBeanDefinition(),
						registeredBean.getBeanName(), registeredBean.getBeanFactory());
			}
			return BeanUtils.instantiateClass(constructor, args);
		}
		if (executable instanceof Method method) {
			Object target = null;
			String factoryBeanName = registeredBean.getMergedBeanDefinition().getFactoryBeanName();
			if (factoryBeanName != null) {
				target = registeredBean.getBeanFactory().getBean(factoryBeanName, method.getDeclaringClass());
			}
			else if (!Modifier.isStatic(method.getModifiers())) {
				throw new IllegalStateException("Cannot invoke instance method without factoryBeanName: " + method);
			}
			try {
				ReflectionUtils.makeAccessible(method);
				return method.invoke(target, args);
			}
			catch (Throwable ex) {
				throw new BeanInstantiationException(method, ex.getMessage(), ex);
			}
		}
		throw new IllegalStateException("Unsupported executable " + executable.getClass().getName());
	}


	private static String toCommaSeparatedNames(Class<?>... parameterTypes) {
		return Arrays.stream(parameterTypes).map(Class::getName).collect(Collectors.joining(", "));
	}


	/**
	 * 执行 {@link Executable} 的查找。
	 */
	abstract static class ExecutableLookup {

		abstract Executable get(RegisteredBean registeredBean);
	}


	/**
	 * 执行 {@link Constructor} 的查找。
	 */
	private static class ConstructorLookup extends ExecutableLookup {

		/** 构造器参数类型。 */
		private final Class<?>[] parameterTypes;

		ConstructorLookup(Class<?>[] parameterTypes) {
			this.parameterTypes = parameterTypes;
		}

		@Override
		public Executable get(RegisteredBean registeredBean) {
			Class<?> beanClass = registeredBean.getMergedBeanDefinition().getBeanClass();
			try {
				return beanClass.getDeclaredConstructor(this.parameterTypes);
			}
			catch (NoSuchMethodException ex) {
				throw new IllegalArgumentException(
						"%s cannot be found on %s".formatted(this, beanClass.getName()), ex);
			}
		}

		@Override
		public String toString() {
			return "Constructor with parameter types [%s]".formatted(toCommaSeparatedNames(this.parameterTypes));
		}
	}


	/**
	 * 执行工厂 {@link Method} 的查找。
	 */
	private static class FactoryMethodLookup extends ExecutableLookup {

		/** 声明工厂方法的类。 */
		private final Class<?> declaringClass;

		/** 工厂方法名。 */
		private final String methodName;

		/** 工厂方法参数类型。 */
		private final Class<?>[] parameterTypes;

		/** 缓存的已解析方法。 */
		private volatile @Nullable Method resolvedMethod;

		FactoryMethodLookup(Class<?> declaringClass, String methodName, Class<?>[] parameterTypes) {
			this.declaringClass = declaringClass;
			this.methodName = methodName;
			this.parameterTypes = parameterTypes;
		}

		@Override
		public Executable get(RegisteredBean registeredBean) {
			return get();
		}

		Method get() {
			Method method = this.resolvedMethod;
			if (method == null) {
				method = ReflectionUtils.findMethod(
						ClassUtils.getUserClass(this.declaringClass), this.methodName, this.parameterTypes);
				Assert.notNull(method, () -> "%s cannot be found".formatted(this));
				this.resolvedMethod = method;
			}
			return method;
		}

		@Override
		public String toString() {
			return "Factory method '%s' with parameter types [%s] declared on %s".formatted(
					this.methodName, toCommaSeparatedNames(this.parameterTypes),
					this.declaringClass);
		}
	}

}
