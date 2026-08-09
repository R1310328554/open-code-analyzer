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

import org.jspecify.annotations.Nullable;

import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.DisposableBean;

/**
 * 池化 {@link org.springframework.aop.TargetSource} 实现的抽象基类，
 * 维护目标实例池，每次方法调用时从池中获取并释放目标对象。
 * 本抽象基类与具体池化技术无关；
 * 具体示例见子类 {@link CommonsPool2TargetSource}。
 *
 * <p>子类须根据所选对象池实现 {@link #getTarget} 与
 * {@link #releaseTarget} 方法。
 * 可继承 {@link AbstractPrototypeBasedTargetSource} 的
 * {@link #newPrototypeInstance()} 方法创建对象并放入池中。
 *
 * <p>子类还须实现 {@link PoolingConfig} 接口的部分监控方法。
 * {@link #getPoolingConfigMixin()} 通过 IntroductionAdvisor
 * 在代理对象上暴露这些统计信息。
 *
 * <p>本类实现 {@link org.springframework.beans.factory.DisposableBean} 接口，
 * 强制子类实现 {@link #destroy()} 方法以关闭对象池。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getTarget
 * @see #releaseTarget
 * @see #destroy
 */
@SuppressWarnings("serial")
public abstract class AbstractPoolingTargetSource extends AbstractPrototypeBasedTargetSource
		implements PoolingConfig, DisposableBean {

	/** 池的最大容量。 */
	private int maxSize = -1;


	/**
	 * 设置池的最大容量。
	 * 默认为 -1，表示无大小限制。
	 */
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	/**
	 * 返回池的最大容量。
	 */
	@Override
	public int getMaxSize() {
		return this.maxSize;
	}


	@Override
	public final void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		super.setBeanFactory(beanFactory);
		try {
			createPool();
		}
		catch (Throwable ex) {
			throw new BeanInitializationException("Could not create instance pool for TargetSource", ex);
		}
	}


	/**
	 * 创建对象池。
	 * @throws Exception 避免对池化 API 施加约束
	 */
	protected abstract void createPool() throws Exception;

	/**
	 * 从池中获取对象。
	 * @return 池中的对象
	 * @throws Exception 池化 API 可能抛出受检异常，故签名较宽松
	 */
	@Override
	public abstract @Nullable Object getTarget() throws Exception;

	/**
	 * 将给定对象归还池中。
	 * @param target 须通过 {@code getTarget()} 从池中获取的对象
	 * @throws Exception 允许池化 API 抛出异常
	 * @see #getTarget
	 */
	@Override
	public abstract void releaseTarget(Object target) throws Exception;


	/**
	 * 返回提供 mixin 的 IntroductionAdvisor，
	 * 暴露本对象维护的池统计信息。
	 */
	public DefaultIntroductionAdvisor getPoolingConfigMixin() {
		DelegatingIntroductionInterceptor dii = new DelegatingIntroductionInterceptor(this);
		return new DefaultIntroductionAdvisor(dii, PoolingConfig.class);
	}

}
