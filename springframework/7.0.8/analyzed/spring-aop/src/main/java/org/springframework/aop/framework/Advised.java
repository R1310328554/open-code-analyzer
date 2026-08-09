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

package org.springframework.aop.framework;

import org.aopalliance.aop.Advice;

import org.springframework.aop.Advisor;
import org.springframework.aop.TargetClassAware;
import org.springframework.aop.TargetSource;

/**
 * 由保存 AOP 代理工厂配置的类实现的接口。此配置包括拦截器和其他建议、顾问和代理接口。
 * <p>A从 Spring 获取的任何 AOP 代理都可以转换为此接口，以允许操作其 AOP 建议。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 13.03.2003
 * @see org.springframework.aop.framework.AdvisedSupport
 */
public interface Advised extends TargetClassAware {

	/**
	 * 返回建议配置是否被冻结，在这种情况下不能进行建议更改。
	 */
	boolean isFrozen();

	/**
	 * 我们是否代理完整的目标类而不是指定的接口？
	 */
	boolean isProxyTargetClass();

	/**
	 * 返回AOP代理代理的接口。 <p>不会包含目标类，该类也可能被代理。
	 */
	Class<?>[] getProxiedInterfaces();

	/**
	 * 确定给定接口是否被代理。
	 * @param ifc 要检查的接口
	 */
	boolean isInterfaceProxied(Class<?> ifc);

	/**
	 * 更改此 {@code Advised} 对象使用的 {@code TargetSource}。 <p>仅在配置不是 {@linkplain #isFrozen frozen}
	 * 时有效。
	 * @param targetSource 要使用的新 TargetSource
	 */
	void setTargetSource(TargetSource targetSource);

	/**
	 * 返回此 {@code Advised} 对象使用的 {@code TargetSource}。
	 */
	TargetSource getTargetSource();

	/**
	 * 设置 AOP 框架是否应将代理公开为 {@link ThreadLocal}，以便通过 {@link AopContext} 类进行检索。 <p> 如果建议对象需要在应用建议的
	 * 情况下调用自身的方法，则可能需要公开代理。否则，如果建议对象调用 {@code this} 上的方法，则不会应用任何建议。 <p>D默认为 {@code false}，以获得最
	 * 佳性能。
	 */
	void setExposeProxy(boolean exposeProxy);

	/**
	 * 返回工厂是否应将代理公开为 {@link ThreadLocal}。 <p> 如果建议对象需要在应用建议的情况下调用自身的方法，则可能需要公开代理。否则，如果建议对象调用 {@
	 * code this} 上的方法，则不会应用任何建议。 <p> 获取代理类似于 EJB 调用 {@code getEJBObject()}。
	 * @see AopContext
	 */
	boolean isExposeProxy();

	/**
	 * 设置是否预先过滤此代理配置，以便它仅包含适用的顾问程序（与此代理的目标类匹配）。 <p>默认为“假”。如果顾问已被预先过滤，则将其设置为“true”，这意味着在构建代理调用的实
	 * 际顾问链时可以跳过 ClassFilter 检查。
	 * @see org.springframework.aop.ClassFilter
	 */
	void setPreFiltered(boolean preFiltered);

	/**
	 * 返回此代理配置是否已预先过滤，以便它仅包含适用的顾问程序（与此代理的目标类匹配）。
	 */
	boolean isPreFiltered();

	/**
	 * 返回申请此代理的顾问。
	 * @return 申请此代理的顾问列表（绝不是 {@code null}）
	 */
	Advisor[] getAdvisors();

	/**
	 * 返回申请此代理的顾问数量。 <p>默认实现委托给{@code getAdvisors().length}。
	 * @since 5.3.1
	 */
	default int getAdvisorCount() {
		return getAdvisors().length;
	}

	/**
	 * 在顾问链的末尾添加顾问。 <p>顾问可以是{@link
	 * org.springframework.aop.IntroductionAdvisor}，其中当下次从相关工厂获得代理时，新接口将可用。
	 * @param advisor 添加到链末尾的顾问
	 * @throws AopConfigException 如果建议无效
	 */
	void addAdvisor(Advisor advisor) throws AopConfigException;

