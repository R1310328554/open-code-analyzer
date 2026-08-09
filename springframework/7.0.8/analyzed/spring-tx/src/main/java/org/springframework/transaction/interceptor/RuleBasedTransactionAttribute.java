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

package org.springframework.transaction.interceptor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * 通过应用若干正负回滚规则判定给定异常是否应导致事务回滚的
 * {@link TransactionAttribute} 实现。若无自定义回滚规则适用，
 * 本属性行为类似 DefaultTransactionAttribute（对运行时异常回滚）。
 *
 * <p>{@link TransactionAttributeEditor} 创建本类的对象。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 09.04.2003
 * @see TransactionAttributeEditor
 */
@SuppressWarnings("serial")
public class RuleBasedTransactionAttribute extends DefaultTransactionAttribute implements Serializable {

	/** 描述字符串中“遇异常回滚”规则的前缀。 */
	public static final String PREFIX_ROLLBACK_RULE = "-";

	/** 描述字符串中“遇异常提交”规则的前缀。 */
	public static final String PREFIX_COMMIT_RULE = "+";


	private @Nullable List<RollbackRuleAttribute> rollbackRules;


	/**
	 * 以默认设置创建新的 RuleBasedTransactionAttribute。
	 * 可通过 Bean 属性 setter 修改。
	 * @see #setPropagationBehavior
	 * @see #setIsolationLevel
	 * @see #setTimeout
	 * @see #setReadOnly
	 * @see #setName
	 * @see #setRollbackRules
	 */
	public RuleBasedTransactionAttribute() {
	}

	/**
	 * 拷贝构造函数。定义可通过 Bean 属性 setter 修改。
	 * @see #setPropagationBehavior
	 * @see #setIsolationLevel
	 * @see #setTimeout
	 * @see #setReadOnly
	 * @see #setName
	 * @see #setRollbackRules
	 */
	public RuleBasedTransactionAttribute(RuleBasedTransactionAttribute other) {
		super(other);
		this.rollbackRules = (other.rollbackRules != null ? new ArrayList<>(other.rollbackRules) : null);
	}

	/**
	 * 以给定传播行为创建新的 DefaultTransactionAttribute。
	 * 可通过 Bean 属性 setter 修改。
	 * @param propagationBehavior TransactionDefinition 接口中的传播常量之一
	 * @param rollbackRules 要应用的 RollbackRuleAttribute 列表
	 * @see #setIsolationLevel
	 * @see #setTimeout
	 * @see #setReadOnly
	 */
	public RuleBasedTransactionAttribute(int propagationBehavior, List<RollbackRuleAttribute> rollbackRules) {
		super(propagationBehavior);
		this.rollbackRules = rollbackRules;
	}


	/**
	 * 设置要应用的 {@code RollbackRuleAttribute} 对象列表
	 * （及/或 {@code NoRollbackRuleAttribute} 对象）。
	 * @see RollbackRuleAttribute
	 * @see NoRollbackRuleAttribute
	 */
	public void setRollbackRules(List<RollbackRuleAttribute> rollbackRules) {
		this.rollbackRules = rollbackRules;
	}

	/**
	 * 返回 {@code RollbackRuleAttribute} 对象列表（永不为 {@code null}）。
	 */
	public List<RollbackRuleAttribute> getRollbackRules() {
		if (this.rollbackRules == null) {
			this.rollbackRules = new ArrayList<>();
		}
		return this.rollbackRules;
	}


	/**
	 * 胜出的规则为最浅规则（即继承层次中最接近异常的规则）。
	 * 若无规则适用（-1），返回 {@code false}。
	 * @see TransactionAttribute#rollbackOn(java.lang.Throwable)
	 */
	@Override
	public boolean rollbackOn(Throwable ex) {
		RollbackRuleAttribute winner = null;
		int deepest = Integer.MAX_VALUE;

		if (this.rollbackRules != null) {
			for (RollbackRuleAttribute rule : this.rollbackRules) {
				int depth = rule.getDepth(ex);
				if (depth >= 0 && depth < deepest) {
					deepest = depth;
					winner = rule;
				}
			}
		}

		// 若无规则匹配，使用超类行为（对 unchecked 异常回滚）。
		if (winner == null) {
			return super.rollbackOn(ex);
		}

		return !(winner instanceof NoRollbackRuleAttribute);
	}


	@Override
	public String toString() {
		StringBuilder result = getAttributeDescription();
		if (this.rollbackRules != null) {
			for (RollbackRuleAttribute rule : this.rollbackRules) {
				String sign = (rule instanceof NoRollbackRuleAttribute ? PREFIX_COMMIT_RULE : PREFIX_ROLLBACK_RULE);
				result.append(',').append(sign).append(rule.getExceptionName());
			}
		}
		return result.toString();
	}

}
