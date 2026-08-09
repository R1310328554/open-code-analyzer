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

/* ===== [OCA 中文解析] =====
文件意图总览

{@link Bean} 注解方法处理工具：判断 Bean 方法、解析 Bean 名称、检测作用域代理。
===== [OCA 中文解析结束] ===== */
package org.springframework.context.annotation;

import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.ConcurrentReferenceHashMap;

/* ===== [OCA 中文解析] =====
class BeanAnnotationHelper — 意图说明

为配置类解析阶段提供 {@link Bean} 方法元数据的缓存与查询辅助。

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * Utilities for processing {@link Bean}-annotated methods.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 */
abstract class BeanAnnotationHelper {

	// [OCA] 字段 `beanNameCache`：方法 → Bean 名称缓存。
	private static final Map<Method, String> beanNameCache = new ConcurrentReferenceHashMap<>();

	// [OCA] 字段 `scopedProxyCache`：方法 → 是否启用作用域代理缓存。
	private static final Map<Method, Boolean> scopedProxyCache = new ConcurrentReferenceHashMap<>();


	public static boolean isBeanAnnotated(Method method) {
		return AnnotatedElementUtils.hasAnnotation(method, Bean.class);
	}

	public static String determineBeanNameFor(Method beanMethod, ConfigurableBeanFactory beanFactory) {
		String beanName = retrieveBeanNameFor(beanMethod);
		if (beanFactory.getSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR)
				instanceof ConfigurationBeanNameGenerator cbng) {
			return cbng.deriveBeanName(MethodMetadata.introspect(beanMethod), (!beanName.isEmpty() ? beanName : null));
		}
		return determineBeanNameFrom(beanName, beanMethod);
	}

	public static String determineBeanNameFor(Method beanMethod) {
		return determineBeanNameFrom(retrieveBeanNameFor(beanMethod), beanMethod);
	}

	private static String retrieveBeanNameFor(Method beanMethod) {
		String beanName = beanNameCache.get(beanMethod);
		if (beanName == null) {
			// [OCA] 默认 Bean 名称为空（表示从方法名派生）。
			beanName = "";
			// [OCA] 检查用户是否显式设置了自定义 Bean 名称。
			AnnotationAttributes bean =
					AnnotatedElementUtils.findMergedAnnotationAttributes(beanMethod, Bean.class, false, false);
			if (bean != null) {
				String[] names = bean.getStringArray("name");
				if (names.length > 0) {
					beanName = names[0];
				}
			}
			beanNameCache.put(beanMethod, beanName);
		}
		return beanName;
	}

	private static String determineBeanNameFrom(String derivedBeanName, Method beanMethod) {
		return (!derivedBeanName.isEmpty() ? derivedBeanName : beanMethod.getName());
	}

	public static boolean isScopedProxy(Method beanMethod) {
		Boolean scopedProxy = scopedProxyCache.get(beanMethod);
		if (scopedProxy == null) {
			AnnotationAttributes scope =
					AnnotatedElementUtils.findMergedAnnotationAttributes(beanMethod, Scope.class, false, false);
			scopedProxy = (scope != null && scope.getEnum("proxyMode") != ScopedProxyMode.NO);
			scopedProxyCache.put(beanMethod, scopedProxy);
		}
		return scopedProxy;
	}

	static void clearCaches() {
		scopedProxyCache.clear();
		beanNameCache.clear();
	}

}
