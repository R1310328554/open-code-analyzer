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

import javax.lang.model.element.Modifier;

import org.jspecify.annotations.Nullable;

import org.springframework.aot.generate.GeneratedClass;
import org.springframework.aot.generate.GeneratedMethod;
import org.springframework.aot.generate.GeneratedMethods;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.generate.MethodReference;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.javapoet.ClassName;
import org.springframework.util.StringUtils;

/**
 * 生成返回待注册 {@link BeanDefinition} 的方法。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @author Sebastien Deleuze
 * @since 6.0
 * @see BeanDefinitionMethodGeneratorFactory
 */
class BeanDefinitionMethodGenerator {

	private final BeanDefinitionMethodGeneratorFactory methodGeneratorFactory;

	private final RegisteredBean registeredBean;

	private final @Nullable String currentPropertyName;

	private final List<BeanRegistrationAotContribution> aotContributions;


	/**
	 * 创建新的 {@link BeanDefinitionMethodGenerator} 实例。
	 * @param methodGeneratorFactory 方法生成器工厂
	 * @param registeredBean 已注册 bean
	 * @param currentPropertyName 当前属性名
	 * @param aotContributions AOT 贡献列表
	 * @throws IllegalArgumentException 若 bean 定义定义了 instance supplier，因其无法支持代码生成
	 */
	BeanDefinitionMethodGenerator(
			BeanDefinitionMethodGeneratorFactory methodGeneratorFactory,
			RegisteredBean registeredBean, @Nullable String currentPropertyName,
			List<BeanRegistrationAotContribution> aotContributions) {

		this.methodGeneratorFactory = methodGeneratorFactory;
		this.registeredBean = registeredBean;
		this.currentPropertyName = currentPropertyName;
		this.aotContributions = aotContributions;
	}


	/**
	 * 生成返回待注册 {@link BeanDefinition} 的方法。
	 * @param generationContext 生成上下文
	 * @param beanRegistrationsCode bean 注册代码
	 * @return 生成方法的引用
	 */
	MethodReference generateBeanDefinitionMethod(GenerationContext generationContext,
			BeanRegistrationsCode beanRegistrationsCode) {

		BeanRegistrationCodeFragments codeFragments = getCodeFragments(generationContext, beanRegistrationsCode);
		ClassName target = codeFragments.getTarget(this.registeredBean);
		if (isWritablePackageName(target)) {
			// 目标包可写：在目标类旁生成公开静态方法
			GeneratedClass generatedClass = lookupGeneratedClass(generationContext, target);
			GeneratedMethods generatedMethods = generatedClass.getMethods().withPrefix(getName());
			GeneratedMethod generatedMethod = generateBeanDefinitionMethod(generationContext,
					generatedClass.getName(), generatedMethods, codeFragments, Modifier.PUBLIC);
			return generatedMethod.toMethodReference();
		}
		// 目标包不可写：在 bean 注册类中生成私有方法
		GeneratedMethods generatedMethods = beanRegistrationsCode.getMethods().withPrefix(getName());
		GeneratedMethod generatedMethod = generateBeanDefinitionMethod(generationContext,
				beanRegistrationsCode.getClassName(), generatedMethods, codeFragments, Modifier.PRIVATE);
		return generatedMethod.toMethodReference();
	}

	/**
	 * 判断 {@link ClassName} 是否属于可写入的包。
	 * @param target 待检查的目标
	 * @return 若允许在该包中生成代码则返回 {@code true}
	 */
	private boolean isWritablePackageName(ClassName target) {
		String packageName = target.packageName();
		return (!packageName.startsWith("java.") && !packageName.startsWith("javax."));
	}

