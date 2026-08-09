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

/* ===== [OCA 中文解析] =====
class AspectJWeaverMessageHandler — 意图说明

class `AspectJWeaverMessageHandler`：请结合所属模块与调用方理解其在整体架构中的职责。；源文件: `spring-aop/src/main/java/org/springframework/aop/aspectj/AspectJWeaverMessageHandler.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * Implementation of AspectJ's {@link IMessageHandler} interface that
 * routes AspectJ weaving messages through the same logging system as the
 * regular Spring messages.
 *
 * <p>Pass the option...
 *
 * <p><code class="code">-XmessageHandlerClass:org.springframework.aop.aspectj.AspectJWeaverMessageHandler</code>
 *
 * <p>to the weaver; for example, specifying the following in a
 * "{@code META-INF/aop.xml} file:
 *
 * <p><code class="code">&lt;weaver options="..."/&gt;</code>
 *
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @since 2.0
 */
public class AspectJWeaverMessageHandler implements IMessageHandler {

	// [OCA] 字段 `AJ_ID`：类成员状态。
	private static final String AJ_ID = "[AspectJ] ";

	// [OCA] 字段 `logger`：类成员状态。
	private static final Log logger = LogFactory.getLog("AspectJ Weaver");


	@Override
	/* ===== [OCA 中文解析] =====
方法 handleMessage — 意图与阅读要点

方法 `handleMessage` 复杂度较高（CCN≈12, NLOC≈34）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
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

	private String makeMessageFor(IMessage aMessage) {
		return AJ_ID + aMessage.getMessage();
	}

	@Override
	public boolean isIgnoring(Kind messageKind) {
		// We want to see everything, and allow configuration of log levels dynamically.
		return false;
	}

	@Override
	public void dontIgnore(Kind messageKind) {
		// We weren't ignoring anything anyway...
	}

	@Override
	public void ignore(Kind kind) {
		// We weren't ignoring anything anyway...
	}

}
