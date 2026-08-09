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

package org.springframework.context.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指示 Bean 是否应延迟初始化。
 *
 * <p>可直接或间接标注了 {@link org.springframework.stereotype.Component @Component}
 * 的任意类上使用，也可标注 {@link Bean @Bean} 方法。
 *
 * <p>若 {@code @Component} 或 {@code @Bean} 定义上未出现本注解，将执行急切初始化。
 * 若存在且设为 {@code true}，则 {@code @Bean} 或 {@code @Component} 在被其他 Bean 引用
 * 或从包含的 {@link org.springframework.beans.factory.BeanFactory BeanFactory}
 * 显式获取之前不会初始化。若存在且设为 {@code false}，执行单例急切初始化的
 * Bean 工厂将在启动时实例化该 Bean。
 *
 * <p>若在 {@link Configuration @Configuration} 类上使用 {@code @Lazy}，
 * 表示该 {@code @Configuration} 内所有 {@code @Bean} 方法均应延迟初始化。
 * 若在已标注 {@code @Lazy} 的 {@code @Configuration} 类内的 {@code @Bean} 方法上
 * 存在 {@code @Lazy} 且为 {@code false}，表示覆盖“默认延迟”行为，该 Bean 应急切初始化。
 *
 * <p>除组件初始化角色外，本注解也可标注带有
 * {@link org.springframework.beans.factory.annotation.Autowired}
 * 或 {@link jakarta.inject.Inject} 的注入点：此时将为受影响依赖创建延迟解析代理，
 * 单例在首次访问时缓存，否则每次访问重新解析。这是使用
 * {@link org.springframework.beans.factory.ObjectFactory} 或
 * {@link jakarta.inject.Provider} 的替代方案。请注意，此类延迟解析代理始终会被注入；
 * 若目标依赖不存在，只能在调用时通过异常发现。因此，此类注入点对可选依赖的行为不够直观。
 * 若需更精细的编程式延迟引用，请考虑
 * {@link org.springframework.beans.factory.ObjectProvider}。
 *
 * <p>本注解可作为<em>元注解</em>使用，以创建自定义<em>组合注解</em>。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.0
 * @see Primary
 * @see Bean
 * @see Configuration
 * @see org.springframework.stereotype.Component
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Lazy {

	/**
	 * 是否应延迟初始化。
	 */
	boolean value() default true;

}
