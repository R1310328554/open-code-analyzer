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

package org.springframework.validation;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

/**
 * {@link Errors} 接口的抽象实现。
 * 提供嵌套路径处理，但不定义 {@link ObjectError ObjectErrors}
 * 与 {@link FieldError FieldErrors} 的具体管理方式。
 *
 * @author Juergen Hoeller
 * @author Rossen Stoyanchev
 * @since 2.5.3
 * @see AbstractBindingResult
 */
@SuppressWarnings("serial")
public abstract class AbstractErrors implements Errors, Serializable {

	private String nestedPath = "";

	private final Deque<String> nestedPathStack = new ArrayDeque<>();


	@Override
	public void setNestedPath(@Nullable String nestedPath) {
		doSetNestedPath(nestedPath);
		this.nestedPathStack.clear();
	}

	@Override
	public String getNestedPath() {
		return this.nestedPath;
	}

	@Override
	public void pushNestedPath(String subPath) {
		this.nestedPathStack.push(getNestedPath());
		doSetNestedPath(getNestedPath() + subPath);
	}

	@Override
	public void popNestedPath() throws IllegalStateException {
		try {
			String formerNestedPath = this.nestedPathStack.pop();
			doSetNestedPath(formerNestedPath);
		}
		catch (NoSuchElementException ex) {
			throw new IllegalStateException("Cannot pop nested path: no nested path on stack");
		}
	}

	/**
	 * 实际设置嵌套路径。
	 * 由 setNestedPath 与 pushNestedPath 委托调用。
	 */
	protected void doSetNestedPath(@Nullable String nestedPath) {
		if (nestedPath == null) {
			nestedPath = "";
		}
		nestedPath = canonicalFieldName(nestedPath);
		if (!nestedPath.isEmpty() && !nestedPath.endsWith(NESTED_PATH_SEPARATOR)) {
			nestedPath += NESTED_PATH_SEPARATOR;
		}
		this.nestedPath = nestedPath;
	}

	/**
	 * 根据本实例的嵌套路径，将给定字段转换为完整路径。
	 */
	protected String fixedField(@Nullable String field) {
		if (StringUtils.hasLength(field)) {
			return getNestedPath() + canonicalFieldName(field);
		}
		else {
			String path = getNestedPath();
			return (path.endsWith(NESTED_PATH_SEPARATOR) ?
					path.substring(0, path.length() - NESTED_PATH_SEPARATOR.length()) : path);
		}
	}

	/**
	 * 确定给定字段的规范字段名。
	 * <p>默认实现直接原样返回字段名。
	 * @param field 原始字段名
	 * @return 规范字段名
	 */
	protected String canonicalFieldName(String field) {
		return field;
	}

	@Override
	public List<FieldError> getFieldErrors(String field) {
		List<FieldError> fieldErrors = getFieldErrors();
		List<FieldError> result = new ArrayList<>();
		String fixedField = fixedField(field);
		for (FieldError fieldError : fieldErrors) {
			if (isMatchingFieldError(fixedField, fieldError)) {
				result.add(fieldError);
			}
		}
		return Collections.unmodifiableList(result);
	}

	/**
	 * 检查给定 {@link FieldError} 是否与给定字段匹配。
	 * @param field 要查找 FieldError 的字段
	 * @param fieldError 候选 FieldError
	 * @return 该 FieldError 是否与给定字段匹配
	 */
	protected boolean isMatchingFieldError(String field, FieldError fieldError) {
		if (field.equals(fieldError.getField())) {
			return true;
		}
		// 优化：使用 charAt 与 regionMatches 替代 endsWith 与 startsWith（SPR-11304）
		int endIndex = field.length() - 1;
		return (endIndex >= 0 && field.charAt(endIndex) == '*' &&
				(endIndex == 0 || field.regionMatches(0, fieldError.getField(), 0, endIndex)));
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getName());
		sb.append(": ").append(getErrorCount()).append(" errors");
		for (ObjectError error : getAllErrors()) {
			sb.append('\n').append(error);
		}
		return sb.toString();
	}

}
