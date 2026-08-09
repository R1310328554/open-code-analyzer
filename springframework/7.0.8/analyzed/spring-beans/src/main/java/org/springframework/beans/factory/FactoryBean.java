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
 * 由 {@link BeanFactory} 中使用的、自身又充当「对象工厂」的对象所实现的接口。
 * 若某个 Bean 实现了本接口，则它会被当作工厂来暴露其创建的对象，
 * 而不是把该 Bean 实例本身直接暴露出去。
 *
 * <p><b>注意：实现了本接口的 Bean 不能当作普通 Bean 使用。</b>
 * FactoryBean 以 Bean 的方式定义，但通过 Bean 引用拿到的对象
 * （{@link #getObject()}）始终是它创建出来的那个对象。
 *
 * <p>FactoryBean 既可支持单例，也可支持原型，既可以按需惰性创建，
 * 也可以在启动时急切创建。{@link SmartFactoryBean} 接口还能暴露更细粒度的行为元数据。
 *
 * <p>框架内部大量使用本接口，例如 AOP 的
 * {@link org.springframework.aop.framework.ProxyFactoryBean}，或
 * {@link org.springframework.jndi.JndiObjectFactoryBean}。
 * 自定义组件也可以使用；不过这通常只见于基础设施代码。
 *
 * <p><b>{@code FactoryBean} 是一种编程式契约。实现类不应依赖注解驱动注入
 * 或其他反射设施。</b>
 * {@link #getObjectType()} 与 {@link #getObject()} 的调用可能发生在引导过程的早期，
 * 甚至早于任何后置处理器的就绪。若需要访问其他 Bean，请实现 {@link BeanFactoryAware}
 * 并以编程方式获取。
 *
 * <p><b>容器只负责管理 FactoryBean 实例本身的生命周期，
 * 不负责管理由 FactoryBean 创建出的对象的生命周期。</b>
 * 因此，被暴露对象上的销毁方法（例如 {@link java.io.Closeable#close()}）
 * <i>不会</i>被自动调用。FactoryBean 应实现 {@link DisposableBean}，
 * 并将关闭调用委托给底层对象。
 *
 * <p>最后，FactoryBean 对象会参与所属 BeanFactory 的 Bean 创建同步。
 * 因此，通常无需额外的内部同步，除非是 FactoryBean 自身内部的惰性初始化之类场景。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 08.03.2003
 * @param <T> Bean 类型
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.aop.framework.ProxyFactoryBean
 * @see org.springframework.jndi.JndiObjectFactoryBean
 */
public interface FactoryBean<T> {

	/**
	 * 可在 {@link org.springframework.beans.factory.config.BeanDefinition} 上
	 * 通过 {@link org.springframework.core.AttributeAccessor#setAttribute setAttribute}
	 * 设置的属性名。当无法从 FactoryBean 类推断出对象类型时，
	 * FactoryBean 可用该属性声明其对象类型。
	 * @since 5.2
	 */
	String OBJECT_TYPE_ATTRIBUTE = "factoryBeanObjectType";


	/**
	 * 返回本工厂所管理对象的一个实例（可能是共享的，也可能是独立的）。
	 * <p>与 {@link BeanFactory} 一样，这里同时支持单例与原型设计模式。
	 * <p>若调用时本 FactoryBean 尚未完全初始化（例如因循环引用），
	 * 应抛出相应的 {@link FactoryBeanNotInitializedException}。
	 * <p>FactoryBean 允许返回 {@code null}。BeanFactory 会把它当作正常值使用，
	 * 此时不会抛出 {@code FactoryBeanNotInitializedException}。
	 * 不过仍鼓励实现类在合适时自行抛出 {@code FactoryBeanNotInitializedException}。
	 * @return Bean 实例（可为 {@code null}）
	 * @throws Exception 创建出错时
	 * @see FactoryBeanNotInitializedException
	 */
	@Nullable T getObject() throws Exception;

	/**
	 * 返回本 FactoryBean 所创建对象的类型；若事先未知则返回 {@code null}。
	 * <p>这样可在不实例化对象的情况下检查特定类型的 Bean，例如在自动装配时。
	 * <p>对于创建单例对象的实现，本方法应尽量避免触发单例创建，
	 * 而应预先估算类型。对于原型，这里返回有意义的类型同样可取。
	 * <p>本方法可在 FactoryBean <i>完全初始化之前</i>被调用。
	 * 不得依赖初始化过程中建立的状态；当然，若该状态已可用则仍可使用。
	 * <p><b>注意：</b>自动装配会直接忽略此处返回 {@code null} 的 FactoryBean。
	 * 因此强烈建议根据 FactoryBean 的当前状态正确实现本方法。
	 * @return 本 FactoryBean 所创建对象的类型；
	 * 若调用时尚不知晓则为 {@code null}
	 * @see ListableBeanFactory#getBeansOfType
	 */
	@Nullable Class<?> getObjectType();

	/**
	 * 本工厂管理的对象是否为单例？也就是说，
	 * {@link #getObject()} 是否总是返回同一个对象（可被缓存的引用）？
	 * <p><b>注意：</b>若 FactoryBean 表明持有单例对象，
	 * 则所属 BeanFactory 可能会缓存 {@code getObject()} 的返回值。
	 * 因此，除非 FactoryBean 始终暴露同一引用，否则不要返回 {@code true}。
	 * <p>FactoryBean 自身的单例状态通常由所属 BeanFactory 决定；
	 * 一般需要在那里定义为单例。
	 * <p><b>注意：</b>本方法返回 {@code false} 并不一定表示返回的对象是独立实例。
	 * 扩展的 {@link SmartFactoryBean} 接口可通过其
	 * {@link SmartFactoryBean#isPrototype()} 方法显式声明独立实例。
	 * 未实现该扩展接口的普通 {@link FactoryBean}，在 {@code isSingleton()}
	 * 返回 {@code false} 时，会被简单地假定为总是返回独立实例。
	 * <p>默认实现返回 {@code true}，因为 {@code FactoryBean} 通常管理单例实例。
	 * @return 所暴露的对象是否为单例
	 * @see #getObject()
	 * @see SmartFactoryBean#isPrototype()
	 */
	default boolean isSingleton() {
		return true;
	}

}
