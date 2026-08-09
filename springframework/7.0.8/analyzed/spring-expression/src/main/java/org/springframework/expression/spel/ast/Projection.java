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

package org.springframework.expression.spel.ast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.springframework.expression.EvaluationException;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.ExpressionState;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/* ===== [OCA 中文解析] =====
class Projection — 意图说明

class `Projection`：请结合所属模块与调用方理解其在整体架构中的职责。；源文件: `spring-expression/src/main/java/org/springframework/expression/spel/ast/Projection.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * Represents projection, where a given operation is performed on all elements in
 * some input sequence, returning a new sequence of the same size.
 *
 * <p>For example: <code>{1,2,3,4,5,6,7,8,9,10}.![#isEven(#this)]</code> evaluates
 * to {@code [n, y, n, y, n, y, n, y, n, y]}.
 *
 * <h3>Null-safe Projection</h3>
 *
 * <p>Null-safe projection is supported via the {@code '?.!'} operator. For example,
 * {@code 'names?.![#this.length]'} will evaluate to {@code null} if {@code names}
 * is {@code null} and will otherwise evaluate to a sequence containing the lengths
 * of the names. As of Spring Framework 7.0, null-safe projection also applies when
 * performing projection on an {@link Optional} target. For example, if {@code names}
 * is of type {@code Optional<List<String>>}, the expression
 * {@code 'names?.![#this.length]'} will evaluate to {@code null} if {@code names}
 * is {@code null} or {@link Optional#isEmpty() empty} and will otherwise evaluate
 * to a sequence containing the lengths of the names, effectively
 * {@code names.get().stream().map(String::length).toList()}.
 *
 * @author Andy Clement
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.0
 */
public class Projection extends SpelNodeImpl {

	// [OCA] 字段 `nullSafe`：类成员状态。
	private final boolean nullSafe;


	public Projection(boolean nullSafe, int startPos, int endPos, SpelNodeImpl expression) {
		super(startPos, endPos, expression);
		this.nullSafe = nullSafe;
	}


	/**
	 * Does this node represent a null-safe projection operation?
	 * @since 6.1.6
	 */
	@Override
	public final boolean isNullSafe() {
		return this.nullSafe;
	}

	@Override
	public TypedValue getValueInternal(ExpressionState state) throws EvaluationException {
		return getValueRef(state).getValue();
	}

	@Override
	/* ===== [OCA 中文解析] =====
方法 getValueRef — 意图与阅读要点

方法 `getValueRef` 复杂度较高（CCN≈16, NLOC≈70）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
	protected ValueRef getValueRef(ExpressionState state) throws EvaluationException {
		TypedValue contextObject = state.getActiveContextObject();
		Object operand = contextObject.getValue();

		if (isNullSafe()) {
			if (operand == null) {
				return ValueRef.NullValueRef.INSTANCE;
			}
			if (operand instanceof Optional<?> optional) {
				if (optional.isEmpty()) {
					return ValueRef.NullValueRef.INSTANCE;
				}
				operand = optional.get();
			}
		}

		if (operand == null) {
			throw new SpelEvaluationException(getStartPosition(), SpelMessage.PROJECTION_NOT_SUPPORTED_ON_TYPE, "null");
		}

		// When the input is a map, we push a Map.Entry on the stack before calling
		// the specified operation. Map.Entry has two properties 'key' and 'value'
		// that can be referenced in the operation -- for example,
		// {'a':'y', 'b':'n'}.![value == 'y' ? key : null] evaluates to ['a', null].
		if (operand instanceof Map<?, ?> mapData) {
			state.trackOperation();
			List<Object> result = new ArrayList<>();
			for (Map.Entry<?, ?> entry : mapData.entrySet()) {
				try {
					state.pushActiveContextObject(new TypedValue(entry));
					state.enterScope();
					state.trackOperation();
					result.add(this.children[0].getValueInternal(state).getValue());
				}
				finally {
					state.popActiveContextObject();
					state.exitScope();
				}
			}
			return new ValueRef.TypedValueHolderValueRef(new TypedValue(result), this);
		}

		boolean operandIsArray = ObjectUtils.isArray(operand);
		if (operand instanceof Iterable || operandIsArray) {
			Iterable<?> data = (operand instanceof Iterable<?> iterable ?
					iterable : Arrays.asList(ObjectUtils.toObjectArray(operand)));

			state.trackOperation();
			List<Object> result = new ArrayList<>();
			Class<?> arrayElementType = null;
			for (Object element : data) {
				try {
					state.pushActiveContextObject(new TypedValue(element));
					state.enterScope();
					Object value = this.children[0].getValueInternal(state).getValue();
					if (value != null && operandIsArray) {
						arrayElementType = determineCommonType(arrayElementType, value.getClass());
					}
					state.trackOperation();
					result.add(value);
				}
				finally {
					state.exitScope();
					state.popActiveContextObject();
				}
			}

			if (operandIsArray) {
				if (arrayElementType == null) {
					arrayElementType = Object.class;
				}
				Object resultArray = Array.newInstance(arrayElementType, result.size());
				System.arraycopy(result.toArray(), 0, resultArray, 0, result.size());
				return new ValueRef.TypedValueHolderValueRef(new TypedValue(resultArray),this);
			}

			return new ValueRef.TypedValueHolderValueRef(new TypedValue(result),this);
		}

		throw new SpelEvaluationException(getStartPosition(), SpelMessage.PROJECTION_NOT_SUPPORTED_ON_TYPE,
				operand.getClass().getName());
	}

	@Override
	public String toStringAST() {
		return "![" + getChild(0).toStringAST() + "]";
	}

	private Class<?> determineCommonType(@Nullable Class<?> oldType, Class<?> newType) {
		if (oldType == null) {
			return newType;
		}
		if (oldType.isAssignableFrom(newType)) {
			return oldType;
		}
		Class<?> nextType = newType;
		while (nextType != Object.class) {
			if (nextType.isAssignableFrom(oldType)) {
				return nextType;
			}
			nextType = nextType.getSuperclass();
		}
		for (Class<?> nextInterface : ClassUtils.getAllInterfacesForClassAsSet(newType)) {
			if (nextInterface.isAssignableFrom(oldType)) {
				return nextInterface;
			}
		}
		return Object.class;
	}

}
