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

package org.springframework.beans.factory.aot;

import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import org.springframework.core.ResolvableType;
import org.springframework.javapoet.AnnotationSpec;
import org.springframework.javapoet.AnnotationSpec.Builder;
import org.springframework.javapoet.CodeBlock;
import org.springframework.javapoet.FieldSpec;
import org.springframework.javapoet.MethodSpec;
import org.springframework.javapoet.TypeSpec;
import org.springframework.util.ClassUtils;

/**
 * 辅助类，用于登记编译器可能在生成代码上触发的警告。
 *
 * @author Stephane Nicoll
 * @since 6.1
 * @see SuppressWarnings
 */
public class CodeWarnings {

	/** 已登记的警告集合。 */
	private final Set<String> warnings = new LinkedHashSet<>();


	/**
	 * 登记要包含在本代码块中的警告。若警告已登记则不做任何操作。
	 * @param warning 待登记的警告
	 */
	public void register(String warning) {
		this.warnings.add(warning);
	}

	/**
	 * 检测指定元素上是否存在 {@link Deprecated} 注解。
	 * @param elements 待检测的元素
	 * @return 本实例
	 */
	public CodeWarnings detectDeprecation(AnnotatedElement... elements) {
		for (AnnotatedElement element : elements) {
			registerDeprecationIfNecessary(element);
		}
		return this;
	}

	/**
	 * 检测指定元素上是否存在 {@link Deprecated} 注解。
	 * @param elements 待检测的元素流
	 * @return 本实例
	 */
	public CodeWarnings detectDeprecation(Stream<AnnotatedElement> elements) {
		elements.forEach(element -> register(element.getAnnotation(Deprecated.class)));
		return this;
	}

	/**
	 * 检测指定 {@link ResolvableType} 签名上是否存在 {@link Deprecated} 注解。
	 * @param resolvableType 类型签名
	 * @return 本实例
	 * @since 6.1.8
	 */
	public CodeWarnings detectDeprecation(ResolvableType resolvableType) {
		if (ResolvableType.NONE.equals(resolvableType)) {
			return this;
		}
		Class<?> type = ClassUtils.getUserClass(resolvableType.toClass());
		detectDeprecation(type);
		if (resolvableType.hasGenerics() && !resolvableType.hasUnresolvableGenerics()) {
			for (ResolvableType generic : resolvableType.getGenerics()) {
				detectDeprecation(generic);
			}
		}
		return this;
	}

	/**
	 * 如有必要，在指定方法上添加 {@link SuppressWarnings} 注解。
	 * @param method 待更新的方法
	 */
	public void suppress(MethodSpec.Builder method) {
		suppress(annotationBuilder -> method.addAnnotation(annotationBuilder.build()));
	}

	/**
	 * 如有必要，在指定类型上添加 {@link SuppressWarnings} 注解。
	 * @param type 待更新的类型
	 */
	public void suppress(TypeSpec.Builder type) {
		suppress(annotationBuilder -> type.addAnnotation(annotationBuilder.build()));
	}

	/**
	 * 如有必要，消费 {@link SuppressWarnings} 的构建器。
	 * 若本实例未登记任何警告，则不调用消费者。
	 * @param annotationSpec {@link AnnotationSpec.Builder} 的消费者
	 * @see MethodSpec.Builder#addAnnotation(AnnotationSpec)
	 * @see TypeSpec.Builder#addAnnotation(AnnotationSpec)
	 * @see FieldSpec.Builder#addAnnotation(AnnotationSpec)
	 */
	protected void suppress(Consumer<AnnotationSpec.Builder> annotationSpec) {
		if (!this.warnings.isEmpty()) {
			Builder annotation = AnnotationSpec.builder(SuppressWarnings.class)
					.addMember("value", generateValueCode());
			annotationSpec.accept(annotation);
		}
	}

	/**
	 * 返回当前已登记的警告。
	 * @return 警告集合
	 */
	protected Set<String> getWarnings() {
		return Collections.unmodifiableSet(this.warnings);
	}

	private void registerDeprecationIfNecessary(@Nullable AnnotatedElement element) {
		if (element == null) {
			return;
		}
		register(element.getAnnotation(Deprecated.class));
		if (element instanceof Class<?> type) {
			registerDeprecationIfNecessary(type.getEnclosingClass());
		}
	}

	private void register(@Nullable Deprecated annotation) {
		if (annotation != null) {
			if (annotation.forRemoval()) {
				register("removal");
			}
			else {
				register("deprecation");
			}
		}
	}

	private CodeBlock generateValueCode() {
		if (this.warnings.size() == 1) {
			return CodeBlock.of("$S", this.warnings.iterator().next());
		}
		CodeBlock values = CodeBlock.join(this.warnings.stream()
				.map(warning -> CodeBlock.of("$S", warning)).toList(), ", ");
		return CodeBlock.of("{ $L }", values);
	}

	@Override
	public String toString() {
		return CodeWarnings.class.getSimpleName() + this.warnings;
	}

}
