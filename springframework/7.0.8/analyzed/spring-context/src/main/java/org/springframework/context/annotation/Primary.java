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
 * 当多个候选 Bean 均可自动装配到单值依赖时，指示应优先选用该 Bean。
 * 若候选中恰好存在一个 primary Bean，则它将成为自动装配的值。
 *
 * <p>Primary 仅在单注入点存在多个候选时生效。自动装配数组、集合、Map
 * 或 {@code ObjectProvider} 流时，会包含所有类型匹配的 Bean。
 *
 * <p>本注解在语义上等价于 Spring XML 中 {@code <bean>} 元素的 {@code primary} 属性。
 *
 * <p>可用于直接或间接标注了 {@code @Component} 的任意类，或标注了 @{@link Bean} 的方法。
 *
 * <h2>示例</h2>
 * <pre class="code">
 * &#064;Component
 * public class FooService {
 *
 *     private FooRepository fooRepository;
 *
 *     &#064;Autowired
 *     public FooService(FooRepository fooRepository) {
 *         this.fooRepository = fooRepository;
 *     }
 * }
 *
 * &#064;Component
 * public class JdbcFooRepository extends FooRepository {
 *
 *     public JdbcFooRepository(DataSource dataSource) {
 *         // ...
 *     }
 * }
 *
 * &#064;Primary
 * &#064;Component
 * public class HibernateFooRepository extends FooRepository {
 *
 *     public HibernateFooRepository(SessionFactory sessionFactory) {
 *         // ...
 *     }
 * }
 * </pre>
 *
 * <p>由于 {@code HibernateFooRepository} 标注了 {@code @Primary}，在同一 Spring 应用上下文中
 * 若两者均注册为 Bean（常见于广泛组件扫描），将优先注入它而非基于 JDBC 的实现。
 *
 * <p>注意：类级别的 {@code @Primary} 仅在启用组件扫描时才有意义。若通过 XML 声明
 * {@code @Primary} 标注的类，注解元数据会被忽略，应使用
 * {@code <bean primary="true|false"/>}。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.0
 * @see Fallback
 * @see Lazy
 * @see Bean
 * @see ComponentScan
 * @see org.springframework.stereotype.Component
 * @see org.springframework.beans.factory.config.BeanDefinition#setPrimary
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Primary {

}
