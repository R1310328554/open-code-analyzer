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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.PointcutPrimitive;
import org.jspecify.annotations.Nullable;

import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.util.StringUtils;

/**
 * {@link ParameterNameDiscoverer} 实现尝试从切入点表达式、返回和抛出子句推导出建议方法的参数名称。若无法明确推断参数名，则返回 {@code null}。
 * <h3>算法摘要</h3> <p>如果可以推断出明确的绑定，那么它就是。如果不可能满足建议要求，则返回 {@code null}。通过将 {@link
 * #setRaiseExceptions(boolean) raiseExceptions} 属性设置为 {@code
 * true}，在无法发现参数名称的情况下，将引发描述性异常，而不是返回 {@code null}。
 * <h3>算法详细信息</h3> <p>该类按以下方式解释参数： <ol> <li> 如果方法的第一个参数是 {@link JoinPoint} 或 {@link
 * ProceedingJoinPoint} 类型，则假定用于将 {@code thisJoinPoint} 传递给通知，并且参数名称将被分配值{@code
 * "thisJoinPoint"}.</li> <li>如果该方法的第一个参数是 {@code JoinPoint.StaticPart} 类型，则假定用于将 {@code
 * "thisJoinPointStaticPart"} 传递给通知，并且参数名称将分配值 {@code "thisJoinPointStaticPart"}.</li> <li>
 * 如果已设置 {@link #setThrowingName(String) throwingName}，并且有没有 {@code Throwable+}
 * 类型的未绑定参数，则引发 {@link IllegalArgumentException}。如果存在多个 {@code Throwable+} 类型的未绑定参数，则会引发
 * {@link AmbiguousBindingException}。如果恰好存在一个 {@code Throwable+} 类型的未绑定参数，则为相应的参数名称分配值 <
 * throwingName>.</li> <li> 如果仍有未绑定参数，则检查切入点表达式。令 {@code a}
 * 为以绑定形式使用的基于注释的切入点表达式（&#64;annotation、&#64;this、&#64;target、&#64;args、&#64;within、&#64;withincode）的数量。绑定形式的用法本身是可以推导的：如果切入点内的表达式是满足
 * Java 变量名称约定的单个字符串文字，则假定它是变量名称。如果 {@code a} 为零，我们将进入下一阶段。如果 {@code a} > 1 然后出现 {@code
 * AmbiguousBindingException}。如果 {@code a} == 1，并且没有 {@code Annotation+} 类型的未绑定参数，则引发
 * {@code IllegalArgumentException}。如果恰好有一个这样的参数，则相应的参数名称将被分配切入点表达式中的值。 </li> <li> 如果已设置
 * {@code returningName}，并且没有未绑定的参数，则会引发 {@code IllegalArgumentException}。如果存在多个未绑定参数，则会引发
 * {@code AmbiguousBindingException}。如果恰好有一个未绑定参数，则将相应的参数名称分配为 {@code returningName}.</li>
 * <li> 的值。如果仍有未绑定参数，则再次检查切入点表达式以查找绑定形式中使用的 {@code this}、{@code target} 和 {@code args}
 * 切入点表达式（绑定形式按照基于注释的描述推导）切入点）。如果仍然存在多个原始类型的未绑定参数（只能在 {@code args} 中绑定），则会引发 {@code
 * AmbiguousBindingException}。如果恰好有一个基本类型的参数，那么如果恰好找到一个 {@code args}
 * 绑定变量，我们将相应的参数名称分配给变量名称。如果没有找到 {@code args} 绑定变量，则会引发 {@code IllegalStateException}。如果有多个
 * {@code args} 绑定变量，则会引发 {@code AmbiguousBindingException}。此时，如果仍有多个未绑定参数，我们将提出 {@code
 * AmbiguousBindingException}。如果没有剩余的未绑定参数，我们就完成了。如果仅剩下 1 个未绑定参数，并且只有 1 个候选变量名称未与 {@code
 * this}、{@code target} 或 {@code args} 绑定，则将其指定为相应的参数名称。如果有多种可能性，则会引发 {@code
 * AmbiguousBindingException}。 </li> </ol>
 * <p> 引发 {@code IllegalArgumentException} 或 {@code AmbiguousBindingException}
 * 的行为是可配置的，以允许将此发现器用作责任链的一部分。默认情况下，将记录条件，并且 {@link #getParameterNames(Method)} 方法将仅返回
 * {@code null}。如果 {@link #setRaiseExceptions(boolean) raiseExceptions} 属性设置为 {@code
 * true}，则条件将分别抛出为 {@code IllegalArgumentException} 和 {@code AmbiguousBindingException}。
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @since 2.0
 */
