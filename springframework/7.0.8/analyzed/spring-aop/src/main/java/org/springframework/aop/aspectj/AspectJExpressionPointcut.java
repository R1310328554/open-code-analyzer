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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Set;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.weaver.patterns.NamePattern;
import org.aspectj.weaver.reflect.ReflectionWorld.ReflectionWorldException;
import org.aspectj.weaver.reflect.ShadowMatchImpl;
import org.aspectj.weaver.tools.ContextBasedMatcher;
import org.aspectj.weaver.tools.FuzzyBoolean;
import org.aspectj.weaver.tools.JoinPointMatch;
import org.aspectj.weaver.tools.MatchingContext;
import org.aspectj.weaver.tools.PointcutDesignatorHandler;
import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParameter;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.PointcutPrimitive;
import org.aspectj.weaver.tools.ShadowMatch;
import org.aspectj.weaver.tools.UnsupportedPointcutPrimitiveException;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.IntroductionAwareMethodMatcher;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.framework.autoproxy.ProxyCreationContext;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.aop.support.AbstractExpressionPointcut;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Spring {@link org.springframework.aop.Pointcut} 实现使用 AspectJ 编织器来计算切入点表达式。
 * <p>切入点表达式值是AspectJ表达式。这可以引用其他切入点并使用组合和其他操作。
 * <p>自然地，由于这是由Spring AOP的基于代理的模型处理的，因此仅支持方法执行切入点。
 * @author Rob Harrop
 * @author Adrian Colyer
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @author Dave Syer
 * @author Yanming Zhou
 * @since 2.0
 */
