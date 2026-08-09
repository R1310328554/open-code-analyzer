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

package org.springframework.aop.aspectj;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.weaver.tools.JoinPointMatch;
import org.aspectj.weaver.tools.PointcutParameter;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.AopInvocationException;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.MethodMatchers;
import org.springframework.aop.support.StaticMethodMatcher;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.lang.Contract;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * AOP 联盟 {@link org.aopalliance.aop.Advice} 类的基类包装了 AspectJ 方面或 AspectJ 注释的建议方法。
 * @author Rod Johnson
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @since 2.0
 */
@SuppressWarnings("serial")
public abstract class AbstractAspectJAdvice implements Advice, AspectJPrecedenceInformation, Serializable {

	/**
	 * 当前连接点的 ReflectiveMethodInitation userAttributes 映射中使用的键。
	 */
	protected static final String JOIN_POINT_KEY = JoinPoint.class.getName();


	/**
	 * 延迟实例化当前调用的连接点。要求 MethodInitation 与 ExposeInitationInterceptor 绑定。 <p>如果当前
	 * ReflectiveMethodInitation 可以访问（在周围建议中），则不要使用。
	 * @return AspectJ 连接点，或者如果我们不在 Spring AOP 调用中则通过异常。
	 */
	public static JoinPoint currentJoinPoint() {
		MethodInvocation mi = ExposeInvocationInterceptor.currentInvocation();
		if (!(mi instanceof ProxyMethodInvocation pmi)) {
			throw new IllegalStateException("MethodInvocation is not a Spring ProxyMethodInvocation: " + mi);
		}
		JoinPoint jp = (JoinPoint) pmi.getUserAttribute(JOIN_POINT_KEY);
		if (jp == null) {
			jp = new MethodInvocationProceedingJoinPoint(pmi);
			pmi.setUserAttribute(JOIN_POINT_KEY, jp);
		}
		return jp;
	}


	/** 类相关状态（`declaringClass`）。 */
	private final Class<?> declaringClass;

	/** 名称相关状态（`methodName`）。 */
	private final String methodName;

	/** 参数相关状态（`parameterTypes`）。 */
	private final Class<?>[] parameterTypes;

	/** 方法相关状态（`aspectJAdviceMethod`）。 */
	protected transient Method aspectJAdviceMethod;

	/** 切点相关状态（`pointcut`）。 */
	private final AspectJExpressionPointcut pointcut;

	/** 工厂相关状态（`aspectInstanceFactory`）。 */
	private final AspectInstanceFactory aspectInstanceFactory;

	/**
	 * 定义此建议的方面（ref bean）的名称（在确定建议优先级时使用，以便我们可以确定两条建议是否来自同一方面）。
	 */
	private String aspectName = "";

	/**
	 * 该建议在方面内的声明顺序。
	 */
	private int declarationOrder;

	/**
	 * 如果此建议对象的创建者知道参数名称并显式设置它们，则这将是非空的。
	 */
	private @Nullable String @Nullable [] argumentNames;

	/**
	 */
	private @Nullable String throwingName;

	/**
	 */
	private @Nullable String returningName;

	private Class<?> discoveredReturningType = Object.class;

	private Class<?> discoveredThrowingType = Object.class;

	/**
	 * thisJoinPoint 参数的索引（当前仅支持索引 0（如果存在））。
	 */
	private int joinPointArgumentIndex = -1;

	/**
	 * thisJoinPointStaticPart 参数的索引（当前仅支持索引 0（如果存在））。
	 */
	private int joinPointStaticPartArgumentIndex = -1;

	/** `argumentBindings`：该类的成员状态。 */
	private @Nullable Map<String, Integer> argumentBindings;

	/** `false`：该类的成员状态。 */
	private boolean argumentsIntrospected = false;

	/** 类型相关状态（`discoveredReturningGenericType`）。 */
	private @Nullable Type discoveredReturningGenericType;
	// 注意：与返回类型不同，抛出类型不需要此类通用信息，
	// 因为 Java 不允许异常类型被参数化。


