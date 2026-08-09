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

import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.generate.MethodReference;
import org.springframework.beans.factory.support.InstanceSupplier;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.javapoet.ClassName;
import org.springframework.javapoet.CodeBlock;

/**
 * 生成注册 Bean 所需各类代码片段的接口。
 *
 * <p>提供了适用于大多数场景的默认实现；自定义代码片段通常仅由
 * 在核心容器之上构建了自定义编排的库作者使用。
 *
 * <p>用户不应直接实现本接口，而应继承
 * {@link BeanRegistrationCodeFragmentsDecorator} 并仅重写必要的方法。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 * @see BeanRegistrationCodeFragmentsDecorator
 * @see BeanRegistrationAotContribution#withCustomCodeFragments(UnaryOperator)
 */
public interface BeanRegistrationCodeFragments {

	/**
	 * 创建 Bean 定义时使用的变量名。
	 */
	String BEAN_DEFINITION_VARIABLE = "beanDefinition";

	/**
	 * 创建实例供应器时使用的变量名。
	 */
	String INSTANCE_SUPPLIER_VARIABLE = "instanceSupplier";


	/**
	 * 返回注册代码的目标类。用于确定代码的写入位置。
	 * 应考虑可见性问题，例如待注册 Bean 元素的包访问权限。
	 * @param registeredBean 已注册的 Bean
	 * @return 目标 {@link ClassName}
	 */
	ClassName getTarget(RegisteredBean registeredBean);

	/**
	 * 生成定义新 Bean 定义实例的代码。
	 * <p>应声明名为 {@value BEAN_DEFINITION_VARIABLE} 的变量，
	 * 以便后续片段可引用该变量进一步调整 Bean 定义。
	 * @param generationContext 生成上下文
	 * @param beanType Bean 类型
	 * @param beanRegistrationCode Bean 注册代码
	 * @return 生成的代码
	 */
	CodeBlock generateNewBeanDefinitionCode(GenerationContext generationContext,
			ResolvableType beanType, BeanRegistrationCode beanRegistrationCode);

	/**
	 * 生成设置 Bean 定义属性的代码。
	 * @param generationContext 生成上下文
	 * @param beanRegistrationCode Bean 注册代码
	 * @param attributeFilter 应应用的属性过滤器
	 * @return 生成的代码
	 */
	CodeBlock generateSetBeanDefinitionPropertiesCode(
			GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode,
			RootBeanDefinition beanDefinition, Predicate<String> attributeFilter);

	/**
	 * 生成在 Bean 定义上设置实例供应器的代码。
	 * <p>{@code postProcessors} 表示实例创建后用于进一步配置的方法。
	 * 每个方法应接受两个参数：{@link RegisteredBean} 和 Bean 实例，
	 * 并返回修改后的 Bean 实例。
	 * @param generationContext 生成上下文
	 * @param beanRegistrationCode Bean 注册代码
	 * @param instanceSupplierCode 实例供应器代码
	 * @param postProcessors 应应用的实例后处理器
	 * @return 生成的代码
	 * @see #generateInstanceSupplierCode
	 */
	CodeBlock generateSetBeanInstanceSupplierCode(
			GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode,
			CodeBlock instanceSupplierCode, List<MethodReference> postProcessors);

	/**
	 * 生成实例供应器代码。
	 * @param generationContext 生成上下文
	 * @param beanRegistrationCode Bean 注册代码
	 * @param allowDirectSupplierShortcut 是否允许使用直接供应器，
	 * 而非始终需要 {@link InstanceSupplier}
	 * @return 生成的代码
	 */
	CodeBlock generateInstanceSupplierCode(
			GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode,
			boolean allowDirectSupplierShortcut);

	/**
	 * 生成 return 语句代码。
	 * @param generationContext 生成上下文
	 * @param beanRegistrationCode Bean 注册代码
	 * @return 生成的代码
	 */
	CodeBlock generateReturnCode(
			GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode);

}
