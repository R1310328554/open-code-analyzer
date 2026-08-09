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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.lang.Contract;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

/**
 * 用于管理注入元数据的内部类。
 *
 * <p>不打算由应用代码直接使用。
 *
 * <p>由 {@link AutowiredAnnotationBeanPostProcessor}、
 * {@link org.springframework.context.annotation.CommonAnnotationBeanPostProcessor} 以及
 * {@link org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor} 使用。
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
public class InjectionMetadata {

	/**
	 * 空的 {@code InjectionMetadata} 实例，各回调均为空操作。
	 * @since 5.2
	 */
	public static final InjectionMetadata EMPTY = new InjectionMetadata(Object.class, Collections.emptyList()) {
		@Override
		protected boolean needsRefresh(Class<?> clazz) {
			return false;
		}
		@Override
		public void checkConfigMembers(RootBeanDefinition beanDefinition) {
		}
		@Override
		public void inject(Object target, @Nullable String beanName, @Nullable PropertyValues pvs) {
		}
		@Override
		public void clear(@Nullable PropertyValues pvs) {
		}
	};


	/** 注入目标类 */
	private final Class<?> targetClass;

	/** 待注入的元素集合 */
	private final Collection<InjectedElement> injectedElements;

	/** 已登记到 Bean 定义、实际需要注入的元素 */
	private volatile @Nullable Set<InjectedElement> checkedElements;


	/**
	 * 创建新的 {@code InjectionMetadata} 实例。
	 * <p>若没有注入元素，优先使用 {@link #forElements}，以便复用 {@link #EMPTY}。
	 * @param targetClass 目标类
	 * @param elements 关联的待注入元素
	 * @see #forElements
	 */
	public InjectionMetadata(Class<?> targetClass, Collection<InjectedElement> elements) {
		this.targetClass = targetClass;
		this.injectedElements = elements;
	}


	/**
	 * 返回待注入的 {@link InjectedElement 元素}。
	 * @return 待注入元素
	 */
	public Collection<InjectedElement> getInjectedElements() {
		return Collections.unmodifiableCollection(this.injectedElements);
	}

	/**
	 * 根据指定的 {@link PropertyValues} 返回仍需注入的 {@link InjectedElement 元素}。
	 * 若某个 {@link InjectedElement} 对应的属性已有定义，则排除该元素。
	 * @param pvs 要考虑的属性值
	 * @return 待注入元素
	 * @since 6.0.10
	 */
	public Collection<InjectedElement> getInjectedElements(@Nullable PropertyValues pvs) {
		return this.injectedElements.stream().filter(candidate -> candidate.shouldInject(pvs)).toList();
	}

	/**
	 * 判断本元数据实例是否需要刷新。
	 * @param clazz 当前目标类
	 * @return {@code true} 表示需要刷新，否则为 {@code false}
	 * @since 5.2.4
	 */
	protected boolean needsRefresh(Class<?> clazz) {
		return (this.targetClass != clazz);
	}

	/**
	 * 将尚未由外部管理的配置成员登记到 Bean 定义中。
	 */
	public void checkConfigMembers(RootBeanDefinition beanDefinition) {
		if (this.injectedElements.isEmpty()) {
			this.checkedElements = Collections.emptySet();
		}
		else {
			Set<InjectedElement> checkedElements = CollectionUtils.newLinkedHashSet(this.injectedElements.size());
			for (InjectedElement element : this.injectedElements) {
				Member member = element.getMember();
				if (!beanDefinition.isExternallyManagedConfigMember(member)) {
					beanDefinition.registerExternallyManagedConfigMember(member);
					checkedElements.add(element);
				}
			}
			this.checkedElements = checkedElements;
		}
	}

	/**
	 * 对目标对象执行全部（已检查或全部）注入元素的注入。
	 */
	public void inject(Object target, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
		Collection<InjectedElement> checkedElements = this.checkedElements;
		Collection<InjectedElement> elementsToIterate =
				(checkedElements != null ? checkedElements : this.injectedElements);
		if (!elementsToIterate.isEmpty()) {
			for (InjectedElement element : elementsToIterate) {
				element.inject(target, beanName, pvs);
			}
		}
	}

	/**
	 * 清除所含元素上的「属性跳过」标记。
	 * @since 3.2.13
	 */
	public void clear(@Nullable PropertyValues pvs) {
		Collection<InjectedElement> checkedElements = this.checkedElements;
		Collection<InjectedElement> elementsToIterate =
				(checkedElements != null ? checkedElements : this.injectedElements);
		if (!elementsToIterate.isEmpty()) {
			for (InjectedElement element : elementsToIterate) {
				element.clearPropertySkipping(pvs);
			}
		}
	}


	/**
	 * 返回一个 {@code InjectionMetadata} 实例（元素可能为空）。
	 * @param elements 要注入的元素（可能为空）
	 * @param clazz 目标类
	 * @return 新的 {@link #InjectionMetadata(Class, Collection)} 实例
	 * @since 5.2
	 */
	public static InjectionMetadata forElements(Collection<InjectedElement> elements, Class<?> clazz) {
		return (elements.isEmpty() ? new InjectionMetadata(clazz, Collections.emptyList()) :
				new InjectionMetadata(clazz, elements));
	}

	/**
	 * 检查给定的注入元数据是否需要刷新。
	 * @param metadata 已有的元数据实例
	 * @param clazz 当前目标类
	 * @return {@code true} 表示需要刷新，否则为 {@code false}
	 * @see #needsRefresh(Class)
	 */
	@Contract("null, _ -> true")
	public static boolean needsRefresh(@Nullable InjectionMetadata metadata, Class<?> clazz) {
		return (metadata == null || metadata.needsRefresh(clazz));
	}


	/**
	 * 单个注入元素。
	 */
	public abstract static class InjectedElement {

		/** 被注入的成员（字段或方法） */
		protected final Member member;

		/** 是否为字段注入（否则为方法/属性注入） */
		protected final boolean isField;

		/** 关联的属性描述符（方法注入时可能有） */
		protected final @Nullable PropertyDescriptor pd;

		/** 是否因显式属性值而跳过注入的缓存标记 */
		protected volatile @Nullable Boolean skip;

		protected InjectedElement(Member member, @Nullable PropertyDescriptor pd) {
			this.member = member;
			this.isField = (member instanceof Field);
			this.pd = pd;
		}

		public final Member getMember() {
			return this.member;
		}

		/**
		 * 获取该注入点期望的资源类型。
		 */
		protected final Class<?> getResourceType() {
			if (this.isField) {
				return ((Field) this.member).getType();
			}
			else if (this.pd != null) {
				return this.pd.getPropertyType();
			}
			else {
				return ((Method) this.member).getParameterTypes()[0];
			}
		}

		/**
		 * 检查指定资源类型是否与字段/参数类型兼容。
		 */
		protected final void checkResourceType(Class<?> resourceType) {
			if (this.isField) {
				Class<?> fieldType = ((Field) this.member).getType();
				if (!(resourceType.isAssignableFrom(fieldType) || fieldType.isAssignableFrom(resourceType))) {
					throw new IllegalStateException("Specified field type [" + fieldType +
							"] is incompatible with resource type [" + resourceType.getName() + "]");
				}
			}
			else {
				Class<?> paramType =
						(this.pd != null ? this.pd.getPropertyType() : ((Method) this.member).getParameterTypes()[0]);
				if (!(resourceType.isAssignableFrom(paramType) || paramType.isAssignableFrom(resourceType))) {
					throw new IllegalStateException("Specified parameter type [" + paramType +
							"] is incompatible with resource type [" + resourceType.getName() + "]");
				}
			}
		}

		/**
		 * 是否应根据给定属性值执行注入。
		 * @param pvs 要检查的属性值
		 * @return 是否应注入
		 * @since 6.0.10
		 */
		protected boolean shouldInject(@Nullable PropertyValues pvs) {
			if (this.isField) {
				return true;
			}
			return !checkPropertySkipping(pvs);
		}

		/**
		 * 执行注入；或者覆盖 {@link #getResourceToInject}。
		 */
		protected void inject(Object target, @Nullable String requestingBeanName, @Nullable PropertyValues pvs)
				throws Throwable {

			if (!shouldInject(pvs)) {
				return;
			}
			if (this.isField) {
				Field field = (Field) this.member;
				ReflectionUtils.makeAccessible(field);
				field.set(target, getResourceToInject(target, requestingBeanName));
			}
			else {
				try {
					Method method = (Method) this.member;
					ReflectionUtils.makeAccessible(method);
					method.invoke(target, getResourceToInject(target, requestingBeanName));
				}
				catch (InvocationTargetException ex) {
					throw ex.getTargetException();
				}
			}
		}

		/**
		 * 检查是否因 Bean 定义中已显式提供属性值而应跳过本注入器。
		 * 同时会把受影响的属性标记为已处理，供其他处理器忽略。
		 */
		protected boolean checkPropertySkipping(@Nullable PropertyValues pvs) {
			Boolean skip = this.skip;
			if (skip != null) {
				return skip;
			}
			if (pvs == null) {
				this.skip = false;
				return false;
			}
			synchronized (pvs) {
				skip = this.skip;
				if (skip != null) {
					return skip;
				}
				if (this.pd != null) {
					if (pvs.contains(this.pd.getName())) {
						// Bean 定义中已显式提供该属性值
						this.skip = true;
						return true;
					}
					else if (pvs instanceof MutablePropertyValues mpvs) {
						mpvs.registerProcessedProperty(this.pd.getName());
					}
				}
				this.skip = false;
				return false;
			}
		}

		/**
		 * 清除本元素上的「属性跳过」标记。
		 * @since 3.2.13
		 */
		protected void clearPropertySkipping(@Nullable PropertyValues pvs) {
			if (pvs == null) {
				return;
			}
			synchronized (pvs) {
				if (Boolean.FALSE.equals(this.skip) && this.pd != null && pvs instanceof MutablePropertyValues mpvs) {
					mpvs.clearProcessedProperty(this.pd.getName());
				}
			}
		}

		/**
		 * 获取要注入的资源；或者覆盖 {@link #inject}。
		 */
		protected @Nullable Object getResourceToInject(Object target, @Nullable String requestingBeanName) {
			return null;
		}

		@Override
		public boolean equals(@Nullable Object other) {
			return (this == other || (other instanceof InjectedElement that &&
					this.member.equals(that.member)));
		}

		@Override
		public int hashCode() {
			return this.member.getClass().hashCode() * 29 + this.member.getName().hashCode();
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + " for " + this.member;
		}
	}

}
