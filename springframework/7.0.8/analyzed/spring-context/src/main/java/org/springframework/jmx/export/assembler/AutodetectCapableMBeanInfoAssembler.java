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

package org.springframework.jmx.export.assembler;

/**
 * 扩展 {@code MBeanInfoAssembler}，增加自动检测逻辑。
 * 该接口的实现类可由 {@code MBeanExporter} 调用，以在注册过程中纳入额外的 Bean。
 *
 * <p>具体决定纳入哪些 Bean 的机制由实现类自行定义。
 *
 * @author Rob Harrop
 * @since 1.2
 * @see org.springframework.jmx.export.MBeanExporter
 */
public interface AutodetectCapableMBeanInfoAssembler extends MBeanInfoAssembler {

	/**
	 * 判断某个 Bean 是否应纳入注册流程（当其未在 {@code MBeanExporter} 的
	 * {@code beans} 映射中显式指定时）。
	 * @param beanClass Bean 的类（可能是代理类）
	 * @param beanName Bean 在 BeanFactory 中的名称
	 * @return 是否纳入自动注册
	 */
	boolean includeBean(Class<?> beanClass, String beanName);

}