public class AspectJAdviceParameterNameDiscoverer implements ParameterNameDiscoverer {

	private static final String THIS_JOIN_POINT = "thisJoinPoint";
	private static final String THIS_JOIN_POINT_STATIC_PART = "thisJoinPointStaticPart";

	// 绑定算法的步骤...
	private static final int STEP_JOIN_POINT_BINDING = 1;
	private static final int STEP_THROWING_BINDING = 2;
	private static final int STEP_ANNOTATION_BINDING = 3;
	private static final int STEP_RETURNING_BINDING = 4;
	private static final int STEP_PRIMITIVE_ARGS_BINDING = 5;
	private static final int STEP_THIS_TARGET_ARGS_BINDING = 6;
	private static final int STEP_REFERENCE_PCUT_BINDING = 7;
	private static final int STEP_FINISHED = 8;

	/**
	 * 支持的 AspectJ 切入点原语集合。
	 */
	private static final Set<String> singleValuedAnnotationPcds = Set.of(
			"@this",
			"@target",
			"@within",
			"@withincode",
			"@annotation");

	private static final Set<String> nonReferencePointcutTokens = new HashSet<>();


	static {
		Set<PointcutPrimitive> pointcutPrimitives = PointcutParser.getAllSupportedPointcutPrimitives();
		for (PointcutPrimitive primitive : pointcutPrimitives) {
			nonReferencePointcutTokens.add(primitive.getName());
		}
		nonReferencePointcutTokens.add("&&");
		nonReferencePointcutTokens.add("!");
		nonReferencePointcutTokens.add("||");
		nonReferencePointcutTokens.add("and");
		nonReferencePointcutTokens.add("or");
		nonReferencePointcutTokens.add("not");
	}


	/**
	 */
	private final @Nullable String pointcutExpression;

	/** 异常相关状态（`raiseExceptions`）。 */
	private boolean raiseExceptions;

	/**
	 */
	private @Nullable String returningName;

	/**
	 */
	private @Nullable String throwingName;

	private Class<?>[] argumentTypes = new Class<?>[0];

	private @Nullable String[] parameterNameBindings = new String[0];

	/** `numberOfRemainingUnboundArguments`：该类的成员状态。 */
	private int numberOfRemainingUnboundArguments;


	/**
	 * 创建一个尝试发现参数名称的新发现器。从给定的切入点表达式。
	 */
	public AspectJAdviceParameterNameDiscoverer(@Nullable String pointcutExpression) {
		this.pointcutExpression = pointcutExpression;
	}


	/**
	 * 指示在无法推导建议参数名称的情况下是否必须适当地抛出 {@link IllegalArgumentException} 和 {@link AmbiguousBindingExc
	 * eption}。
	 * @param raiseExceptions {@code true} 如果要抛出异常
	 */
	public void setRaiseExceptions(boolean raiseExceptions) {
		this.raiseExceptions = raiseExceptions;
	}

	/**
	 * 如果 {@code afterReturning} 建议绑定返回值，则必须指定 {@code returning} 变量名。
	 * @param returningName 返回变量的名称
	 */
	public void setReturningName(@Nullable String returningName) {
		this.returningName = returningName;
	}

	/**
	 * 如果 {@code afterThrowing} 建议绑定抛出的值，则必须指定 {@code throwing} 变量名。
	 * @param throwingName 抛出变量的名称
	 */
	public void setThrowingName(@Nullable String throwingName) {
		this.throwingName = throwingName;
	}

