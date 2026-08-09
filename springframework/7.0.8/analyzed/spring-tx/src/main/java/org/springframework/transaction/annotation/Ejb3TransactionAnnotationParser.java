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

package org.springframework.transaction.annotation;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;

import jakarta.ejb.ApplicationException;
import jakarta.ejb.TransactionAttributeType;
import org.jspecify.annotations.Nullable;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;

/**
 * 解析 EJB3 {@link jakarta.ejb.TransactionAttribute} 注解的策略实现，
 * 支持 EJB3 基于注解异常的回滚规则。
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see SpringTransactionAnnotationParser
 * @see JtaTransactionAnnotationParser
 */
@SuppressWarnings("serial")
public class Ejb3TransactionAnnotationParser implements TransactionAnnotationParser, Serializable {

	@Override
	public boolean isCandidateClass(Class<?> targetClass) {
		return AnnotationUtils.isCandidateClass(targetClass, jakarta.ejb.TransactionAttribute.class);
	}

	@Override
	public @Nullable TransactionAttribute parseTransactionAnnotation(AnnotatedElement element) {
		jakarta.ejb.TransactionAttribute ann = element.getAnnotation(jakarta.ejb.TransactionAttribute.class);
		if (ann != null) {
			return parseTransactionAnnotation(ann);
		}
		else {
			return null;
		}
	}

	public TransactionAttribute parseTransactionAnnotation(jakarta.ejb.TransactionAttribute ann) {
		return new Ejb3TransactionAttribute(ann.value());
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (other instanceof Ejb3TransactionAnnotationParser);
	}

	@Override
	public int hashCode() {
		return Ejb3TransactionAnnotationParser.class.hashCode();
	}


	/**
	 * EJB3 专用 TransactionAttribute，实现基于注解异常的 EJB3 回滚规则。
	 */
	private static class Ejb3TransactionAttribute extends DefaultTransactionAttribute {

		public Ejb3TransactionAttribute(TransactionAttributeType type) {
			setPropagationBehaviorName(PREFIX_PROPAGATION + type.name());
		}

		@Override
		public boolean rollbackOn(Throwable ex) {
			ApplicationException ann = ex.getClass().getAnnotation(ApplicationException.class);
			return (ann != null ? ann.rollback() : super.rollbackOn(ex));
		}
	}

}
