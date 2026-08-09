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

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.function.Consumer;

import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;
import kotlin.reflect.KFunction;
import kotlin.reflect.KParameter;
import org.jspecify.annotations.Nullable;

import org.springframework.aot.generate.AccessControl;
import org.springframework.aot.generate.AccessControl.Visibility;
import org.springframework.aot.generate.GeneratedMethod;
import org.springframework.aot.generate.GeneratedMethods;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.generate.MethodReference.ArgumentCodeGenerator;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.InstanceSupplier;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.beans.factory.support.RegisteredBean.InstantiationDescriptor;
import org.springframework.core.KotlinDetector;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.javapoet.ClassName;
import org.springframework.javapoet.CodeBlock;
import org.springframework.javapoet.CodeBlock.Builder;
import org.springframework.javapoet.MethodSpec;
import org.springframework.javapoet.ParameterizedTypeName;
import org.springframework.util.ClassUtils;
import org.springframework.util.function.ThrowingSupplier;

/**
 * 创建 {@link InstanceSupplier} 的默认代码生成器，通常以
 * {@link BeanInstanceSupplier} 形式保留用于实例化 bean 的可执行对象。
 * 负责在需要反射或 JDK 代理时注册必要的 hints。
 *
 * <p>生成的代码通常是生成 {@link BeanInstanceSupplier} 的方法引用，
 * 但也可使用快捷方式，例如：
 * <pre class="code">
 * InstanceSupplier.of(TheGeneratedClass::getMyBeanInstance);
 * </pre>
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @author Sebastien Deleuze
 * @since 6.0
 * @see BeanRegistrationCodeFragments
 */
public class InstanceSupplierCodeGenerator {

	/** 生成代码中 RegisteredBean 参数的名称。 */
	private static final String REGISTERED_BEAN_PARAMETER_NAME = "registeredBean";

	/** 生成代码中构造器/工厂方法参数数组的名称。 */
	private static final String ARGS_PARAMETER_NAME = "args";

	/** 私有静态方法的修饰符。 */
	private static final javax.lang.model.element.Modifier[] PRIVATE_STATIC =
			{javax.lang.model.element.Modifier.PRIVATE, javax.lang.model.element.Modifier.STATIC};

	/** 无参代码块占位符。 */
	private static final CodeBlock NO_ARGS = CodeBlock.of("");

	/** Kotlin 反射是否可用。 */
	private static final boolean KOTLIN_REFLECT_PRESENT = KotlinDetector.isKotlinReflectPresent();


	/** AOT 代码生成上下文。 */
	private final GenerationContext generationContext;

	/** 要实例化的 bean 所在生成类的类名。 */
	private final ClassName className;

	/** 生成方法的容器。 */
	private final GeneratedMethods generatedMethods;

	/** 是否允许直接使用 Supplier 快捷方式而非始终需要 {@link InstanceSupplier}。 */
	private final boolean allowDirectSupplierShortcut;


	/**
	 * 创建新的生成器实例。
	 * @param generationContext 生成上下文
	 * @param className 要实例化的 bean 所在类的类名
	 * @param generatedMethods 生成方法的容器
	 * @param allowDirectSupplierShortcut 是否允许直接使用 supplier 快捷方式，
	 * 而非始终需要 {@link InstanceSupplier}
	 */
	public InstanceSupplierCodeGenerator(GenerationContext generationContext,
			ClassName className, GeneratedMethods generatedMethods, boolean allowDirectSupplierShortcut) {

		this.generationContext = generationContext;
		this.className = className;
		this.generatedMethods = generatedMethods;
		this.allowDirectSupplierShortcut = allowDirectSupplierShortcut;
	}


