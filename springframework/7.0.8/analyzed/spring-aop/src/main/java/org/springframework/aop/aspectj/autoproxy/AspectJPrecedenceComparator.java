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

package org.springframework.aop.aspectj.autoproxy;

import java.util.Comparator;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJAopUtils;
import org.springframework.aop.aspectj.AspectJPrecedenceInformation;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.Assert;

/**
 * 按优先级（<i>非</i>调用顺序）对 AspectJ 通知/Advisor 排序。
 *
 * <p>给定两条通知 {@code A} 与 {@code B}：
 * <ul>
 * <li>若 {@code A} 与 {@code B} 定义在不同切面中，
 * 则 order 值最小的切面中的通知优先级最高。</li>
 * <li>若 {@code A} 与 {@code B} 定义在同一切面中，
 * 若其中一条为 <em>after</em> 通知，则切面中后声明的通知优先级最高；
 * 若两者均非 <em>after</em> 通知，则先声明的通知优先级最高。</li>
 * </ul>
 *
 * <p>重要：本比较器与 AspectJ 的
 * {@link org.aspectj.util.PartialOrder PartialOrder} 排序工具配合使用。
 * 因此与普通 {@link Comparator} 不同，返回 {@code 0} 表示不关心排序，
 * 而非两元素必须完全相同顺序。
 *
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @since 2.0
 */
class AspectJPrecedenceComparator implements Comparator<Advisor> {

	private static final int HIGHER_PRECEDENCE = -1;

	private static final int SAME_PRECEDENCE = 0;

	private static final int LOWER_PRECEDENCE = 1;


	private final Comparator<? super Advisor> advisorComparator;


	/**
	 * 创建默认的 {@code AspectJPrecedenceComparator}。
	 */
	public AspectJPrecedenceComparator() {
		this.advisorComparator = AnnotationAwareOrderComparator.INSTANCE;
	}

	/**
	 * 创建 {@code AspectJPrecedenceComparator}，
	 * 使用给定 {@link Comparator} 比较 {@link org.springframework.aop.Advisor} 实例。
	 * @param advisorComparator 用于 Advisor 的 {@code Comparator}
	 */
	public AspectJPrecedenceComparator(Comparator<? super Advisor> advisorComparator) {
		Assert.notNull(advisorComparator, "Advisor comparator must not be null");
		this.advisorComparator = advisorComparator;
	}


	@Override
	public int compare(Advisor o1, Advisor o2) {
		int advisorPrecedence = this.advisorComparator.compare(o1, o2);
		if (advisorPrecedence == SAME_PRECEDENCE && declaredInSameAspect(o1, o2)) {
			advisorPrecedence = comparePrecedenceWithinAspect(o1, o2);
		}
		return advisorPrecedence;
	}

	private int comparePrecedenceWithinAspect(Advisor advisor1, Advisor advisor2) {
		boolean oneOrOtherIsAfterAdvice =
				(AspectJAopUtils.isAfterAdvice(advisor1) || AspectJAopUtils.isAfterAdvice(advisor2));
		int adviceDeclarationOrderDelta = getAspectDeclarationOrder(advisor1) - getAspectDeclarationOrder(advisor2);

		if (oneOrOtherIsAfterAdvice) {
			// 后声明的通知优先级更高
			if (adviceDeclarationOrderDelta < 0) {
				// advice1 先于 advice2 声明，故 advice1 优先级更低
				return LOWER_PRECEDENCE;
			}
			else if (adviceDeclarationOrderDelta == 0) {
				return SAME_PRECEDENCE;
			}
			else {
				return HIGHER_PRECEDENCE;
			}
		}
		else {
			// 先声明的通知优先级更高
			if (adviceDeclarationOrderDelta < 0) {
				// advice1 先于 advice2 声明，故 advice1 优先级更高
				return HIGHER_PRECEDENCE;
			}
			else if (adviceDeclarationOrderDelta == 0) {
				return SAME_PRECEDENCE;
			}
			else {
				return LOWER_PRECEDENCE;
			}
		}
	}

	private boolean declaredInSameAspect(Advisor advisor1, Advisor advisor2) {
		return (hasAspectName(advisor1) && hasAspectName(advisor2) &&
				getAspectName(advisor1).equals(getAspectName(advisor2)));
	}

	private boolean hasAspectName(Advisor advisor) {
		return (advisor instanceof AspectJPrecedenceInformation ||
				advisor.getAdvice() instanceof AspectJPrecedenceInformation);
	}

	// 前置条件：hasAspectName 已返回 true
	private String getAspectName(Advisor advisor) {
		AspectJPrecedenceInformation precedenceInfo = AspectJAopUtils.getAspectJPrecedenceInformationFor(advisor);
		Assert.state(precedenceInfo != null, () -> "Unresolvable AspectJPrecedenceInformation for " + advisor);
		return precedenceInfo.getAspectName();
	}

	private int getAspectDeclarationOrder(Advisor advisor) {
		AspectJPrecedenceInformation precedenceInfo = AspectJAopUtils.getAspectJPrecedenceInformationFor(advisor);
		return (precedenceInfo != null ? precedenceInfo.getDeclarationOrder() : 0);
	}

}