	/**
	 * 为给定的建议方法创建一个新的 AbstractAspectJAdvice。
	 * @param aspectJAdviceMethod AspectJ 风格的建议方法
	 * @param pointcut AspectJ 表达式切入点
	 * @param aspectInstanceFactory 方面实例的工厂
	 */
	public AbstractAspectJAdvice(
			Method aspectJAdviceMethod, AspectJExpressionPointcut pointcut, AspectInstanceFactory aspectInstanceFactory) {

		Assert.notNull(aspectJAdviceMethod, "Advice method must not be null");
		this.declaringClass = aspectJAdviceMethod.getDeclaringClass();
		this.methodName = aspectJAdviceMethod.getName();
		this.parameterTypes = aspectJAdviceMethod.getParameterTypes();
		this.aspectJAdviceMethod = aspectJAdviceMethod;
		this.pointcut = pointcut;
		this.aspectInstanceFactory = aspectInstanceFactory;
	}


	/**
	 * 返回 AspectJ 风格的通知方法。
	 */
	public final Method getAspectJAdviceMethod() {
		return this.aspectJAdviceMethod;
	}

	/**
	 * 返回 AspectJ 表达式切入点。
	 */
	public final AspectJExpressionPointcut getPointcut() {
		calculateArgumentBindings();
		return this.pointcut;
	}

	/**
	 * 构建一个排除 AspectJ 建议方法本身的“安全”切入点。
	 * @return 构建在原始 AspectJ 表达式切入点之上的可组合切入点
	 * @see #getPointcut()
	 */
	public final Pointcut buildSafePointcut() {
		Pointcut pc = getPointcut();
		MethodMatcher safeMethodMatcher = MethodMatchers.intersection(
				new AdviceExcludingMethodMatcher(this.aspectJAdviceMethod), pc.getMethodMatcher());
		return new ComposablePointcut(pc.getClassFilter(), safeMethodMatcher);
	}

	/**
	 * 返回方面实例的工厂。
	 */
	public final AspectInstanceFactory getAspectInstanceFactory() {
		return this.aspectInstanceFactory;
	}

	/**
	 * 返回方面实例的类加载器。
	 */
	public final @Nullable ClassLoader getAspectClassLoader() {
		return this.aspectInstanceFactory.getAspectClassLoader();
	}

	/**
	 * 获取 Order（`Order`）。
	 */
	@Override
	public int getOrder() {
		return this.aspectInstanceFactory.getOrder();
	}


	/**
	 * 设置声明通知的方面（bean）的名称。
	 */
	public void setAspectName(String name) {
		this.aspectName = name;
	}

	/**
	 * 获取 Aspect Name（`AspectName`）。
	 */
	@Override
	public String getAspectName() {
		return this.aspectName;
	}

	/**
	 * 设置该建议在方面内的声明顺序。
	 */
	public void setDeclarationOrder(int order) {
		this.declarationOrder = order;
	}

	/**
	 * 获取 Declaration Order（`DeclarationOrder`）。
	 */
	@Override
	public int getDeclarationOrder() {
		return this.declarationOrder;
	}

	/**
	 * 如果参数名称已知，则由该建议对象的创建者设置。 <p> 这可能是因为它们已在 XML 或建议注释中显式指定。
	 * @param argumentNames 逗号分隔的参数名称列表
	 */
	public void setArgumentNames(String argumentNames) {
		String[] tokens = StringUtils.commaDelimitedListToStringArray(argumentNames);
		setArgumentNamesFromStringArray(tokens);
	}

