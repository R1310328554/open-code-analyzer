/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot.context.properties;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记被注解的类型为配置属性元数据的来源。
 * <p>
 * 此注解对实际绑定过程无影响，但会向 {@code spring-boot-configuration-processor}
 * 发出提示，以便为该类型生成完整元数据。
 * <p>
 * 通常，仅当被引用类型与引用它的 {@code @ConfigurationProperties} 类
 * 位于不同模块时才需要此注解。当两者在同一模块且源码可用时，
 * 注解处理器可自动发现完整元数据。
 * <p>
 * 当需要为模块外类型的元数据生成时使用此注解：
 * <ol>
 * <li>由 {@code @NestedConfigurationProperty} 标注的嵌套类型</li>
 * <li>{@code @ConfigurationProperties} 标注类型所继承的基类</li>
 * </ol>
 * <p>
 * 下例中，{@code ServerProperties} 位于模块 "A"，{@code Host} 位于模块 "B"：<pre><code class="java">
 * &#064;ConfigurationProperties("example.server")
 * class ServerProperties {
 *
 *     &#064;NestedConfigurationProperty
 *     private final Host host = new Host();
 *
 *     public Host getHost() { ... }
 *
 *     // Other properties, getter, setter.
 *
 * }</code></pre>
 * <p>
 * 基于类型可检测到 {@code Host} 的属性，但缺少描述与默认值。
 * 为此，若模块 "B" 尚未引入 {@code spring-boot-configuration-processor}，
 * 请添加该依赖并按如下方式更新 {@code Host}：<pre><code class="java">
 * &#064;ConfigurationPropertiesSource
 * class Host {
 *
 *     /**
 *      * 要使用的 URL。
 *      *&#47;
 *     private String url = "https://example.com";
 *
 *     // Other properties, getter, setter.
 *
 * }</code></pre>
 * <p>
 * 类似地，{@code @ConfigurationProperties} 标注类型所继承的基类元数据也可被检测。
 * 示例：<pre><code class="java">
 * &#064;ConfigurationProperties("example.client.github")
 * class GitHubClientProperties extends AbstractClientProperties {
 *
 *     // Additional properties, getter, setter.
 *
 * }</code></pre>
 * <p>
 * 与嵌套类型相同，在 {@code AbstractClientProperties} 上添加
 * {@code @ConfigurationPropertiesSource}，并在其模块中引入
 * {@code spring-boot-configuration-processor}，即可确保生成完整元数据。
 *
 * @author Stephane Nicoll
 * @since 4.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationPropertiesSource {

}
