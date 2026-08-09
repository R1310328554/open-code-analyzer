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

package org.springframework.aop.aspectj.annotation;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareParents;
import org.aspectj.lang.annotation.Pointcut;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.Advisor;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.aspectj.AbstractAspectJAdvice;
import org.springframework.aop.aspectj.AspectJAfterAdvice;
import org.springframework.aop.aspectj.AspectJAfterReturningAdvice;
import org.springframework.aop.aspectj.AspectJAfterThrowingAdvice;
import org.springframework.aop.aspectj.AspectJAroundAdvice;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.AspectJMethodBeforeAdvice;
import org.springframework.aop.aspectj.DeclareParentsAdvisor;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.converter.ConvertingComparator;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodFilter;
import org.springframework.util.StringUtils;
import org.springframework.util.comparator.InstanceComparator;

/**
 * 可以从遵循 AspectJ 注释语法的类中给定 AspectJ 类创建 Spring AOP Advisor 的工厂，使用反射来调用相应的建议方法。
 * @author Rod Johnson
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 2.0
 */
@SuppressWarnings("serial")
public class ReflectiveAspectJAdvisorFactory extends AbstractAspectJAdvisorFactory implements Serializable {

	// 排除@Pointcut方法
	/**
	 * 方法 `and`：完成本类中与「and」相关的职责。
	 */
	private static final MethodFilter adviceMethodFilter = ReflectionUtils.USER_DECLARED_METHODS
			.and(method -> (AnnotationUtils.getAnnotation(method, Pointcut.class) == null));

	/** 方法相关状态（`adviceMethodComparator`）。 */
	private static final Comparator<Method> adviceMethodComparator;

	static {
		// 注意：虽然@After排在@AfterReturning和@AfterThrowing之前，
		// @After通知方法实际上会在@AfterReturning之后被调用并且
		// @AfterThrowing 方法，因为 AspectJAfterAdvice.invoke(MethodIn Vocation)
		// 在`try`块中调用proceed()并且仅调用@After通知方法
		// 在相应的 `finally` 块中。
		Comparator<Method> adviceKindComparator = new ConvertingComparator<Method, @Nullable Annotation>(
				new InstanceComparator<>(
						Around.class, Before.class, After.class, AfterReturning.class, AfterThrowing.class),
				method -> {
					AspectJAnnotation ann = AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(method);
					return (ann != null ? ann.getAnnotation() : null);
				});
		Comparator<Method> methodNameComparator = new ConvertingComparator<>(Method::getName);
		adviceMethodComparator = adviceKindComparator.thenComparing(methodNameComparator);
	}


	/** 底层 BeanFactory 引用。 */
	private final @Nullable BeanFactory beanFactory;


	/**
	 * 创建一个新的 {@code ReflectiveAspectJAdvisorFactory}。
	 */
	public ReflectiveAspectJAdvisorFactory() {
		this(null);
	}

	/**
	 * 创建一个新的 {@code ReflectiveAspectJAdvisorFactory}，将给定的 {@link BeanFactory} 传播到创建的 {@link
	 * AspectJExpressionPointcut} 实例，以进行 bean 切入点处理以及一致的 {@link ClassLoader} 解析。
	 * @param beanFactory 要传播的 BeanFactory（可能是 {@code null}）
	 * @since 4.3.6
	 * @see AspectJExpressionPointcut#setBeanFactory
	 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#getBeanClassLoader()
	 */
	public ReflectiveAspectJAdvisorFactory(@Nullable BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}


