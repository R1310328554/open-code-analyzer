/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot;

import org.springframework.context.ApplicationContext;

/**
 * 用于添加或移除 JVM 关闭时应执行代码的接口。
 * <p>
 * 关闭处理器类似 JVM {@link Runtime#addShutdownHook(Thread) 关闭钩子}，
 * 但以顺序方式执行而非并发。
 * <p>
 * 保证仅在已注册的 {@link ApplicationContext} 实例全部关闭且不再活跃后才调用。
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @since 2.5.1
 * @see SpringApplication#getShutdownHandlers()
 * @see SpringApplication#setRegisterShutdownHook(boolean)
 */
public interface SpringApplicationShutdownHandlers {

	/**
	 * 向 JVM 退出时将执行的处理列表添加动作。
	 *
	 * @param action 要添加的动作
	 */
	void add(Runnable action);

	/**
	 * 移除先前添加的动作，使其在 JVM 退出时不再执行。
	 *
	 * @param action 要移除的动作
	 */
	void remove(Runnable action);

}
