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

package org.springframework.beans;

/**
 * 策略接口：向 {@link org.springframework.beans.PropertyEditorRegistry 属性编辑器注册表}
 * 注册自定义 {@link java.beans.PropertyEditor 属性编辑器}。
 *
 * <p>当需要在多种场景下复用同一组属性编辑器时特别有用：
 * 编写一个对应的注册器，并在各处复用即可。
 *
 * @author Juergen Hoeller
 * @since 1.2.6
 * @see PropertyEditorRegistry
 * @see java.beans.PropertyEditor
 */
public interface PropertyEditorRegistrar {

	/**
	 * 向给定的 {@code PropertyEditorRegistry} 注册自定义
	 * {@link java.beans.PropertyEditor PropertyEditor}。
	 * <p>传入的注册表通常是 {@link BeanWrapper} 或
	 * {@link org.springframework.validation.DataBinder DataBinder}。
	 * <p>实现类应在每次调用本方法时创建全新的
	 * {@code PropertyEditor} 实例（因为 {@code PropertyEditor} 不是线程安全的）。
	 * @param registry 用于注册自定义 {@code PropertyEditor} 的
	 * {@code PropertyEditorRegistry}
	 */
	void registerCustomEditors(PropertyEditorRegistry registry);

	/**
	 * 表明本注册器是否仅覆盖默认编辑器，而不是注册自定义编辑器，
	 * 并意图被延迟应用。
	 * <p>这会影响 bean 工厂中对注册器的处理，参见
	 * {@link org.springframework.beans.factory.config.ConfigurableBeanFactory#addPropertyEditorRegistrar}。
	 * @return 若仅覆盖默认编辑器则返回 {@code true}；否则返回 {@code false}
	 * @since 6.2.3
	 * @see PropertyEditorRegistry#registerCustomEditor
	 * @see PropertyEditorRegistrySupport#overrideDefaultEditor
	 * @see PropertyEditorRegistrySupport#setDefaultEditorRegistrar
	 */
	default boolean overridesDefaultEditors() {
		return false;
	}

}
