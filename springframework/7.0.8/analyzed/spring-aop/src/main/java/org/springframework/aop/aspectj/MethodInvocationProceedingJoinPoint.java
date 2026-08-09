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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.reflect.SourceLocation;
import org.aspectj.runtime.internal.AroundClosure;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.util.Assert;

/**
 * 包装 AOP 联盟 {@link org.aopalliance.intercept.MethodInvocation} 的 AspectJ {@link
 * ProceedingJoinPoint} 接口的实现。
 * <p><b>Note</b>：{@code getThis()} 方法返回当前的 Spring AOP 代理。 {@code getTarget()} 方法返回当前 Sprin
 * g AOP 目标（如果没有目标实例，则可能是 {@code null}）作为普通 POJO，没有任何建议。 <b>如果要调用该对象并使建议生效，请使用 {@code getTh
 * is()}.</b> 一个常见的示例是将对象强制转换为在引入的实现中引入的接口。 AspectJ 本身没有目标和代理之间的这种区别。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Adrian Colyer
 * @author Ramnivas Laddad
 * @since 2.0
 */
public class MethodInvocationProceedingJoinPoint implements ProceedingJoinPoint, JoinPoint.StaticPart {

	/** 方法相关状态（`methodInvocation`）。 */
	private final ProxyMethodInvocation methodInvocation;

	/** `args`：该类的成员状态。 */
	private @Nullable Object @Nullable [] args;

	/**
	 */
	private @Nullable Signature signature;

	/**
	 */
	private @Nullable SourceLocation sourceLocation;


	/**
	 * 创建一个新的 MethodInitationProceedingJoinPoint，包装给定的 Spring ProxyMethodInitation 对象。
	 * @param methodInvocation Spring 代理方法调用对象
	 */
	public MethodInvocationProceedingJoinPoint(ProxyMethodInvocation methodInvocation) {
		Assert.notNull(methodInvocation, "MethodInvocation must not be null");
		this.methodInvocation = methodInvocation;
	}


	/**
	 * 方法 `AroundClosure`：完成本类中与「Around Closure」相关的职责。
	 */
	@Override
	public void set$AroundClosure(AroundClosure aroundClosure) {
		throw new UnsupportedOperationException();
	}

	/**
	 * 方法 `proceed`：完成本类中与「proceed」相关的职责。
	 */
	@Override
	public @Nullable Object proceed() throws Throwable {
		return this.methodInvocation.invocableClone().proceed();
	}

	/**
	 * 方法 `proceed`：完成本类中与「proceed」相关的职责。
	 */
	@Override
	public @Nullable Object proceed(Object[] arguments) throws Throwable {
		Assert.notNull(arguments, "Argument array passed to proceed cannot be null");
		if (arguments.length != this.methodInvocation.getArguments().length) {
			throw new IllegalArgumentException("Expecting " +
					this.methodInvocation.getArguments().length + " arguments to proceed, " +
					"but was passed " + arguments.length + " arguments");
		}
		this.methodInvocation.setArguments(arguments);
		return this.methodInvocation.invocableClone(arguments).proceed();
	}

	/**
	 * 返回 Spring AOP 代理。不能是 {@code null}。
	 */
	@Override
	public Object getThis() {
		return this.methodInvocation.getProxy();
	}

	/**
	 * 返回 Spring AOP 目标。如果没有目标，可能是 {@code null}。
	 */
	@Override
	public @Nullable Object getTarget() {
		return this.methodInvocation.getThis();
	}

	/**
	 * 获取 Args（`Args`）。
	 */
	@Override
	@SuppressWarnings("NullAway") // Overridden method does not define nullness
	public @Nullable Object[] getArgs() {
		if (this.args == null) {
			this.args = this.methodInvocation.getArguments().clone();
		}
		return this.args;
	}

	/**
	 * 获取 Signature（`Signature`）。
	 */
	@Override
	public Signature getSignature() {
		if (this.signature == null) {
			this.signature = new MethodSignatureImpl();
		}
		return this.signature;
	}

	/**
	 * 获取 Source Location（`SourceLocation`）。
	 */
	@Override
	public SourceLocation getSourceLocation() {
		if (this.sourceLocation == null) {
			this.sourceLocation = new SourceLocationImpl();
		}
		return this.sourceLocation;
	}

	/**
	 * 获取 Kind（`Kind`）。
	 */
	@Override
	public String getKind() {
		return ProceedingJoinPoint.METHOD_EXECUTION;
	}

	/**
	 * 获取 Id（`Id`）。
	 */
	@Override
	public int getId() {
		// TODO：它只是一个适配器，但返回 0 可能仍然有副作用......
		return 0;
	}

