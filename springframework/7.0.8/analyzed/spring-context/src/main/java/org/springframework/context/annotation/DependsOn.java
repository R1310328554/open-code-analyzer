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
 * 当前 Bean 所依赖的其他 Bean。指定的 Bean 保证由容器在本 Bean 之前创建。
 * 用于 Bean 未通过属性或构造参数显式依赖另一 Bean、而是依赖另一 Bean 初始化副作用的少见场景。
 *
 * <p>depends-on 声明可同时指定初始化时依赖；对于单例 Bean，还可指定对应的销毁时依赖。
 * 与给定 Bean 存在 depends-on 关系的依赖 Bean 会先被销毁，随后才销毁给定 Bean 本身。
 * 因此 depends-on 也可控制关闭顺序。
 *
 * <p>可用于直接或间接标注 {@link org.springframework.stereotype.Component} 的任意类，
 * 或标注 {@link Bean} 的方法。
 *
 * <p>在类级别使用 {@link DependsOn} 除非启用组件扫描则无效果。若通过 XML 声明带
 * {@link DependsOn} 的类，注解元数据会被忽略，转而遵循 {@code <bean depends-on="..."/>}。
 *
 * @author Juergen Hoeller
 * @since 3.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DependsOn {

	String[] value() default {};

}
