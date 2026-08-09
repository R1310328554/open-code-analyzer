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

import org.springframework.boot.context.properties.bind.Nested;

/**
 * 标记 {@link ConfigurationProperties @ConfigurationProperties} 对象中的属性
 * 应视为嵌套类型。此注解不影响实际绑定过程，但供
 * {@code spring-boot-configuration-processor} 提示该属性并非作为单个值绑定。
 * 指定后会为该属性创建嵌套组并收集其类型信息。
 * <p>
 * 下例中，{@code Host} 通过字段标记为嵌套属性，并为 {@code Host} 定义的属性
 * 创建 {@code example.server.host} 嵌套组：<pre><code class="java">
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
 * 也可标注在 getter 方法上；使用 record 时可标注 record 组件。
 * <p>
 * 对集合与 Map 无效，这些类型会自动识别。若目标类型是
 * {@link ConfigurationProperties @ConfigurationProperties} 对象的内部类，
 * 则无需此注解。下例中 {@code Host} 作为内部类定义，会被自动识别为嵌套类型：
 * <pre><code class="java">
 * &#064;ConfigurationProperties("example.server")
 * class ServerProperties {
 *
 *     private final Host host = new Host();
 *
 *     public Host getHost() { ... }
 *
 *     // Other properties, getter, setter.
 *
 *     public static class Host {
 *
 *         // properties, getter, setter.
 *
 *     }
 *
 * }</code></pre>
 *
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @author Jared Bates
 * @since 1.2.0
 */
@Target({ ElementType.FIELD, ElementType.RECORD_COMPONENT, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Nested
public @interface NestedConfigurationProperty {

}
