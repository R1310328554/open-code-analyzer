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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.core.AttributeAccessor;
import org.springframework.core.ResolvableType;

/**
 * 需要处理 {@link org.springframework.beans.factory.FactoryBean} 实例的单例注册表支持基类，
 * 与 {@link DefaultSingletonBeanRegistry} 的单例管理集成。
 *
 * <p>作为 {@link AbstractBeanFactory} 的基类。
 *
 * @author Juergen Hoeller
 * @since 2.5.1
 */
public abstract class FactoryBeanRegistrySupport extends DefaultSingletonBeanRegistry {

	/** FactoryBean 创建的单例对象缓存：FactoryBean 名称 -> 对象。 */
	private final Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<>(16);


	/**
	 * 确定给定 FactoryBean 的类型。
	 * @param factoryBean 要检查的 FactoryBean 实例
	 * @return FactoryBean 的对象类型，若尚无法确定则为 {@code null}
	 */
	protected @Nullable Class<?> getTypeForFactoryBean(FactoryBean<?> factoryBean) {
		try {
			return factoryBean.getObjectType();
		}
		catch (Throwable ex) {
			// 由 FactoryBean 的 getObjectType 实现抛出
			logger.info("FactoryBean threw exception from getObjectType, despite the contract saying " +
					"that it should return null if the type of its object cannot be determined yet", ex);
			return null;
		}
	}

