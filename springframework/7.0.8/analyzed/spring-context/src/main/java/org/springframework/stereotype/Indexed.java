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

package org.springframework.stereotype;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表示被注解元素是索引的构造型。
 *
 * <p>{@code CandidateComponentsIndex} 是类路径扫描的替代方案，
 * 使用编译时生成的元数据文件。索引允许基于构造型检索候选组件
 * （即完全限定名）。本注解指示生成器为存在被注解元素的元素建立索引，
 * 或为其所实现/继承的被注解元素建立索引。构造型即被注解元素的完全限定名。
 *
 * <p>考虑默认以本注解元注解的 {@link Component} 注解。
 * 若组件以 {@link Component} 注解，
 * 将使用 {@code org.springframework.stereotype.Component} 构造型
 * 为该组件向索引添加条目。
 *
 * <p>本注解在元注解上同样生效。考虑以下自定义注解：
 * <pre class="code">
 * package com.example;
 *
 * &#064;Target(ElementType.TYPE)
 * &#064;Retention(RetentionPolicy.RUNTIME)
 * &#064;Documented
 * &#064;Indexed
 * &#064;Service
 * public @interface PrivilegedService { ... }
 * </pre>
 *
 * 若上述注解出现在某类型上，将以两种构造型建立索引：
 * {@code org.springframework.stereotype.Component} 和
 * {@code com.example.PrivilegedService}。虽然 {@link Service} 未直接以
 * {@code Indexed} 注解，但它以 {@link Component} 元注解。
 *
 * <p>也可通过在接口或类上添加 {@code @Indexed}
 * 为某接口的所有实现或某类的所有子类建立索引。
 *
 * 考虑以下基接口：
 * <pre class="code">
 * package com.example;
 *
 * &#064;Indexed
 * public interface AdminService { ... }
 * </pre>
 *
 * 再考虑某处 {@code AdminService} 的实现：
 * <pre class="code">
 * package com.example.foo;
 *
 * import com.example.AdminService;
 *
 * public class ConfigurationAdminService implements AdminService { ... }
 * </pre>
 *
 * 由于该类实现了已建立索引的接口，
 * 将自动以 {@code com.example.AdminService} 构造型纳入索引。
 * 若层次结构中还有更多 {@code @Indexed} 接口和/或超类，
 * 该类将映射到所有相关构造型。
 *
 * @author Stephane Nicoll
 * @since 5.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Indexed {
}
