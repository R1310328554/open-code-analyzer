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

import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.aot.hint.annotation.RegisterReflection;
import org.springframework.core.annotation.AliasFor;

/**
 * 扫描任意类型中对 {@link Reflective} 的使用。通常用于
 * {@link Configuration @Configuration} 类，也可添加到任意 Bean。
 * 扫描在 AOT 处理期间进行，通常在构建时执行。
 *
 * <p>下例扫描 {@code com.example.app} 及其子包：<pre><code class="java">
 * &#064;Configuration
 * &#064;ReflectiveScan("com.example.app")
 * class MyConfiguration {
 *     // ...
 * }</code></pre>
 *
 * <p>可通过 {@link #basePackageClasses} 或 {@link #basePackages}
 * （及其别名 {@link #value}）指定要扫描的包。若未定义具体包，则从声明本注解的类所在包开始递归扫描。
 *
 * <p>类型无需在类级别标注即可成为候选；本注解会对目标包中的每个类执行「深度扫描」，
 * 在类型、构造器、方法与字段上查找 {@link Reflective}。内部类也是候选。加载失败的类会被忽略。
 *
 * @author Stephane Nicoll
 * @since 6.2
 * @see Reflective @Reflective
 * @see RegisterReflection @RegisterReflection
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface ReflectiveScan {

	/**
	 * {@link #basePackages} 的别名。
	 * <p>若无需其他属性，可更简洁地声明，例如
	 * {@code @ReflectiveScan("org.my.pkg")} 而非
	 * {@code @ReflectiveScan(basePackages = "org.my.pkg")}。
	 */
	@AliasFor("basePackages")
	String[] value() default {};

	/**
	 * 要扫描反射用法的基包。
	 * <p>{@link #value} 是本属性的别名（且与之互斥）。
	 * <p>类型安全的包名替代方案请使用 {@link #basePackageClasses}。
	 */
	@AliasFor("value")
	String[] basePackages() default {};

	/**
	 * {@link #basePackages} 的类型安全替代：指定若干类，将扫描各自所在包。
	 * <p>可在每个包中创建一个仅用于被本属性引用的无操作标记类或接口。
	 */
	Class<?>[] basePackageClasses() default {};

}
