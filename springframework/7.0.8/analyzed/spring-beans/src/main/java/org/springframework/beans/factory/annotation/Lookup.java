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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记「查找（lookup）」方法：容器将覆盖这些方法，把调用重定向回
 * {@link org.springframework.beans.factory.BeanFactory} 的 {@code getBean}。
 * 本质上是 XML {@code lookup-method} 属性的注解版，运行时安排相同。
 *
 * <p>目标 Bean 可按返回类型解析（{@code getBean(Class)}），也可按建议的 Bean 名称解析
 * （{@code getBean(String)}）；两种情况下都会把方法参数传给 {@code getBean}，
 * 作为目标工厂方法参数或构造器参数。
 *
 * <p>这类查找方法可以有默认（桩）实现，运行时会被容器替换；也可以声明为 abstract，
 * 由容器在运行时填充。无论哪种方式，容器都会通过 CGLIB 为目标方法所在类生成运行时子类，
 * 因此查找方法只适用于容器通过常规构造器实例化的 Bean：
 * 从工厂方法返回的 Bean 无法被动态提供子类，查找方法也就无法被替换。
 *
 * <p><b>典型 Spring 配置场景的建议：</b>
 * 若某些场景仍需要具体类，可为查找方法提供桩实现。另请记住：配置类中
 * {@code @Bean} 方法返回的 Bean 上，查找方法无效；
 * 此时应改用 {@code @Inject Provider<TargetBean>} 等方式。
 *
 * @author Juergen Hoeller
 * @since 4.1
 * @see org.springframework.beans.factory.BeanFactory#getBean(Class, Object...)
 * @see org.springframework.beans.factory.BeanFactory#getBean(String, Object...)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Lookup {

	/**
	 * 可建议要查找的目标 Bean 名称。
	 * 未指定时，将根据被注解方法的返回类型声明解析目标 Bean。
	 */
	String value() default "";

}
