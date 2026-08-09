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

package org.springframework.aop.aspectj;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.bridge.IMessageHandler;

/**
 * AspectJ 的 {@link IMessageHandler} 接口的实现，通过与常规 Spring 消息相同的日志系统路由 AspectJ 编织消息。
 * <p>通过选项...
 * <p><code
 * class="code">-XmessageHandlerClass:org.springframework.aop.aspectj.AspectJWeaverMessageHandler</code>
 * <p>给织工；例如，在“{@code META-INF/aop.xml} 文件中指定以下内容：
 * <p><code class="code"><weaver options="..."/></code>
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @since 2.0
 */
public class AspectJWeaverMessageHandler implements IMessageHandler {

	private static final String AJ_ID = "[AspectJ] ";

	/**
	 * 获取 Log（`Log`）。
	 */
	private static final Log logger = LogFactory.getLog("AspectJ Weaver");


	/**
	 * 处理：Message（方法 `handleMessage`）。
	 */
	@Override
	public boolean handleMessage(IMessage message) throws AbortException {
		Kind messageKind = message.getKind();
		if (messageKind == IMessage.DEBUG) {
			if (logger.isDebugEnabled()) {
				logger.debug(makeMessageFor(message));
				return true;
			}
		}
		else if (messageKind == IMessage.INFO || messageKind == IMessage.WEAVEINFO) {
			if (logger.isInfoEnabled()) {
				logger.info(makeMessageFor(message));
				return true;
			}
		}
		else if (messageKind == IMessage.WARNING) {
			if (logger.isWarnEnabled()) {
				logger.warn(makeMessageFor(message));
				return true;
			}
		}
		else if (messageKind == IMessage.ERROR) {
			if (logger.isErrorEnabled()) {
				logger.error(makeMessageFor(message));
				return true;
			}
		}
		else if (messageKind == IMessage.ABORT) {
			if (logger.isFatalEnabled()) {
				logger.fatal(makeMessageFor(message));
				return true;
			}
		}
		return false;
	}

	/**
	 * 方法 `makeMessageFor`：完成本类中与「make Message For」相关的职责。
	 */
	private String makeMessageFor(IMessage aMessage) {
		return AJ_ID + aMessage.getMessage();
	}

	/**
	 * 判断是否 Ignoring。
	 */
	@Override
	public boolean isIgnoring(Kind messageKind) {
		// 我们希望看到一切，并允许动态配置日志级别。
		return false;
	}

	/**
	 * 执行核心逻辑：nt Ignore（方法 `dontIgnore`）。
	 */
	@Override
	public void dontIgnore(Kind messageKind) {
		// 无论如何，我们并没有忽视任何事情......
	}

	/**
	 * 方法 `ignore`：完成本类中与「ignore」相关的职责。
	 */
	@Override
	public void ignore(Kind kind) {
		// 无论如何，我们并没有忽视任何事情......
	}

}
