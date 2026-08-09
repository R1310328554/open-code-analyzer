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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.aspectj.lang.reflect.PerClauseKind;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.Pointcut;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.AspectJPrecedenceInformation;
import org.springframework.aop.aspectj.InstantiationModelAwarePointcutAdvisor;
import org.springframework.aop.aspectj.annotation.AbstractAspectJAdvisorFactory.AspectJAnnotation;
import org.springframework.aop.support.DynamicMethodMatcherPointcut;
import org.springframework.aop.support.Pointcuts;
import org.springframework.util.ObjectUtils;

/**
 * AspectJPointcutAdvisor 的内部实现。
 * <p>请注意，每个目标方法都会有一个该顾问程序的实例。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.0
 */
@SuppressWarnings("serial")
final class InstantiationModelAwarePointcutAdvisorImpl
		implements InstantiationModelAwarePointcutAdvisor, AspectJPrecedenceInformation, Serializable {

	/**
	 * 方法 `Advice`：完成本类中与「Advice」相关的职责。
	 */
	private static final Advice EMPTY_ADVICE = new Advice() {};


	/** 切点相关状态（`declaredPointcut`）。 */
	private final AspectJExpressionPointcut declaredPointcut;

	/** 类相关状态（`declaringClass`）。 */
	private final Class<?> declaringClass;

	/** 名称相关状态（`methodName`）。 */
	private final String methodName;

	/** 参数相关状态（`parameterTypes`）。 */
	private final Class<?>[] parameterTypes;

	/** 方法相关状态（`aspectJAdviceMethod`）。 */
	private transient Method aspectJAdviceMethod;

	/** 工厂相关状态（`aspectJAdvisorFactory`）。 */
	private final AspectJAdvisorFactory aspectJAdvisorFactory;

	/** 工厂相关状态（`aspectInstanceFactory`）。 */
	private final MetadataAwareAspectInstanceFactory aspectInstanceFactory;

	/** `declarationOrder`：该类的成员状态。 */
	private final int declarationOrder;

	/** 名称相关状态（`aspectName`）。 */
	private final String aspectName;

	/** 切点相关状态（`pointcut`）。 */
	private final Pointcut pointcut;

	/** `lazy`：该类的成员状态。 */
	private final boolean lazy;

	/** 通知相关状态（`instantiatedAdvice`）。 */
	private @Nullable Advice instantiatedAdvice;

	/** 通知相关状态（`isBeforeAdvice`）。 */
	@SuppressWarnings("NullAway.Init")
	private Boolean isBeforeAdvice;

	/** 通知相关状态（`isAfterAdvice`）。 */
	@SuppressWarnings("NullAway.Init")
	private Boolean isAfterAdvice;


	/**
	 * 创建 `InstantiationModelAwarePointcutAdvisorImpl` 的新实例。
	 */
	public InstantiationModelAwarePointcutAdvisorImpl(AspectJExpressionPointcut declaredPointcut,
			Method aspectJAdviceMethod, AspectJAdvisorFactory aspectJAdvisorFactory,
			MetadataAwareAspectInstanceFactory aspectInstanceFactory, int declarationOrder, String aspectName) {

		this.declaredPointcut = declaredPointcut;
		this.declaringClass = aspectJAdviceMethod.getDeclaringClass();
		this.methodName = aspectJAdviceMethod.getName();
		this.parameterTypes = aspectJAdviceMethod.getParameterTypes();
		this.aspectJAdviceMethod = aspectJAdviceMethod;
		this.aspectJAdvisorFactory = aspectJAdvisorFactory;
		this.aspectInstanceFactory = aspectInstanceFactory;
		this.declarationOrder = declarationOrder;
		this.aspectName = aspectName;

		if (aspectInstanceFactory.getAspectMetadata().isLazilyInstantiated()) {
			// 切入点的静态部分是惰性类型。
			Pointcut preInstantiationPointcut = Pointcuts.union(
					aspectInstanceFactory.getAspectMetadata().getPerClausePointcut(), this.declaredPointcut);

			// 使其动态：必须从实例化前状态转变为实例化后状态。
			// 如果不是动态切入点，则可能会被优化掉
			// 通过Spring AOP基础设施的第一次评估后。
			this.pointcut = new PerTargetInstantiationModelPointcut(
					this.declaredPointcut, preInstantiationPointcut, aspectInstanceFactory);
			this.lazy = true;
		}
		else {
			// 单例方面。
			this.pointcut = this.declaredPointcut;
			this.lazy = false;
			this.instantiatedAdvice = instantiateAdvice(this.declaredPointcut);
		}
	}


	/**
	 * Spring AOP 使用的切入点。切入点的实际行为将根据建议的状态而变化。
	 */
	@Override
	public Pointcut getPointcut() {
		return this.pointcut;
	}

	/**
	 * 判断是否 Lazy。
	 */
	@Override
	public boolean isLazy() {
		return this.lazy;
	}

	/**
	 * 判断是否 Advice Instantiated。
	 */
	@Override
	public synchronized boolean isAdviceInstantiated() {
		return (this.instantiatedAdvice != null);
	}

	/**
	 * 如有必要，延迟实例化建议。
	 */
	@Override
	public synchronized Advice getAdvice() {
		if (this.instantiatedAdvice == null) {
			this.instantiatedAdvice = instantiateAdvice(this.declaredPointcut);
		}
		return this.instantiatedAdvice;
	}

	/**
	 * 实例化：Advice（方法 `instantiateAdvice`）。
	 */
	private Advice instantiateAdvice(AspectJExpressionPointcut pointcut) {
		Advice advice = this.aspectJAdvisorFactory.getAdvice(this.aspectJAdviceMethod, pointcut,
				this.aspectInstanceFactory, this.declarationOrder, this.aspectName);
		return (advice != null ? advice : EMPTY_ADVICE);
	}

	/**
	 * 这仅对 Spring AOP 感兴趣：AspectJ 实例化语义要丰富得多。在 AspectJ 术语中，{@code true} 的返回意味着该方面不是 SINGLETON。
	 */
	@Override
	public boolean isPerInstance() {
		return (getAspectMetadata().getAjType().getPerClause().getKind() != PerClauseKind.SINGLETON);
	}

	/**
	 * 返回此顾问程序的 AspectJ AspectMetadata。
	 */
	public AspectMetadata getAspectMetadata() {
		return this.aspectInstanceFactory.getAspectMetadata();
	}

	/**
	 * 获取 Aspect Instance Factory（`AspectInstanceFactory`）。
	 */
	public MetadataAwareAspectInstanceFactory getAspectInstanceFactory() {
		return this.aspectInstanceFactory;
	}

	/**
	 * 获取 Declared Pointcut（`DeclaredPointcut`）。
	 */
	public AspectJExpressionPointcut getDeclaredPointcut() {
		return this.declaredPointcut;
	}

	/**
	 * 获取 Order（`Order`）。
	 */
	@Override
	public int getOrder() {
		return this.aspectInstanceFactory.getOrder();
	}

	/**
	 * 获取 Aspect Name（`AspectName`）。
	 */
	@Override
	public String getAspectName() {
		return this.aspectName;
	}

	/**
	 * 获取 Declaration Order（`DeclarationOrder`）。
	 */
	@Override
	public int getDeclarationOrder() {
		return this.declarationOrder;
	}

	/**
	 * 判断是否 Before Advice。
	 */
	@Override
	public boolean isBeforeAdvice() {
		if (this.isBeforeAdvice == null) {
			determineAdviceType();
		}
		return this.isBeforeAdvice;
	}

	/**
	 * 判断是否 After Advice。
	 */
	@Override
	public boolean isAfterAdvice() {
		if (this.isAfterAdvice == null) {
			determineAdviceType();
		}
		return this.isAfterAdvice;
	}

	/**
	 * 重复 getAdvice 的一些逻辑，但重要的是不强制创建建议。
	 */
	private void determineAdviceType() {
		AspectJAnnotation aspectJAnnotation =
				AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(this.aspectJAdviceMethod);
		if (aspectJAnnotation == null) {
			this.isBeforeAdvice = false;
			this.isAfterAdvice = false;
		}
		else {
			switch (aspectJAnnotation.getAnnotationType()) {
				case AtPointcut, AtAround -> {
					this.isBeforeAdvice = false;
					this.isAfterAdvice = false;
				}
				case AtBefore -> {
					this.isBeforeAdvice = true;
					this.isAfterAdvice = false;
				}
				case AtAfter, AtAfterReturning, AtAfterThrowing -> {
					this.isBeforeAdvice = false;
					this.isAfterAdvice = true;
				}
			}
		}
	}


	/**
	 * 方法 `readObject`：完成本类中与「read Object」相关的职责。
	 */
	private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
		inputStream.defaultReadObject();
		try {
			this.aspectJAdviceMethod = this.declaringClass.getMethod(this.methodName, this.parameterTypes);
		}
		catch (NoSuchMethodException ex) {
			throw new IllegalStateException("Failed to find advice method on deserialization", ex);
		}
	}

	/**
	 * 返回字符串表示。
	 */
	@Override
	public String toString() {
		return "InstantiationModelAwarePointcutAdvisor: expression [" + getDeclaredPointcut().getExpression() +
				"]; advice method [" + this.aspectJAdviceMethod + "]; perClauseKind=" +
				this.aspectInstanceFactory.getAspectMetadata().getAjType().getPerClause().getKind();
	}


	/**
	 * 实例化通知时更改其行为的切入点实现。请注意，这是一个 <i>dynamic</i> 切入点；否则，如果它最初不静态匹配，则可能会被优化掉。
	 */
	private static final class PerTargetInstantiationModelPointcut extends DynamicMethodMatcherPointcut {

		private final AspectJExpressionPointcut declaredPointcut;

		private final Pointcut preInstantiationPointcut;

		private @Nullable LazySingletonAspectInstanceFactoryDecorator aspectInstanceFactory;

		public PerTargetInstantiationModelPointcut(AspectJExpressionPointcut declaredPointcut,
				Pointcut preInstantiationPointcut, MetadataAwareAspectInstanceFactory aspectInstanceFactory) {

			this.declaredPointcut = declaredPointcut;
			this.preInstantiationPointcut = preInstantiationPointcut;
			if (aspectInstanceFactory instanceof LazySingletonAspectInstanceFactoryDecorator lazyFactory) {
				this.aspectInstanceFactory = lazyFactory;
			}
		}

		@Override
		public boolean matches(Method method, Class<?> targetClass) {
			// 我们要么实例化并匹配声明的切入点，
			// 或任一切入点上的未实例化匹配...
			return (isAspectMaterialized() && this.declaredPointcut.matches(method, targetClass)) ||
					this.preInstantiationPointcut.getMethodMatcher().matches(method, targetClass);
		}

		@Override
		public boolean matches(Method method, Class<?> targetClass, @Nullable Object... args) {
			// 这只能匹配声明的切入点。
			return (isAspectMaterialized() && this.declaredPointcut.matches(method, targetClass, args));
		}

		private boolean isAspectMaterialized() {
			return (this.aspectInstanceFactory == null || this.aspectInstanceFactory.isMaterialized());
		}

		@Override
		public boolean equals(@Nullable Object other) {
			// 为了等效，我们只需要比较 preInstantiationPointcut 字段，因为
			// 它们包括声明的Pointcut 字段。另外，我们不应该比较
			// 自 LazySingletonAspectInstanceFactoryDe​​corator 以来的aspectInstanceFactory 字段
			// 不实现 equals()。
			return (this == other || (other instanceof PerTargetInstantiationModelPointcut that &&
					ObjectUtils.nullSafeEquals(this.preInstantiationPointcut, that.preInstantiationPointcut)));
		}

		@Override
		public int hashCode() {
			return ObjectUtils.nullSafeHashCode(this.declaredPointcut.getExpression());
		}

		@Override
		public String toString() {
			return PerTargetInstantiationModelPointcut.class.getName() + ": " + this.declaredPointcut.getExpression();
		}

	}

}