	/**
	 * 推导建议方法的参数名称。 <p> 有关所用算法的详细信息，请参阅此类的 {@link AspectJAdviceParameterNameDiscoverer
	 * class-level javadoc}。
	 * @param method 目标 {@link Method}
	 * @return 参数名称
	 */
	@Override
	public @Nullable String @Nullable [] getParameterNames(Method method) {
		this.argumentTypes = method.getParameterTypes();
		this.numberOfRemainingUnboundArguments = this.argumentTypes.length;
		this.parameterNameBindings = new String[this.numberOfRemainingUnboundArguments];

		int minimumNumberUnboundArgs = 0;
		if (this.returningName != null) {
			minimumNumberUnboundArgs++;
		}
		if (this.throwingName != null) {
			minimumNumberUnboundArgs++;
		}
		if (this.numberOfRemainingUnboundArguments < minimumNumberUnboundArgs) {
			throw new IllegalStateException(
					"Not enough arguments in method to satisfy binding of returning and throwing variables");
		}

		try {
			int algorithmicStep = STEP_JOIN_POINT_BINDING;
			while (this.numberOfRemainingUnboundArguments > 0 && algorithmicStep < STEP_FINISHED) {
				switch (algorithmicStep++) {
					case STEP_JOIN_POINT_BINDING -> {
						if (!maybeBindThisJoinPoint()) {
							maybeBindThisJoinPointStaticPart();
						}
					}
					case STEP_THROWING_BINDING -> maybeBindThrowingVariable();
					case STEP_ANNOTATION_BINDING -> maybeBindAnnotationsFromPointcutExpression();
					case STEP_RETURNING_BINDING -> maybeBindReturningVariable();
					case STEP_PRIMITIVE_ARGS_BINDING -> maybeBindPrimitiveArgsFromPointcutExpression();
					case STEP_THIS_TARGET_ARGS_BINDING -> maybeBindThisOrTargetOrArgsFromPointcutExpression();
					case STEP_REFERENCE_PCUT_BINDING -> maybeBindReferencePointcutParameter();
					default -> throw new IllegalStateException("Unknown algorithmic step: " + (algorithmicStep - 1));
				}
			}
		}
		catch (AmbiguousBindingException | IllegalArgumentException ex) {
			if (this.raiseExceptions) {
				throw ex;
			}
			else {
				return null;
			}
		}

		if (this.numberOfRemainingUnboundArguments == 0) {
			return this.parameterNameBindings;
		}
		else {
			if (this.raiseExceptions) {
				throw new IllegalStateException("Failed to bind all argument names: " +
						this.numberOfRemainingUnboundArguments + " argument(s) could not be bound");
			}
			else {
				// 失败的约定是返回 null，允许参与责任链
				return null;
			}
		}
	}

	/**
	 * 在 Spring 中，通知方法永远不能是构造函数。
	 * @return 无效的}
	 * @throws UnsupportedOperationException 如果 {@link #setRaiseExceptions(boolean) raiseExceptions} 已设置为 {@code true}
	 */
	@Override
	public String @Nullable [] getParameterNames(Constructor<?> ctor) {
		if (this.raiseExceptions) {
			throw new UnsupportedOperationException("An advice method can never be a constructor");
		}
		else {
			// 我们返回 null 而不是抛出异常，以便我们表现良好
			// 在责任链中。
			return null;
		}
	}


	/**
	 * 绑定：Parameter Name（方法 `bindParameterName`）。
	 */
	private void bindParameterName(int index, @Nullable String name) {
		this.parameterNameBindings[index] = name;
		this.numberOfRemainingUnboundArguments--;
	}