@SuppressWarnings("serial")
public class AspectJExpressionPointcut extends AbstractExpressionPointcut
		implements ClassFilter, IntroductionAwareMethodMatcher, BeanFactoryAware {

	private static final String AJC_MAGIC = "ajc$";

	/**
	 * 方法 `of`：完成本类中与「of」相关的职责。
	 */
	private static final Set<PointcutPrimitive> SUPPORTED_PRIMITIVES = Set.of(
			PointcutPrimitive.EXECUTION,
			PointcutPrimitive.ARGS,
			PointcutPrimitive.REFERENCE,
			PointcutPrimitive.THIS,
			PointcutPrimitive.TARGET,
			PointcutPrimitive.WITHIN,
			PointcutPrimitive.AT_ANNOTATION,
			PointcutPrimitive.AT_WITHIN,
			PointcutPrimitive.AT_ARGS,
			PointcutPrimitive.AT_TARGET);

	/**
	 * 获取 Log（`Log`）。
	 */
	private static final Log logger = LogFactory.getLog(AspectJExpressionPointcut.class);

	/** 作用域相关状态（`pointcutDeclarationScope`）。 */
	private @Nullable Class<?> pointcutDeclarationScope;

	/** `aspectCompiledByAjc`：该类的成员状态。 */
	private boolean aspectCompiledByAjc;

	private String[] pointcutParameterNames = new String[0];

	private Class<?>[] pointcutParameterTypes = new Class<?>[0];

	/** 底层 BeanFactory 引用。 */
	private @Nullable BeanFactory beanFactory;

	/** 类相关状态（`pointcutClassLoader`）。 */
	private transient volatile @Nullable ClassLoader pointcutClassLoader;

	/** 切点相关状态（`pointcutExpression`）。 */
	private transient volatile @Nullable PointcutExpression pointcutExpression;

	/** 切点相关状态（`pointcutParsingFailed`）。 */
	private transient volatile boolean pointcutParsingFailed;


	/**
	 * 创建一个新的默认 AspectJExpressionPointcut。
	 */
	public AspectJExpressionPointcut() {
	}

	/**
	 * 使用给定的设置创建一个新的 AspectJExpressionPointcut。
	 * @param declarationScope 切入点的声明范围
	 * @param paramNames 切入点的参数名称
	 * @param paramTypes 切入点的参数类型
	 */
	public AspectJExpressionPointcut(Class<?> declarationScope, String[] paramNames, Class<?>[] paramTypes) {
		setPointcutDeclarationScope(declarationScope);
		if (paramNames.length != paramTypes.length) {
			throw new IllegalStateException(
					"Number of pointcut parameter names must match number of pointcut parameter types");
		}
		this.pointcutParameterNames = paramNames;
		this.pointcutParameterTypes = paramTypes;
	}


	/**
	 * 设置切入点的声明范围。
	 */
	public void setPointcutDeclarationScope(Class<?> pointcutDeclarationScope) {
		this.pointcutDeclarationScope = pointcutDeclarationScope;
		this.aspectCompiledByAjc = compiledByAjc(pointcutDeclarationScope);
	}

	/**
	 * 设置切入点的参数名称。
	 */
	public void setParameterNames(String... names) {
		this.pointcutParameterNames = names;
	}

	/**
	 * 设置切入点的参数类型。
	 */
	public void setParameterTypes(Class<?>... types) {
		this.pointcutParameterTypes = types;
	}

	/**
	 * 设置 Bean Factory（`BeanFactory`）。
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}


	/**
	 * 获取 Class Filter（`ClassFilter`）。
	 */
	@Override
	public ClassFilter getClassFilter() {
		checkExpression();
		return this;
	}

	/**
	 * 获取 Method Matcher（`MethodMatcher`）。
	 */
	@Override
	public MethodMatcher getMethodMatcher() {
		checkExpression();
		return this;
	}


	/**
	 * 检查这个切入点是否准备好匹配。
	 */
	private void checkExpression() {
		if (getExpression() == null) {
			throw new IllegalStateException("Must set property 'expression' before attempting to match");
		}
	}

	/**
	 * 延迟构建底层 AspectJ 切入点表达式。
	 */
	private PointcutExpression obtainPointcutExpression() {
		PointcutExpression pointcutExpression = this.pointcutExpression;
		if (pointcutExpression == null) {
			ClassLoader pointcutClassLoader = determinePointcutClassLoader();
			pointcutExpression = buildPointcutExpression(pointcutClassLoader);
			this.pointcutClassLoader = pointcutClassLoader;
			this.pointcutExpression = pointcutExpression;
		}
		return pointcutExpression;
	}

	/**
	 * 确定用于切入点评估的类加载器。
	 */
	private @Nullable ClassLoader determinePointcutClassLoader() {
		if (this.beanFactory instanceof ConfigurableBeanFactory cbf) {
			return cbf.getBeanClassLoader();
		}
		if (this.pointcutDeclarationScope != null) {
			return this.pointcutDeclarationScope.getClassLoader();
		}
		return ClassUtils.getDefaultClassLoader();
	}

	/**
	 * 构建底层 AspectJ 切入点表达式。
	 */
	private PointcutExpression buildPointcutExpression(@Nullable ClassLoader classLoader) {
		PointcutParser parser = initializePointcutParser(classLoader);
		PointcutParameter[] pointcutParameters = new PointcutParameter[this.pointcutParameterNames.length];
		for (int i = 0; i < pointcutParameters.length; i++) {
			pointcutParameters[i] = parser.createPointcutParameter(
					this.pointcutParameterNames[i], this.pointcutParameterTypes[i]);
		}
		return parser.parsePointcutExpression(replaceBooleanOperators(resolveExpression()),
				this.pointcutDeclarationScope, pointcutParameters);
	}

	/**
	 * 解析：Expression（方法 `resolveExpression`）。
	 */
	private String resolveExpression() {
		String expression = getExpression();
		Assert.state(expression != null, "No expression set");
		return expression;
	}

	/**
	 * 初始化底层 AspectJ 切入点解析器。
	 */
	private PointcutParser initializePointcutParser(@Nullable ClassLoader classLoader) {
		PointcutParser parser = PointcutParser
				.getPointcutParserSupportingSpecifiedPrimitivesAndUsingSpecifiedClassLoaderForResolution(
						SUPPORTED_PRIMITIVES, classLoader);
		parser.registerPointcutDesignatorHandler(new BeanPointcutDesignatorHandler());
		return parser;
	}


	/**
	 * 如果在 XML 中指定了切入点表达式，则用户不能将“and”写为“&&”（尽管 {@code &amp;&amp;} 可以工作）。 <p>我们还允许两个切入点子表达式之间存在“
	 * and”。 <p>此方法转换回 AspectJ 切入点解析器的 {@code &&}。
	 */
	private String replaceBooleanOperators(String pcExpr) {
		String result = StringUtils.replace(pcExpr, " and ", " && ");
		result = StringUtils.replace(result, " or ", " || ");
		result = StringUtils.replace(result, " not ", " ! ");
		return result;
	}


	/**
	 * 返回基础 AspectJ 切入点表达式。
	 */
	public PointcutExpression getPointcutExpression() {
		return obtainPointcutExpression();
	}

	/**
	 * 匹配：es（方法 `matches`）。
	 */
	@Override
	public boolean matches(Class<?> targetClass) {
		if (this.pointcutParsingFailed) {
			// 在下面之前切入点解析失败 -> 避免重试。
			return false;
		}
		if (this.aspectCompiledByAjc && compiledByAjc(targetClass)) {
			// ajc 编译的目标类的 ajc 编译的方面类 -> 已经编织。
			return false;
		}

		try {
			try {
				return obtainPointcutExpression().couldMatchJoinPointsInType(targetClass);
			}
			catch (ReflectionWorldException ex) {
				logger.debug("PointcutExpression matching rejected target class - trying fallback expression", ex);
				// 实际上这仍然是一个“也许”——如果我们还不够了解，请将切入点视为动态
				PointcutExpression fallbackExpression = getFallbackPointcutExpression(targetClass);
				if (fallbackExpression != null) {
					return fallbackExpression.couldMatchJoinPointsInType(targetClass);
				}
			}
		}
		catch (IllegalArgumentException | IllegalStateException | UnsupportedPointcutPrimitiveException ex) {
			this.pointcutParsingFailed = true;
			if (logger.isDebugEnabled()) {
				logger.debug("Pointcut parser rejected expression [" + getExpression() + "]: " + ex);
			}
		}
		catch (Throwable ex) {
			logger.debug("PointcutExpression matching rejected target class", ex);
		}
		return false;
	}

	/**
	 * 匹配：es（方法 `matches`）。
	 */
	@Override
	public boolean matches(Method method, Class<?> targetClass, boolean hasIntroductions) {
		ShadowMatch shadowMatch = getTargetShadowMatch(method, targetClass);

		// 对 this、target、@this、@target、@annotation 的特殊处理
		// 在 Spring - 我们可以优化，因为我们知道我们有这个类，
		// 并且在运行时永远不会有匹配的子类。
		if (shadowMatch.alwaysMatches()) {
			return true;
		}
		else if (shadowMatch.neverMatches()) {
			return false;
		}
		else {
			// 可能的情况
			if (hasIntroductions) {
				return true;
			}
			// 可能返回匹配测试 - 如果有任何子类型敏感变量
			// 参与测试(this, target, at_this, at_target, at_annotation) then
			// 我们说这不是一场比赛，因为春天永远不会有不同的比赛
			// 运行时子类型。
			RuntimeTestWalker walker = getRuntimeTestWalker(shadowMatch);
			return (!walker.testsSubtypeSensitiveVars() || walker.testTargetInstanceOfResidue(targetClass));
		}
	}

	/**
	 * 匹配：es（方法 `matches`）。
	 */
	@Override
	public boolean matches(Method method, Class<?> targetClass) {
		return matches(method, targetClass, false);
	}

	/**
	 * 判断是否 Runtime。
	 */
	@Override
	public boolean isRuntime() {
		return obtainPointcutExpression().mayNeedDynamicTest();
	}

	/**
	 * 匹配：es（方法 `matches`）。
	 */
	@Override
	public boolean matches(Method method, Class<?> targetClass, @Nullable Object... args) {
		ShadowMatch shadowMatch = getTargetShadowMatch(method, targetClass);

		// 将 Spring AOP 代理绑定到 AspectJ“this”，将 Spring AOP 目标绑定到 AspectJ 目标，
		// 与 MethodInitationProceedingJoinPoint 的返回一致
		ProxyMethodInvocation pmi = null;
		Object targetObject = null;
		Object thisObject = null;
		try {
			MethodInvocation curr = ExposeInvocationInterceptor.currentInvocation();
			if (curr.getMethod() == method) {
				targetObject = curr.getThis();
				if (!(curr instanceof ProxyMethodInvocation currPmi)) {
					throw new IllegalStateException("MethodInvocation is not a Spring ProxyMethodInvocation: " + curr);
				}
				pmi = currPmi;
				thisObject = pmi.getProxy();
			}
		}
		catch (IllegalStateException ex) {
			// 当前没有调用...
			if (logger.isDebugEnabled()) {
				logger.debug("Could not access current invocation - matching with limited context: " + ex);
			}
		}

		try {
			JoinPointMatch joinPointMatch = shadowMatch.matchesJoinPoint(thisObject, targetObject, args);

			/*
			 * Do a final check to see if any this(TYPE) kind of residue match. For
			 * this purpose, we use the original method's (proxy method's) shadow to
			 * ensure that 'this' is correctly checked against. Without this check,
			 * we get incorrect match on this(TYPE) where TYPE matches the target
			 * type but not 'this' (as would be the case of JDK dynamic proxies).
			 * <p>See SPR-2979 for the original bug.
			 */
			if (pmi != null && thisObject != null) {  // there is a current invocation
				RuntimeTestWalker originalMethodResidueTest = getRuntimeTestWalker(getShadowMatch(method, method));
				if (!originalMethodResidueTest.testThisInstanceOfResidue(thisObject.getClass())) {
					return false;
				}
				if (joinPointMatch.matches()) {
					bindParameters(pmi, joinPointMatch);
				}
			}

			return joinPointMatch.matches();
		}
		catch (Throwable ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Failed to evaluate join point for arguments " + Arrays.toString(args) +
						" - falling back to non-match", ex);
			}
			return false;
		}
	}

	/**
	 * 获取 Current Proxied Bean Name（`CurrentProxiedBeanName`）。
	 */
	protected @Nullable String getCurrentProxiedBeanName() {
		return ProxyCreationContext.getCurrentProxiedBeanName();
	}


	/**
	 * 根据目标类的加载器而不是默认的加载器获取新的切入点表达式。
	 */
	private @Nullable PointcutExpression getFallbackPointcutExpression(Class<?> targetClass) {
		try {
			ClassLoader classLoader = targetClass.getClassLoader();
			if (classLoader != null && classLoader != this.pointcutClassLoader) {
				return buildPointcutExpression(classLoader);
			}
		}
		catch (Throwable ex) {
			logger.debug("Failed to create fallback PointcutExpression", ex);
		}
		return null;
	}

	/**
	 * 获取 Runtime Test Walker（`RuntimeTestWalker`）。
	 */
	private RuntimeTestWalker getRuntimeTestWalker(ShadowMatch shadowMatch) {
		if (shadowMatch instanceof DefensiveShadowMatch defensiveShadowMatch) {
			return new RuntimeTestWalker(defensiveShadowMatch.primary);
		}
		return new RuntimeTestWalker(shadowMatch);
	}

	/**
	 * 绑定：Parameters（方法 `bindParameters`）。
	 */
	private void bindParameters(ProxyMethodInvocation invocation, JoinPointMatch jpm) {
		// 注意：不能使用 JoinPointMatch.getClass().getName() 作为键，因为
		// Spring AOP 在连接点处进行所有匹配，然后进行所有调用
		// 在这种情况下，如果我们只使用 JoinPointMatch 作为键，那么
		// “最后一个人获胜”这根本不是我们想要的。
		// 使用表达式保证是安全的，因为有 2 个相同的表达式
		// 保证以完全相同的方式绑定。
		invocation.setUserAttribute(resolveExpression(), jpm);
	}

	/**
	 * 获取 Target Shadow Match（`TargetShadowMatch`）。
	 */
	private ShadowMatch getTargetShadowMatch(Method method, Class<?> targetClass) {
		Method targetMethod = AopUtils.getMostSpecificMethod(method, targetClass);
		if (targetMethod.getDeclaringClass().isInterface() && targetMethod.getDeclaringClass() != targetClass &&
				obtainPointcutExpression().getPointcutExpression().contains("." + targetMethod.getName() + "(")) {
			// 尝试为继承的方法构建最具体的接口
			// 也考虑子接口匹配，特别是代理类。
			// 注意：AspectJ 仅考虑 Method.getDeclaringClass()。
			Set<Class<?>> ifcs = ClassUtils.getAllInterfacesForClassAsSet(targetClass);
			if (ifcs.size() > 1) {
				try {
					Class<?> compositeInterface = ClassUtils.createCompositeInterface(
							ClassUtils.toClassArray(ifcs), targetClass.getClassLoader());
					targetMethod = ClassUtils.getMostSpecificMethod(targetMethod, compositeInterface);
				}
				catch (IllegalArgumentException ex) {
					// 实现的接口可能会暴露冲突的方法签名......
					// 继续原来的目标方法。
				}
			}
		}
		return getShadowMatch(targetMethod, method);
	}

	/**
	 * 获取 Shadow Match（`ShadowMatch`）。
	 */
	private ShadowMatch getShadowMatch(Method targetMethod, Method originalMethod) {
		ShadowMatchKey key = new ShadowMatchKey(this, targetMethod);
		ShadowMatch shadowMatch = ShadowMatchUtils.getShadowMatch(key);
		if (shadowMatch == null) {
			PointcutExpression pointcutExpression = obtainPointcutExpression();
			synchronized (pointcutExpression) {
				shadowMatch = ShadowMatchUtils.getShadowMatch(key);
				if (shadowMatch != null) {
					return shadowMatch;
				}
				PointcutExpression fallbackExpression = null;
				Method methodToMatch = targetMethod;
				try {
					try {
						shadowMatch = pointcutExpression.matchesMethodExecution(methodToMatch);
					}
					catch (ReflectionWorldException ex) {
						// 无法自省目标方法，可能是因为它已被加载
						// 在一个特殊的类加载器中。让我们尝试声明 ClassLoader...
						try {
							fallbackExpression = getFallbackPointcutExpression(methodToMatch.getDeclaringClass());
							if (fallbackExpression != null) {
								shadowMatch = fallbackExpression.matchesMethodExecution(methodToMatch);
							}
						}
						catch (ReflectionWorldException ex2) {
							fallbackExpression = null;
						}
					}
					if (targetMethod != originalMethod && (shadowMatch == null ||
							(Proxy.isProxyClass(targetMethod.getDeclaringClass()) &&
									(shadowMatch.neverMatches() || containsAnnotationPointcut())))) {
						// 如果没有可解析的匹配或
						// 代理类上的负匹配（其上不带有任何注释）
						// 重新声明的方法），以及注释切入点。
						methodToMatch = originalMethod;
						try {
							shadowMatch = pointcutExpression.matchesMethodExecution(methodToMatch);
						}
						catch (ReflectionWorldException ex) {
							// 既不能内省目标类，也不能内省代理类 ->
							// 在放弃之前让我们尝试一下原始方法的声明类......
							try {
								fallbackExpression = getFallbackPointcutExpression(methodToMatch.getDeclaringClass());
								if (fallbackExpression != null) {
									shadowMatch = fallbackExpression.matchesMethodExecution(methodToMatch);
								}
							}
							catch (ReflectionWorldException ex2) {
								fallbackExpression = null;
							}
						}
					}
				}
				catch (Throwable ex) {
					// 可能 AspectJ 1.8.10 遇到无效签名
					logger.debug("PointcutExpression matching rejected target method", ex);
					fallbackExpression = null;
				}
				if (shadowMatch == null) {
					shadowMatch = new ShadowMatchImpl(org.aspectj.util.FuzzyBoolean.NO, null, null, null);
				}
				else if (shadowMatch.maybeMatches() && fallbackExpression != null) {
					shadowMatch = new DefensiveShadowMatch(shadowMatch,
							fallbackExpression.matchesMethodExecution(methodToMatch));
				}
				shadowMatch = ShadowMatchUtils.setShadowMatch(key, shadowMatch);
			}
		}
		return shadowMatch;
	}

	/**
	 * 方法 `containsAnnotationPointcut`：完成本类中与「contains Annotation Pointcut」相关的职责。
	 */
	private boolean containsAnnotationPointcut() {
		return resolveExpression().contains("@annotation");
	}

	/**
	 * 方法 `compiledByAjc`：完成本类中与「compiled By Ajc」相关的职责。
	 */
	private static boolean compiledByAjc(Class<?> clazz) {
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getName().startsWith(AJC_MAGIC)) {
				return true;
			}
		}
		Class<?> superclass = clazz.getSuperclass();
		return (superclass != null && compiledByAjc(superclass));
	}


	/**
	 * 比较是否相等。
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof AspectJExpressionPointcut that &&
				ObjectUtils.nullSafeEquals(getExpression(), that.getExpression()) &&
				ObjectUtils.nullSafeEquals(this.pointcutDeclarationScope, that.pointcutDeclarationScope) &&
				ObjectUtils.nullSafeEquals(this.pointcutParameterNames, that.pointcutParameterNames) &&
				ObjectUtils.nullSafeEquals(this.pointcutParameterTypes, that.pointcutParameterTypes)));
	}

	/**
	 * 判断是否包含/具备 h Code。
	 */
	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHash(getExpression(), this.pointcutDeclarationScope,
				this.pointcutParameterNames, this.pointcutParameterTypes);
	}

	/**
	 * 返回字符串表示。
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("AspectJExpressionPointcut: (");
		for (int i = 0; i < this.pointcutParameterTypes.length; i++) {
			sb.append(this.pointcutParameterTypes[i].getName());
			sb.append(' ');
			sb.append(this.pointcutParameterNames[i]);
			if ((i+1) < this.pointcutParameterTypes.length) {
				sb.append(", ");
			}
		}
		sb.append(") ");
		if (getExpression() != null) {
			sb.append(getExpression());
		}
		else {
			sb.append("<pointcut expression not set>");
		}
		return sb.toString();
	}


	/**
	 * 用于 AspectJ 的 Spring 特定 {@code bean()} 切入点指示符扩展的处理程序。 <p>此处理程序必须添加到需要处理 {@code bean()} PC
	 * D 的每个切入点对象中。匹配上下文是通过检查线程局部变量自动获得的，因此不需要在切入点上设置匹配上下文。
	 */
	private class BeanPointcutDesignatorHandler implements PointcutDesignatorHandler {

		private static final String BEAN_DESIGNATOR_NAME = "bean";

		@Override
		public String getDesignatorName() {
			return BEAN_DESIGNATOR_NAME;
		}

		@Override
		public ContextBasedMatcher parse(String expression) {
			return new BeanContextMatcher(expression);
		}
	}


	/**
	 * BeanNamePointcutDesignatorHandler 的匹配器类。 <p> 此匹配器的动态匹配测试始终返回 true，因为匹配决策是在代理创建时做出的。对于静态匹
	 * 配测试，即使在 bean() 切入点使用否定时，此匹配器也会放弃以允许整体切入点匹配。
	 */
	private class BeanContextMatcher implements ContextBasedMatcher {

		private final NamePattern expressionPattern;

		public BeanContextMatcher(String expression) {
			this.expressionPattern = new NamePattern(expression);
		}

		@Override
		@SuppressWarnings("rawtypes")
		@Deprecated(since = "4.0") // deprecated by AspectJ
		public boolean couldMatchJoinPointsInType(Class someClass) {
			return (contextMatch(someClass) == FuzzyBoolean.YES);
		}

		@Override
		@SuppressWarnings("rawtypes")
		@Deprecated(since = "4.0") // deprecated by AspectJ
		public boolean couldMatchJoinPointsInType(Class someClass, MatchingContext context) {
			return (contextMatch(someClass) == FuzzyBoolean.YES);
		}

		@Override
		public boolean matchesDynamically(MatchingContext context) {
			return true;
		}

		@Override
		public FuzzyBoolean matchesStatically(MatchingContext context) {
			return contextMatch(null);
		}

		@Override
		public boolean mayNeedDynamicTest() {
			return false;
		}

		private FuzzyBoolean contextMatch(@Nullable Class<?> targetType) {
			String advisedBeanName = getCurrentProxiedBeanName();
			if (advisedBeanName == null) {  // no proxy creation in progress
				// 弃权；不能返回 YES，因为这会使切入点否定失败
				return FuzzyBoolean.MAYBE;
			}
			if (BeanFactoryUtils.isGeneratedBeanName(advisedBeanName)) {
				return FuzzyBoolean.NO;
			}
			if (targetType != null) {
				boolean isFactory = FactoryBean.class.isAssignableFrom(targetType);
				return FuzzyBoolean.fromBoolean(
						matchesBean(isFactory ? BeanFactory.FACTORY_BEAN_PREFIX + advisedBeanName : advisedBeanName));
			}
			else {
				return FuzzyBoolean.fromBoolean(matchesBean(advisedBeanName) ||
						matchesBean(BeanFactory.FACTORY_BEAN_PREFIX + advisedBeanName));
			}
		}

		private boolean matchesBean(String advisedBeanName) {
			return BeanFactoryAnnotationUtils.isQualifierMatch(
					this.expressionPattern::matches, advisedBeanName, beanFactory);
		}
	}


	private static class DefensiveShadowMatch implements ShadowMatch {

		private final ShadowMatch primary;

		private final ShadowMatch other;

		public DefensiveShadowMatch(ShadowMatch primary, ShadowMatch other) {
			this.primary = primary;
			this.other = other;
		}

		@Override
		public boolean alwaysMatches() {
			return this.primary.alwaysMatches();
		}

		@Override
		public boolean maybeMatches() {
			return this.primary.maybeMatches();
		}

		@Override
		public boolean neverMatches() {
			return this.primary.neverMatches();
		}

		@Override
		public JoinPointMatch matchesJoinPoint(Object thisObject, Object targetObject, Object[] args) {
			try {
				return this.primary.matchesJoinPoint(thisObject, targetObject, args);
			}
			catch (ReflectionWorldException ex) {
				return this.other.matchesJoinPoint(thisObject, targetObject, args);
			}
		}

		@Override
		public void setMatchingContext(MatchingContext aMatchContext) {
			this.primary.setMatchingContext(aMatchContext);
			this.other.setMatchingContext(aMatchContext);
		}
	}


	/**
	 * 方法 `ShadowMatchKey`：完成本类中与「Shadow Match Key」相关的职责。
	 */
	private record ShadowMatchKey(AspectJExpressionPointcut expression, Method method) {
	}

}