	/**
	 * 返回用于指定 {@code target} 的 {@link GeneratedClass}。
	 * <p>若目标类为内部类，则在原始结构中创建对应的内部类。
	 * @param generationContext 使用的生成上下文
	 * @param target 为 bean 定义选择的目标类名
	 * @return 要使用的生成类
	 */
	private static GeneratedClass lookupGeneratedClass(GenerationContext generationContext, ClassName target) {
		ClassName topLevelClassName = target.topLevelClassName();
		GeneratedClass generatedClass = generationContext.getGeneratedClasses()
				.getOrAddForFeatureComponent("BeanDefinitions", topLevelClassName, type -> {
					type.addJavadoc("Bean definitions for {@link $T}.", topLevelClassName);
					type.addModifiers(Modifier.PUBLIC);
				});

		List<String> names = target.simpleNames();
		if (names.size() == 1) {
			return generatedClass;
		}

		// 逐级创建嵌套内部类
		List<String> namesToProcess = names.subList(1, names.size());
		ClassName currentTargetClassName = topLevelClassName;
		GeneratedClass tmp = generatedClass;
		for (String nameToProcess : namesToProcess) {
			currentTargetClassName = currentTargetClassName.nestedClass(nameToProcess);
			tmp = createInnerClass(tmp, nameToProcess, currentTargetClassName);
		}
		return tmp;
	}

	private static GeneratedClass createInnerClass(GeneratedClass generatedClass, String name, ClassName target) {
		return generatedClass.getOrAdd(name, type -> {
			type.addJavadoc("Bean definitions for {@link $T}.", target);
			type.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
		});
	}

	private BeanRegistrationCodeFragments getCodeFragments(GenerationContext generationContext,
			BeanRegistrationsCode beanRegistrationsCode) {

		BeanRegistrationCodeFragments codeFragments = new DefaultBeanRegistrationCodeFragments(
				beanRegistrationsCode, this.registeredBean, this.methodGeneratorFactory);
		for (BeanRegistrationAotContribution aotContribution : this.aotContributions) {
			codeFragments = aotContribution.customizeBeanRegistrationCodeFragments(generationContext, codeFragments);
		}
		return codeFragments;
	}

	private GeneratedMethod generateBeanDefinitionMethod(GenerationContext generationContext,
			ClassName className, GeneratedMethods generatedMethods,
			BeanRegistrationCodeFragments codeFragments, Modifier modifier) {

		BeanRegistrationCodeGenerator codeGenerator = new BeanRegistrationCodeGenerator(
				className, generatedMethods, this.registeredBean, codeFragments);

		this.aotContributions.forEach(aotContribution -> aotContribution.applyTo(generationContext, codeGenerator));

		CodeWarnings codeWarnings = new CodeWarnings();
		codeWarnings.detectDeprecation(this.registeredBean.getBeanType());
		return generatedMethods.add("getBeanDefinition", method -> {
			method.addJavadoc("Get the $L definition for '$L'.",
					(this.registeredBean.isInnerBean() ? "inner-bean" : "bean"),
					getName());
			method.addModifiers(modifier, Modifier.STATIC);
			codeWarnings.suppress(method);
			method.returns(BeanDefinition.class);
			method.addCode(codeGenerator.generateCode(generationContext));
		});
	}

	private String getName() {
		if (this.currentPropertyName != null) {
			return this.currentPropertyName;
		}
		if (!this.registeredBean.isGeneratedBeanName()) {
			return getSimpleBeanName(this.registeredBean.getBeanName());
		}
		RegisteredBean nonGeneratedParent = this.registeredBean;
		while (nonGeneratedParent != null && nonGeneratedParent.isGeneratedBeanName()) {
			nonGeneratedParent = nonGeneratedParent.getParent();
		}
		if (nonGeneratedParent != null) {
			return getSimpleBeanName(nonGeneratedParent.getBeanName()) + "InnerBean";
		}
		return "innerBean";
	}

	private String getSimpleBeanName(String beanName) {
		int lastDot = beanName.lastIndexOf('.');
		beanName = (lastDot != -1 ? beanName.substring(lastDot + 1) : beanName);
		int lastDollar = beanName.lastIndexOf('$');
		beanName = (lastDollar != -1 ? beanName.substring(lastDollar + 1) : beanName);
		return StringUtils.uncapitalize(beanName);
	}

}
