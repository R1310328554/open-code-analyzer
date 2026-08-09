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

import java.util.Iterator;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * 大多数可列举 bean 工厂应实现的配置接口。
 * 除 {@link ConfigurableBeanFactory} 外，还提供分析和修改 bean 定义、
 * 以及预实例化单例的设施。
 *
 * <p>此 {@link org.springframework.beans.factory.BeanFactory} 子接口
 * 不适用于普通应用代码：典型场景请使用
 * {@link org.springframework.beans.factory.BeanFactory} 或
 * {@link org.springframework.beans.factory.ListableBeanFactory}。
 * 此接口仅用于框架内部即插即用，且需要访问 bean 工厂配置方法时。
 *
 * @author Juergen Hoeller
 * @since 03.11.2003
 * @see org.springframework.context.support.AbstractApplicationContext#getBeanFactory()
 */
public interface ConfigurableListableBeanFactory
		extends ListableBeanFactory, AutowireCapableBeanFactory, ConfigurableBeanFactory {

	/**
	 * 忽略给定依赖类型的自动装配：例如 String。默认无。
	 * @param type 要忽略的依赖类型
	 */
	void ignoreDependencyType(Class<?> type);

	/**
	 * 忽略给定依赖接口的自动装配。
	 * <p>应用上下文通常用此方法注册以其他方式解析的依赖，
	 * 如通过 BeanFactoryAware 解析 BeanFactory，
	 * 或通过 ApplicationContextAware 解析 ApplicationContext。
	 * <p>默认仅忽略 BeanFactoryAware 接口。
	 * 要忽略更多类型，请为每个类型调用此方法。
	 * @param ifc 要忽略的依赖接口
	 * @see org.springframework.beans.factory.BeanFactoryAware
	 * @see org.springframework.context.ApplicationContextAware
	 */
	void ignoreDependencyInterface(Class<?> ifc);

	/**
	 * 注册特殊依赖类型及其对应的自动装配值。
	 * <p>用于应在工厂中自动装配但未定义为 bean 的工厂/上下文引用：
	 * 例如类型为 ApplicationContext 的依赖解析为 bean 所在的 ApplicationContext 实例。
	 * <p>注意：普通 BeanFactory 中不注册此类默认类型，甚至不包括 BeanFactory 接口本身。
	 * @param dependencyType 要注册的依赖类型。通常为基础接口如 BeanFactory，
	 * 若声明为自动装配依赖，其扩展接口（如 ListableBeanFactory）也会被解析，
	 * 只要给定值实际实现了扩展接口。
	 * @param autowiredValue 对应的自动装配值。也可以是
	 * {@link org.springframework.beans.factory.ObjectFactory} 接口的实现，
	 * 允许延迟解析实际目标值。
	 */
	void registerResolvableDependency(Class<?> dependencyType, @Nullable Object autowiredValue);

	/**
	 * 判断指定 bean 是否符合自动装配候选条件，可注入到声明了匹配类型依赖的其他 bean 中。
	 * <p>此方法也会检查祖先工厂。
	 * @param beanName 要检查的 bean 名称
	 * @param descriptor 要解析的依赖描述符
	 * @return bean 是否应被视为自动装配候选
	 * @throws NoSuchBeanDefinitionException 若不存在给定名称的 bean
	 */
	boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor)
			throws NoSuchBeanDefinitionException;

	/**
	 * 返回指定 bean 的已注册 BeanDefinition，允许访问其属性值和构造器参数值
	 * （可在 bean 工厂后处理期间修改）。
	 * <p>返回的 BeanDefinition 对象不应是副本，而应是工厂中注册的原始定义对象。
	 * 这意味着必要时可转换为更具体的实现类型。
	 * <p><b>注意：</b>此方法<i>不</i>考虑祖先工厂。
	 * 仅用于访问此工厂的本地 bean 定义。
	 * @param beanName bean 的名称
	 * @return 已注册的 BeanDefinition
	 * @throws NoSuchBeanDefinitionException 若此工厂中未定义给定名称的 bean
	 */
	BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	/**
	 * 返回此工厂管理的所有 bean 名称的统一视图。
	 * <p>包括 bean 定义名称以及手动注册的单例实例名称，
	 * bean 定义名称始终排在前面，类似于按类型/注解检索 bean 名称的方式。
	 * @return bean 名称视图的组合迭代器
	 * @since 4.1.2
	 * @see #containsBeanDefinition
	 * @see #registerSingleton
	 * @see #getBeanNamesForType
	 * @see #getBeanNamesForAnnotation
	 */
	Iterator<String> getBeanNamesIterator();

	/**
	 * 清除合并 bean 定义缓存，移除尚不符合完整元数据缓存条件的 bean 条目。
	 * <p>通常在原始 bean 定义变更后触发，例如应用 {@link BeanFactoryPostProcessor} 后。
	 * 注意此时已创建的 bean 的元数据将保留。
	 * @since 4.2
	 * @see #getBeanDefinition
	 * @see #getMergedBeanDefinition
	 */
	void clearMetadataCache();

	/**
	 * 冻结所有 bean 定义，表示已注册的 bean 定义将不再被修改或后处理。
	 * <p>这允许工厂在清除初始临时元数据缓存后积极缓存 bean 定义元数据。
	 * @see #clearMetadataCache()
	 * @see #isConfigurationFrozen()
	 */
	void freezeConfiguration();

	/**
	 * 返回此工厂的 bean 定义是否已冻结，
	 * 即不应再被修改或后处理。
	 * @return 若工厂配置被视为已冻结则为 {@code true}
	 * @see #freezeConfiguration()
	 */
	boolean isConfigurationFrozen();

	/**
	 * 将当前线程标记为主引导线程以进行单例实例化，
	 * 后台线程应用宽松的引导锁定。
	 * <p>此类标记应在托管引导结束时于 {@link #preInstantiateSingletons()} 中移除。
	 * @since 6.2.12
	 * @see #setBootstrapExecutor
	 * @see #preInstantiateSingletons()
	 */
	default void prepareSingletonBootstrap() {
	}

	/**
	 * 确保所有非延迟初始化的单例被实例化，同时考虑
	 * {@link org.springframework.beans.factory.FactoryBean FactoryBean}。
	 * 通常在工厂设置结束时调用（如需要）。
	 * @throws BeansException 若某个单例 bean 无法创建。
	 * 注意：这可能导致工厂中部分 bean 已初始化！
	 * 此情况下请调用 {@link #destroySingletons()} 进行完整清理。
	 * @see #prepareSingletonBootstrap()
	 * @see #destroySingletons()
	 */
	void preInstantiateSingletons() throws BeansException;

}
