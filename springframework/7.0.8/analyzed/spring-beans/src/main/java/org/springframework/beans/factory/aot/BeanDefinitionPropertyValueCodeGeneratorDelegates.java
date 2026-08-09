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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jspecify.annotations.Nullable;

import org.springframework.aot.generate.GeneratedMethod;
import org.springframework.aot.generate.GeneratedMethods;
import org.springframework.aot.generate.ValueCodeGenerator;
import org.springframework.aot.generate.ValueCodeGenerator.Delegate;
import org.springframework.aot.generate.ValueCodeGeneratorDelegates;
import org.springframework.aot.generate.ValueCodeGeneratorDelegates.CollectionDelegate;
import org.springframework.aot.generate.ValueCodeGeneratorDelegates.MapDelegate;
import org.springframework.beans.factory.config.AutowiredPropertyMarker;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.javapoet.AnnotationSpec;
import org.springframework.javapoet.CodeBlock;

/**
 * 针对常见 bean 定义属性值的代码生成器 {@link Delegate}。
 *
 * @author Stephane Nicoll
 * @since 6.1.2
 */
public abstract class BeanDefinitionPropertyValueCodeGeneratorDelegates {

	/**
	 * 针对以下常见 bean 定义属性值类型的 {@link Delegate} 实现列表：
	 * <ul>
	 * <li>{@link ManagedList}</li>
	 * <li>{@link ManagedSet}</li>
	 * <li>{@link ManagedMap}</li>
	 * <li>{@link LinkedHashMap}</li>
	 * <li>{@link BeanReference}</li>
	 * <li>{@link TypedStringValue}</li>
	 * <li>{@link AutowiredPropertyMarker}</li>
	 * </ul>
	 * 与 {@linkplain ValueCodeGeneratorDelegates#INSTANCES 常见值类型的委托} 组合使用时，
	 * 应优先添加本列表，因为它们对 list、set 和 map 有特殊处理。
	 */
	public static final List<Delegate> INSTANCES = List.of(
			new ManagedListDelegate(),
			new ManagedSetDelegate(),
			new ManagedMapDelegate(),
			new LinkedHashMapDelegate(),
			new BeanReferenceDelegate(),
			new TypedStringValueDelegate(),
			new AutowiredPropertyMarkerDelegate()
	);


	/**
	 * 创建同时包含本 {@link #INSTANCES 委托} 和
	 * {@link ValueCodeGeneratorDelegates#INSTANCES 核心委托} 的
	 * {@link ValueCodeGenerator} 实例。
	 * @param generatedMethods 使用的 {@link GeneratedMethods}
	 * @param customDelegates 应优先考虑的额外委托
	 * @return 配置好的值代码生成器
	 * @since 7.0
	 * @see ValueCodeGenerator#add(List)
	 */
	public static ValueCodeGenerator createValueCodeGenerator(
			GeneratedMethods generatedMethods, List<Delegate> customDelegates) {
		List<Delegate> allDelegates = new ArrayList<>();
		allDelegates.addAll(customDelegates);
		allDelegates.addAll(INSTANCES);
		allDelegates.addAll(ValueCodeGeneratorDelegates.INSTANCES);
		return ValueCodeGenerator.with(allDelegates).scoped(generatedMethods);
	}


	/**
	 * {@link ManagedList} 类型的 {@link Delegate}。
	 */
	private static class ManagedListDelegate extends CollectionDelegate<ManagedList<?>> {

		public ManagedListDelegate() {
			super(ManagedList.class, CodeBlock.of("new $T()", ManagedList.class));
		}
	}


	/**
	 * {@link ManagedSet} 类型的 {@link Delegate}。
	 */
	private static class ManagedSetDelegate extends CollectionDelegate<ManagedSet<?>> {

		public ManagedSetDelegate() {
			super(ManagedSet.class, CodeBlock.of("new $T()", ManagedSet.class));
		}
	}


	/**
	 * {@link ManagedMap} 类型的 {@link Delegate}。
	 */
	private static class ManagedMapDelegate implements Delegate {

		private static final CodeBlock EMPTY_RESULT = CodeBlock.of("$T.ofEntries()", ManagedMap.class);

		@Override
		public @Nullable CodeBlock generateCode(ValueCodeGenerator valueCodeGenerator, Object value) {
			if (value instanceof ManagedMap<?, ?> managedMap) {
				return generateManagedMapCode(valueCodeGenerator, managedMap);
			}
			return null;
		}

