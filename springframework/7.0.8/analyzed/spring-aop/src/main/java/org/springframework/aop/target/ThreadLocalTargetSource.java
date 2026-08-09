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

package org.springframework.aop.target;

import java.util.HashSet;
import java.util.Set;

import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.NamedThreadLocal;

/**
 * 对象池的替代方案。本 {@link org.springframework.aop.TargetSource}
 * 采用每线程持有目标副本的线程模型。
 * 目标无竞争，运行服务器上目标对象创建次数最少。
 *
 * <p>应用代码写法类似普通池；调用方不能假设不同线程调用会处理同一实例。
 * 但在单线程操作期间可依赖状态：例如同一调用方多次调用 AOP 代理。
 *
 * <p>BeanFactory 销毁时清理线程绑定对象，
 * 若可用则调用其 {@code DisposableBean.destroy()}。
 * 注意：许多线程绑定对象可能直到应用真正关闭才释放。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @see ThreadLocalTargetSourceStats
 * @see org.springframework.beans.factory.DisposableBean#destroy()
 */
@SuppressWarnings("serial")
public class ThreadLocalTargetSource extends AbstractPrototypeBasedTargetSource
		implements ThreadLocalTargetSourceStats, DisposableBean {

	/**
	 * 持有当前线程关联目标的 ThreadLocal。
	 * 与多数 static 的 ThreadLocal 不同，
	 * 本变量为每个 ThreadLocalTargetSource 实例、每线程一份。
	 */
	private final ThreadLocal<Object> targetInThread =
			new NamedThreadLocal<>("Thread-local instance of bean") {
				@Override
				public String toString() {
					return super.toString() + " '" + targetBeanName + "'";
				}
			};

	/**
	 * 受管目标集合，用于跟踪已创建的目标。
	 */
	private final Set<Object> targetSet = new HashSet<>();

	private int invocationCount;

	private int hitCount;


	/**
	 * 抽象 getTarget() 方法的实现。
	 * 在 ThreadLocal 中查找目标；若无则创建并绑定到线程。无需同步。
	 */
	@Override
	public Object getTarget() throws BeansException {
		++this.invocationCount;
		Object target = this.targetInThread.get();
		if (target == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("No target for prototype '" + this.targetBeanName + "' bound to thread: " +
						"creating one and binding it to thread '" + Thread.currentThread().getName() + "'");
			}
			// 将目标关联到 ThreadLocal。
			target = newPrototypeInstance();
			this.targetInThread.set(target);
			synchronized (this.targetSet) {
				this.targetSet.add(target);
			}
		}
		else {
			++this.hitCount;
		}
		return target;
	}

	/**
	 * 必要时销毁目标；清除 ThreadLocal。
	 * @see #destroyPrototypeInstance
	 */
	@Override
	public void destroy() {
		logger.debug("正在销毁 ThreadLocalTargetSource 绑定");
		synchronized (this.targetSet) {
			for (Object target : this.targetSet) {
				destroyPrototypeInstance(target);
			}
			this.targetSet.clear();
		}
		// 清除 ThreadLocal，以防万一。
		this.targetInThread.remove();
	}


	@Override
	public int getInvocationCount() {
		return this.invocationCount;
	}

	@Override
	public int getHitCount() {
		return this.hitCount;
	}

	@Override
	public int getObjectCount() {
		synchronized (this.targetSet) {
			return this.targetSet.size();
		}
	}


	/**
	 * 返回 introduction advisor mixin，
	 * 允许将 AOP 代理转型为 ThreadLocalInvokerStats。
	 */
	public IntroductionAdvisor getStatsMixin() {
		DelegatingIntroductionInterceptor dii = new DelegatingIntroductionInterceptor(this);
		return new DefaultIntroductionAdvisor(dii, ThreadLocalTargetSourceStats.class);
	}

}
