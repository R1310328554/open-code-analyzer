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

import org.springframework.context.weaving.DefaultContextLoadTimeWeaver;
import org.springframework.instrument.classloading.LoadTimeWeaver;

/**
 * 为本应用上下文激活 Spring {@link LoadTimeWeaver}，以 Bean 名 "loadTimeWeaver" 提供，
 * 类似于 Spring XML 中的 {@code <context:load-time-weaver/>} 元素。
 *
 * <p>用于 @{@link org.springframework.context.annotation.Configuration Configuration} 类；
 * 最简示例如下：
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableLoadTimeWeaving
 * public class AppConfig {
 *
 *     // application-specific &#064;Bean definitions ...
 * }</pre>
 *
 * 上述示例等价于以下 Spring XML 配置：
 *
 * <pre class="code">
 * &lt;beans&gt;
 *
 *     &lt;context:load-time-weaver/&gt;
 *
 *     &lt;!-- application-specific &lt;bean&gt; definitions --&gt;
 *
 * &lt;/beans&gt;
 * </pre>
 *
 * <h2>{@code LoadTimeWeaverAware} 接口</h2>
 * 任何实现 {@link
 * org.springframework.context.weaving.LoadTimeWeaverAware LoadTimeWeaverAware} 接口的 Bean
 * 将自动收到 {@code LoadTimeWeaver} 引用；例如 Spring 的 JPA 引导支持。
 *
 * <h2>自定义 {@code LoadTimeWeaver}</h2>
 * 默认织入器自动确定：参见 {@link DefaultContextLoadTimeWeaver}。
 *
 * <p>要自定义所用织入器，带 {@code @EnableLoadTimeWeaving} 的 {@code @Configuration} 类
 * 还可实现 {@link LoadTimeWeavingConfigurer} 接口，通过
 * {@code #getLoadTimeWeaver} 方法返回自定义 {@code LoadTimeWeaver} 实例：
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableLoadTimeWeaving
 * public class AppConfig implements LoadTimeWeavingConfigurer {
 *
 *     &#064;Override
 *     public LoadTimeWeaver getLoadTimeWeaver() {
 *         MyLoadTimeWeaver ltw = new MyLoadTimeWeaver();
 *         ltw.addClassTransformer(myClassFileTransformer);
 *         // ...
 *         return ltw;
 *     }
 * }</pre>
 *
 * <p>上述代码示例可与以下 Spring XML 配置对比：
 *
 * <pre class="code">
 * &lt;beans&gt;
 *
 *     &lt;context:load-time-weaver weaverClass="com.acme.MyLoadTimeWeaver"/&gt;
 *
 * &lt;/beans&gt;
 * </pre>
 *
 * <p>代码示例与 XML 示例的区别在于：代码会实际实例化 {@code MyLoadTimeWeaver} 类型，
 * 因而还可配置该实例，例如调用 {@code #addClassTransformer} 方法。这体现了代码配置
 * 通过直接编程访问而更具灵活性。
 *
 * <h2>启用基于 AspectJ 的织入</h2>
 * 可通过 {@link #aspectjWeaving()} 属性启用 AspectJ 加载时织入，这将通过
 * {@link LoadTimeWeaver#addTransformer} 注册
 * {@linkplain org.aspectj.weaver.loadtime.ClassPreProcessorAgentAdapter AspectJ 类转换器}。
 * 若 classpath 存在 "META-INF/aop.xml" 资源，默认即会激活 AspectJ 织入。示例：
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableLoadTimeWeaving(aspectjWeaving=ENABLED)
 * public class AppConfig {
 * }</pre>
 *
 * <p>上述示例可与以下 Spring XML 配置对比：
 *
 * <pre class="code">
 * &lt;beans&gt;
 *
 *     &lt;context:load-time-weaver aspectj-weaving="on"/&gt;
 *
 * &lt;/beans&gt;
 * </pre>
 *
 * <p>两示例基本等价，但有一处重要差异：XML 情况下，当 {@code aspectj-weaving} 为 "on" 时，
 * {@code <context:spring-configured>} 的功能会隐式启用。使用
 * {@code @EnableLoadTimeWeaving(aspectjWeaving=ENABLED)} 时则不会；须显式添加
 * {@code @EnableSpringConfigured}（位于 {@code spring-aspects} 模块）。
 *
 * @author Chris Beams
 * @since 3.1
 * @see LoadTimeWeaver
 * @see DefaultContextLoadTimeWeaver
 * @see org.aspectj.weaver.loadtime.ClassPreProcessorAgentAdapter
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(LoadTimeWeavingConfiguration.class)
public @interface EnableLoadTimeWeaving {

	/**
	 * 是否启用 AspectJ 织入。
	 */
	AspectJWeaving aspectjWeaving() default AspectJWeaving.AUTODETECT;


	/**
	 * AspectJ 织入启用选项。
	 */
	enum AspectJWeaving {

		/**
		 * 开启基于 Spring 的 AspectJ 加载时织入。
		 */
		ENABLED,

		/**
		 * 关闭基于 Spring 的 AspectJ 加载时织入（即使 classpath 存在 "META-INF/aop.xml"）。
		 */
		DISABLED,

		/**
		 * 若 classpath 存在 "META-INF/aop.xml" 则开启 AspectJ 加载时织入；
		 * 若无此类资源则关闭。
		 */
		AUTODETECT
	}

}