	/**
	 * 生成实例 supplier 代码。
	 * @param registeredBean 要处理的 bean
	 * @param constructorOrFactoryMethod 用于创建 bean 的可执行对象
	 * @return 生成的代码
	 * @deprecated 推荐使用 {@link #generateCode(RegisteredBean, InstantiationDescriptor)}
	 */
	@Deprecated(since = "6.1.7")
	public CodeBlock generateCode(RegisteredBean registeredBean, Executable constructorOrFactoryMethod) {
		return generateCode(registeredBean, new InstantiationDescriptor(
				constructorOrFactoryMethod, constructorOrFactoryMethod.getDeclaringClass()));
	}

	/**
	 * 生成实例 supplier 代码。
	 * @param registeredBean 要处理的 bean
	 * @param instantiationDescriptor 用于创建 bean 的实例化描述符
	 * @return 生成的代码
	 * @since 6.1.7
	 */
	public CodeBlock generateCode(RegisteredBean registeredBean, InstantiationDescriptor instantiationDescriptor) {
		Executable constructorOrFactoryMethod = instantiationDescriptor.executable();
		// 必要时注册运行时反射/代理 hints
		registerRuntimeHintsIfNecessary(registeredBean, constructorOrFactoryMethod);
		if (constructorOrFactoryMethod instanceof Constructor<?> constructor) {
			return generateCodeForConstructor(registeredBean, constructor);
		}
		if (constructorOrFactoryMethod instanceof Method method && !KotlinDetector.isSuspendingFunction(method)) {
			return generateCodeForFactoryMethod(registeredBean, method, instantiationDescriptor.targetClass());
		}
		throw new AotBeanProcessingException(registeredBean, "no suitable constructor or factory method found");
	}

	/**
	 * 在需要反射或代理时注册运行时 hints。
	 */
	private void registerRuntimeHintsIfNecessary(RegisteredBean registeredBean, Executable constructorOrFactoryMethod) {
		if (registeredBean.getBeanFactory() instanceof DefaultListableBeanFactory dlbf) {
			RuntimeHints runtimeHints = this.generationContext.getRuntimeHints();
			ProxyRuntimeHintsRegistrar registrar = new ProxyRuntimeHintsRegistrar(dlbf.getAutowireCandidateResolver());
			registrar.registerRuntimeHints(runtimeHints, constructorOrFactoryMethod);
		}
	}

	/**
	 * 为构造器生成实例 supplier 代码。
	 */
	private CodeBlock generateCodeForConstructor(RegisteredBean registeredBean, Constructor<?> constructor) {
		ConstructorDescriptor descriptor = new ConstructorDescriptor(
				registeredBean.getBeanName(), constructor, registeredBean.getBeanClass());

		Class<?> publicType = descriptor.publicType();
		// Kotlin 带可选参数的构造器需要反射路径
		if (KOTLIN_REFLECT_PRESENT && KotlinDetector.isKotlinType(publicType) && KotlinDelegate.hasConstructorWithOptionalParameter(publicType)) {
			return generateCodeForInaccessibleConstructor(descriptor,
					hints -> hints.registerType(publicType, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS));
		}

		// 不可见构造器或存在方法覆盖时走反射路径
		if (!isVisible(constructor, constructor.getDeclaringClass()) ||
				registeredBean.getMergedBeanDefinition().hasMethodOverrides()) {
			return generateCodeForInaccessibleConstructor(descriptor,
					hints -> hints.registerConstructor(constructor, ExecutableMode.INVOKE));
		}
		return generateCodeForAccessibleConstructor(descriptor);
	}

	/**
	 * 为可直接访问的构造器生成代码（方法引用或生成方法）。
	 */
	private CodeBlock generateCodeForAccessibleConstructor(ConstructorDescriptor descriptor) {
		Constructor<?> constructor = descriptor.constructor();
		this.generationContext.getRuntimeHints().reflection().registerType(constructor.getDeclaringClass());

		if (constructor.getParameterCount() == 0) {
			if (!this.allowDirectSupplierShortcut) {
				return CodeBlock.of("$T.using($T::new)", InstanceSupplier.class, descriptor.actualType());
			}
			if (!isThrowingCheckedException(constructor)) {
				return CodeBlock.of("$T::new", descriptor.actualType());
			}
			return CodeBlock.of("$T.of($T::new)", ThrowingSupplier.class, descriptor.actualType());
		}

		// 有参构造器：生成私有静态方法
		GeneratedMethod generatedMethod = generateGetInstanceSupplierMethod(method ->
				buildGetInstanceMethodForConstructor(method, descriptor, PRIVATE_STATIC));
		return generateReturnStatement(generatedMethod);
	}