	/**
	 * 如果第一个参数的类型为 JoinPoint 或 ProceedingJoinPoint，则将“thisJoinPoint”绑定为参数名称并返回 true，否则返回
	 * false。
	 */
	private boolean maybeBindThisJoinPoint() {
		if ((this.argumentTypes[0] == JoinPoint.class) || (this.argumentTypes[0] == ProceedingJoinPoint.class)) {
			bindParameterName(0, THIS_JOIN_POINT);
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * 执行 maybeBindThisJoinPointStaticPart 相关逻辑。
	 */
	private void maybeBindThisJoinPointStaticPart() {
		if (this.argumentTypes[0] == JoinPoint.StaticPart.class) {
			bindParameterName(0, THIS_JOIN_POINT_STATIC_PART);
		}
	}

	/**
	 * 如果指定了抛出名称，并且只剩下一个选择（参数是 Throwable 的子类型），则绑定它。
	 */
	private void maybeBindThrowingVariable() {
		if (this.throwingName == null) {
			return;
		}

		// 因此，还有一些绑定工作要做......
		int throwableIndex = -1;
		for (int i = 0; i < this.argumentTypes.length; i++) {
			if (isUnbound(i) && isSubtypeOf(Throwable.class, i)) {
				if (throwableIndex == -1) {
					throwableIndex = i;
				}
				else {
					// 我们发现的第二个候选者 - 不明确的绑定
					throw new AmbiguousBindingException("Binding of throwing parameter '" +
							this.throwingName + "' is ambiguous: could be bound to argument " +
							throwableIndex + " or " + i);
				}
			}
		}

		if (throwableIndex == -1) {
			throw new IllegalStateException("Binding of throwing parameter '" + this.throwingName +
					"' could not be completed as no available arguments are a subtype of Throwable");
		}
		else {
			bindParameterName(throwableIndex, this.throwingName);
		}
	}

	/**
	 * 如果指定了返回变量并且只剩下一个选择，则绑定它。
	 */
	private void maybeBindReturningVariable() {
		if (this.numberOfRemainingUnboundArguments == 0) {
			throw new IllegalStateException(
					"Algorithm assumes that there must be at least one unbound parameter on entry to this method");
		}

		if (this.returningName != null) {
			if (this.numberOfRemainingUnboundArguments > 1) {
				throw new AmbiguousBindingException("Binding of returning parameter '" + this.returningName +
						"' is ambiguous: there are " + this.numberOfRemainingUnboundArguments + " candidates. " +
						"Consider compiling with -parameters in order to make declared parameter names available.");
			}

			// 我们都准备好了...找到未绑定的参数，然后绑定它。
			for (int i = 0; i < this.parameterNameBindings.length; i++) {
				if (this.parameterNameBindings[i] == null) {
					bindParameterName(i, this.returningName);
					break;
				}
			}
		}
	}

	/**
	 * 解析字符串切入点表达式，查找：&#64;this、&#64;target、&#64;args、&#64;within、&#64;withincode、&#64;annotati
	 * on。如果我们找到这些切入点表达式之一，请尝试提取候选变量名称（或变量名称，在 args 的情况下）。 <p> 如果 AspectJ 在执行此练习时提供更多支持，那就太好了..
	 * .:)
	 */
	private void maybeBindAnnotationsFromPointcutExpression() {
		List<String> varNames = new ArrayList<>();
		String[] tokens = StringUtils.tokenizeToStringArray(this.pointcutExpression, " ");
		for (int i = 0; i < tokens.length; i++) {
			String toMatch = tokens[i];
			int firstParenIndex = toMatch.indexOf('(');
			if (firstParenIndex != -1) {
				toMatch = toMatch.substring(0, firstParenIndex);
			}
			if (singleValuedAnnotationPcds.contains(toMatch)) {
				PointcutBody body = getPointcutBody(tokens, i);
				i += body.numTokensConsumed;
				String varName = maybeExtractVariableName(body.text);
				if (varName != null) {
					varNames.add(varName);
				}
			}
			else if (tokens[i].startsWith("@args(") || tokens[i].equals("@args")) {
				PointcutBody body = getPointcutBody(tokens, i);
				i += body.numTokensConsumed;
				maybeExtractVariableNamesFromArgs(body.text, varNames);
			}
		}

		bindAnnotationsFromVarNames(varNames);
	}

	/**
	 * 将给定的提取变量名称列表与参数槽进行匹配。
	 */
	private void bindAnnotationsFromVarNames(List<String> varNames) {
		if (!varNames.isEmpty()) {
			// 我们还有工作要做...
			int numAnnotationSlots = countNumberOfUnboundAnnotationArguments();
			if (numAnnotationSlots > 1) {
				throw new AmbiguousBindingException("Found " + varNames.size() +
						" potential annotation variable(s) and " +
						numAnnotationSlots + " potential argument slots");
			}
			else if (numAnnotationSlots == 1) {
				if (varNames.size() == 1) {
					// 这是一场比赛
					findAndBind(Annotation.class, varNames.get(0));
				}
				else {
					// 多个候选变量，但只有一个槽位
					throw new IllegalArgumentException("Found " + varNames.size() +
							" candidate annotation binding variables" +
							" but only one potential argument binding slot");
				}
			}
			else {
				// 没有插槽，因此假设这些候选变量实际上是类型名称
			}
		}
	}

	/**
	 * 如果令牌的开头符合 Java 标识符约定，则它就在。
	 */
	private @Nullable String maybeExtractVariableName(@Nullable String candidateToken) {
		if (AspectJProxyUtils.isVariableName(candidateToken)) {
			return candidateToken;
		}
		return null;
	}

	/**
	 * 给定一个 args 切入点主体（可以是 {@code args} 或 {@code at_args}），将任何候选变量名称添加到给定列表中。
	 */
	private void maybeExtractVariableNamesFromArgs(@Nullable String argsSpec, List<String> varNames) {
		if (argsSpec == null) {
			return;
		}
		String[] tokens = StringUtils.tokenizeToStringArray(argsSpec, ",");
		for (int i = 0; i < tokens.length; i++) {
			tokens[i] = tokens[i].strip();
			String varName = maybeExtractVariableName(tokens[i]);
			if (varName != null) {
				varNames.add(varName);
			}
		}
	}

	/**
	 * 解析字符串切入点表达式，查找 this()、target() 和 args() 表达式。如果我们找到一个，请尝试提取候选变量名称并绑定它。
	 */
	private void maybeBindThisOrTargetOrArgsFromPointcutExpression() {
		if (this.numberOfRemainingUnboundArguments > 1) {
			throw new AmbiguousBindingException("Still " + this.numberOfRemainingUnboundArguments +
					" unbound args at this()/target()/args() binding stage, with no way to determine between them");
		}

		List<String> varNames = new ArrayList<>();
		String[] tokens = StringUtils.tokenizeToStringArray(this.pointcutExpression, " ");
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].equals("this") ||
					tokens[i].startsWith("this(") ||
					tokens[i].equals("target") ||
					tokens[i].startsWith("target(")) {
				PointcutBody body = getPointcutBody(tokens, i);
				i += body.numTokensConsumed;
				String varName = maybeExtractVariableName(body.text);
				if (varName != null) {
					varNames.add(varName);
				}
			}
			else if (tokens[i].equals("args") || tokens[i].startsWith("args(")) {
				PointcutBody body = getPointcutBody(tokens, i);
				i += body.numTokensConsumed;
				List<String> candidateVarNames = new ArrayList<>();
				maybeExtractVariableNamesFromArgs(body.text, candidateVarNames);
				// 我们可能发现了一些在之前的原始参数绑定步骤中绑定的变量名称，
				// 将它们过滤掉...
				for (String varName : candidateVarNames) {
					if (!alreadyBound(varName)) {
						varNames.add(varName);
					}
				}
			}
		}

