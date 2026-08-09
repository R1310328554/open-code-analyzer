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
 * 由持有 AOP 代理工厂配置的类实现的接口。
 * 该配置包括 Interceptor 及其他 advice、Advisor 与被代理接口。
 *
 * <p>从 Spring 获取的任意 AOP 代理可转型为本接口以操作其 AOP advice。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 13.03.2003
 * @see org.springframework.aop.framework.AdvisedSupport
 */
public interface Advised extends TargetClassAware {

	/**
	 * 返回 Advised 配置是否已冻结，冻结后不可修改 advice。
	 */
	boolean isFrozen();

	/**
	 * 是否代理完整目标类而非指定接口。
	 */
	boolean isProxyTargetClass();

	/**
	 * 返回 AOP 代理所代理的接口。
	 * <p>不包含目标类（目标类本身也可能被代理）。
	 */
	Class<?>[] getProxiedInterfaces();

	/**
	 * 判断给定接口是否被代理。
	 * @param ifc 待检查的接口
	 */
	boolean isInterfaceProxied(Class<?> ifc);

	/**
	 * 更改本 {@code Advised} 对象使用的 {@code TargetSource}。
	 * <p>仅当配置未 {@linkplain #isFrozen 冻结} 时有效。
	 * @param targetSource 新的 TargetSource
	 */
	void setTargetSource(TargetSource targetSource);

	/**
	 * 返回本 {@code Advised} 对象使用的 {@code TargetSource}。
	 */
	TargetSource getTargetSource();

	/**
	 * 设置 AOP 框架是否将代理以 {@link ThreadLocal} 暴露，
	 * 以便通过 {@link AopContext} 类获取。
	 * <p>若被通知对象需在自身上调用带 advice 的方法，可能需要暴露代理。
	 * 否则对 {@code this} 调用方法时不会应用 advice。
	 * <p>默认为 {@code false}，以获得最佳性能。
	 */
	void setExposeProxy(boolean exposeProxy);

	/**
	 * 返回工厂是否应将代理以 {@link ThreadLocal} 暴露。
	 * <p>若被通知对象需在自身上调用带 advice 的方法，可能需要暴露代理。
	 * 否则对 {@code this} 调用方法时不会应用 advice。
	 * <p>获取代理类似 EJB 调用 {@code getEJBObject()}。
	 * @see AopContext
	 */
	boolean isExposeProxy();

	/**
	 * 设置本代理配置是否已预过滤，仅包含适用的通知器（匹配本代理目标类）。
	 * <p>默认为 "false"。若通知器已预过滤，设为 "true"，
	 * 构建代理调用的实际通知器链时可跳过 ClassFilter 检查。
	 * @see org.springframework.aop.ClassFilter
	 */
	void setPreFiltered(boolean preFiltered);

	/**
	 * 返回本代理配置是否已预过滤，仅包含适用的通知器（匹配本代理目标类）。
	 */
	boolean isPreFiltered();

	/**
	 * 返回应用于本代理的通知器。
	 * @return 应用于本代理的通知器列表（永不 {@code null}）
	 */
	Advisor[] getAdvisors();

	/**
	 * 返回应用于本代理的通知器数量。
	 * <p>默认实现委托给 {@code getAdvisors().length}。
	 * @since 5.3.1
	 */
	default int getAdvisorCount() {
		return getAdvisors().length;
	}

	/**
	 * 在通知器链末尾添加通知器。
	 * <p>通知器可以是 {@link org.springframework.aop.IntroductionAdvisor}，
	 * 下次从相关工厂获取代理时将可用新接口。
	 * @param advisor 要添加到链末尾的通知器
	 * @throws AopConfigException advice 无效时
	 */
	void addAdvisor(Advisor advisor) throws AopConfigException;

