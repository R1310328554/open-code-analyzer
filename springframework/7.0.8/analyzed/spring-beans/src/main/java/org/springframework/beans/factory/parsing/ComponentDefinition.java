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

package org.springframework.beans.factory.parsing;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;

/**
 * 描述在某一配置上下文中呈现的一组 {@link BeanDefinition} 与
 * {@link BeanReference} 的逻辑视图的接口。
 *
 * <p>随着可插拔自定义 XML 标签 {@link org.springframework.beans.factory.xml.NamespaceHandler}
 * 的引入，单个逻辑配置实体（例如一个 XML 标签）现在可以创建多个
 * {@link BeanDefinition} 和 {@link BeanReference}，以提供更简洁的配置和
 * 更好的用户体验。因此，不能再假定每个配置实体（例如 XML 标签）对应一个
 * {@link BeanDefinition}。对于希望呈现可视化或支持配置 Spring 应用的工具厂商
 * 及其他用户，需要有某种机制将 {@link org.springframework.beans.factory.BeanFactory}
 * 中的 {@link BeanDefinition} 与对用户具有具体含义的配置数据关联起来。
 * 因此，{@link org.springframework.beans.factory.xml.NamespaceHandler} 实现
 * 能够为每个正在配置的逻辑实体以 {@code ComponentDefinition} 形式发布事件。
 * 第三方可以 {@link ReaderEventListener 订阅这些事件}，从而获得面向用户的
 * Bean 元数据视图。
 *
 * <p>每个 {@code ComponentDefinition} 都有一个配置特定的 {@link #getSource 来源对象}。
 * 对于基于 XML 的配置，这通常是包含用户所提供配置信息的 {@link org.w3c.dom.Node}。
 * 此外，封装在 {@code ComponentDefinition} 中的每个 {@link BeanDefinition} 还有
 * 自己的 {@link BeanDefinition#getSource() 来源对象}，可能指向更具体的配置数据。
 * 更进一步，Bean 元数据的各个部分（例如 {@link org.springframework.beans.PropertyValue}
 *）也可能有来源对象以提供更细粒度的信息。来源对象提取由可按需定制的
 * {@link SourceExtractor} 处理。
 *
 * <p>虽然可通过 {@link #getBeanReferences} 直接访问重要的 {@link BeanReference}，
 * 工具可能希望检查所有 {@link BeanDefinition} 以收集完整的
 * {@link BeanReference} 集合。实现必须提供验证整体逻辑实体配置所需的全部
 * {@link BeanReference}，以及提供完整用户可视化所需的引用。某些
 * {@link BeanReference} 对验证或用户视图并不重要，因此可以省略。工具可能希望
 * 显示通过所提供的 {@link BeanDefinition} 获取的其他 {@link BeanReference}，
 * 但这并非典型情况。
 *
 * <p>工具可通过检查 {@link BeanDefinition#getRole 角色标识} 判断所包含
 * {@link BeanDefinition} 的重要性。角色本质上是配置提供方认为某个
 * {@link BeanDefinition} 对最终用户重要程度的提示。预期工具<strong>不会</strong>
 * 显示给定 {@code ComponentDefinition} 的全部 {@link BeanDefinition}，而是
 * 根据角色进行过滤。工具可让用户配置此过滤行为。应特别注意
 * {@link BeanDefinition#ROLE_INFRASTRUCTURE INFRASTRUCTURE 角色标识}。具有此角色的
 * {@link BeanDefinition} 对最终用户完全不重要，仅用于内部实现目的。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see AbstractComponentDefinition
 * @see CompositeComponentDefinition
 * @see BeanComponentDefinition
 * @see ReaderEventListener#componentRegistered(ComponentDefinition)
 */
public interface ComponentDefinition extends BeanMetadataElement {

	/**
	 * 获取本 {@code ComponentDefinition} 的用户可见名称。
	 * <p>应能直接关联到给定上下文中该组件的对应配置数据。
	 */
	String getName();

	/**
	 * 返回所描述组件的友好描述。
	 * <p>鼓励实现从 {@code toString()} 返回相同值。
	 */
	String getDescription();

	/**
	 * 返回为构成本 {@code ComponentDefinition} 而注册的 {@link BeanDefinition}。
	 * <p>需注意 {@code ComponentDefinition} 可能通过 {@link BeanReference 引用}
	 * 与其他 {@link BeanDefinition} 关联，但这些<strong>不会</strong>包含在内，
	 * 因为它们可能尚不可用。重要的 {@link BeanReference} 可从
	 * {@link #getBeanReferences()} 获取。
	 * @return BeanDefinition 数组，若无则返回空数组
	 */
	BeanDefinition[] getBeanDefinitions();

	/**
	 * 返回表示本组件内所有相关内部 Bean 的 {@link BeanDefinition}。
	 * <p>关联的 {@link BeanDefinition} 中可能存在其他内部 Bean，但这些不被认为
	 * 对验证或用户可视化有必要。
	 * @return BeanDefinition 数组，若无则返回空数组
	 */
	BeanDefinition[] getInnerBeanDefinitions();

	/**
	 * 返回被认为对本 {@code ComponentDefinition} 重要的
	 * {@link BeanReference} 集合。
	 * <p>关联的 {@link BeanDefinition} 中可能存在其他 {@link BeanReference}，
	 * 但这些不被认为对验证或用户可视化有必要。
	 * @return BeanReference 数组，若无则返回空数组
	 */
	BeanReference[] getBeanReferences();

}
