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

package org.springframework.scheduling.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.aot.hint.annotation.Reflective;

/**
 * 聚合多个 {@link Scheduled} 注解的容器注解。
 *
 * <p>可原生使用，声明多个嵌套 {@link Scheduled} 注解。
 * 也可与 Java 可重复注解支持配合：在同一方法上多次声明
 * {@link Scheduled @Scheduled}，将隐式生成本容器注解。
 *
 * <p>本注解可作为<em>元注解</em>创建自定义<em>组合注解</em>。
 *
 * @author Juergen Hoeller
 * @since 4.0
 * @see Scheduled
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Reflective
public @interface Schedules {

	Scheduled[] value();

}