	/**
	 * 获取 Static Part（`StaticPart`）。
	 */
	@Override
	public JoinPoint.StaticPart getStaticPart() {
		return this;
	}

	/**
	 * 方法 `toShortString`：完成本类中与「to Short String」相关的职责。
	 */
	@Override
	public String toShortString() {
		return "execution(" + getSignature().toShortString() + ")";
	}

	/**
	 * 方法 `toLongString`：完成本类中与「to Long String」相关的职责。
	 */
	@Override
	public String toLongString() {
		return "execution(" + getSignature().toLongString() + ")";
	}

	/**
	 * 返回字符串表示。
	 */
	@Override
	public String toString() {
		return "execution(" + getSignature() + ")";
	}


	/**
	 * 延迟初始化 MethodSignature。
	 */
	private class MethodSignatureImpl implements MethodSignature {

		private volatile @Nullable String @Nullable [] parameterNames;

		@Override
		public String getName() {
			return methodInvocation.getMethod().getName();
		}

		@Override
		public int getModifiers() {
			return methodInvocation.getMethod().getModifiers();
		}

		@Override
		public Class<?> getDeclaringType() {
			return methodInvocation.getMethod().getDeclaringClass();
		}

		@Override
		public String getDeclaringTypeName() {
			return methodInvocation.getMethod().getDeclaringClass().getName();
		}

		@Override
		public Class<?> getReturnType() {
			return methodInvocation.getMethod().getReturnType();
		}

		@Override
		public Method getMethod() {
			return methodInvocation.getMethod();
		}

		@Override
		public Class<?>[] getParameterTypes() {
			return methodInvocation.getMethod().getParameterTypes();
		}

		@Override
		@SuppressWarnings("NullAway") // Overridden method does not define nullness
		public @Nullable String @Nullable [] getParameterNames() {
			@Nullable String[] parameterNames = this.parameterNames;
			if (parameterNames == null) {
				parameterNames = DefaultParameterNameDiscoverer.getSharedInstance().getParameterNames(getMethod());
				this.parameterNames = parameterNames;
			}
			return parameterNames;
		}

		@Override
		public Class<?>[] getExceptionTypes() {
			return methodInvocation.getMethod().getExceptionTypes();
		}

		@Override
		public String toShortString() {
			return toString(false, false, false, false);
		}

		@Override
		public String toLongString() {
			return toString(true, true, true, true);
		}

		@Override
		public String toString() {
			return toString(false, true, false, true);
		}

		private String toString(boolean includeModifier, boolean includeReturnTypeAndArgs,
				boolean useLongReturnAndArgumentTypeName, boolean useLongTypeName) {

			StringBuilder sb = new StringBuilder();
			if (includeModifier) {
				sb.append(Modifier.toString(getModifiers()));
				sb.append(' ');
			}
			if (includeReturnTypeAndArgs) {
				appendType(sb, getReturnType(), useLongReturnAndArgumentTypeName);
				sb.append(' ');
			}
			appendType(sb, getDeclaringType(), useLongTypeName);
			sb.append('.');
			sb.append(getMethod().getName());
			sb.append('(');
			Class<?>[] parametersTypes = getParameterTypes();
			appendTypes(sb, parametersTypes, includeReturnTypeAndArgs, useLongReturnAndArgumentTypeName);
			sb.append(')');
			return sb.toString();
		}

		private void appendTypes(StringBuilder sb, Class<?>[] types, boolean includeArgs,
				boolean useLongReturnAndArgumentTypeName) {

			if (includeArgs) {
				for (int size = types.length, i = 0; i < size; i++) {
					appendType(sb, types[i], useLongReturnAndArgumentTypeName);
					if (i < size - 1) {
						sb.append(',');
					}
				}
			}
			else {
				if (types.length != 0) {
					sb.append("..");
				}
			}
		}

		private void appendType(StringBuilder sb, Class<?> type, boolean useLongTypeName) {
			if (type.isArray()) {
				appendType(sb, type.componentType(), useLongTypeName);
				sb.append("[]");
			}
			else {
				sb.append(useLongTypeName ? type.getName() : type.getSimpleName());
			}
		}
	}


	/**
	 * 延迟初始化 SourceLocation。
	 */
	private class SourceLocationImpl implements SourceLocation {

		@Override
		public Class<?> getWithinType() {
			if (methodInvocation.getThis() == null) {
				throw new UnsupportedOperationException("No source location joinpoint available: target is null");
			}
			return methodInvocation.getThis().getClass();
		}

		@Override
		public String getFileName() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getLine() {
			throw new UnsupportedOperationException();
		}

		@Override
		@Deprecated(since = "4.0") // deprecated by AspectJ
		public int getColumn() {
			throw new UnsupportedOperationException();
		}
	}

}
