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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.springframework.asm.MethodVisitor;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.spel.CodeFlow;
import org.springframework.expression.spel.ExpressionState;
import org.springframework.expression.spel.support.BooleanTypedValue;
import org.springframework.util.NumberUtils;

/* ===== [OCA 中文解析] =====
class OpLE — 意图说明

class `OpLE`：请结合所属模块与调用方理解其在整体架构中的职责。；源文件: `spring-expression/src/main/java/org/springframework/expression/spel/ast/OpLE.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * Implements the less-than-or-equal operator.
 *
 * @author Andy Clement
 * @author Juergen Hoeller
 * @author Giovanni Dall'Oglio Risso
 * @since 3.0
 */
public class OpLE extends Operator {

	public OpLE(int startPos, int endPos, SpelNodeImpl... operands) {
		super("<=", startPos, endPos, operands);
		this.exitTypeDescriptor = "Z";
	}


	@Override
	/* ===== [OCA 中文解析] =====
方法 getValueInternal — 意图与阅读要点

方法 `getValueInternal` 复杂度较高（CCN≈19, NLOC≈41）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
	public BooleanTypedValue getValueInternal(ExpressionState state) throws EvaluationException {
		Object left = getLeftOperand().getValueInternal(state).getValue();
		Object right = getRightOperand().getValueInternal(state).getValue();
		state.trackOperation();

		this.leftActualDescriptor = CodeFlow.toDescriptorFromObject(left);
		this.rightActualDescriptor = CodeFlow.toDescriptorFromObject(right);

		if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
			if (leftNumber instanceof BigDecimal || rightNumber instanceof BigDecimal) {
				BigDecimal leftBigDecimal = NumberUtils.convertNumberToTargetClass(leftNumber, BigDecimal.class);
				BigDecimal rightBigDecimal = NumberUtils.convertNumberToTargetClass(rightNumber, BigDecimal.class);
				return BooleanTypedValue.forValue(leftBigDecimal.compareTo(rightBigDecimal) <= 0);
			}
			else if (leftNumber instanceof Double || rightNumber instanceof Double) {
				return BooleanTypedValue.forValue(leftNumber.doubleValue() <= rightNumber.doubleValue());
			}
			else if (leftNumber instanceof Float || rightNumber instanceof Float) {
				return BooleanTypedValue.forValue(leftNumber.floatValue() <= rightNumber.floatValue());
			}
			else if (leftNumber instanceof BigInteger || rightNumber instanceof BigInteger) {
				BigInteger leftBigInteger = NumberUtils.convertNumberToTargetClass(leftNumber, BigInteger.class);
				BigInteger rightBigInteger = NumberUtils.convertNumberToTargetClass(rightNumber, BigInteger.class);
				return BooleanTypedValue.forValue(leftBigInteger.compareTo(rightBigInteger) <= 0);
			}
			else if (leftNumber instanceof Long || rightNumber instanceof Long) {
				return BooleanTypedValue.forValue(leftNumber.longValue() <= rightNumber.longValue());
			}
			else if (leftNumber instanceof Integer || rightNumber instanceof Integer) {
				return BooleanTypedValue.forValue(leftNumber.intValue() <= rightNumber.intValue());
			}
			else if (leftNumber instanceof Short || rightNumber instanceof Short) {
				return BooleanTypedValue.forValue(leftNumber.shortValue() <= rightNumber.shortValue());
			}
			else if (leftNumber instanceof Byte || rightNumber instanceof Byte) {
				return BooleanTypedValue.forValue(leftNumber.byteValue() <= rightNumber.byteValue());
			}
			else {
				// Unknown Number subtypes -> best guess is double comparison
				return BooleanTypedValue.forValue(leftNumber.doubleValue() <= rightNumber.doubleValue());
			}
		}

		return BooleanTypedValue.forValue(state.getTypeComparator().compare(left, right) <= 0);
	}

	@Override
	public boolean isCompilable() {
		return isCompilableOperatorUsingNumerics();
	}

	@Override
	public void generateCode(MethodVisitor mv, CodeFlow cf) {
		generateComparisonCode(mv, cf, IFGT, IF_ICMPGT);
	}

}
