/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot.context.properties.bind;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.JavaBeanBinder.BeanProperties;
import org.springframework.boot.context.properties.bind.JavaBeanBinder.BeanProperty;
import org.springframework.core.KotlinDetector;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * 可用于为 {@link Bindable} 类型注册 {@link ReflectionHints} 的 {@link RuntimeHintsRegistrar}，
 * 并发现其通过属性可能暴露的嵌套类型。
 * <p>
 * 此类可作为基类使用，或通过 {@code forTypes} 与 {@code forBindables} 工厂方法实例化。
 *
 * @author Andy Wilkinson
 * @author Moritz Halbritter
 * @author Sebastien Deleuze
 * @author Phillip Webb
 * @since 3.0.0
 */
public class BindableRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

	private static final Log logger = LogFactory.getLog(BindableRuntimeHintsRegistrar.class);

	private final Bindable<?>[] bindables;

	/**
	 * 为指定类型创建新的 {@link BindableRuntimeHintsRegistrar}。
	 *
	 * @param types 待处理的类型
	 */
	protected BindableRuntimeHintsRegistrar(Class<?>... types) {
		this(Stream.of(types).map(Bindable::of).toArray(Bindable[]::new));
	}

	/**
	 * 为指定 bindable 创建新的 {@link BindableRuntimeHintsRegistrar}。
	 *
	 * @param bindables 待处理的 bindable
	 * @since 3.0.8
	 */
	protected BindableRuntimeHintsRegistrar(Bindable<?>... bindables) {
		this.bindables = bindables;
	}

	@Override
	public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
		registerHints(hints);
	}

	/**
	 * 向给定的 {@link RuntimeHints} 实例贡献提示信息。
	 *
	 * @param hints 部署单元目前已贡献的提示
	 */
	public void registerHints(RuntimeHints hints) {
		for (Bindable<?> bindable : this.bindables) {
			try {
				new Processor(bindable).process(hints.reflection());
			}
			catch (Exception ex) {
				logger.debug("Skipping hints for " + bindable, ex);
			}
		}
	}

	/**
	 * 为指定类型创建新的 {@link BindableRuntimeHintsRegistrar}。
	 *
	 * @param types 待处理的类型
	 * @return 新的 {@link BindableRuntimeHintsRegistrar} 实例
	 */
	public static BindableRuntimeHintsRegistrar forTypes(Iterable<Class<?>> types) {
		Assert.notNull(types, "'types' must not be null");
		return forTypes(StreamSupport.stream(types.spliterator(), false).toArray(Class<?>[]::new));
	}

	/**
	 * 为指定类型创建新的 {@link BindableRuntimeHintsRegistrar}。
	 *
	 * @param types 待处理的类型
	 * @return 新的 {@link BindableRuntimeHintsRegistrar} 实例
	 */
	public static BindableRuntimeHintsRegistrar forTypes(Class<?>... types) {
		return new BindableRuntimeHintsRegistrar(types);
	}

	/**
	 * 为指定 bindable 创建新的 {@link BindableRuntimeHintsRegistrar}。
	 *
	 * @param bindables 待处理的 bindable
	 * @return 新的 {@link BindableRuntimeHintsRegistrar} 实例
	 * @since 3.0.8
	 */
	public static BindableRuntimeHintsRegistrar forBindables(Iterable<Bindable<?>> bindables) {
		Assert.notNull(bindables, "'bindables' must not be null");
		return forBindables(StreamSupport.stream(bindables.spliterator(), false).toArray(Bindable[]::new));
	}

	/**
	 * 为指定 bindable 创建新的 {@link BindableRuntimeHintsRegistrar}。
	 *
	 * @param bindables 待处理的 bindable
	 * @return 新的 {@link BindableRuntimeHintsRegistrar} 实例
	 * @since 3.0.8
	 */
	public static BindableRuntimeHintsRegistrar forBindables(Bindable<?>... bindables) {
		return new BindableRuntimeHintsRegistrar(bindables);
	}

	/**
	 * 用于注册提示信息的处理器。
	 */
	private static final class Processor {

		private final Class<?> type;

		private final @Nullable Constructor<?> bindConstructor;

		private final BeanProperties bean;

		private final Set<Class<?>> seen;

		Processor(Bindable<?> bindable) {
			this(bindable, false, new HashSet<>());
		}

		private Processor(Bindable<?> bindable, boolean nestedType, Set<Class<?>> seen) {
			this.type = getRawClass(bindable);
			this.bindConstructor = (bindable.getBindMethod() != BindMethod.JAVA_BEAN)
					? BindConstructorProvider.DEFAULT.getBindConstructor(getBindableType(bindable), nestedType) : null;
			this.bean = JavaBeanBinder.BeanProperties.of(bindable);
			this.seen = seen;
		}

		private static Class<?> getBindableType(Bindable<?> bindable) {
			Class<?> resolved = bindable.getType().resolve();
			Assert.state(resolved != null, "'resolved' must not be null");
			return resolved;
		}

		private static Class<?> getRawClass(Bindable<?> bindable) {
			Class<?> rawClass = bindable.getType().getRawClass();
			Assert.state(rawClass != null, "'rawClass' must not be null");
			return rawClass;
		}

		void process(ReflectionHints hints) {
			if (this.seen.contains(this.type)) {
				return;
			}
			this.seen.add(this.type);
			handleConstructor(hints);
			if (this.bindConstructor != null) {
				handleValueObjectProperties(hints);
			}
			else if (this.bean != null && !this.bean.getProperties().isEmpty()) {
				handleJavaBeanProperties(hints);
			}
		}

		private void handleConstructor(ReflectionHints hints) {
			if (this.bindConstructor != null) {
				if (KotlinDetector.isKotlinType(this.bindConstructor.getDeclaringClass())) {
					KotlinDelegate.handleConstructor(hints, this.bindConstructor);
				}
				else {
					hints.registerConstructor(this.bindConstructor, ExecutableMode.INVOKE);
				}
				return;
			}
			Arrays.stream(this.type.getDeclaredConstructors())
				.filter(this::hasNoParameters)
				.findFirst()
				.ifPresent((constructor) -> hints.registerConstructor(constructor, ExecutableMode.INVOKE));
		}

		private boolean hasNoParameters(Constructor<?> candidate) {
			return candidate.getParameterCount() == 0;
		}

		private void handleValueObjectProperties(ReflectionHints hints) {
			Assert.state(this.bindConstructor != null, "'bindConstructor' must not be null");
			for (int i = 0; i < this.bindConstructor.getParameterCount(); i++) {
				String propertyName = this.bindConstructor.getParameters()[i].getName();
				ResolvableType propertyType = ResolvableType.forConstructorParameter(this.bindConstructor, i);
				handleProperty(hints, propertyName, propertyType);
			}
		}

		private void handleJavaBeanProperties(ReflectionHints hints) {
			Map<String, BeanProperty> properties = this.bean.getProperties();
			properties.forEach((name, property) -> {
				Method getter = property.getGetter();
				if (getter != null) {
					hints.registerMethod(getter, ExecutableMode.INVOKE);
				}
				Method setter = property.getSetter();
				if (setter != null) {
					hints.registerMethod(setter, ExecutableMode.INVOKE);
				}
				Field field = property.getField();
				if (field != null) {
					hints.registerField(field);
				}
				handleProperty(hints, name, property.getType());
			});
		}

		private void handleProperty(ReflectionHints hints, String propertyName, ResolvableType propertyType) {
			Class<?> propertyClass = propertyType.resolve();
			if (propertyClass == null) {
				return;
			}
			if (propertyClass.equals(this.type)) {
				return; // Prevent infinite recursion
			}
			Class<?> componentType = getComponentClass(propertyType);
			if (componentType != null) {
				// Can be a list of simple types
				if (!isJavaType(componentType)) {
					processNested(componentType, hints);
				}
			}
			else if (isNestedType(propertyName, propertyClass)) {
				processNested(propertyClass, hints);
			}
		}

		private void processNested(Class<?> type, ReflectionHints hints) {
			new Processor(Bindable.of(type), true, this.seen).process(hints);
		}

		private @Nullable Class<?> getComponentClass(ResolvableType type) {
			ResolvableType componentType = getComponentType(type);
			if (componentType == null) {
				return null;
			}
			if (isContainer(componentType)) {
				// Resolve nested generics like Map<String, List<SomeType>>
				return getComponentClass(componentType);
			}
			return componentType.toClass();
		}

		private @Nullable ResolvableType getComponentType(ResolvableType type) {
			if (type.isArray()) {
				return type.getComponentType();
			}
			if (isCollection(type)) {
				return type.asCollection().getGeneric();
			}
			if (isMap(type)) {
				return type.asMap().getGeneric(1);
			}
			return null;
		}

		private boolean isContainer(ResolvableType type) {
			return type.isArray() || isCollection(type) || isMap(type);
		}

		private boolean isCollection(ResolvableType type) {
			return Collection.class.isAssignableFrom(type.toClass());
		}

		private boolean isMap(ResolvableType type) {
			return Map.class.isAssignableFrom(type.toClass());
		}

		/**
		 * 指定给定属性是否指向嵌套类型。嵌套类型表示需要完全解析的子命名空间。
		 * 嵌套类型可以是内部类，或标注了 {@link NestedConfigurationProperty} 的类型。
		 *
		 * @param propertyName 属性名
		 * @param propertyType 属性类型
		 * @return 指定 {@code propertyType} 是否为嵌套类型
		 */
		private boolean isNestedType(String propertyName, Class<?> propertyType) {
			Class<?> declaringClass = propertyType.getDeclaringClass();
			if (declaringClass != null && isNested(declaringClass, this.type)) {
				return true;
			}
			Field field = ReflectionUtils.findField(this.type, propertyName);
			return (field != null) && MergedAnnotations.from(field).isPresent(Nested.class);
		}

		private static boolean isNested(Class<?> type, Class<?> candidate) {
			if (type.isAssignableFrom(candidate)) {
				return true;
			}
			return (candidate.getDeclaringClass() != null && isNested(type, candidate.getDeclaringClass()));
		}

		private boolean isJavaType(Class<?> candidate) {
			return candidate.getPackageName().startsWith("java.");
		}

	}

	/**
	 * 内部类，避免在运行时对 Kotlin 产生硬依赖。
	 */
	private static final class KotlinDelegate {

		static void handleConstructor(ReflectionHints hints, Constructor<?> constructor) {
			KClass<?> kClass = JvmClassMappingKt.getKotlinClass(constructor.getDeclaringClass());
			if (kClass.isData()) {
				hints.registerType(constructor.getDeclaringClass(), MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
			}
			else {
				hints.registerConstructor(constructor, ExecutableMode.INVOKE);
			}
		}

	}

}
