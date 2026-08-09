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

import org.springframework.core.convert.converter.ConverterRegistry;

/**
 * 字段格式化逻辑的注册表。
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @since 3.0
 */
public interface FormatterRegistry extends ConverterRegistry {

	/**
	 * 添加用于打印特定类型字段的 Printer。
	 * 字段类型由参数化的 Printer 实例隐含指定。
	 * @param printer 要添加的打印器
	 * @since 5.2
	 * @see #addFormatter(Formatter)
	 */
	void addPrinter(Printer<?> printer);

	/**
	 * 添加用于解析特定类型字段的 Parser。
	 * 字段类型由参数化的 Parser 实例隐含指定。
	 * @param parser 要添加的解析器
	 * @since 5.2
	 * @see #addFormatter(Formatter)
	 */
	void addParser(Parser<?> parser);

	/**
	 * 添加用于格式化特定类型字段的 Formatter。
	 * 字段类型由参数化的 Formatter 实例隐含指定。
	 * @param formatter 要添加的格式化器
	 * @since 3.1
	 * @see #addFormatterForFieldType(Class, Formatter)
	 */
	void addFormatter(Formatter<?> formatter);

	/**
	 * 添加用于格式化给定类型字段的 Formatter。
	 * <p>打印时，若 Formatter 的类型 T 已声明且 {@code fieldType} 不可赋值给 T，
	 * 则在委托 {@code formatter} 打印字段值之前会尝试强制转换为 T。
	 * 解析时，若 {@code formatter} 返回的已解析对象不可赋值给运行时字段类型，
	 * 则在返回已解析字段值之前会尝试强制转换为字段类型。
	 * @param fieldType 要格式化的字段类型
	 * @param formatter 要添加的格式化器
	 */
	void addFormatterForFieldType(Class<?> fieldType, Formatter<?> formatter);

	/**
	 * 添加 Printer/Parser 对，用于格式化特定类型的字段。
	 * 该格式化器在打印时委托给指定的 {@code printer}，在解析时委托给指定的 {@code parser}。
	 * <p>打印时，若 Printer 的类型 T 已声明且 {@code fieldType} 不可赋值给 T，
	 * 则在委托 {@code printer} 打印字段值之前会尝试强制转换为 T。
	 * 解析时，若 Parser 返回的对象不可赋值给运行时字段类型，
	 * 则在返回已解析字段值之前会尝试强制转换为字段类型。
	 * @param fieldType 要格式化的字段类型
	 * @param printer 格式化器的打印部分
	 * @param parser 格式化器的解析部分
	 */
	void addFormatterForFieldType(Class<?> fieldType, Printer<?> printer, Parser<?> parser);

	/**
	 * 添加用于格式化带有特定格式注解的字段的 Formatter。
	 * @param annotationFormatterFactory 要添加的注解格式化器工厂
	 */
	void addFormatterForFieldAnnotation(AnnotationFormatterFactory<? extends Annotation> annotationFormatterFactory);

}
