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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;

/**
 * 遵循 JSR-330 作用域规则的简单 {@link ScopeMetadataResolver} 实现：
 * 除非存在 {@link jakarta.inject.Singleton}，否则默认为 prototype 作用域。
 *
 * <p>本作用域解析器可与 {@link ClassPathBeanDefinitionScanner} 和
 * {@link AnnotatedBeanDefinitionReader} 配合以实现标准 JSR-330 合规。
 * 但实践中通常使用 Spring 丰富的默认作用域——或扩展本解析器，
 * 使自定义作用域注解指向扩展的 Spring 作用域。
 *
 * @author Juergen Hoeller
 * @since 3.0
 * @see #registerScope
 * @see #resolveScopeName
 * @see ClassPathBeanDefinitionScanner#setScopeMetadataResolver
 * @see AnnotatedBeanDefinitionReader#setScopeMetadataResolver
 */
public class Jsr330ScopeMetadataResolver implements ScopeMetadataResolver {

	/** JSR-330 作用域注解到 Spring 作用域名称的映射。 */
	private final Map<String, String> scopeMap = new HashMap<>();


	public Jsr330ScopeMetadataResolver() {
		registerScope("jakarta.inject.Singleton", BeanDefinition.SCOPE_SINGLETON);
	}


	/**
	 * 注册扩展的 JSR-330 作用域注解，将其映射到指定的 Spring 作用域名称。
	 * @param annotationType JSR-330 注解类型（Class 形式）
	 * @param scopeName Spring 作用域名称
	 */
	public final void registerScope(Class<?> annotationType, String scopeName) {
		this.scopeMap.put(annotationType.getName(), scopeName);
	}

	/**
	 * 注册扩展的 JSR-330 作用域注解，将其映射到指定的 Spring 作用域名称。
	 * @param annotationType JSR-330 注解类型（名称形式）
	 * @param scopeName Spring 作用域名称
	 */
	public final void registerScope(String annotationType, String scopeName) {
		this.scopeMap.put(annotationType, scopeName);
	}

	/**
	 * 将给定注解类型解析为命名的 Spring 作用域。
	 * <p>默认实现仅对照已注册作用域检查。可覆盖以实现自定义映射规则（例如命名约定）。
	 * @param annotationType JSR-330 注解类型
	 * @return Spring 作用域名称
	 */
	protected @Nullable String resolveScopeName(String annotationType) {
		return this.scopeMap.get(annotationType);
	}


	@Override
	public ScopeMetadata resolveScopeMetadata(BeanDefinition definition) {
		ScopeMetadata metadata = new ScopeMetadata();
		// 默认 prototype 作用域
		metadata.setScopeName(BeanDefinition.SCOPE_PROTOTYPE);
		if (definition instanceof AnnotatedBeanDefinition annDef) {
			Set<String> annTypes = annDef.getMetadata().getAnnotationTypes();
			String found = null;
			for (String annType : annTypes) {
				// 查找元注解为 jakarta.inject.Scope 的作用域注解
				Set<String> metaAnns = annDef.getMetadata().getMetaAnnotationTypes(annType);
				if (metaAnns.contains("jakarta.inject.Scope")) {
					if (found != null) {
						throw new IllegalStateException("Found ambiguous scope annotations on bean class [" +
								definition.getBeanClassName() + "]: " + found + ", " + annType);
					}
					found = annType;
					// 将 JSR-330 作用域注解映射为 Spring 作用域名称
					String scopeName = resolveScopeName(annType);
					if (scopeName == null) {
						throw new IllegalStateException(
								"Unsupported scope annotation - not mapped onto Spring scope name: " + annType);
					}
					metadata.setScopeName(scopeName);
				}
			}
		}
		return metadata;
	}

}
