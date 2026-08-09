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

package org.springframework.context;

/**
 * 定义启动/停止生命周期控制的通用接口。
 * 典型用途是控制异步处理。
 * <b>注意：本接口不隐含特定的自动启动语义。
 * 若需要该能力，请考虑实现 {@link SmartLifecycle}。</b>
 *
 * <p>既可由组件实现（通常是 Spring 上下文中定义的 Bean），
 * 也可由容器实现（通常是 Spring {@link ApplicationContext} 本身）。
 * 容器会向其中所有适用的组件传播启动/停止信号，例如运行时的停止/重启场景。
 *
 * <p>可用于直接调用，也可通过 JMX 进行管理操作。
 * 后者通常将 {@link org.springframework.jmx.export.MBeanExporter} 配置为使用
 * {@link org.springframework.jmx.export.assembler.InterfaceBasedMBeanInfoAssembler}，
 * 将受生命周期控制的组件的可见性限制在 {@code Lifecycle} 接口上。
 *
 * <p>注意，当前的 {@code Lifecycle} 接口仅支持<b>顶层单例 Bean</b>。
 * 在其他组件上，{@code Lifecycle} 接口不会被检测到，因而被忽略。
 * 此外，扩展接口 {@link SmartLifecycle} 提供了与应用程序上下文
 * 启动和关闭阶段更紧密的集成。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see SmartLifecycle
 * @see ConfigurableApplicationContext
 * @see org.springframework.jms.listener.AbstractMessageListenerContainer
 * @see org.springframework.scheduling.quartz.SchedulerFactoryBean
 */
public interface Lifecycle {

	/**
	 * 启动此组件。
	 * <p>若组件已在运行，不应抛出异常。
	 * <p>对于容器，会向所有适用的组件传播强制启动信号，
	 * 包括非自动启动的组件。
	 * @see SmartLifecycle#isAutoStartup()
	 */
	void start();

	/**
	 * 停止此组件，通常以同步方式执行，使得本方法返回时组件已完全停止。
	 * 若需要异步停止行为，请考虑实现 {@link SmartLifecycle} 及其 {@code stop(Runnable)} 变体。
	 * <p>注意，此停止通知不保证在销毁之前发生：
	 * 正常关闭时，{@code Lifecycle} Bean 会先收到停止通知，再传播通用销毁回调；
	 * 但在上下文生命周期内的热刷新或刷新中止时，给定 Bean 的 destroy 方法
	 * 会在未事先收到停止信号的情况下被调用。
	 * <p>若组件未在运行（尚未启动），不应抛出异常。
	 * <p>对于容器，会向所有适用的组件传播停止信号。
	 * @see SmartLifecycle#stop(Runnable)
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	void stop();

	/**
	 * 检查此组件当前是否正在运行。
	 * <p>对于容器，仅当所有适用的组件<i>都</i>在运行时，才返回 {@code true}。
	 * @return whether the component is currently running
	 */
	boolean isRunning();

}