	/**
	 * 在链的指定位置添加通知器。
	 * @param advisor 要添加的通知器
	 * @param pos 链中位置（0 为头部），须有效
	 * @throws AopConfigException advice 无效时
	 */
	void addAdvisor(int pos, Advisor advisor) throws AopConfigException;

	/**
	 * 移除给定通知器。
	 * @param advisor 要移除的通知器
	 * @return 若已移除则为 {@code true}；
	 * 未找到因而无法移除则为 {@code false}
	 */
	boolean removeAdvisor(Advisor advisor);

	/**
	 * 移除指定索引处的通知器。
	 * @param index 要移除的通知器索引
	 * @throws AopConfigException 索引无效时
	 */
	void removeAdvisor(int index) throws AopConfigException;

	/**
	 * 返回给定通知器从 0 起的索引，
	 * 若无适用通知器则返回 -1。
	 * <p>返回值可用于索引通知器数组。
	 * @param advisor 要查找的通知器
	 * @return 该通知器从 0 起的索引，不存在则为 -1
	 */
	int indexOf(Advisor advisor);

	/**
	 * 替换给定通知器。
	 * <p><b>注意：</b> 若通知器为 {@link org.springframework.aop.IntroductionAdvisor}
	 * 而替换项不是或实现不同接口，须重新获取代理，
	 * 否则旧接口不受支持且新接口不会实现。
	 * @param a 要替换的通知器
	 * @param b 替换为的通知器
	 * @return 是否已替换。若通知器列表中未找到，
	 * 返回 {@code false} 且不执行任何操作。
	 * @throws AopConfigException advice 无效时
	 */
	boolean replaceAdvisor(Advisor a, Advisor b) throws AopConfigException;

	/**
	 * 将给定 AOP Alliance advice 添加到 advice（拦截器）链尾部。
	 * <p>将包装为始终适用的 DefaultPointcutAdvisor，
	 * 并以包装形式从 {@code getAdvisors()} 返回。
	 * <p>注意：给定 advice 将应用于代理上的所有调用，
	 * 包括 {@code toString()}！请使用合适的 advice 实现
	 * 或指定更窄的切点以限制方法范围。
	 * @param advice 要添加到链尾部的 advice
	 * @throws AopConfigException advice 无效时
	 * @see #addAdvice(int, Advice)
	 * @see org.springframework.aop.support.DefaultPointcutAdvisor
	 */
	void addAdvice(Advice advice) throws AopConfigException;

	/**
	 * 在 advice 链的指定位置添加给定 AOP Alliance Advice。
	 * <p>将包装为始终适用的 {@link org.springframework.aop.support.DefaultPointcutAdvisor}，
	 * 并以包装形式从 {@link #getAdvisors()} 返回。
	 * <p>注意：给定 advice 将应用于代理上的所有调用，
	 * 包括 {@code toString()}！请使用合适的 advice 实现
	 * 或指定更窄的切点以限制方法范围。
	 * @param pos 从 0（头部）起的索引
	 * @param advice 要添加到 advice 链指定位置的 advice
	 * @throws AopConfigException advice 无效时
	 */
	void addAdvice(int pos, Advice advice) throws AopConfigException;

	/**
	 * 移除包含给定 advice 的通知器。
	 * @param advice 要移除的 advice
	 * @return 若找到并移除则为 {@code true}；
	 * 无此 advice 则为 {@code false}
	 */
	boolean removeAdvice(Advice advice);

	/**
	 * 返回给定 AOP Alliance Advice 从 0 起的索引，
	 * 若无适用 advice 则返回 -1。
	 * <p>返回值可用于索引通知器数组。
	 * @param advice 要查找的 AOP Alliance advice
	 * @return 该 advice 从 0 起的索引，不存在则为 -1
	 */
	int indexOf(Advice advice);

	/**
	 * 因 {@code toString()} 通常委托给目标，
	 * 本方法返回 AOP 代理的等价描述。
	 * @return 代理配置的字符串描述
	 */
	String toProxyConfigString();

}