	/**
	 * 为不可直接访问的构造器生成代码（通过反射 hints + 生成方法）。
	 */
	private CodeBlock generateCodeForInaccessibleConstructor(ConstructorDescriptor descriptor,
			Consumer<ReflectionHints> hints) {

		Constructor<?> constructor = descriptor.constructor();
		CodeWarnings codeWarnings = new CodeWarnings();
		codeWarnings.detectDeprecation(constructor.getDeclaringClass(), constructor)
				.detectDeprecation(Arrays.stream(constructor.getParameters()).map(Parameter::getType));
		hints.accept(this.generationContext.getRuntimeHints().reflection());

		GeneratedMethod generatedMethod = generateGetInstanceSupplierMethod(method -> {
			method.addJavadoc("Get the bean instance supplier for '$L'.", descriptor.beanName());
			method.addModifiers(PRIVATE_STATIC);
			codeWarnings.suppress(method);
			method.returns(ParameterizedTypeName.get(BeanInstanceSupplier.class, descriptor.publicType()));
			method.addStatement(generateResolverForConstructor(descriptor));
		});

		return generateReturnStatement(generatedMethod);
	}

	/**
	 * 构建获取构造器实例 supplier 的生成方法体。
	 */
	private void buildGetInstanceMethodForConstructor(MethodSpec.Builder method, ConstructorDescriptor descriptor,
			javax.lang.model.element.Modifier... modifiers) {

		Constructor<?> constructor = descriptor.constructor();
		Class<?> publicType = descriptor.publicType();
		Class<?> actualType = descriptor.actualType();

		CodeWarnings codeWarnings = new CodeWarnings();
		codeWarnings.detectDeprecation(actualType, constructor)
				.detectDeprecation(Arrays.stream(constructor.getParameters()).map(Parameter::getType));
		method.addJavadoc("Get the bean instance supplier for '$L'.", descriptor.beanName());
		method.addModifiers(modifiers);
		codeWarnings.suppress(method);
		method.returns(ParameterizedTypeName.get(BeanInstanceSupplier.class, publicType));

		CodeBlock.Builder code = CodeBlock.builder();
		code.add(generateResolverForConstructor(descriptor));
		boolean hasArguments = constructor.getParameterCount() > 0;
		boolean onInnerClass = ClassUtils.isInnerClass(actualType);

		// 生成自动装配参数代码
		CodeBlock arguments = hasArguments ?
				new AutowiredArgumentsCodeGenerator(actualType, constructor)
						.generateCode(constructor.getParameterTypes(), (onInnerClass ? 1 : 0)) : NO_ARGS;

		CodeBlock newInstance = generateNewInstanceCodeForConstructor(actualType, arguments);
		code.add(generateWithGeneratorCode(hasArguments, newInstance));
		method.addStatement(code.build());
	}

	/** 生成 BeanInstanceSupplier.forConstructor(...) 代码。 */
	private CodeBlock generateResolverForConstructor(ConstructorDescriptor descriptor) {
		CodeBlock parameterTypes = generateParameterTypesCode(descriptor.constructor().getParameterTypes());
		return CodeBlock.of("return $T.<$T>forConstructor($L)", BeanInstanceSupplier.class,
				descriptor.publicType(), parameterTypes);
	}