		if (varNames.size() > 1) {
			throw new AmbiguousBindingException("Found " + varNames.size() +
					" candidate this(), target(), or args() variables but only one unbound argument slot");
		}
		else if (varNames.size() == 1) {
			for (int j = 0; j < this.parameterNameBindings.length; j++) {
				if (isUnbound(j)) {
					bindParameterName(j, varNames.get(0));
					break;
				}
			}
		}
		// 否则 varNames.size 必须为 0，并且我们没有任何可绑定的内容。
	}

	/**
	 * 执行 maybeBindReferencePointcutParameter 相关逻辑。
	 */
	private void maybeBindReferencePointcutParameter() {
		if (this.numberOfRemainingUnboundArguments > 1) {
			throw new AmbiguousBindingException("Still " + this.numberOfRemainingUnboundArguments +
					" unbound args at reference pointcut binding stage, with no way to determine between them");
		}

		List<String> varNames = new ArrayList<>();
		String[] tokens = StringUtils.tokenizeToStringArray(this.pointcutExpression, " ");
		for (int i = 0; i < tokens.length; i++) {
			String toMatch = tokens[i];
			if (toMatch.startsWith("!")) {
				toMatch = toMatch.substring(1);
			}
			int firstParenIndex = toMatch.indexOf('(');
			if (firstParenIndex != -1) {
				toMatch = toMatch.substring(0, firstParenIndex);
			}
			else {
				if (tokens.length < i + 2) {
					// 没有“(”并且后面没有任何内容
					continue;
				}
				else {
					String nextToken = tokens[i + 1];
					if (nextToken.charAt(0) != '(') {
						// 下一个令牌也不是“(”，不可能是电脑......
						continue;
					}
				}

			}

			// 吃身体
			PointcutBody body = getPointcutBody(tokens, i);
			i += body.numTokensConsumed;

			if (!nonReferencePointcutTokens.contains(toMatch)) {
				// 那么它可能是一个参考切入点
				String varName = maybeExtractVariableName(body.text);
				if (varName != null) {
					varNames.add(varName);
				}
			}
		}

		if (varNames.size() > 1) {
			throw new AmbiguousBindingException("Found " + varNames.size() +
					" candidate reference pointcut variables but only one unbound argument slot");
		}
		else if (varNames.size() == 1) {
			for (int j = 0; j < this.parameterNameBindings.length; j++) {
				if (isUnbound(j)) {
					bindParameterName(j, varNames.get(0));
					break;
				}
			}
		}
		// 否则 varNames.size 必须为 0，并且我们没有任何可绑定的内容。
	}

	/**
	 * 我们已经在令牌数组的给定索引处找到了绑定切入点的开始。现在我们需要提取切入点主体并返回它。
	 */
	private PointcutBody getPointcutBody(String[] tokens, int startIndex) {
		int numTokensConsumed = 0;
		String currentToken = tokens[startIndex];
		int bodyStart = currentToken.indexOf('(');
		if (currentToken.charAt(currentToken.length() - 1) == ')') {
			// 这是一个一体化...获取第一个（和最后一个）之间的文本
			return new PointcutBody(0, currentToken.substring(bodyStart + 1, currentToken.length() - 1));
		}
		else {
			StringBuilder sb = new StringBuilder();
			if (bodyStart >= 0 && bodyStart != (currentToken.length() - 1)) {
				sb.append(currentToken.substring(bodyStart + 1));
				sb.append(' ');
			}
			numTokensConsumed++;
			int currentIndex = startIndex + numTokensConsumed;
			while (currentIndex < tokens.length) {
				if (tokens[currentIndex].equals("(")) {
					currentIndex++;
					continue;
				}

				if (tokens[currentIndex].endsWith(")")) {
					sb.append(tokens[currentIndex], 0, tokens[currentIndex].length() - 1);
					return new PointcutBody(numTokensConsumed, sb.toString().trim());
				}

				String toAppend = tokens[currentIndex];
				if (toAppend.startsWith("(")) {
					toAppend = toAppend.substring(1);
				}
				sb.append(toAppend);
				sb.append(' ');
				currentIndex++;
				numTokensConsumed++;
			}

		}

		// 我们看了看，但失败了……
		return new PointcutBody(numTokensConsumed, null);
	}

	/**
	 * 将 args 与原始类型的未绑定参数进行匹配。
	 */
	private void maybeBindPrimitiveArgsFromPointcutExpression() {
		int numUnboundPrimitives = countNumberOfUnboundPrimitiveArguments();
		if (numUnboundPrimitives > 1) {
			throw new AmbiguousBindingException("Found " + numUnboundPrimitives +
					" unbound primitive arguments with no way to distinguish between them.");
		}
		if (numUnboundPrimitives == 1) {
			// 寻找 arg 变量，如果我们找到一个就绑定它......
			List<String> varNames = new ArrayList<>();
			String[] tokens = StringUtils.tokenizeToStringArray(this.pointcutExpression, " ");
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i].equals("args") || tokens[i].startsWith("args(")) {
					PointcutBody body = getPointcutBody(tokens, i);
					i += body.numTokensConsumed;
					maybeExtractVariableNamesFromArgs(body.text, varNames);
				}
			}
			if (varNames.size() > 1) {
				throw new AmbiguousBindingException("Found " + varNames.size() +
						" candidate variable names but only one candidate binding slot when matching primitive args");
			}
			else if (varNames.size() == 1) {
				// 1 个原始参数和一个候选参数...
				for (int i = 0; i < this.argumentTypes.length; i++) {
					if (isUnbound(i) && this.argumentTypes[i].isPrimitive()) {
						bindParameterName(i, varNames.get(0));
						break;
					}
				}
			}
		}
	}

	/*
	 * Return true if the parameter name binding for the given parameter
	 * index has not yet been assigned.
	 */
	/**
	 * 判断是否 Unbound。
	 */
	private boolean isUnbound(int i) {
		return this.parameterNameBindings[i] == null;
	}

	/**
	 * 执行 alreadyBound 相关逻辑。
	 */
	private boolean alreadyBound(String varName) {
		for (int i = 0; i < this.parameterNameBindings.length; i++) {
			if (!isUnbound(i) && varName.equals(this.parameterNameBindings[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 如果给定参数类型是给定超类型的子类，则返回 {@code true}。
	 */
	private boolean isSubtypeOf(Class<?> supertype, int argumentNumber) {
		return supertype.isAssignableFrom(this.argumentTypes[argumentNumber]);
	}

	/**
	 * 执行 countNumberOfUnboundAnnotationArguments 相关逻辑。
	 */
	private int countNumberOfUnboundAnnotationArguments() {
		int count = 0;
		for (int i = 0; i < this.argumentTypes.length; i++) {
			if (isUnbound(i) && isSubtypeOf(Annotation.class, i)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 执行 countNumberOfUnboundPrimitiveArguments 相关逻辑。
	 */
	private int countNumberOfUnboundPrimitiveArguments() {
		int count = 0;
		for (int i = 0; i < this.argumentTypes.length; i++) {
			if (isUnbound(i) && this.argumentTypes[i].isPrimitive()) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 找到给定类型的参数索引，并将给定的 {@code varName} 绑定到该位置。
	 */
	private void findAndBind(Class<?> argumentType, String varName) {
		for (int i = 0; i < this.argumentTypes.length; i++) {
			if (isUnbound(i) && isSubtypeOf(argumentType, i)) {
				bindParameterName(i, varName);
				return;
			}
		}
		throw new IllegalStateException("Expected to find an unbound argument of type '" +
				argumentType.getName() + "'");
	}


	/**
	 * 用于保存从切入点主体中提取的文本的简单记录，以及提取文本时消耗的标记数量。
	 */
	private record PointcutBody(int numTokensConsumed, @Nullable String text) {
	}


	/**
	 * 因尝试解析方法的参数名称时检测到不明确的绑定而引发。
	 */
	@SuppressWarnings("serial")
	public static class AmbiguousBindingException extends RuntimeException {

		/**
		 * 使用指定的消息构造一个新的 AmbigeousBindingException。
		 * @param msg 详细消息
		 */
		public AmbiguousBindingException(String msg) {
			super(msg);
		}
	}

}
