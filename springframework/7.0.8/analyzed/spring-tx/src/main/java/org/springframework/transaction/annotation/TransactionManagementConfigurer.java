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

package org.springframework.transaction.annotation;

import org.springframework.transaction.TransactionManager;

/**
 * 用于显式指定注解驱动事务管理所使用的默认
 * {@link org.springframework.transaction.PlatformTransactionManager} Bean
 *（或 {@link org.springframework.transaction.ReactiveTransactionManager} Bean）的接口，
 * 而非默认的按类型查找方式。
 * 当容器中存在两个 {@code PlatformTransactionManager} Bean
 *（或两个 {@code ReactiveTransactionManager} Bean）时可能需要。
 *
 * <p>通常由带 @{@link EnableTransactionManagement} 的
 * @{@link org.springframework.context.annotation.Configuration Configuration} 类实现。
 * 一般示例与上下文见 @{@link EnableTransactionManagement}；
 * 详细说明见 {@link #annotationDrivenTransactionManager()}。
 *
 * <p><b>注意：{@code TransactionManagementConfigurer} 会较早初始化。</b>
 * 请勿直接向 autowired 字段注入常见依赖；
 * 可考虑为这些依赖声明 lazy {@link org.springframework.beans.factory.ObjectProvider}。
 *
 * <p>在按类型查找消歧场景中，
 * 替代实现本接口的方式是将其中一个
 * {@code PlatformTransactionManager} {@code @Bean} 方法
 *（或 {@code ReactiveTransactionManager} {@code @Bean} 方法）
 * 标记为 {@link org.springframework.context.annotation.Primary @Primary}。
 * 这通常更受青睐，因为不会导致 {@code TransactionManager} Bean 过早初始化。
 *
 * @author Chris Beams
 * @since 3.1
 * @see EnableTransactionManagement
 * @see org.springframework.context.annotation.Primary
 * @see org.springframework.transaction.PlatformTransactionManager
 * @see org.springframework.transaction.ReactiveTransactionManager
 */
public interface TransactionManagementConfigurer {

	/**
	 * 返回用于注解驱动数据库事务管理（即处理 {@code @Transactional} 方法时）
	 * 的默认事务管理器 Bean。
	 * <p>实现此方法有两种基本方式：
	 * <h4>1. 实现方法并用 {@code @Bean} 标注</h4>
	 * 此时实现 {@code @Configuration} 类实现此方法，
	 * 用 {@code @Bean} 标注，并在方法体内直接配置并返回事务管理器：
	 * <pre class="code">
	 * &#064;Bean
	 * &#064;Override
	 * public PlatformTransactionManager annotationDrivenTransactionManager() {
	 *     return new DataSourceTransactionManager(dataSource());
	 * }</pre>
	 * <h4>2. 不用 {@code @Bean} 实现方法，委托给已有 {@code @Bean} 方法</h4>
	 * <pre class="code">
	 * &#064;Bean
	 * public PlatformTransactionManager txManager() {
	 *     return new DataSourceTransactionManager(dataSource());
	 * }
	 *
	 * &#064;Override
	 * public PlatformTransactionManager annotationDrivenTransactionManager() {
	 *     return txManager(); // reference the existing {@code @Bean} method above
	 * }</pre>
	 * 若采用方式 #2，请确保<em>仅一个</em>方法标记 {@code @Bean}！
	 * <p>无论方式 #1 或 #2，{@code PlatformTransactionManager} 实例
	 * 都必须在容器中作为 Spring Bean 管理，
	 * 因为大多数实现会利用 {@code InitializingBean}、
	 * {@code BeanFactoryAware} 等 Spring 生命周期回调。
	 * 相同准则也适用于 {@code ReactiveTransactionManager} Bean。
	 * @return {@link org.springframework.transaction.PlatformTransactionManager} 或
	 * {@link org.springframework.transaction.ReactiveTransactionManager} 实现
	 */
	TransactionManager annotationDrivenTransactionManager();

}