	/** 生成 new 实例代码（处理内部类需先获取外部类实例）。 */
	private CodeBlock generateNewInstanceCodeForConstructor(Class<?> declaringClass, CodeBlock args) {
		if (ClassUtils.isInnerClass(declaringClass)) {
			return CodeBlock.of("$L.getBeanFactory().getBean($T.class).new $L($L)",
					REGISTERED_BEAN_PARAMETER_NAME, declaringClass.getEnclosingClass(),
					declaringClass.getSimpleName(), args);
		}
		return CodeBlock.of("new $T($L)", declaringClass, args);
	}

	/**
	 * 为工厂方法生成实例 supplier 代码。
	 */
	private CodeBlock generateCodeForFactoryMethod(
			RegisteredBean registeredBean, Method factoryMethod, Class<?> targetClass) {

		if (!isVisible(factoryMethod, targetClass)) {
			return generateCodeForInaccessibleFactoryMethod(registeredBean.getBeanName(), factoryMethod, targetClass);
		}
		return generateCodeForAccessibleFactoryMethod(registeredBean.getBeanName(), factoryMethod, targetClass,
				registeredBean.getMergedBeanDefinition().getFactoryBeanName());
	}

	/**
	 * 为可直接访问的工厂方法生成代码。
	 */
	private CodeBlock generateCodeForAccessibleFactoryMethod(String beanName,
			Method factoryMethod, Class<?> targetClass, @Nullable String factoryBeanName) {

		this.generationContext.getRuntimeHints().reflection().registerType(factoryMethod.getDeclaringClass());

		// 无参静态工厂方法可内联生成
		if (factoryBeanName == null && factoryMethod.getParameterCount() == 0) {
			Class<?> suppliedType = ClassUtils.resolvePrimitiveIfNecessary(factoryMethod.getReturnType());
			CodeBlock.Builder code = CodeBlock.builder();
			code.add("$T.<$T>forFactoryMethod($T.class, $S)", BeanInstanceSupplier.class,
					suppliedType, targetClass, factoryMethod.getName());
			code.add(".withGenerator(($L) -> $T.$L())", REGISTERED_BEAN_PARAMETER_NAME,
					ClassUtils.getUserClass(targetClass), factoryMethod.getName());
			return code.build();
		}

		GeneratedMethod getInstanceMethod = generateGetInstanceSupplierMethod(method ->
				buildGetInstanceMethodForFactoryMethod(method, beanName, factoryMethod,
						targetClass, factoryBeanName, PRIVATE_STATIC));
		return generateReturnStatement(getInstanceMethod);
	}

	/**
	 * 为不可直接访问的工厂方法生成代码。
	 */
	private CodeBlock generateCodeForInaccessibleFactoryMethod(
			String beanName, Method factoryMethod, Class<?> targetClass) {

		this.generationContext.getRuntimeHints().reflection().registerMethod(factoryMethod, ExecutableMode.INVOKE);
		GeneratedMethod getInstanceMethod = generateGetInstanceSupplierMethod(method -> {
			CodeWarnings codeWarnings = new CodeWarnings();
			Class<?> suppliedType = ClassUtils.resolvePrimitiveIfNecessary(factoryMethod.getReturnType());
			codeWarnings.detectDeprecation(suppliedType, factoryMethod);
			method.addJavadoc("Get the bean instance supplier for '$L'.", beanName);
			method.addModifiers(PRIVATE_STATIC);
			codeWarnings.suppress(method);
			method.returns(ParameterizedTypeName.get(BeanInstanceSupplier.class, suppliedType));
			method.addStatement(generateInstanceSupplierForFactoryMethod(
					factoryMethod, suppliedType, targetClass, factoryMethod.getName()));
		});
		return generateReturnStatement(getInstanceMethod);
	}

