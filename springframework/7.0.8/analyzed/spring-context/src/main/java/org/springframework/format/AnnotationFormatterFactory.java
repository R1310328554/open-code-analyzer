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

package org.springframework.format;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * 为带有特定 {@link Annotation} 的字段创建格式化器的工厂。
 *
 * <p>例如，{@code DateTimeFormatAnnotationFormatterFactory} 可创建格式化器，
 * 用于格式化标注了 {@code @DateTimeFormat} 的字段上的 {@code Date} 值。
 *
 * @author Keith Donald
 * @since 3.0
 * @param <A> 应触发格式化的注解类型
 */
public interface AnnotationFormatterFactory<A extends Annotation> {

	/**
	 * 可使用 &lt;A&gt; 注解标注的字段类型集合。
	 */
	Set<Class<?>> getFieldTypes();

	/**
	 * 获取用于打印标注了 {@code annotation}、类型为 {@code fieldType} 的字段值的 {@link Printer}。
	 * <p>若 Printer 接受的类型 T 不可赋值给 {@code fieldType}，则在调用 Printer 之前
	 * 会尝试将 {@code fieldType} 强制转换为 T。
	 * @param annotation 注解实例
	 * @param fieldType 被标注字段的类型
	 * @return 打印器
	 */
	Printer<?> getPrinter(A annotation, Class<?> fieldType);

	/**
	 * 获取用于解析标注了 {@code annotation}、类型为 {@code fieldType} 的字段所提交值的 {@link Parser}。
	 * <p>若 Parser 返回的对象不可赋值给 {@code fieldType}，则在设置字段之前
	 * 会尝试强制转换为 {@code fieldType}。
	 * @param annotation 注解实例
	 * @param fieldType 被标注字段的类型
	 * @return 解析器
	 */
	Parser<?> getParser(A annotation, Class<?> fieldType);

}
