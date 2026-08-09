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
 * 按优先级对 AspectJ 建议/顾问进行排序（<i> 而非 </i> 调用顺序）。
 * <p>给定两条建议，{@code A} 和 {@code B}： <ul> <li> 如果 {@code A} 和 {@code B} 定义在不同方面，则顺序值最低的方面中的建
 * 议具有最高优先级。 </li> <li> 如果 {@code A} 和{@code B} 在同一切面中定义，如果 {@code A} 或 {@code B} 之一是 <em>a
 * fter</em> 建议的一种形式，则该切面中最后声明的建议具有最高优先级。如果 {@code A} 和 {@code B} 都不是 <em>after</em> 建议的形式，
 * 则该方面中首先声明的建议具有最高优先级。 </li> </ul>
 * <p>I 重要提示：此比较器与 AspectJ 的 {@link org.aspectj.util.PartialOrder PartialOrder}
 * 排序实用程序一起使用。因此，与普通的 {@link Comparator} 不同，此比较器的 {@code 0}
 * 返回值意味着我们不关心顺序，而不是两个元素必须以相同的方式排序。
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @since 2.0
 */
class AspectJPrecedenceComparator implements Comparator<Advisor> {

	private static final int HIGHER_PRECEDENCE = -1;

	private static final int SAME_PRECEDENCE = 0;

	private static final int LOWER_PRECEDENCE = 1;


	/** 通知器相关状态（`advisorComparator`）。 */
	private final Comparator<? super Advisor> advisorComparator;


	/**
	 * 创建默认的 {@code AspectJPrecedenceComparator}。
	 */
	public AspectJPrecedenceComparator() {
		this.advisorComparator = AnnotationAwareOrderComparator.INSTANCE;
	}

	/**
	 * 创建 {@code AspectJPrecedenceComparator}，使用给定的 {@link Comparator} 来比较 {@link
	 * org.springframework.aop.Advisor} 实例。
	 * @param advisorComparator 用于顾问的 {@code Comparator}
	 */
	public AspectJPrecedenceComparator(Comparator<? super Advisor> advisorComparator) {
		Assert.notNull(advisorComparator, "Advisor comparator must not be null");
		this.advisorComparator = advisorComparator;
	}


	/**
	 * 方法 `compare`：完成本类中与「compare」相关的职责。
	 */
	@Override
	public int compare(Advisor o1, Advisor o2) {
		int advisorPrecedence = this.advisorComparator.compare(o1, o2);
		if (advisorPrecedence == SAME_PRECEDENCE && declaredInSameAspect(o1, o2)) {
			advisorPrecedence = comparePrecedenceWithinAspect(o1, o2);
		}
		return advisorPrecedence;
	}

	/**
	 * 方法 `comparePrecedenceWithinAspect`：完成本类中与「compare Precedence Within Aspect」相关的职责。
	 */
	private int comparePrecedenceWithinAspect(Advisor advisor1, Advisor advisor2) {
		boolean oneOrOtherIsAfterAdvice =
				(AspectJAopUtils.isAfterAdvice(advisor1) || AspectJAopUtils.isAfterAdvice(advisor2));
		int adviceDeclarationOrderDelta = getAspectDeclarationOrder(advisor1) - getAspectDeclarationOrder(advisor2);

		if (oneOrOtherIsAfterAdvice) {
			// 最后声明的建议具有更高的优先级
			if (adviceDeclarationOrderDelta < 0) {
				// 通知 1 在通知 2 之前声明
				// 所以advice1的优先级较低
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
			// 首先声明的建议具有更高的优先级
			if (adviceDeclarationOrderDelta < 0) {
				// 通知 1 在通知 2 之前声明
				// 所以advice1有更高的优先级
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

	/**
	 * 方法 `declaredInSameAspect`：完成本类中与「declared In Same Aspect」相关的职责。
	 */
	private boolean declaredInSameAspect(Advisor advisor1, Advisor advisor2) {
		return (hasAspectName(advisor1) && hasAspectName(advisor2) &&
				getAspectName(advisor1).equals(getAspectName(advisor2)));
	}

	/**
	 * 判断是否包含/具备 Aspect Name。
	 */
	private boolean hasAspectName(Advisor advisor) {
		return (advisor instanceof AspectJPrecedenceInformation ||
				advisor.getAdvice() instanceof AspectJPrecedenceInformation);
	}

	// 前提条件是 hasAspectName 返回 true
	/**
	 * 获取 Aspect Name（`AspectName`）。
	 */
	private String getAspectName(Advisor advisor) {
		AspectJPrecedenceInformation precedenceInfo = AspectJAopUtils.getAspectJPrecedenceInformationFor(advisor);
		Assert.state(precedenceInfo != null, () -> "Unresolvable AspectJPrecedenceInformation for " + advisor);
		return precedenceInfo.getAspectName();
	}

	/**
	 * 获取 Aspect Declaration Order（`AspectDeclarationOrder`）。
	 */
	private int getAspectDeclarationOrder(Advisor advisor) {
		AspectJPrecedenceInformation precedenceInfo = AspectJAopUtils.getAspectJPrecedenceInformationFor(advisor);
		return (precedenceInfo != null ? precedenceInfo.getDeclarationOrder() : 0);
	}

}