	/**
	 * 在链中的指定位置添加顾问。
	 * @param advisor 添加到链中指定位置的顾问
	 * @param pos 链中的位置（0 为头）。必须有效。
	 * @throws AopConfigException 如果建议无效
	 */
	void addAdvisor(int pos, Advisor advisor) throws AopConfigException;

	/**
	 * 删除给定的顾问。
	 * @param advisor 要删除的顾问
	 * @return true} 如果顾问被删除； {@code false} 如果未找到顾问程序，因此无法删除
	 */
	boolean removeAdvisor(Advisor advisor);

	/**
	 * 删除给定索引处的顾问程序。
	 * @param index 要删除的顾问索引
	 * @throws AopConfigException 如果索引无效
	 */
	void removeAdvisor(int index) throws AopConfigException;

	/**
	 * 返回给定顾问的索引（从 0 开始），如果没有此类顾问适用于此代理，则返回 -1。 <p>该方法的返回值可用于索引到advisors数组中。
	 * @param advisor 要寻找的顾问
	 * @return 从该顾问的 0 开始，如果没有这样的顾问，则为 -1
	 */
	int indexOf(Advisor advisor);

	/**
	 * 替换给定的顾问。 <p><b>注意：</b> 如果顾问程序是 {@link
	 * org.springframework.aop.IntroductionAdvisor}，而替换不是 {@link
	 * org.springframework.aop.IntroductionAdvisor} 或实现不同的接口，则需要重新获取代理，否则将不支持旧接口，并且不会实现新接口。
	 * @param a 更换顾问
	 * @param b 替换它的顾问
	 * @return 它被替换了。如果在顾问列表中未找到顾问，则此方法返回 {@code false} 并且不执行任何操作。
	 * @throws AopConfigException 如果建议无效
	 */
	boolean replaceAdvisor(Advisor a, Advisor b) throws AopConfigException;

	/**
	 * 将给定的 AOP 联盟建议添加到建议（拦截器）链的尾部。 <p>This 将被包装在具有始终适用的切入点的 DefaultPointcutAdvisor 中，并以此包装形式从 
	 * {@code getAdvisors()} 方法返回。 <p>请注意，给定的建议将适用于代理上的所有调用，甚至适用于 {@code toString()} 方法！使用适当的建议
	 * 实现或指定适当的切入点以应用于更窄的方法集。
	 * @param advice 添加到链尾的建议
	 * @throws AopConfigException 如果建议无效
	 * @see #addAdvice(int, Advice)
	 * @see org.springframework.aop.support.DefaultPointcutAdvisor
	 */
	void addAdvice(Advice advice) throws AopConfigException;

	/**
	 * 将给定的 AOP 联盟建议添加到建议链中的指定位置。 <p>This 将被包装在 {@link org.springframework.aop.support.DefaultP
	 * ointcutAdvisor} 中，并带有始终适用的切入点，并以此包装形式从 {@link #getAdvisors()} 方法返回。 <p>注意：给定的建议将适用于代理上的所
	 * 有调用，甚至适用于 {@code toString()} 方法！使用适当的建议实现或指定适当的切入点以应用于更窄的方法集。
	 * @param pos 索引从0开始（头）
	 * @param advice 添加到建议链中指定位置的建议
	 * @throws AopConfigException 如果建议无效
	 */
	void addAdvice(int pos, Advice advice) throws AopConfigException;

	/**
	 * 删除包含给定建议的 Advisor。
	 * @param advice 删除的建议
	 * @return true} 的建议被发现并被删除； {@code false} 如果没有这样的建议
	 */
	boolean removeAdvice(Advice advice);

	/**
	 * 返回给定 AOP 联盟建议的索引（从 0 开始），如果没有此类建议是此代理的建议，则返回 -1。 <p>该方法的返回值可用于索引到advisors数组中。
	 * @param advice AOP 联盟建议搜索
	 * @return 从 0 条这条建议开始，如果没有这条建议，则从 -1 开始
	 */
	int indexOf(Advice advice);

	/**
	 * 由于 {@code toString()} 通常会委托给目标，因此这将返回 AOP 代理的等效项。
	 * @return 代理配置的字符串描述
	 */
	String toProxyConfigString();

}
