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
 * 在 {@code ApplicationContext} 内处理 {@link Lifecycle} Bean 的策略接口。
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 3.0
 */
public interface LifecycleProcessor extends Lifecycle {

	/**
	 * 上下文刷新通知，用于自动启动组件。
	 * @see ConfigurableApplicationContext#refresh()
	 */
	default void onRefresh() {
		start();
	}

	/**
	 * 上下文重启通知，先自动停止再自动启动组件。
	 * @since 7.0
	 * @see ConfigurableApplicationContext#restart()
	 */
	default void onRestart() {
		stop();
		start();
	}

	/**
	 * 上下文暂停通知，用于自动停止组件。
	 * @since 7.0
	 * @see ConfigurableApplicationContext#pause()
	 */
	default void onPause() {
		stop();
	}

	/**
	 * 上下文关闭阶段通知，在销毁之前自动停止组件。
	 * @see ConfigurableApplicationContext#close()
	 */
	default void onClose() {
		stop();
	}

}