	/**
	 * 如果参数名称已知，则由该建议对象的创建者设置。 <p> 这可能是因为它们已在 XML 或建议注释中显式指定。
	 * @param argumentNames 参数名称列表
	 */
	public void setArgumentNamesFromStringArray(@Nullable String... argumentNames) {
		this.argumentNames = new String[argumentNames.length];
		for (int i = 0; i < argumentNames.length; i++) {
			String argumentName = argumentNames[i];
			this.argumentNames[i] = argumentName != null ? argumentName.strip() : null;
			if (!isVariableName(this.argumentNames[i])) {
				throw new IllegalArgumentException(
						"'argumentNames' property of AbstractAspectJAdvice contains an argument name '" +
						this.argumentNames[i] + "' that is not a valid Java identifier");
			}
		}
		if (this.aspectJAdviceMethod.getParameterCount() == this.argumentNames.length + 1) {
			// 可能需要添加隐式连接点参数名称...
			for (int i = 0; i < this.aspectJAdviceMethod.getParameterCount(); i++) {
				Class<?> argType = this.aspectJAdviceMethod.getParameterTypes()[i];
				if (argType == JoinPoint.class ||
						argType == ProceedingJoinPoint.class ||
						argType == JoinPoint.StaticPart.class) {
					@Nullable String[] oldNames = this.argumentNames;
				this.argumentNames = new String[oldNames.length + 1];
				System.arraycopy(oldNames, 0, this.argumentNames, 0, i);
					this.argumentNames[i] = "THIS_JOIN_POINT";
					System.arraycopy(oldNames, i, this.argumentNames, i + 1, oldNames.length - i);
					break;
				}
			}
		}
	}

	/**
	 * 设置 Returning Name（`ReturningName`）。
	 */
	public void setReturningName(String name) {
		throw new UnsupportedOperationException("Only afterReturning advice can be used to bind a return value");
	}

	/**
	 * 我们需要在此级别保存返回名称以进行参数绑定计算，此方法允许 afterReturning 建议子类设置名称。
	 */
	protected void setReturningNameNoCheck(String name) {
		// 名称可以是变量或类型...
		if (isVariableName(name)) {
			this.returningName = name;
		}
		else {
			// 假设一个类型
			try {
				this.discoveredReturningType = ClassUtils.forName(name, getAspectClassLoader());
			}
			catch (Throwable ex) {
				throw new IllegalArgumentException("Returning name '" + name +
						"' is neither a valid argument name nor the fully-qualified " +
						"name of a Java type on the classpath. Root cause: " + ex);
			}
		}
	}

	/**
	 * 获取 Discovered Returning Type（`DiscoveredReturningType`）。
	 */
	protected Class<?> getDiscoveredReturningType() {
		return this.discoveredReturningType;
	}

	/**
	 * 获取 Discovered Returning Generic Type（`DiscoveredReturningGenericType`）。
	 */
	protected @Nullable Type getDiscoveredReturningGenericType() {
		return this.discoveredReturningGenericType;
	}

	/**
	 * 设置 Throwing Name（`ThrowingName`）。
	 */
	public void setThrowingName(String name) {
		throw new UnsupportedOperationException("Only afterThrowing advice can be used to bind a thrown exception");
	}

	/**
	 * 我们需要在此级别保存抛出名称以进行参数绑定计算，此方法允许 afterThrowing 建议子类设置名称。
	 */
	protected void setThrowingNameNoCheck(String name) {
		// 名称可以是变量或类型...
		if (isVariableName(name)) {
			this.throwingName = name;
		}
		else {
			// 假设一个类型
			try {
				this.discoveredThrowingType = ClassUtils.forName(name, getAspectClassLoader());
			}
			catch (Throwable ex) {
				throw new IllegalArgumentException("Throwing name '" + name +
						"' is neither a valid argument name nor the fully-qualified " +
						"name of a Java type on the classpath. Root cause: " + ex);
			}
		}
	}

	/**
	 * 获取 Discovered Throwing Type（`DiscoveredThrowingType`）。
	 */
	protected Class<?> getDiscoveredThrowingType() {
		return this.discoveredThrowingType;
	}

	/**
	 * 判断是否 Variable Name。
	 */
	@Contract("null -> false")
	private static boolean isVariableName(@Nullable String name) {
		return AspectJProxyUtils.isVariableName(name);
	}


