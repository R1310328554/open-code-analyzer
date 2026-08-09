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
 * 指示 Bean 可作为自动装配的备选候选。
 * 这是 {@link Primary} 注解的配套与替代方案。
 *
 * <p>若多个匹配候选中除一个外均标记为 fallback，则选中剩余的那个 Bean。
 *
 * <p>与 primary Bean 类似，fallback Bean 仅在单注入点存在多个候选时生效。
 * 自动装配数组、集合、Map 或 ObjectProvider 流时，所有类型匹配的 Bean 均会纳入。
 *
 * @author Juergen Hoeller
 * @since 6.2
 * @see Primary
 * @see Lazy
 * @see Bean
 * @see org.springframework.beans.factory.config.BeanDefinition#setFallback
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Fallback {

}
