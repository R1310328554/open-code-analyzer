/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot.diagnostics.analyzer;

import java.lang.reflect.Method;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.boot.diagnostics.FailureAnalyzer;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 处理某种注入失败的 {@link FailureAnalyzer} 抽象基类。
 *
 * @param <T> 待分析异常的类型
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 * @since 1.4.1
 */
public abstract class AbstractInjectionFailureAnalyzer<T extends Throwable> extends AbstractFailureAnalyzer<T> {

	@Override
	protected final @Nullable FailureAnalysis analyze(Throwable rootFailure, T cause) {
		return analyze(rootFailure, cause, getDescription(rootFailure));
	}

	private @Nullable String getDescription(Throwable rootFailure) {
		UnsatisfiedDependencyException unsatisfiedDependency = findMostNestedCause(rootFailure,
				UnsatisfiedDependencyException.class);
		if (unsatisfiedDependency != null) {
			return getDescription(unsatisfiedDependency);
		}
		BeanInstantiationException beanInstantiationException = findMostNestedCause(rootFailure,
				BeanInstantiationException.class);
		if (beanInstantiationException != null) {
			return getDescription(beanInstantiationException);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private <C extends Exception> @Nullable C findMostNestedCause(Throwable root, Class<C> type) {
		Throwable candidate = root;
		C result = null;
		while (candidate != null) {
			if (type.isAssignableFrom(candidate.getClass())) {
				result = (C) candidate;
			}
			candidate = candidate.getCause();
		}
		return result;
	}

	private @Nullable String getDescription(UnsatisfiedDependencyException ex) {
		InjectionPoint injectionPoint = ex.getInjectionPoint();
		if (injectionPoint != null) {
			if (injectionPoint.getField() != null) {
				return String.format("Field %s in %s", injectionPoint.getField().getName(),
						injectionPoint.getField().getDeclaringClass().getName());
			}
			if (injectionPoint.getMethodParameter() != null) {
				if (injectionPoint.getMethodParameter().getConstructor() != null) {
					return String.format("Parameter %d of constructor in %s",
							injectionPoint.getMethodParameter().getParameterIndex(),
							injectionPoint.getMethodParameter().getDeclaringClass().getName());
				}
				Method method = injectionPoint.getMethodParameter().getMethod();
				Assert.state(method != null, "Neither constructor nor method is available");
				return String.format("Parameter %d of method %s in %s",
						injectionPoint.getMethodParameter().getParameterIndex(), method.getName(),
						injectionPoint.getMethodParameter().getDeclaringClass().getName());
			}
		}
		return ex.getResourceDescription();
	}

	private String getDescription(BeanInstantiationException ex) {
		if (ex.getConstructingMethod() != null) {
			return String.format("Method %s in %s", ex.getConstructingMethod().getName(),
					ex.getConstructingMethod().getDeclaringClass().getName());
		}
		if (ex.getConstructor() != null) {
			return String.format("Constructor in %s",
					ClassUtils.getUserClass(ex.getConstructor().getDeclaringClass()).getName());
		}
		return ex.getBeanClass().getName();
	}

	/**
	 * 返回对给定 {@code rootFailure} 的分析，若无法分析则返回 {@code null}。
	 *
	 * @param rootFailure 传入分析器的根失败
	 * @param cause 实际找到的 cause
	 * @param description 注入点描述或 {@code null}
	 * @return 分析结果或 {@code null}
	 */
	protected abstract @Nullable FailureAnalysis analyze(Throwable rootFailure, T cause, @Nullable String description);

}
