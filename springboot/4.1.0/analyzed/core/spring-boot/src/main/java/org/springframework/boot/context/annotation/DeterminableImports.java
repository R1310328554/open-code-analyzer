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

package org.springframework.boot.context.annotation;

import java.util.Set;

import org.springframework.beans.factory.Aware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 当 {@link ImportSelector} 与 {@link ImportBeanDefinitionRegistrar} 实现可提前确定导入内容时可实现的接口。
 * 上述接口较灵活，难以精确预知将添加的 Bean 定义；若给定相同源时导入结果一致，应使用本接口。
 * <p>
 * 配合 Spring 测试支持时尤其有用，可更好地生成 {@link ApplicationContext} 缓存键。
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @since 1.5.0
 */
@FunctionalInterface
public interface DeterminableImports {

	/**
	 * 返回表示导入内容的对象集合；集合内对象须实现有效的
	 * {@link Object#hashCode() hashCode} 与 {@link Object#equals(Object) equals}。
	 * <p>
	 * 调用方可合并多个 {@link DeterminableImports} 实例的导入以构成完整集合。
	 * <p>
	 * 与 {@link ImportSelector}、{@link ImportBeanDefinitionRegistrar} 不同，
	 * 调用本方法前不会触发任何 {@link Aware} 回调。
	 *
	 * @param metadata 源元数据
	 * @return 代表实际驱动导入的注解的键
	 */
	Set<Object> determineImports(AnnotationMetadata metadata);

}
