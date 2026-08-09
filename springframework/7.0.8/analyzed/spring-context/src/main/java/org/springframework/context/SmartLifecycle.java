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
 * {@link Lifecycle} 接口的扩展，适用于需要在 {@code ApplicationContext}
 * 刷新和/或关闭时按特定顺序启动的对象。
 *
 * <p>{@link #isAutoStartup()} 的返回值指示此对象是否应在上下文刷新时自动启动。
 * 接受回调的 {@link #stop(Runnable)} 方法适用于具有异步关闭过程的对象。
 * 本接口的任何实现<b>必须</b>在关闭完成后调用回调的 {@code run()} 方法，
 * 以避免整体 {@code ApplicationContext} 关闭出现不必要的延迟。
 *
 * <p>本接口扩展了 {@link Phased}，{@link #getPhase()} 的返回值指示此
 * {@code Lifecycle} 组件应在哪个阶段内启动和停止。启动过程从<b>最低</b>阶段值开始，
 * 到<b>最高</b>阶段值结束（{@code Integer.MIN_VALUE} 为最低，
 * {@code Integer.MAX_VALUE} 为最高）。关闭过程按相反顺序进行。
 * 相同阶段值的组件在该阶段内顺序任意。
 *
 * <p>示例：若组件 B 依赖组件 A 已启动，则组件 A 的阶段值应低于组件 B。
 * 关闭时，组件 B 会在组件 A 之前停止。
 *
 * <p>任何显式的 "depends-on" 关系优先于阶段顺序，
 * 使得依赖 Bean 始终在依赖项之后启动、在依赖项之前停止。
 *
 * <p>上下文中未同时实现 {@code SmartLifecycle} 的任何 {@code Lifecycle} 组件
 * 将被视为阶段值为 {@code 0}。这使得 {@code SmartLifecycle} 组件
 * 若阶段值为负可在那些 {@code Lifecycle} 组件之前启动，
 * 若阶段值为正则可在那些组件之后启动。
 *
 * <p>注意，由于 {@code SmartLifecycle} 中的自动启动支持，
 * {@code SmartLifecycle} Bean 实例通常会在应用上下文启动时即被初始化。
 * 因此，Bean 定义的 lazy-init 标志对 {@code SmartLifecycle} Bean 的实际效果非常有限。
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.0
 * @see LifecycleProcessor
 * @see ConfigurableApplicationContext
 */
public interface SmartLifecycle extends Lifecycle, Phased {

	/**
	 * {@code SmartLifecycle} 的默认阶段：{@code Integer.MAX_VALUE}。
	 * <p>这与普通 {@link Lifecycle} 实现常用的阶段 {@code 0} 不同，
	 * 使通常自动启动的 {@code SmartLifecycle} Bean 处于更晚的启动阶段、更早的关闭阶段。
	 * <p>注意，某些 {@code SmartLifecycle} 组件带有不同的默认阶段：
	 * 例如，执行器/调度器的默认阶段为 {@code Integer.MAX_VALUE / 2}。
	 * @since 5.1
	 * @see #getPhase()
	 * @see org.springframework.scheduling.concurrent.ExecutorConfigurationSupport#DEFAULT_PHASE
	 * @see org.springframework.context.support.DefaultLifecycleProcessor#setTimeoutPerShutdownPhase
	 */
	int DEFAULT_PHASE = Integer.MAX_VALUE;


	/**
	 * 若此 {@code Lifecycle} 组件应在包含它的 {@link ApplicationContext}
	 * 刷新或重启时由容器自动启动，则返回 {@code true}。
	 * <p>{@code false} 表示组件应通过显式 {@link #start()} 调用启动，
	 * 类似于普通 {@link Lifecycle} 实现。
	 * <p>默认实现返回 {@code true}。
	 * @see #start()
	 * @see #getPhase()
	 * @see LifecycleProcessor#onRefresh()
	 * @see LifecycleProcessor#onRestart()
	 * @see ConfigurableApplicationContext#refresh()
	 * @see ConfigurableApplicationContext#restart()
	 */
	default boolean isAutoStartup() {
		return true;
	}

	/**
	 * 若此 {@code Lifecycle} 组件能够参与重启序列（可能先暂停再收到
	 * {@link #stop()} 和 {@link #start()} 调用），则返回 {@code true}。
	 * <p>{@code false} 表示组件在暂停场景中希望被跳过，
	 * 既不收到 {@link #stop()} 也不收到随后的 {@link #start()}，
	 * 类似于普通 {@link Lifecycle} 实现。它仅在关闭和显式上下文级停止时
	 * 收到 {@link #stop()}，暂停时不会。
	 * <p>默认实现返回 {@code true}。
	 * @since 7.0
	 * @see #stop()
	 * @see LifecycleProcessor#onPause()
	 * @see LifecycleProcessor#onClose()
	 * @see ConfigurableApplicationContext#pause()
	 * @see ConfigurableApplicationContext#close()
	 */
	default boolean isPauseable() {
		return true;
	}

	/**
	 * 指示 Lifecycle 组件若当前正在运行则必须停止。
	 * <p>提供的回调由 {@link LifecycleProcessor} 使用，以支持具有相同关闭顺序值的
	 * 所有组件的有序（可能并发）关闭。回调<b>必须</b>在
	 * {@code SmartLifecycle} 组件确实停止后执行。
	 * <p>{@link LifecycleProcessor} 将<i>仅</i>调用此 {@code stop} 变体；
	 * 即，除非在本方法实现中显式委托，否则不会为 {@code SmartLifecycle}
	 * 实现调用 {@link Lifecycle#stop()}。
	 * <p>默认实现委托给 {@link #stop()} 并在调用线程中立即触发给定回调。
	 * 注意两者之间没有同步，因此自定义实现至少应将其共同步骤
	 * 放在共同的生命周期监视器（若有）内。
	 * @see #stop()
	 * @see #getPhase()
	 */
	default void stop(Runnable callback) {
		stop();
		callback.run();
	}

	/**
	 * 返回此生命周期对象应运行的阶段。
	 * <p>默认实现返回 {@link #DEFAULT_PHASE}，以便在普通
	 * {@code Lifecycle} 实现之前执行 {@code stop()} 回调。
	 * @see #isAutoStartup()
	 * @see #start()
	 * @see #stop(Runnable)
	 * @see org.springframework.context.support.DefaultLifecycleProcessor#getPhase(Lifecycle)
	 */
	@Override
	default int getPhase() {
		return DEFAULT_PHASE;
	}

}
