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

package org.springframework.boot.context.properties.bind;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于指定绑定属性时使用的名称的注解。绑定与语言保留关键字冲突的名称时可能需要此注解。
 * <p>
 * 命名基于 JavaBean 的属性时，标注在字段上；命名构造器绑定的属性时，标注在构造器参数或 record 组件上。
 *
 * @author Phillip Webb
 * @author Lasse Wulff
 * @since 2.4.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Documented
public @interface Name {

	/**
	 * 绑定时要使用的属性名。
	 *
	 * @return 属性名
	 */
	String value();

}