	/**
	 * 构建获取工厂方法实例 supplier 的生成方法体。
	 */
	private void buildGetInstanceMethodForFactoryMethod(MethodSpec.Builder method,
			String beanName, Method factoryMethod, Class<?> targetClass,
			@Nullable String factoryBeanName, javax.lang.model.element.Modifier... modifiers) {

		String factoryMethodName = factoryMethod.getName();
		Class<?> suppliedType = ClassUtils.resolvePrimitiveIfNecessary(factoryMethod.getReturnType());
		CodeWarnings codeWarnings = new CodeWarnings();
		codeWarnings.detectDeprecation(ClassUtils.getUserClass(targetClass), factoryMethod, suppliedType)
				.detectDeprecation(Arrays.stream(factoryMethod.getParameters()).map(Parameter::getType));

		method.addJavadoc("Get the bean instance supplier for '$L'.", beanName);
		method.addModifiers(modifiers);
		codeWarnings.suppress(method);
		method.returns(ParameterizedTypeName.get(BeanInstanceSupplier.class, suppliedType));

		CodeBlock.Builder code = CodeBlock.builder();
		code.add(generateInstanceSupplierForFactoryMethod(
				factoryMethod, suppliedType, targetClass, factoryMethodName));

		boolean hasArguments = factoryMethod.getParameterCount() > 0;
		CodeBlock arguments = hasArguments ?
				new AutowiredArgumentsCodeGenerator(ClassUtils.getUserClass(targetClass), factoryMethod)
						.generateCode(factoryMethod.getParameterTypes()) : NO_ARGS;

		CodeBlock newInstance = generateNewInstanceCodeForMethod(
				factoryBeanName, ClassUtils.getUserClass(targetClass), factoryMethodName, arguments);
		code.add(generateWithGeneratorCode(hasArguments, newInstance));
		method.addStatement(code.build());
	}

	/** 生成 BeanInstanceSupplier.forFactoryMethod(...) 代码。 */
	private CodeBlock generateInstanceSupplierForFactoryMethod(Method factoryMethod,
			Class<?> suppliedType, Class<?> targetClass, String factoryMethodName) {

		if (factoryMethod.getParameterCount() == 0) {
			return CodeBlock.of("return $T.<$T>forFactoryMethod($T.class, $S)",
					BeanInstanceSupplier.class, suppliedType, targetClass, factoryMethodName);
		}

		CodeBlock parameterTypes = generateParameterTypesCode(factoryMethod.getParameterTypes());
		return CodeBlock.of("return $T.<$T>forFactoryMethod($T.class, $S, $L)",
				BeanInstanceSupplier.class, suppliedType, targetClass, factoryMethodName, parameterTypes);
	}

	/** 生成工厂方法调用代码（静态或实例工厂 bean）。 */
	private CodeBlock generateNewInstanceCodeForMethod(@Nullable String factoryBeanName,
			Class<?> targetClass, String factoryMethodName, CodeBlock args) {

		if (factoryBeanName == null) {
			return CodeBlock.of("$T.$L($L)", targetClass, factoryMethodName, args);
		}
		return CodeBlock.of("$L.getBeanFactory().getBean(\"$L\", $T.class).$L($L)",
				REGISTERED_BEAN_PARAMETER_NAME, factoryBeanName, targetClass, factoryMethodName, args);
	}

	/** 生成对生成方法的调用返回语句。 */
	private CodeBlock generateReturnStatement(GeneratedMethod generatedMethod) {
		return generatedMethod.toMethodReference().toInvokeCodeBlock(
				ArgumentCodeGenerator.none(), this.className);
	}

	/** 生成 .withGenerator(...) lambda 代码块。 */
	private CodeBlock generateWithGeneratorCode(boolean hasArguments, CodeBlock newInstance) {
		CodeBlock lambdaArguments = (hasArguments ?
				CodeBlock.of("($L, $L)", REGISTERED_BEAN_PARAMETER_NAME, ARGS_PARAMETER_NAME) :
				CodeBlock.of("($L)", REGISTERED_BEAN_PARAMETER_NAME));
		Builder code = CodeBlock.builder();
		code.add("\n");
		code.indent().indent();
		code.add(".withGenerator($L -> $L)", lambdaArguments, newInstance);
		code.unindent().unindent();
		return code.build();
	}

