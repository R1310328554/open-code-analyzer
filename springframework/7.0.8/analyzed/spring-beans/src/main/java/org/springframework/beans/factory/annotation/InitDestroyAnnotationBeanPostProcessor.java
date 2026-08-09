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

package org.springframework.beans.factory.annotation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.aot.BeanRegistrationAotContribution;
import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

/**
 * {@link org.springframework.beans.factory.config.BeanPostProcessor} 实现：
 * 调用带注解的 init / destroy 方法。可作为 Spring
 * {@link org.springframework.beans.factory.InitializingBean} 与
 * {@link org.springframework.beans.factory.DisposableBean} 回调接口的注解式替代。
 *
 * <p>本后置处理器实际检查的注解类型可通过
 * {@link #setInitAnnotationType "initAnnotationType"} 与
 * {@link #setDestroyAnnotationType "destroyAnnotationType"} 属性配置。
 * 可使用任意自定义注解，因为不要求注解具备特定属性。
 *
 * <p>init / destroy 注解可用于任意可见性的方法：public、包级、protected 或 private。
 * 可以标注多个方法，但建议分别只标注一个 init 方法与一个 destroy 方法。
 *
 * <p>Spring 的 {@link org.springframework.context.annotation.CommonAnnotationBeanPostProcessor}
 * 开箱即用地支持 {@link jakarta.annotation.PostConstruct} 与
 * {@link jakarta.annotation.PreDestroy}，分别作为 init 与 destroy 注解。
 * 此外还支持 {@link jakarta.annotation.Resource}，用于按名称进行注解驱动注入。
 *
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 2.5
 * @see #setInitAnnotationType
 * @see #setDestroyAnnotationType
 */
