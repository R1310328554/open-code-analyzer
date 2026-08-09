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

import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

/**
 * 导入并应用指定的自动配置类。应用与 {@code @EnableAutoConfiguration} 相同的排序规则，
 * 但将自动配置类限制为指定集合，而非查询 {@link ImportCandidates}。
 * <p>
 * 也可用于 {@link #exclude()} 排除特定自动配置类，使其永远不会被应用。
 * <p>
 * 通常应优先使用 {@code @EnableAutoConfiguration}；
 * 但在某些场景（尤其是编写测试时）{@code @ImportAutoConfiguration} 很有用。
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @since 1.3.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(ImportAutoConfigurationImportSelector.class)
public @interface ImportAutoConfiguration {

	/**
	 * 要导入的自动配置类，是 {@link #classes()} 的别名。
	 * @return 要导入的类
	 */
	@AliasFor("classes")
	Class<?>[] value() default {};

	/**
	 * 要导入的自动配置类。为空时，通过 {@code META-INF/spring} 下的文件指定，
	 * 文件名为被注解类的全限定名加 {@code .imports} 后缀。
	 * 文件中的条目可加 {@code optional:} 前缀，表示若类不在类路径上则忽略。
	 * @return 要导入的类
	 */
	@AliasFor("value")
	Class<?>[] classes() default {};

	/**
	 * 排除指定的自动配置类，使其永远不会被应用。
	 * @return 要排除的类
	 */
	Class<?>[] exclude() default {};

}