	/**
	 * 判断成员在目标类上下文中是否可见（public 或同包非 private）。
	 */
	private boolean isVisible(Member member, Class<?> targetClass) {
		AccessControl classAccessControl = AccessControl.forClass(targetClass);
		AccessControl memberAccessControl = AccessControl.forMember(member);
		Visibility visibility = AccessControl.lowest(classAccessControl, memberAccessControl).getVisibility();
		return (visibility == Visibility.PUBLIC || (visibility != Visibility.PRIVATE &&
				member.getDeclaringClass().getPackageName().equals(this.className.packageName())));
	}

	/** 生成参数类型数组的 .class 引用代码。 */
	private CodeBlock generateParameterTypesCode(Class<?>[] parameterTypes) {
		CodeBlock.Builder code = CodeBlock.builder();
		for (int i = 0; i < parameterTypes.length; i++) {
			code.add(i > 0 ? ", " : "");
			code.add("$T.class", parameterTypes[i]);
		}
		return code.build();
	}

	/** 注册并返回名为 getInstanceSupplier 的生成方法。 */
	private GeneratedMethod generateGetInstanceSupplierMethod(Consumer<MethodSpec.Builder> method) {
		return this.generatedMethods.add("getInstanceSupplier", method);
	}

	/** 判断可执行对象是否声明了受检异常。 */
	private boolean isThrowingCheckedException(Executable executable) {
		return Arrays.stream(executable.getGenericExceptionTypes())
				.map(ResolvableType::forType)
				.map(ResolvableType::toClass)
				.anyMatch(Exception.class::isAssignableFrom);
	}


	/**
	 * 内部类，避免在运行时对 Kotlin 的硬依赖。
	 */
	private static class KotlinDelegate {

		/**
		 * 判断 Kotlin 类是否有带可选参数的构造器。
		 */
		public static boolean hasConstructorWithOptionalParameter(Class<?> beanClass) {
			KClass<?> kClass = JvmClassMappingKt.getKotlinClass(beanClass);
			for (KFunction<?> constructor : kClass.getConstructors()) {
				for (KParameter parameter : constructor.getParameters()) {
					if (parameter.isOptional()) {
						return true;
					}
				}
			}
			return false;
		}
	}


	/**
	 * 为构造器/工厂方法参数注册 JDK 代理运行时 hints 的记录类。
	 */
	private record ProxyRuntimeHintsRegistrar(AutowireCandidateResolver candidateResolver) {

		/**
		 * 遍历可执行对象参数，为需要懒解析代理的类型注册 hints。
		 */
		public void registerRuntimeHints(RuntimeHints runtimeHints, Executable executable) {
			Class<?>[] parameterTypes = executable.getParameterTypes();
			for (int i = 0; i < parameterTypes.length; i++) {
				MethodParameter methodParam = MethodParameter.forExecutable(executable, i);
				DependencyDescriptor dependencyDescriptor = new DependencyDescriptor(methodParam, true);
				registerProxyIfNecessary(runtimeHints, dependencyDescriptor);
			}
		}

		/** 若依赖需要 JDK 代理则注册代理 hints。 */
		private void registerProxyIfNecessary(RuntimeHints runtimeHints, DependencyDescriptor dependencyDescriptor) {
			Class<?> proxyType = this.candidateResolver.getLazyResolutionProxyClass(dependencyDescriptor, null);
			if (proxyType != null && Proxy.isProxyClass(proxyType)) {
				runtimeHints.proxies().registerJdkProxy(proxyType.getInterfaces());
			}
		}
	}


	/**
	 * 构造器描述符，封装 bean 名称、构造器和公开类型。
	 */
	record ConstructorDescriptor(String beanName, Constructor<?> constructor, Class<?> publicType) {

		/** 返回构造器声明的实际类型（即构造器所在类）。 */
		Class<?> actualType() {
			return this.constructor.getDeclaringClass();
		}
	}

}
