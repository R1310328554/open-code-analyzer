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

package org.springframework.boot.autoconfigure;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 启用 Spring 应用上下文的自动配置，尝试猜测并配置你可能需要的 bean。
 * 自动配置类通常根据类路径和已定义的 bean 来决定是否应用。
 * <p>
 * 使用 {@link SpringBootApplication @SpringBootApplication} 时，上下文自动配置
 * 已默认启用，再添加本注解不会产生额外效果。
 * <p>
 * 自动配置会尽可能智能，并在你定义更多自定义配置时自动退让。
 * 你可以随时通过 {@link #exclude()} 手动排除永远不想应用的配置
 *（若无法访问相关类，可使用 {@link #excludeName()}）。
 * 也可通过 {@code spring.autoconfigure.exclude} 属性排除。
 * 自动配置始终在用户定义的 bean 注册完成之后应用。
 * <p>
 * 标注 {@code @EnableAutoConfiguration} 的类（通常通过 {@code @SpringBootApplication}）
 * 所在包具有特殊意义，常作为"默认"包使用，例如在扫描 {@code @Entity} 类时。
 * 一般建议将 {@code @EnableAutoConfiguration}
 *（若未使用 {@code @SpringBootApplication}）放在根包中，以便搜索所有子包和类。
 * <p>
 * 自动配置类是普通的 Spring {@link Configuration @Configuration} bean，
 * 通过 {@link ImportCandidates} 被发现。通常它们是
 * {@link Conditional @Conditional} bean（最常见的是使用
 * {@link ConditionalOnClass @ConditionalOnClass} 和
 * {@link ConditionalOnMissingBean @ConditionalOnMissingBean}）。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 1.0.0
 * @see ConditionalOnBean
 * @see ConditionalOnMissingBean
 * @see ConditionalOnClass
 * @see AutoConfigureAfter
 * @see SpringBootApplication
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@Import(AutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration {

	/**
	 * 可用于覆盖是否启用自动配置的环境属性。
	 */
	String ENABLED_OVERRIDE_PROPERTY = "spring.boot.enableautoconfiguration";

	/**
	 * 排除指定的自动配置类，使其永远不会被应用。
	 * <p>
	 * 由于本注解通过加载类字节码解析，在此指定最终可能不在类路径上的类是安全的，
	 * 但前提是注解直接标注在受影响的组件上，<b>而非</b>作为组合/元注解使用。
	 * 若要将本注解用作元注解，请仅使用 {@link #excludeName()} 属性。
	 * @return 要排除的类
	 */
	Class<?>[] exclude() default {};

	/**
	 * 排除指定的自动配置类名，使其永远不会被应用。
	 * @return 要排除的类名
	 * @since 1.3.0
	 */
	String[] excludeName() default {};

}
