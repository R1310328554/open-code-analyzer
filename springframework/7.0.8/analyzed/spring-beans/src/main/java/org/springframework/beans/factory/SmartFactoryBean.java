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

package org.springframework.beans.factory;

import org.jspecify.annotations.Nullable;

/**
 * {@link FactoryBean} 接口的扩展。实现类可表明是否总是返回独立实例——
 * 用于其 {@link #isSingleton()} 返回 {@code false}、却又未能清楚表明
 * 是否为独立实例的情形。未实现本扩展接口的普通 {@link FactoryBean}，
 * 在 {@link #isSingleton()} 返回 {@code false} 时，会被简单地假定为
 * 总是返回独立实例；所暴露的对象仅在按需访问时才会被获取。
 *
 * <p>自 7.0 起，本接口还可通过实现一对方法为依赖注入暴露额外对象类型：
 * {@link #getObject(Class)} 与 {@link #supportsType(Class)}。
 * 常规访问仍暴露主类型 {@link #getObjectType()}；
 * 仅在请求特定类型时才会考虑额外类型。
 * 容器不会缓存 {@code SmartFactoryBean} 产生的对象；
 * 请确保 {@code getObject} 实现在重复调用时是线程安全的。
 *
 * <p><b>注意：</b>本接口是专用接口，主要用于框架内部以及协作框架内部。
 * 一般而言，应用提供的 FactoryBean 只需实现普通的 {@link FactoryBean} 接口即可。
 * 即使在点版本中，也可能向本扩展接口添加新方法。
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 * @param <T> Bean 类型
 * @see #isPrototype()
 * @see #isSingleton()
 */
public interface SmartFactoryBean<T> extends FactoryBean<T> {

	/**
	 * 若本工厂支持给定类型，则返回该类型的一个实例。
	 * <p>默认支持本工厂暴露的主类型，即 {@link #getObjectType()} 所指示、
	 * 并由 {@link #getObject()} 返回的类型。
	 * 特定工厂可为依赖注入支持额外类型。
	 * @param type 所请求的类型
	 * @return 本工厂管理的对应实例；若无可用则为 {@code null}
	 * @throws Exception 创建出错时
	 * @since 7.0
	 * @see #getObject()
	 * @see #supportsType(Class)
	 */
	@SuppressWarnings("unchecked")
	default <S> @Nullable S getObject(Class<S> type) throws Exception {
		Class<?> objectType = getObjectType();
		return (objectType != null && type.isAssignableFrom(objectType) ? (S) getObject() : null);
	}

	/**
	 * 判断本工厂是否支持所请求的类型。
	 * <p>默认支持本工厂暴露的主类型，即 {@link #getObjectType()} 所指示的类型。
	 * 特定工厂可为依赖注入支持额外类型。
	 * @param type 所请求的类型
	 * @return 若 {@link #getObject(Class)} 能够返回对应实例则为 {@code true}，
	 * 否则为 {@code false}
	 * @since 7.0
	 * @see #getObject(Class)
	 * @see #getObjectType()
	 */
	default boolean supportsType(Class<?> type) {
		Class<?> objectType = getObjectType();
		return (objectType != null && type.isAssignableFrom(objectType));
	}

	/**
	 * 本工厂管理的对象是否为原型？也就是说，
	 * {@link #getObject()} 是否总是返回独立实例？
	 * <p>FactoryBean 自身的原型状态通常由所属 {@link BeanFactory} 提供；
	 * 一般需要在那里定义为单例。
	 * <p>本方法应严格检查是否为独立实例；
	 * 对于作用域对象或其他非单例、非独立对象，不应返回 {@code true}。
	 * 因此，它并不是简单的 {@link #isSingleton()} 取反。
	 * <p>默认实现返回 {@code false}。
	 * @return 所暴露的对象是否为原型
	 * @see #getObject()
	 * @see #isSingleton()
	 */
	default boolean isPrototype() {
		return false;
	}

	/**
	 * 本 FactoryBean 是否期望急切初始化？也就是说，
	 * 是否急切初始化自身，并期望其单例对象（若有）也被急切初始化？
	 * <p>标准 FactoryBean 预期不会急切初始化：
	 * 即使是单例对象，也只有在实际访问时才会调用其 {@link #getObject()}。
	 * 本方法返回 {@code true} 表示应急切调用 {@link #getObject()}，
	 * 并急切应用后置处理器。对于 {@link #isSingleton() 单例} 对象，
	 * 尤其是后置处理器期望在启动时应用时，这可能是合理的。
	 * <p>默认实现返回 {@code false}。
	 * @return 是否适用急切初始化
	 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory#preInstantiateSingletons()
	 */
	default boolean isEagerInit() {
		return false;
	}

}