	/**
	 * 获取 Advisors（`Advisors`）。
	 */
	@Override
	public List<Advisor> getAdvisors(MetadataAwareAspectInstanceFactory aspectInstanceFactory) {
		Class<?> aspectClass = aspectInstanceFactory.getAspectMetadata().getAspectClass();
		String aspectName = aspectInstanceFactory.getAspectMetadata().getAspectName();
		validate(aspectClass);

		// 我们需要用装饰器包装 MetadataAwareAspectInstanceFactory
		// 这样它只会实例化一次。
		MetadataAwareAspectInstanceFactory lazySingletonAspectInstanceFactory =
				new LazySingletonAspectInstanceFactoryDecorator(aspectInstanceFactory);

		List<Advisor> advisors = new ArrayList<>();
		for (Method method : getAdvisorMethods(aspectClass)) {
			if (method.equals(ClassUtils.getMostSpecificMethod(method, aspectClass))) {
				// 在 Spring Framework 5.2.7 之前，advisors.size() 作为声明OrderInAspect 提供
				// getAdvisor(...) 来表示声明的方法列表中的“当前位置”。
				// 但是，从 Java 7 开始，“当前位置”无效，因为 JDK 不再
				// 按照在源代码中声明的顺序返回声明的方法。
				// 因此，我们现在将所有通知方法的declarationOrderInAspect硬编码为0
				// 通过反射发现，以支持跨 JVM 启动的可靠建议排序。
				// 具体来说，值 0 与中使用的默认值一致
				// AspectJPrecedenceComparator.getAspectDeclarationOrder（顾问）。
				Advisor advisor = getAdvisor(method, lazySingletonAspectInstanceFactory, 0, aspectName);
				if (advisor != null) {
					advisors.add(advisor);
				}
			}
		}

		// 如果它是每个目标方面，则发出虚拟实例化方面。
		if (!advisors.isEmpty() && lazySingletonAspectInstanceFactory.getAspectMetadata().isLazilyInstantiated()) {
			Advisor instantiationAdvisor = new SyntheticInstantiationAdvisor(lazySingletonAspectInstanceFactory);
			advisors.add(0, instantiationAdvisor);
		}

		// 查找介绍字段。
		for (Field field : aspectClass.getDeclaredFields()) {
			Advisor advisor = getDeclareParentsAdvisor(field);
			if (advisor != null) {
				advisors.add(advisor);
			}
		}

		return advisors;
	}

	/**
	 * 获取 Advisor Methods（`AdvisorMethods`）。
	 */
	private List<Method> getAdvisorMethods(Class<?> aspectClass) {
		List<Method> methods = new ArrayList<>();
		ReflectionUtils.doWithMethods(aspectClass, methods::add, adviceMethodFilter);
		if (methods.size() > 1) {
			methods.sort(adviceMethodComparator);
		}
		return methods;
	}

	/**
	 * 为给定的介绍字段构建 {@link org.springframework.aop.aspectj.DeclareParentsAdvisor}。 <p>
	 * 结果顾问将需要针对目标进行评估。
	 * @param introductionField 反思的领域
	 * @return Advisor 实例，如果不是 Advisor，则为 {@code null}
	 */
	private @Nullable Advisor getDeclareParentsAdvisor(Field introductionField) {
		DeclareParents declareParents = introductionField.getAnnotation(DeclareParents.class);
		if (declareParents == null) {
			// 不是介绍字段
			return null;
		}

		if (DeclareParents.class == declareParents.defaultImpl()) {
			throw new IllegalStateException("'defaultImpl' attribute must be set on DeclareParents");
		}

		return new DeclareParentsAdvisor(
				introductionField.getType(), declareParents.value(), declareParents.defaultImpl());
	}