@SuppressWarnings("serial")
public class InitDestroyAnnotationBeanPostProcessor implements DestructionAwareBeanPostProcessor,
		MergedBeanDefinitionPostProcessor, BeanRegistrationAotProcessor, PriorityOrdered, Serializable {

	/** 空生命周期元数据：各回调均为空操作 */
	private final transient LifecycleMetadata emptyLifecycleMetadata =
			new LifecycleMetadata(Object.class, Collections.emptyList(), Collections.emptyList()) {
				@Override
				public void checkInitDestroyMethods(RootBeanDefinition beanDefinition) {
				}
				@Override
				public void invokeInitMethods(Object target, String beanName) {
				}
				@Override
				public void invokeDestroyMethods(Object target, String beanName) {
				}
				@Override
				public boolean hasDestroyMethods() {
					return false;
				}
			};


	/** 日志记录器 */
	protected transient Log logger = LogFactory.getLog(getClass());

	/** 视为「初始化」标记的注解类型集合 */
	private final Set<Class<? extends Annotation>> initAnnotationTypes = new LinkedHashSet<>(2);

	/** 视为「销毁」标记的注解类型集合 */
	private final Set<Class<? extends Annotation>> destroyAnnotationTypes = new LinkedHashSet<>(2);

	/** 后置处理器排序值 */
	private int order = Ordered.LOWEST_PRECEDENCE;

	/** 按 Bean 类缓存的生命周期元数据 */
	private final transient @Nullable Map<Class<?>, LifecycleMetadata> lifecycleMetadataCache = new ConcurrentHashMap<>(256);


	/**
	 * 指定要检查的 init 注解，用于标识 Bean 配置完成后应调用的初始化方法。
	 * <p>可使用任意自定义注解（不要求特定属性）。没有默认值，典型选择是
	 * {@link jakarta.annotation.PostConstruct}。
	 * @see #addInitAnnotationType
	 */
	public void setInitAnnotationType(Class<? extends Annotation> initAnnotationType) {
		this.initAnnotationTypes.clear();
		this.initAnnotationTypes.add(initAnnotationType);
	}

	/**
	 * 追加一个要检查的 init 注解，用于标识 Bean 配置完成后应调用的初始化方法。
	 * @since 6.0.11
	 * @see #setInitAnnotationType
	 */
	public void addInitAnnotationType(@Nullable Class<? extends Annotation> initAnnotationType) {
		if (initAnnotationType != null) {
			this.initAnnotationTypes.add(initAnnotationType);
		}
	}

	/**
	 * 指定要检查的 destroy 注解，用于标识上下文关闭时应调用的销毁方法。
	 * <p>可使用任意自定义注解（不要求特定属性）。没有默认值，典型选择是
	 * {@link jakarta.annotation.PreDestroy}。
	 * @see #addDestroyAnnotationType
	 */
	public void setDestroyAnnotationType(Class<? extends Annotation> destroyAnnotationType) {
		this.destroyAnnotationTypes.clear();
		this.destroyAnnotationTypes.add(destroyAnnotationType);
	}

	/**
	 * 追加一个要检查的 destroy 注解，用于标识上下文关闭时应调用的销毁方法。
	 * @since 6.0.11
	 * @see #setDestroyAnnotationType
	 */
	public void addDestroyAnnotationType(@Nullable Class<? extends Annotation> destroyAnnotationType) {
		if (destroyAnnotationType != null) {
			this.destroyAnnotationTypes.add(destroyAnnotationType);
		}
	}

	/**
	 * 设置本后置处理器的排序值。
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}


	@Override
	public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanClass, String beanName) {
		findLifecycleMetadata(beanDefinition, beanClass);
	}

	@Override
	public @Nullable BeanRegistrationAotContribution processAheadOfTime(RegisteredBean registeredBean) {
		RootBeanDefinition beanDefinition = registeredBean.getMergedBeanDefinition();
		beanDefinition.resolveDestroyMethodIfNecessary();
		LifecycleMetadata metadata = findLifecycleMetadata(beanDefinition, registeredBean.getBeanClass());
		if (!CollectionUtils.isEmpty(metadata.initMethods)) {
			String[] initMethodNames = safeMerge(beanDefinition.getInitMethodNames(), metadata.initMethods);
			beanDefinition.setInitMethodNames(initMethodNames);
		}
		if (!CollectionUtils.isEmpty(metadata.destroyMethods)) {
			String[] destroyMethodNames = safeMerge(beanDefinition.getDestroyMethodNames(), metadata.destroyMethods);
			beanDefinition.setDestroyMethodNames(destroyMethodNames);
		}
		return null;
	}

	/**
	 * 查找生命周期元数据，并将 init/destroy 方法登记到 Bean 定义中。
	 */
	private LifecycleMetadata findLifecycleMetadata(RootBeanDefinition beanDefinition, Class<?> beanClass) {
		LifecycleMetadata metadata = findLifecycleMetadata(beanClass);
		metadata.checkInitDestroyMethods(beanDefinition);
		return metadata;
	}

	/**
	 * 将检测到的生命周期方法名与已有方法名安全合并（去重）。
	 */
	private static String[] safeMerge(String @Nullable [] existingNames, Collection<LifecycleMethod> detectedMethods) {
		Stream<String> detectedNames = detectedMethods.stream().map(LifecycleMethod::getIdentifier);
		Stream<String> mergedNames = (existingNames != null ?
				Stream.concat(detectedNames, Stream.of(existingNames)) : detectedNames);
		return mergedNames.distinct().toArray(String[]::new);
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		LifecycleMetadata metadata = findLifecycleMetadata(bean.getClass());
		try {
			metadata.invokeInitMethods(bean, beanName);
		}
		catch (InvocationTargetException ex) {
			throw new BeanCreationException(beanName, "Invocation of init method failed", ex.getTargetException());
		}
		catch (Throwable ex) {
			throw new BeanCreationException(beanName, "Failed to invoke init method", ex);
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
		LifecycleMetadata metadata = findLifecycleMetadata(bean.getClass());
		try {
			metadata.invokeDestroyMethods(bean, beanName);
		}
		catch (InvocationTargetException ex) {
			String msg = "Destroy method on bean with name '" + beanName + "' threw an exception";
			if (logger.isDebugEnabled()) {
				logger.warn(msg, ex.getTargetException());
			}
			else if (logger.isWarnEnabled()) {
				logger.warn(msg + ": " + ex.getTargetException());
			}
		}
		catch (Throwable ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Failed to invoke destroy method on bean with name '" + beanName + "'", ex);
			}
		}
	}

	@Override
	public boolean requiresDestruction(Object bean) {
		return findLifecycleMetadata(bean.getClass()).hasDestroyMethods();
	}


	/**
	 * 按 Bean 类查找（或构建并缓存）生命周期元数据。
	 */
	private LifecycleMetadata findLifecycleMetadata(Class<?> beanClass) {
		if (this.lifecycleMetadataCache == null) {
			// 反序列化之后、销毁阶段可能走到这里……
			return buildLifecycleMetadata(beanClass);
		}
		// 先在并发 Map 上快速查找，尽量少加锁
		LifecycleMetadata metadata = this.lifecycleMetadataCache.get(beanClass);
		if (metadata == null) {
			synchronized (this.lifecycleMetadataCache) {
				metadata = this.lifecycleMetadataCache.get(beanClass);
				if (metadata == null) {
					metadata = buildLifecycleMetadata(beanClass);
					this.lifecycleMetadataCache.put(beanClass, metadata);
				}
				return metadata;
			}
		}
		return metadata;
	}

	/**
	 * 扫描类层次结构，构建 init/destroy 方法元数据。
	 */
	private LifecycleMetadata buildLifecycleMetadata(final Class<?> beanClass) {
		if (!AnnotationUtils.isCandidateClass(beanClass, this.initAnnotationTypes) &&
				!AnnotationUtils.isCandidateClass(beanClass, this.destroyAnnotationTypes)) {
			return this.emptyLifecycleMetadata;
		}

		List<LifecycleMethod> initMethods = new ArrayList<>();
		List<LifecycleMethod> destroyMethods = new ArrayList<>();
		Class<?> currentClass = beanClass;

		do {
			final List<LifecycleMethod> currInitMethods = new ArrayList<>();
			final List<LifecycleMethod> currDestroyMethods = new ArrayList<>();

			ReflectionUtils.doWithLocalMethods(currentClass, method -> {
				for (Class<? extends Annotation> initAnnotationType : this.initAnnotationTypes) {
					if (initAnnotationType != null && method.isAnnotationPresent(initAnnotationType)) {
						currInitMethods.add(new LifecycleMethod(method, beanClass));
						if (logger.isTraceEnabled()) {
							logger.trace("Found init method on class [" + beanClass.getName() + "]: " + method);
						}
					}
				}
				for (Class<? extends Annotation> destroyAnnotationType : this.destroyAnnotationTypes) {
					if (destroyAnnotationType != null && method.isAnnotationPresent(destroyAnnotationType)) {
						currDestroyMethods.add(new LifecycleMethod(method, beanClass));
						if (logger.isTraceEnabled()) {
							logger.trace("Found destroy method on class [" + beanClass.getName() + "]: " + method);
						}
					}
				}
			});

			initMethods.addAll(0, currInitMethods);
			destroyMethods.addAll(currDestroyMethods);
			currentClass = currentClass.getSuperclass();
		}
		while (currentClass != null && currentClass != Object.class);

		return (initMethods.isEmpty() && destroyMethods.isEmpty() ? this.emptyLifecycleMetadata :
				new LifecycleMetadata(beanClass, initMethods, destroyMethods));
	}


	//---------------------------------------------------------------------
	// 序列化支持
	//---------------------------------------------------------------------

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// 依赖默认序列化；反序列化后仅初始化状态
		ois.defaultReadObject();

		// 初始化 transient 字段
		this.logger = LogFactory.getLog(getClass());
	}


	/**
	 * 表示带注解的 init / destroy 方法信息的内部类。
	 */
	private class LifecycleMetadata {

		/** 目标 Bean 类 */
		private final Class<?> beanClass;

		/** 检测到的全部 init 方法 */
		private final Collection<LifecycleMethod> initMethods;

		/** 检测到的全部 destroy 方法 */
		private final Collection<LifecycleMethod> destroyMethods;

		/** 已登记到 Bean 定义、实际需要调用的 init 方法 */
		private volatile @Nullable Set<LifecycleMethod> checkedInitMethods;

		/** 已登记到 Bean 定义、实际需要调用的 destroy 方法 */
		private volatile @Nullable Set<LifecycleMethod> checkedDestroyMethods;

		public LifecycleMetadata(Class<?> beanClass, Collection<LifecycleMethod> initMethods,
				Collection<LifecycleMethod> destroyMethods) {

			this.beanClass = beanClass;
			this.initMethods = initMethods;
			this.destroyMethods = destroyMethods;
		}

		/**
		 * 将尚未由外部管理的 init/destroy 方法登记到 Bean 定义中。
		 */
		public void checkInitDestroyMethods(RootBeanDefinition beanDefinition) {
			Set<LifecycleMethod> checkedInitMethods = CollectionUtils.newLinkedHashSet(this.initMethods.size());
			for (LifecycleMethod lifecycleMethod : this.initMethods) {
				String methodIdentifier = lifecycleMethod.getIdentifier();
				if (!beanDefinition.isExternallyManagedInitMethod(methodIdentifier)) {
					beanDefinition.registerExternallyManagedInitMethod(methodIdentifier);
					checkedInitMethods.add(lifecycleMethod);
					if (logger.isTraceEnabled()) {
						logger.trace("Registered init method on class [" + this.beanClass.getName() + "]: " + methodIdentifier);
					}
				}
			}
			Set<LifecycleMethod> checkedDestroyMethods = CollectionUtils.newLinkedHashSet(this.destroyMethods.size());
			for (LifecycleMethod lifecycleMethod : this.destroyMethods) {
				String methodIdentifier = lifecycleMethod.getIdentifier();
				if (!beanDefinition.isExternallyManagedDestroyMethod(methodIdentifier)) {
					beanDefinition.registerExternallyManagedDestroyMethod(methodIdentifier);
					checkedDestroyMethods.add(lifecycleMethod);
					if (logger.isTraceEnabled()) {
						logger.trace("Registered destroy method on class [" + this.beanClass.getName() + "]: " + methodIdentifier);
					}
				}
			}
			this.checkedInitMethods = checkedInitMethods;
			this.checkedDestroyMethods = checkedDestroyMethods;
		}

		/**
		 * 调用已检查（或全部）的 init 方法。
		 */
		public void invokeInitMethods(Object target, String beanName) throws Throwable {
			Collection<LifecycleMethod> checkedInitMethods = this.checkedInitMethods;
			Collection<LifecycleMethod> initMethodsToIterate =
					(checkedInitMethods != null ? checkedInitMethods : this.initMethods);
			if (!initMethodsToIterate.isEmpty()) {
				for (LifecycleMethod lifecycleMethod : initMethodsToIterate) {
					if (logger.isTraceEnabled()) {
						logger.trace("Invoking init method on bean '" + beanName + "': " + lifecycleMethod.getMethod());
					}
					lifecycleMethod.invoke(target);
				}
			}
		}

		/**
		 * 调用已检查（或全部）的 destroy 方法。
		 */
		public void invokeDestroyMethods(Object target, String beanName) throws Throwable {
			Collection<LifecycleMethod> checkedDestroyMethods = this.checkedDestroyMethods;
			Collection<LifecycleMethod> destroyMethodsToUse =
					(checkedDestroyMethods != null ? checkedDestroyMethods : this.destroyMethods);
			if (!destroyMethodsToUse.isEmpty()) {
				for (LifecycleMethod lifecycleMethod : destroyMethodsToUse) {
					if (logger.isTraceEnabled()) {
						logger.trace("Invoking destroy method on bean '" + beanName + "': " + lifecycleMethod.getMethod());
					}
					lifecycleMethod.invoke(target);
				}
			}
		}

		/**
		 * 是否存在需要调用的 destroy 方法。
		 */
		public boolean hasDestroyMethods() {
			Collection<LifecycleMethod> checkedDestroyMethods = this.checkedDestroyMethods;
			Collection<LifecycleMethod> destroyMethodsToUse =
					(checkedDestroyMethods != null ? checkedDestroyMethods : this.destroyMethods);
			return !destroyMethodsToUse.isEmpty();
		}
	}


	/**
	 * 表示单个带注解的 init 或 destroy 方法。
	 */
	private static class LifecycleMethod {

		/** 生命周期方法 */
		private final Method method;

		/** 方法标识（方法名，或私有/不可见时的限定方法名） */
		private final String identifier;

		public LifecycleMethod(Method method, Class<?> beanClass) {
			if (method.getParameterCount() != 0) {
				throw new IllegalStateException("Lifecycle annotation requires a no-arg method: " + method);
			}
			this.method = method;
			this.identifier = (isPrivateOrNotVisible(method, beanClass) ?
					ClassUtils.getQualifiedMethodName(method) : method.getName());
		}

		public Method getMethod() {
			return this.method;
		}

		public String getIdentifier() {
			return this.identifier;
		}

		/**
		 * 在目标实例上调用该生命周期方法。
		 */
		public void invoke(Object target) throws Throwable {
			ReflectionUtils.makeAccessible(this.method);
			this.method.invoke(target);
		}

		@Override
		public boolean equals(@Nullable Object other) {
			return (this == other || (other instanceof LifecycleMethod that &&
					this.identifier.equals(that.identifier)));
		}

		@Override
		public int hashCode() {
			return this.identifier.hashCode();
		}

		/**
		 * 判断给定的生命周期 {@link Method} 是否为 private，
		 * 或对给定 Bean {@link Class} 不可见。
		 * @since 6.0.11
		 */
		private static boolean isPrivateOrNotVisible(Method method, Class<?> beanClass) {
			int modifiers = method.getModifiers();
			if (Modifier.isPrivate(modifiers)) {
				return true;
			}
			// 方法声明在与 Bean 类不同包中，且既非 public 也非 protected？
			return (!method.getDeclaringClass().getPackageName().equals(beanClass.getPackageName()) &&
					!(Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)));
		}

	}

}