		private <K, V> CodeBlock generateManagedMapCode(ValueCodeGenerator valueCodeGenerator,
				ManagedMap<K, V> managedMap) {
			if (managedMap.isEmpty()) {
				return EMPTY_RESULT;
			}
			CodeBlock.Builder code = CodeBlock.builder();
			code.add("$T.ofEntries(", ManagedMap.class);
			Iterator<Entry<K, V>> iterator = managedMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<?, ?> entry = iterator.next();
				code.add("$T.entry($L,$L)", Map.class,
						valueCodeGenerator.generateCode(entry.getKey()),
						valueCodeGenerator.generateCode(entry.getValue()));
				if (iterator.hasNext()) {
					code.add(", ");
				}
			}
			code.add(")");
			return code.build();
		}
	}


	/**
	 * {@link Map} 类型的 {@link Delegate}。
	 */
	private static class LinkedHashMapDelegate extends MapDelegate {

		@Override
		protected @Nullable CodeBlock generateMapCode(ValueCodeGenerator valueCodeGenerator, Map<?, ?> map) {
			GeneratedMethods generatedMethods = valueCodeGenerator.getGeneratedMethods();
			if (map instanceof LinkedHashMap<?, ?> && generatedMethods != null) {
				return generateLinkedHashMapCode(valueCodeGenerator, generatedMethods, map);
			}
			return super.generateMapCode(valueCodeGenerator, map);
		}

		private CodeBlock generateLinkedHashMapCode(ValueCodeGenerator valueCodeGenerator,
				GeneratedMethods generatedMethods, Map<?, ?> map) {

			GeneratedMethod generatedMethod = generatedMethods.add("getMap", method -> {
				method.addAnnotation(AnnotationSpec
						.builder(SuppressWarnings.class)
						.addMember("value", "{\"rawtypes\", \"unchecked\"}")
						.build());
				method.addModifiers(javax.lang.model.element.Modifier.PRIVATE,
						javax.lang.model.element.Modifier.STATIC);
				method.returns(Map.class);
				method.addStatement("$T map = new $T($L)", Map.class,
						LinkedHashMap.class, map.size());
				map.forEach((key, value) -> method.addStatement("map.put($L, $L)",
						valueCodeGenerator.generateCode(key),
						valueCodeGenerator.generateCode(value)));
				method.addStatement("return map");
			});
			return CodeBlock.of("$L()", generatedMethod.getName());
		}
	}


	/**
	 * {@link BeanReference} 类型的 {@link Delegate}。
	 */
	private static class BeanReferenceDelegate implements Delegate {

		@Override
		public @Nullable CodeBlock generateCode(ValueCodeGenerator valueCodeGenerator, Object value) {
			if (value instanceof RuntimeBeanReference runtimeBeanReference &&
					runtimeBeanReference.getBeanType() != null) {
				return CodeBlock.of("new $T($S, $T.class)", RuntimeBeanReference.class,
						runtimeBeanReference.getBeanName(), runtimeBeanReference.getBeanType());
			}
			else if (value instanceof BeanReference beanReference) {
				return CodeBlock.of("new $T($S)", RuntimeBeanReference.class,
						beanReference.getBeanName());
			}
			return null;
		}
	}


	/**
	 * {@link TypedStringValue} 类型的 {@link Delegate}。
	 */
	private static class TypedStringValueDelegate implements Delegate {

		@Override
		public @Nullable CodeBlock generateCode(ValueCodeGenerator valueCodeGenerator, Object value) {
			if (value instanceof TypedStringValue typedStringValue) {
				return generateTypeStringValueCode(valueCodeGenerator, typedStringValue);
			}
			return null;
		}

		private CodeBlock generateTypeStringValueCode(ValueCodeGenerator valueCodeGenerator, TypedStringValue typedStringValue) {
			String value = typedStringValue.getValue();
			if (typedStringValue.hasTargetType()) {
				return CodeBlock.of("new $T($S, $L)", TypedStringValue.class, value,
						valueCodeGenerator.generateCode(typedStringValue.getTargetType()));
			}
			return valueCodeGenerator.generateCode(value);
		}
	}

	/**
	 * {@link AutowiredPropertyMarker} 类型的 {@link Delegate}。
	 */
	private static class AutowiredPropertyMarkerDelegate implements Delegate {

		@Override
		public @Nullable CodeBlock generateCode(ValueCodeGenerator valueCodeGenerator, Object value) {
			if (value instanceof AutowiredPropertyMarker) {
				return CodeBlock.of("$T.INSTANCE", AutowiredPropertyMarker.class);
			}
			return null;
		}
	}

}
