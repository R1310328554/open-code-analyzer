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

package org.springframework.beans.factory.config;

import org.springframework.beans.BeanMetadataElement;

/**
 * 以抽象方式暴露 bean 名称引用的接口。
 * 该接口不一定意味着引用实际的 bean 实例；它仅表达对一个 bean 名称的逻辑引用。
 *
 * <p>作为各类 bean 引用持有者的通用接口，例如
 * {@link RuntimeBeanReference RuntimeBeanReference} 和
 * {@link RuntimeBeanNameReference RuntimeBeanNameReference}。
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
public interface BeanReference extends BeanMetadataElement {

	/**
	 * 返回此引用所指向的目标 bean 名称（永不为 {@code null}）。
	 */
	String getBeanName();

}