	/**
	 * 作为设置的一部分，我们要做尽可能多的工作，以便后续建议调用上的参数绑定可以尽可能快。 <p>如果第一个参数是 JoinPoint 或 ProceedingJoinPoint 类
	 * 型，那么我们在该位置传递一个 JoinPoint （用于周围通知的 ProceedingJoinPoint ）。 <p>如果第一个参数的类型为 {@code JoinPoint
	 * .StaticPart}，那么我们在该位置传递 {@code JoinPoint.StaticPart}。 <p>剩余参数必须通过给定连接点处的切入点评估来绑定。我们将返回从参
	 * 数名称到值的映射。我们需要计算哪个通知参数需要绑定到哪个参数名称。有多种策略可以确定此绑定，这些策略排列在 ChainOfResponsibility 中。
	 */
	public final void calculateArgumentBindings() {
		// 简单的情况...没有什么可以绑定的。
		if (this.argumentsIntrospected || this.parameterTypes.length == 0) {
			return;
		}

		int numUnboundArgs = this.parameterTypes.length;
		Class<?>[] parameterTypes = this.aspectJAdviceMethod.getParameterTypes();
		if (maybeBindJoinPoint(parameterTypes[0]) || maybeBindProceedingJoinPoint(parameterTypes[0]) ||
				maybeBindJoinPointStaticPart(parameterTypes[0])) {
			numUnboundArgs--;
		}

		if (numUnboundArgs > 0) {
			// 需要按切入点匹配返回的名称绑定参数
			bindArgumentsByName(numUnboundArgs);
		}

		this.argumentsIntrospected = true;
	}

