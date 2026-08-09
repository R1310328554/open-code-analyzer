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
 * 对象池的替代方案。此 {@link org.springframework.aop.TargetSource} 使用线程模型，其中每个线程都有自己的目标副本。不存在对目标的争夺
 * 。在运行的服务器上目标对象的创建保持在最低限度。
 * <p>应用程序代码是按照普通池编写的；调用者不能假设他们将在不同线程的调用中处理相同的实例。但是，在单个线程的操作期间可以依赖状态：例如，如果一个调用者对 AOP 代理进行重复
 * 调用。
 * 线程绑定对象的 <p>Cleanup 在 BeanFactory 销毁时执行，调用它们的 {@code DisposableBean.destroy()} 方法（如果可用）。请
 * 注意，许多线程绑定对象可能会一直存在，直到应用程序实际关闭为止。
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
	 * ThreadLocal 保存与当前线程关联的目标。与大多数静态 ThreadLocal 不同，此变量适用于 ThreadLocalTargetSource
	 * 类的每个实例的每个线程。
	 */
	private final ThreadLocal<Object> targetInThread =
			new NamedThreadLocal<>("Thread-local instance of bean") {
				@Override
				public String toString() {
					return super.toString() + " '" + targetBeanName + "'";
				}
			};

	/**
	 * 一组托管目标，使我们能够跟踪我们创建的目标。
	 */
	private final Set<Object> targetSet = new HashSet<>();

	/** `invocationCount`：该类的成员状态。 */
	private int invocationCount;

	/** `hitCount`：该类的成员状态。 */
	private int hitCount;


	/**
	 * 抽象 getTarget() 方法的实现。我们寻找 ThreadLocal 中保存的目标。如果找不到，我们就创建一个并将其绑定到线程。不需要同步。
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
			// 将目标与 ThreadLocal 关联。
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
	 * 如有必要，处置目标；清除ThreadLocal。
	 * @see #destroyPrototypeInstance
	 */
	@Override
	public void destroy() {
		logger.debug("Destroying ThreadLocalTargetSource bindings");
		synchronized (this.targetSet) {
			for (Object target : this.targetSet) {
				destroyPrototypeInstance(target);
			}
			this.targetSet.clear();
		}
		// 清除 ThreadLocal，以防万一。
		this.targetInThread.remove();
	}


	/**
	 * 获取 Invocation Count（`InvocationCount`）。
	 */
	@Override
	public int getInvocationCount() {
		return this.invocationCount;
	}

	/**
	 * 获取 Hit Count（`HitCount`）。
	 */
	@Override
	public int getHitCount() {
		return this.hitCount;
	}

	/**
	 * 获取 Object Count（`ObjectCount`）。
	 */
	@Override
	public int getObjectCount() {
		synchronized (this.targetSet) {
			return this.targetSet.size();
		}
	}


	/**
	 * 返回一个引入顾问 mixin，允许将 AOP 代理强制转换为 ThreadLocalInvokerStats。
	 */
	public IntroductionAdvisor getStatsMixin() {
		DelegatingIntroductionInterceptor dii = new DelegatingIntroductionInterceptor(this);
		return new DefaultIntroductionAdvisor(dii, ThreadLocalTargetSourceStats.class);
	}

}
