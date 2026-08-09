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

package org.springframework.jmx.export.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * 类级别注解，指示将某类的实例注册到 JMX 服务器，
 * 对应 {@link org.springframework.jmx.export.metadata.ManagedResource} 元数据属性。
 *
 * <p><b>注意：</b>该注解标记为 {@code @Inherited}，便于编写可管理的通用基类。
 * 在此场景下，建议<i>不要</i>指定 objectName 值，否则多个子类同时注册时可能发生命名冲突。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 1.2
 * @see org.springframework.jmx.export.metadata.ManagedResource
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface ManagedResource {

	/**
	 * {@link #objectName} 属性的别名，便于简写默认用法。
	 */
	@AliasFor("objectName")
	String value() default "";

	@AliasFor("value")
	String objectName() default "";

	/** MBean 描述信息。 */
	String description() default "";

	/** 缓存/刷新时间限制（秒），{@code -1} 表示未指定。 */
	int currencyTimeLimit() default -1;

	/** 是否记录 MBean 调用日志。 */
	boolean log() default false;

	/** 日志文件路径。 */
	String logFile() default "";

	/** 持久化策略。 */
	String persistPolicy() default "";

	/** 持久化周期。 */
	int persistPeriod() default -1;

	/** 持久化名称。 */
	String persistName() default "";

	/** 持久化位置。 */
	String persistLocation() default "";

}
