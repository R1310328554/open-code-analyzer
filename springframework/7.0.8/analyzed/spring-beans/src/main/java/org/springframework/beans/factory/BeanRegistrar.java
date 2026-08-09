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

import org.springframework.core.env.Environment;

/**
 * 以编程方式注册 bean 的契约，通常通过在
 * {@link org.springframework.context.annotation.Configuration @Configuration}
 * 类上使用 {@link org.springframework.context.annotation.Import @Import} 导入。
 * <pre class="code">
 * &#064;Configuration
 * &#064;Import(MyBeanRegistrar.class)
 * class MyConfiguration {
 * }</pre>
 * 也可通过
 * {@link org.springframework.context.support.GenericApplicationContext#register(BeanRegistrar...)}
 * 应用到应用上下文。
 *
 *
 * <p>Bean registrar 实现借助 {@link BeanRegistry} 与 {@link Environment}
 * API，以简洁灵活的方式编程注册 bean。
 * <pre class="code">
 * class MyBeanRegistrar implements BeanRegistrar {
 *
 *     &#064;Override
 *     public void register(BeanRegistry registry, Environment env) {
 *         registry.registerBean("foo", Foo.class);
 *         registry.registerBean("bar", Bar.class, spec -> spec
 *                 .prototype()
 *                 .lazyInit()
 *                 .description("Custom description")
 *                 .supplier(context -> new Bar(context.bean(Foo.class))));
 *         if (env.matchesProfiles("baz")) {
 *             registry.registerBean(Baz.class, spec -> spec
 *                     .supplier(context -> new Baz("Hello World!")));
 *         }
 *     }
 * }</pre>
 *
 * <p>实现了 {@link org.springframework.context.annotation.ImportAware} 的
 * {@code BeanRegistrar} 可在导入场景下按需检查导入元数据；
 * 否则不会调用 {@code setImportMetadata} 方法。
 *
 * <p>在 Kotlin 中，建议使用 {@code BeanRegistrarDsl}，而不是实现 {@code BeanRegistrar}。
 *
 * @author Sebastien Deleuze
 * @since 7.0
 */
@FunctionalInterface
public interface BeanRegistrar {

	/**
	 * 在给定的 {@link BeanRegistry} 上以编程方式注册 bean。
	 * @param registry 要操作的 bean 注册表
	 * @param env 可用于获取激活 profile 或某些属性的环境
	 */
	void register(BeanRegistry registry, Environment env);

}
