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
 * 启用对 AspectJ {@code @Aspect} 标注组件的处理支持，功能类似于 Spring XML 中的
 * {@code <aop:aspectj-autoproxy>} 元素。
 * 在 @{@link Configuration} 类上按如下方式使用：
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableAspectJAutoProxy
 * public class AppConfig {
 *
 *     &#064;Bean
 *     public FooService fooService() {
 *         return new FooService();
 *     }
 *
 *     &#064;Bean
 *     public MyAspect myAspect() {
 *         return new MyAspect();
 *     }
 * }</pre>
 *
 * 其中 {@code FooService} 为典型 POJO 组件，{@code MyAspect} 为 {@code @Aspect} 风格切面：
 *
 * <pre class="code">
 * public class FooService {
 *
 *     // various methods
 * }</pre>
 *
 * <pre class="code">
 * &#064;Aspect
 * public class MyAspect {
 *
 *     &#064;Before("execution(* FooService+.*(..))")
 *     public void advice() {
 *         // advise FooService methods as appropriate
 *     }
 * }</pre>
 *
 * 上述场景中，{@code @EnableAspectJAutoProxy} 确保 {@code MyAspect} 被正确处理，
 * 且 {@code FooService} 会被代理并织入其贡献的通知。
 *
 * <p>用户可通过 {@link #proxyTargetClass()} 属性控制为 {@code FooService} 创建的代理类型。
 * 以下示例启用 CGLIB 子类代理，而非默认的基于接口的 JDK 代理：
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableAspectJAutoProxy(proxyTargetClass=true)
 * public class AppConfig {
 *     // ...
 * }</pre>
 *
 * <p>注意 {@code @Aspect} Bean 可像其他组件一样被组件扫描。
 * 只需同时为切面标注 {@code @Aspect} 与 {@code @Component}：
 *
 * <pre class="code">
 * package com.foo;
 *
 * &#064;Component
 * public class FooService { ... }
 *
 * &#064;Aspect
 * &#064;Component
 * public class MyAspect { ... }</pre>
 *
 * 然后使用 @{@link ComponentScan} 一并扫描：
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;ComponentScan("com.foo")
 * &#064;EnableAspectJAutoProxy
 * public class AppConfig {
 *
 *     // no explicit &#064;Bean definitions required
 * }</pre>
 *
 * <b>注意：{@code @EnableAspectJAutoProxy} 仅作用于其所在本地应用上下文，
 * 允许在不同层级选择性代理 Bean。</b>若需在多个层级应用其行为，请在各上下文单独重新声明
 * {@code @EnableAspectJAutoProxy}，例如公共根 Web 应用上下文与独立的
 * {@code DispatcherServlet} 应用上下文。
 *
 * <p>本功能要求 classpath 上存在 {@code aspectjweaver}。虽对 {@code spring-aop} 一般为可选依赖，
 * 但对 {@code @EnableAspectJAutoProxy} 及其底层设施为必需。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see org.aspectj.lang.annotation.Aspect
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AspectJAutoProxyRegistrar.class)
public @interface EnableAspectJAutoProxy {

	/**
	 * 是否创建基于子类的（CGLIB）代理，而非标准 Java 接口代理。默认为 {@code false}。
	 */
	boolean proxyTargetClass() default false;

	/**
	 * 是否由 AOP 框架将代理以 {@code ThreadLocal} 暴露，供
	 * {@link org.springframework.aop.framework.AopContext} 获取。
	 * 默认关闭，即不保证 {@code AopContext} 访问可用。
	 * @since 4.3.1
	 */
	boolean exposeProxy() default false;

}