	/**
	 * 通过检查属性中的 {@link FactoryBean#OBJECT_TYPE_ATTRIBUTE} 值
	 * 确定 FactoryBean 的 Bean 类型。
	 * @param attributes 要检查的属性
	 * @return 从属性提取的 {@link ResolvableType}，或 {@code ResolvableType.NONE}
	 * @since 5.2
	 */
	ResolvableType getTypeForFactoryBeanFromAttributes(AttributeAccessor attributes) {
		Object attribute = attributes.getAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE);
		if (attribute == null) {
			return ResolvableType.NONE;
		}
		if (attribute instanceof ResolvableType resolvableType) {
			return resolvableType;
		}
		if (attribute instanceof Class<?> clazz) {
			return ResolvableType.forClass(clazz);
		}
		throw new IllegalArgumentException("Invalid value type for attribute '" +
				FactoryBean.OBJECT_TYPE_ATTRIBUTE + "': " + attribute.getClass().getName());
	}

	/**
	 * 从给定泛型声明确定 FactoryBean 对象类型。
	 * @param type FactoryBean 类型
	 * @return 嵌套对象类型，若无法解析则为 {@code NONE}
	 */
	ResolvableType getFactoryBeanGeneric(@Nullable ResolvableType type) {
		return (type != null ? type.as(FactoryBean.class).getGeneric() : ResolvableType.NONE);
	}

	/**
	 * 从给定 FactoryBean 获取要暴露的对象（若缓存中可用）。
	 * 以最小同步进行快速检查。
	 * @param beanName Bean 名称
	 * @return 从 FactoryBean 获取的对象，若不可用则为 {@code null}
	 */
	protected @Nullable Object getCachedObjectForFactoryBean(String beanName) {
		return this.factoryBeanObjectCache.get(beanName);
	}

	/**
	 * 从给定 FactoryBean 获取要暴露的对象。
	 * @param factory FactoryBean 实例
	 * @param beanName Bean 名称
	 * @param shouldPostProcess Bean 是否需后处理
	 * @return 从 FactoryBean 获取的对象
	 * @throws BeanCreationException 若 FactoryBean 对象创建失败
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	protected Object getObjectFromFactoryBean(FactoryBean<?> factory, @Nullable Class<?> requiredType,
			String beanName, boolean shouldPostProcess) {

		if (factory.isSingleton() && containsSingleton(beanName)) {
			Boolean lockFlag = isCurrentThreadAllowedToHoldSingletonLock();
			boolean locked;
			if (lockFlag == null) {
				this.singletonLock.lock();
				locked = true;
			}
			else {
				locked = (lockFlag && this.singletonLock.tryLock());
			}
			try {
				if (factory instanceof SmartFactoryBean<?>) {
					// SmartFactoryBean 可能返回多种对象类型 -> 不缓存
					// 且 SmartFactoryBean 需线程安全 -> 无需同步
					Object object = doGetObjectFromFactoryBean(factory, requiredType, beanName);
					if (shouldPostProcess) {
						object = postProcessObjectFromSingletonFactoryBean(object, beanName, locked);
					}
					return object;
				}
				else {
					// 防御性同步，应对非线程安全的 FactoryBean.getObject() 实现，
					// 主线程在单例锁内调用 getObject() 时，后台线程可能同时调用
					synchronized (factory) {
						Object object = this.factoryBeanObjectCache.get(beanName);
						if (object == null) {
							object = doGetObjectFromFactoryBean(factory, requiredType, beanName);
							// 仅当 getObject() 调用期间尚未放入缓存时才后处理并存储
							// （例如因自定义 getBean 调用触发的循环引用处理）
							Object alreadyThere = this.factoryBeanObjectCache.get(beanName);
							if (alreadyThere != null) {
								object = alreadyThere;
							}
							else {
								if (shouldPostProcess) {
									object = postProcessObjectFromSingletonFactoryBean(object, beanName, locked);
								}
								if (containsSingleton(beanName)) {
									this.factoryBeanObjectCache.put(beanName, object);
								}
							}
						}
						return object;
					}
				}
			}
			finally {
				if (locked) {
					this.singletonLock.unlock();
				}
			}
		}
		else {
			Object object = doGetObjectFromFactoryBean(factory, requiredType, beanName);
			if (shouldPostProcess) {
				try {
					object = postProcessObjectFromFactoryBean(object, beanName);
				}
				catch (Throwable ex) {
					throw new BeanCreationException(beanName, "Post-processing of FactoryBean's object failed", ex);
				}
			}
			return object;
		}
	}

	/**
	 * 从给定 FactoryBean 获取要暴露的对象。
	 * @param factory FactoryBean 实例
	 * @param beanName Bean 名称
	 * @return 从 FactoryBean 获取的对象
	 * @throws BeanCreationException 若 FactoryBean 对象创建失败
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	private Object doGetObjectFromFactoryBean(FactoryBean<?> factory, @Nullable Class<?> requiredType, String beanName)
			throws BeanCreationException {

		Object object;
		try {
			object = (requiredType != null && factory instanceof SmartFactoryBean<?> smartFactoryBean ?
					smartFactoryBean.getObject(requiredType) : factory.getObject());
		}
		catch (FactoryBeanNotInitializedException ex) {
			throw new BeanCurrentlyInCreationException(beanName, ex.toString());
		}
		catch (Throwable ex) {
			throw new BeanCreationException(beanName, "FactoryBean threw exception on object creation", ex);
		}

		// 对于尚未完全初始化的 FactoryBean，不接受 null 值：许多 FactoryBean 此时仅返回 null
		if (object == null) {
			if (isSingletonCurrentlyInCreation(beanName)) {
				throw new BeanCurrentlyInCreationException(
						beanName, "FactoryBean which is currently in creation returned null from getObject");
			}
			object = new NullBean();
		}
		return object;
	}

	/**
	 * 对单例 FactoryBean 产生的对象实例进行后处理。
	 */
	private Object postProcessObjectFromSingletonFactoryBean(Object object, String beanName, boolean locked) {
		if (locked) {
			if (isSingletonCurrentlyInCreation(beanName)) {
				// 暂时返回未后处理的对象，尚不存储
				return object;
			}
			beforeSingletonCreation(beanName);
		}
		try {
			return postProcessObjectFromFactoryBean(object, beanName);
		}
		catch (Throwable ex) {
			throw new BeanCreationException(beanName,
					"Post-processing of FactoryBean's singleton object failed", ex);
		}
		finally {
			if (locked) {
				afterSingletonCreation(beanName);
			}
		}
	}

	/**
	 * 对从 FactoryBean 获取的对象进行后处理。
	 * 处理后的对象将用于 Bean 引用暴露。
	 * <p>默认实现直接返回给定对象。子类可覆盖，例如应用后处理器。
	 * @param object 从 FactoryBean 获取的对象
	 * @param beanName Bean 名称
	 * @return 要暴露的对象
	 * @throws org.springframework.beans.BeansException 若后处理失败
	 */
	protected Object postProcessObjectFromFactoryBean(Object object, String beanName) throws BeansException {
		return object;
	}

	/**
	 * 在可能的情况下获取给定 Bean 对应的 FactoryBean。
	 * @param beanName Bean 名称
	 * @param beanInstance 对应的 Bean 实例
	 * @return 作为 FactoryBean 的 Bean 实例
	 * @throws BeansException 若给定 Bean 无法作为 FactoryBean 暴露
	 */
	protected FactoryBean<?> getFactoryBean(String beanName, Object beanInstance) throws BeansException {
		if (!(beanInstance instanceof FactoryBean<?> factoryBean)) {
			throw new BeanCreationException(beanName,
					"Bean instance of type [" + beanInstance.getClass() + "] is not a FactoryBean");
		}
		return factoryBean;
	}

	/**
	 * 覆盖以同时清除 FactoryBean 对象缓存。
	 */
	@Override
	protected void removeSingleton(String beanName) {
		super.removeSingleton(beanName);
		this.factoryBeanObjectCache.remove(beanName);
	}

	/**
	 * 覆盖以同时清除 FactoryBean 对象缓存。
	 */
	@Override
	protected void clearSingletonCache() {
		super.clearSingletonCache();
		this.factoryBeanObjectCache.clear();
	}

}