	/**
	 * 方法 `maybeBindJoinPoint`：完成本类中与「maybe Bind Join Point」相关的职责。
	 */
	private boolean maybeBindJoinPoint(Class<?> candidateParameterType) {
		if (JoinPoint.class == candidateParameterType) {
			this.joinPointArgumentIndex = 0;
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * 方法 `maybeBindProceedingJoinPoint`：完成本类中与「maybe Bind Proceeding Join Point」相关的职责。
	 */
	private boolean maybeBindProceedingJoinPoint(Class<?> candidateParameterType) {
		if (ProceedingJoinPoint.class == candidateParameterType) {
			if (!supportsProceedingJoinPoint()) {
				throw new IllegalArgumentException("ProceedingJoinPoint is only supported for around advice");
			}
			this.joinPointArgumentIndex = 0;
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * 方法 `supportsProceedingJoinPoint`：完成本类中与「supports Proceeding Join Point」相关的职责。
	 */
	protected boolean supportsProceedingJoinPoint() {
		return false;
	}

	/**
	 * 方法 `maybeBindJoinPointStaticPart`：完成本类中与「maybe Bind Join Point Static Part」相关的职责。
	 */
	private boolean maybeBindJoinPointStaticPart(Class<?> candidateParameterType) {
		if (JoinPoint.StaticPart.class == candidateParameterType) {
			this.joinPointStaticPartArgumentIndex = 0;
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * 绑定：Arguments By Name（方法 `bindArgumentsByName`）。
	 */
	private void bindArgumentsByName(int numArgumentsExpectingToBind) {
		if (this.argumentNames == null) {
			this.argumentNames = createParameterNameDiscoverer().getParameterNames(this.aspectJAdviceMethod);
		}
		if (this.argumentNames != null) {
			// 我们已经能够确定参数名称。
			bindExplicitArguments(numArgumentsExpectingToBind);
		}
		else {
			throw new IllegalStateException("Advice method [" + this.aspectJAdviceMethod.getName() + "] " +
					"requires " + numArgumentsExpectingToBind + " arguments to be bound by name, but " +
					"the argument names were not specified and could not be discovered.");
		}
	}

	/**
	 * 创建用于参数绑定的 ParameterNameDiscoverer。 <p> 默认实现创建 {@link DefaultParameterNameDiscoverer}
	 * 并添加专门配置的 {@link AspectJAdviceParameterNameDiscoverer}。
	 */
	protected ParameterNameDiscoverer createParameterNameDiscoverer() {
		// 我们需要发现它们，或者如果失败了，猜猜，
		// 如果我们不能 100% 准确地猜测，那就失败。
		DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
		AspectJAdviceParameterNameDiscoverer adviceParameterNameDiscoverer =
				new AspectJAdviceParameterNameDiscoverer(this.pointcut.getExpression());
		adviceParameterNameDiscoverer.setReturningName(this.returningName);
		adviceParameterNameDiscoverer.setThrowingName(this.throwingName);
		// 在链条中排在最后，所以如果我们被叫到并且我们失败了，那就糟糕了......
		adviceParameterNameDiscoverer.setRaiseExceptions(true);
		discoverer.addDiscoverer(adviceParameterNameDiscoverer);
		return discoverer;
	}

	/**
	 * 绑定：Explicit Arguments（方法 `bindExplicitArguments`）。
	 */
	@SuppressWarnings("NullAway") // Dataflow analysis limitation
	private void bindExplicitArguments(int numArgumentsLeftToBind) {
		Assert.state(this.argumentNames != null, "No argument names available");
		this.argumentBindings = new HashMap<>();

		int numExpectedArgumentNames = this.aspectJAdviceMethod.getParameterCount();
		if (this.argumentNames.length != numExpectedArgumentNames) {
			throw new IllegalStateException("Expecting to find " + numExpectedArgumentNames +
					" arguments to bind by name in advice, but actually found " +
					this.argumentNames.length + " arguments.");
		}

		// 所以我们的数量是匹配的...
		int argumentIndexOffset = this.parameterTypes.length - numArgumentsLeftToBind;
		for (int i = argumentIndexOffset; i < this.argumentNames.length; i++) {
			this.argumentBindings.put(this.argumentNames[i], i);
		}

		// 检查返回和抛出是否在参数名称列表中，如果
		// 指定，并找到发现的参数类型。
		if (this.returningName != null) {
			if (!this.argumentBindings.containsKey(this.returningName)) {
				throw new IllegalStateException("Returning argument name '" + this.returningName +
						"' was not bound in advice arguments");
			}
			else {
				Integer index = this.argumentBindings.get(this.returningName);
				this.discoveredReturningType = this.aspectJAdviceMethod.getParameterTypes()[index];
				this.discoveredReturningGenericType = this.aspectJAdviceMethod.getGenericParameterTypes()[index];
			}
		}
		if (this.throwingName != null) {
			if (!this.argumentBindings.containsKey(this.throwingName)) {
				throw new IllegalStateException("Throwing argument name '" + this.throwingName +
						"' was not bound in advice arguments");
			}
			else {
				Integer index = this.argumentBindings.get(this.throwingName);
				this.discoveredThrowingType = this.aspectJAdviceMethod.getParameterTypes()[index];
			}
		}

		// 相应地配置切入点表达式。
		configurePointcutParameters(this.argumentNames, argumentIndexOffset);
	}

	/**
	 * 从 argumentIndexOffset 开始的所有参数都是切入点参数的候选者 - 但返回和抛出变量的处理方式不同，并且必须从列表中删除（如果存在）。
	 */
	private void configurePointcutParameters(String[] argumentNames, int argumentIndexOffset) {
		int numParametersToRemove = argumentIndexOffset;
		if (this.returningName != null) {
			numParametersToRemove++;
		}
		if (this.throwingName != null) {
			numParametersToRemove++;
		}
		String[] pointcutParameterNames = new String[argumentNames.length - numParametersToRemove];
		Class<?>[] pointcutParameterTypes = new Class<?>[pointcutParameterNames.length];
		Class<?>[] methodParameterTypes = this.aspectJAdviceMethod.getParameterTypes();

		int index = 0;
		for (int i = 0; i < argumentNames.length; i++) {
			if (i < argumentIndexOffset) {
				continue;
			}
			if (argumentNames[i].equals(this.returningName) ||
				argumentNames[i].equals(this.throwingName)) {
				continue;
			}
			pointcutParameterNames[index] = argumentNames[i];
			pointcutParameterTypes[index] = methodParameterTypes[i];
			index++;
		}

		this.pointcut.setParameterNames(pointcutParameterNames);
		this.pointcut.setParameterTypes(pointcutParameterTypes);
	}

	/**
	 * 获取方法执行连接点处的参数，并将一组参数输出到通知方法。
	 * @param jp 当前连接点
	 * @param jpMatch 与此执行连接点相匹配的连接点匹配
	 * @param returnValue 方法执行的返回值（可能为 null）
	 * @param ex 方法执行抛出的异常（可能为null）
	 * @return 如果没有参数则为空数组
	 */
	protected @Nullable Object[] argBinding(JoinPoint jp, @Nullable JoinPointMatch jpMatch,
			@Nullable Object returnValue, @Nullable Throwable ex) {

		calculateArgumentBindings();

		// AMC启动
		@Nullable Object[] adviceInvocationArgs = new Object[this.parameterTypes.length];
		int numBound = 0;

		if (this.joinPointArgumentIndex != -1) {
			adviceInvocationArgs[this.joinPointArgumentIndex] = jp;
			numBound++;
		}
		else if (this.joinPointStaticPartArgumentIndex != -1) {
			adviceInvocationArgs[this.joinPointStaticPartArgumentIndex] = jp.getStaticPart();
			numBound++;
		}

		if (!CollectionUtils.isEmpty(this.argumentBindings)) {
			// 从切入点匹配绑定
			if (jpMatch != null) {
				PointcutParameter[] parameterBindings = jpMatch.getParameterBindings();
				for (PointcutParameter parameter : parameterBindings) {
					String name = parameter.getName();
					Integer index = this.argumentBindings.get(name);
					Assert.state(index != null, "Index must not be null");
					adviceInvocationArgs[index] = parameter.getBinding();
					numBound++;
				}
			}
			// 来自 return 子句的绑定
			if (this.returningName != null) {
				Integer index = this.argumentBindings.get(this.returningName);
				Assert.state(index != null, "Index must not be null");
				adviceInvocationArgs[index] = returnValue;
				numBound++;
			}
			// 从抛出的异常中绑定
			if (this.throwingName != null) {
				Integer index = this.argumentBindings.get(this.throwingName);
				Assert.state(index != null, "Index must not be null");
				adviceInvocationArgs[index] = ex;
				numBound++;
			}
		}

		if (numBound != this.parameterTypes.length) {
			throw new IllegalStateException("Required to bind " + this.parameterTypes.length +
					" arguments, but only bound " + numBound + " (JoinPointMatch " +
					(jpMatch == null ? "was NOT" : "WAS") + " bound in invocation)");
		}

		return adviceInvocationArgs;
	}


	/**
	 * 调用建议方法。
	 * @param jpMatch 与此执行连接点匹配的 JoinPointMatch
	 * @param returnValue 方法执行的返回值（可能为 null）
	 * @param ex 方法执行抛出的异常（可能为null）
	 * @return 调用结果
	 * @throws Throwable 如果调用失败
	 */
	protected @Nullable Object invokeAdviceMethod(@Nullable JoinPointMatch jpMatch,
			@Nullable Object returnValue, @Nullable Throwable ex) throws Throwable {

		return invokeAdviceMethodWithGivenArgs(argBinding(getJoinPoint(), jpMatch, returnValue, ex));
	}

	// 如上所述，但在本例中我们得到了连接点。
	/**
	 * 调用：Advice Method（方法 `invokeAdviceMethod`）。
	 */
	protected @Nullable Object invokeAdviceMethod(JoinPoint jp, @Nullable JoinPointMatch jpMatch,
			@Nullable Object returnValue, @Nullable Throwable t) throws Throwable {

		return invokeAdviceMethodWithGivenArgs(argBinding(jp, jpMatch, returnValue, t));
	}

	/**
	 * 调用：Advice Method With Given Args（方法 `invokeAdviceMethodWithGivenArgs`）。
	 */
	protected @Nullable Object invokeAdviceMethodWithGivenArgs(@Nullable Object[] args) throws Throwable {
		@Nullable Object[] actualArgs = args;
		if (this.aspectJAdviceMethod.getParameterCount() == 0) {
			actualArgs = null;
		}
		Object aspectInstance = this.aspectInstanceFactory.getAspectInstance();
		if (aspectInstance.equals(null)) {
			// 可能是 NullBean -> 如果需要，只需继续。
			if (getJoinPoint() instanceof ProceedingJoinPoint pjp) {
				return pjp.proceed();
			}
			return null;
		}
		try {
			ReflectionUtils.makeAccessible(this.aspectJAdviceMethod);
			return this.aspectJAdviceMethod.invoke(aspectInstance, actualArgs);
		}
		catch (IllegalArgumentException ex) {
			throw new AopInvocationException("Mismatch on arguments to advice method [" +
					this.aspectJAdviceMethod + "]; pointcut expression [" +
					this.pointcut.getPointcutExpression() + "]", ex);
		}
		catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}
	}

	/**
	 * 覆盖周围建议以返回正在进行的连接点。
	 */
	protected JoinPoint getJoinPoint() {
		return currentJoinPoint();
	}

	/**
	 * 获取我们正在调度的连接点的当前连接点匹配。
	 */
	protected @Nullable JoinPointMatch getJoinPointMatch() {
		MethodInvocation mi = ExposeInvocationInterceptor.currentInvocation();
		if (!(mi instanceof ProxyMethodInvocation pmi)) {
			throw new IllegalStateException("MethodInvocation is not a Spring ProxyMethodInvocation: " + mi);
		}
		return getJoinPointMatch(pmi);
	}

	// 注意：我们不能使用 JoinPointMatch.getClass().getName() 作为键，因为
	// Spring AOP 在连接点处进行所有匹配，然后进行所有调用。
	// 在这种情况下，如果我们只使用 JoinPointMatch 作为键，那么
	// “最后一个人获胜”这根本不是我们想要的。
	// 使用表达式保证是安全的，因为有 2 个相同的表达式
	// 保证以完全相同的方式绑定。
	/**
	 * 获取 Join Point Match（`JoinPointMatch`）。
	 */
	protected @Nullable JoinPointMatch getJoinPointMatch(ProxyMethodInvocation pmi) {
		String expression = this.pointcut.getExpression();
		return (expression != null ? (JoinPointMatch) pmi.getUserAttribute(expression) : null);
	}


	/**
	 * 返回字符串表示。
	 */
	@Override
	public String toString() {
		return getClass().getName() + ": advice method [" + this.aspectJAdviceMethod + "]; " +
				"aspect name '" + this.aspectName + "'";
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
	 * MethodMatcher 排除指定的建议方法。
	 * @see AbstractAspectJAdvice#buildSafePointcut()
	 */
	private static class AdviceExcludingMethodMatcher extends StaticMethodMatcher {

		private final Method adviceMethod;

		public AdviceExcludingMethodMatcher(Method adviceMethod) {
			this.adviceMethod = adviceMethod;
		}

		@Override
		public boolean matches(Method method, Class<?> targetClass) {
			return !this.adviceMethod.equals(method);
		}

		@Override
		public boolean equals(@Nullable Object other) {
			return (this == other || (other instanceof AdviceExcludingMethodMatcher that &&
					this.adviceMethod.equals(that.adviceMethod)));
		}

		@Override
		public int hashCode() {
			return this.adviceMethod.hashCode();
		}

		@Override
		public String toString() {
			return getClass().getName() + ": " + this.adviceMethod;
		}
	}

}