	/**
	 * 获取 Advisor（`Advisor`）。
	 */
	@Override
	public @Nullable Advisor getAdvisor(Method candidateAdviceMethod, MetadataAwareAspectInstanceFactory aspectInstanceFactory,
			int declarationOrderInAspect, String aspectName) {

		validate(aspectInstanceFactory.getAspectMetadata().getAspectClass());

		AspectJExpressionPointcut expressionPointcut = getPointcut(
				candidateAdviceMethod, aspectInstanceFactory.getAspectMetadata().getAspectClass());
		if (expressionPointcut == null) {
			return null;
		}

		try {
			return new InstantiationModelAwarePointcutAdvisorImpl(expressionPointcut, candidateAdviceMethod,
					this, aspectInstanceFactory, declarationOrderInAspect, aspectName);
		}
		catch (IllegalArgumentException | IllegalStateException ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Ignoring incompatible advice method: " + candidateAdviceMethod, ex);
			}
			return null;
		}
	}

	/**
	 * 获取 Pointcut（`Pointcut`）。
	 */
	private @Nullable AspectJExpressionPointcut getPointcut(Method candidateAdviceMethod, Class<?> candidateAspectClass) {
		AspectJAnnotation aspectJAnnotation =
				AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(candidateAdviceMethod);
		if (aspectJAnnotation == null) {
			return null;
		}

		AspectJExpressionPointcut ajexp =
				new AspectJExpressionPointcut(candidateAspectClass, new String[0], new Class<?>[0]);
		ajexp.setExpression(aspectJAnnotation.getPointcutExpression());
		if (this.beanFactory != null) {
			ajexp.setBeanFactory(this.beanFactory);
		}
		return ajexp;
	}


	/**
	 * 获取 Advice（`Advice`）。
	 */
	@Override
	public @Nullable Advice getAdvice(Method candidateAdviceMethod, AspectJExpressionPointcut expressionPointcut,
			MetadataAwareAspectInstanceFactory aspectInstanceFactory, int declarationOrder, String aspectName) {

		Class<?> candidateAspectClass = aspectInstanceFactory.getAspectMetadata().getAspectClass();
		validate(candidateAspectClass);

		AspectJAnnotation aspectJAnnotation =
				AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(candidateAdviceMethod);
		if (aspectJAnnotation == null) {
			return null;
		}

		// 如果我们到达这里，我们就知道我们有一个 AspectJ 方法。
		// 检查它是否是 AspectJ 注解的类
		if (!isAspect(candidateAspectClass)) {
			throw new AopConfigException("Advice must be declared inside an aspect type: " +
					"Offending method '" + candidateAdviceMethod + "' in class [" +
					candidateAspectClass.getName() + "]");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Found AspectJ method: " + candidateAdviceMethod);
		}

		AbstractAspectJAdvice springAdvice;

		switch (aspectJAnnotation.getAnnotationType()) {
			case AtPointcut -> {
				if (logger.isDebugEnabled()) {
					logger.debug("Processing pointcut '" + candidateAdviceMethod.getName() + "'");
				}
				return null;
			}
			case AtAround -> springAdvice = new AspectJAroundAdvice(
					candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
			case AtBefore -> springAdvice = new AspectJMethodBeforeAdvice(
					candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
			case AtAfter -> springAdvice = new AspectJAfterAdvice(
					candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
			case AtAfterReturning -> {
				springAdvice = new AspectJAfterReturningAdvice(
						candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
				AfterReturning afterReturningAnnotation = (AfterReturning) aspectJAnnotation.getAnnotation();
				if (StringUtils.hasText(afterReturningAnnotation.returning())) {
					springAdvice.setReturningName(afterReturningAnnotation.returning());
				}
			}
			case AtAfterThrowing -> {
				springAdvice = new AspectJAfterThrowingAdvice(
						candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
				AfterThrowing afterThrowingAnnotation = (AfterThrowing) aspectJAnnotation.getAnnotation();
				if (StringUtils.hasText(afterThrowingAnnotation.throwing())) {
					springAdvice.setThrowingName(afterThrowingAnnotation.throwing());
				}
			}
			default -> throw new UnsupportedOperationException(
					"Unsupported advice type on method: " + candidateAdviceMethod);
		}

		// 现在配置建议...
		springAdvice.setAspectName(aspectName);
		springAdvice.setDeclarationOrder(declarationOrder);
		@Nullable String[] argNames = this.parameterNameDiscoverer.getParameterNames(candidateAdviceMethod);
		if (argNames != null) {
			springAdvice.setArgumentNamesFromStringArray(argNames);
		}
		springAdvice.calculateArgumentBindings();

		return springAdvice;
	}


	/**
	 * 实例化方面的综合顾问。由非单方面的每个子句切入点触发。该建议没有效果。
	 */
	@SuppressWarnings("serial")
	protected static class SyntheticInstantiationAdvisor extends DefaultPointcutAdvisor {

		public SyntheticInstantiationAdvisor(final MetadataAwareAspectInstanceFactory aif) {
			super(aif.getAspectMetadata().getPerClausePointcut(), (MethodBeforeAdvice)
					(method, args, target) -> aif.getAspectInstance());
		}
	}

}
