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
import java.util.List;
import java.util.function.Predicate;

import org.springframework.aot.generate.GeneratedMethods;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.generate.MethodReference;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.javapoet.ClassName;
import org.springframework.javapoet.CodeBlock;
import org.springframework.util.Assert;

/**
 * 支持代码生成的 {@link BeanRegistrationCode} 实现。
 *
 * @author Phillip Webb
 * @since 6.0
 */
class BeanRegistrationCodeGenerator implements BeanRegistrationCode {

	/** 拒绝所有属性的过滤器。 */
	private static final Predicate<String> REJECT_ALL_ATTRIBUTES_FILTER = attribute -> false;

	/** 生成代码所在类的名称。 */
	private final ClassName className;

	/** 生成的方法集合。 */
	private final GeneratedMethods generatedMethods;

	/** 实例后处理器方法引用列表。 */
	private final List<MethodReference> instancePostProcessors = new ArrayList<>();

	/** 当前正在生成注册代码的已注册 Bean。 */
	private final RegisteredBean registeredBean;

	/** 用于生成各代码片段的策略。 */
	private final BeanRegistrationCodeFragments codeFragments;


	BeanRegistrationCodeGenerator(ClassName className, GeneratedMethods generatedMethods,
			RegisteredBean registeredBean, BeanRegistrationCodeFragments codeFragments) {

		this.className = className;
		this.generatedMethods = generatedMethods;
		this.registeredBean = registeredBean;
		this.codeFragments = codeFragments;
	}


	@Override
	public ClassName getClassName() {
		return this.className;
	}

	@Override
	public GeneratedMethods getMethods() {
		return this.generatedMethods;
	}

	@Override
	public void addInstancePostProcessor(MethodReference methodReference) {
		Assert.notNull(methodReference, "'methodReference' must not be null");
		this.instancePostProcessors.add(methodReference);
	}

	/**
	 * 生成完整的 Bean 注册代码。
	 * @param generationContext 生成上下文
	 * @return 组装后的代码块
	 */
	CodeBlock generateCode(GenerationContext generationContext) {
		CodeBlock.Builder code = CodeBlock.builder();
		// 1. 创建 Bean 定义
		code.add(this.codeFragments.generateNewBeanDefinitionCode(generationContext,
				this.registeredBean.getBeanType(), this));
		// 2. 设置 Bean 定义属性
		code.add(this.codeFragments.generateSetBeanDefinitionPropertiesCode(
				generationContext, this, this.registeredBean.getMergedBeanDefinition(),
				REJECT_ALL_ATTRIBUTES_FILTER));
		// 3. 生成并设置实例供应器
		CodeBlock instanceSupplierCode = this.codeFragments.generateInstanceSupplierCode(
				generationContext, this, this.instancePostProcessors.isEmpty());
		code.add(this.codeFragments.generateSetBeanInstanceSupplierCode(generationContext,
				this, instanceSupplierCode, this.instancePostProcessors));
		// 4. 返回 Bean 定义
		code.add(this.codeFragments.generateReturnCode(generationContext, this));
		return code.build();
	}

}
