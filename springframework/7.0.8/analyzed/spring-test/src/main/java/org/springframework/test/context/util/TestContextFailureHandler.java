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

package org.springframework.test.context.util;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.NativeDetector;
import org.springframework.core.io.support.SpringFactoriesLoader.FailureHandler;

/* ===== [OCA 中文解析] =====
class TestContextFailureHandler — 意图说明

class `TestContextFailureHandler`：请结合所属模块与调用方理解其在整体架构中的职责。；源文件: `spring-test/src/main/java/org/springframework/test/context/util/TestContextFailureHandler.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * Spring factories {@link FailureHandler} used within the <em>Spring TestContext
 * Framework</em>.
 *
 * @author Sam Brannen
 * @since 6.0
 */
class TestContextFailureHandler implements FailureHandler {

	// [OCA] 字段 `logger`：类成员状态。
	private final Log logger = LogFactory.getLog(TestContextSpringFactoriesUtils.class);

	@Override
	/* ===== [OCA 中文解析] =====
方法 handleFailure — 意图与阅读要点

方法 `handleFailure` 复杂度较高（CCN≈12, NLOC≈35）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
	public void handleFailure(Class<?> factoryType, String factoryImplementationName, Throwable failure) {
		Throwable ex = (failure instanceof InvocationTargetException ite ? ite.getTargetException() : failure);
		if (ex instanceof ClassNotFoundException || ex instanceof NoClassDefFoundError) {
			if (logger.isDebugEnabled()) {
				logger.debug("""
						Skipping candidate %1$s [%2$s] due to a missing dependency. \
						Specify custom %1$s classes or make the default %1$s classes \
						and their required dependencies available. Offending class: %3$s"""
							.formatted(factoryType.getSimpleName(), factoryImplementationName, ex.getMessage()));
			}
		}
		else if (ex instanceof LinkageError) {
			if (logger.isDebugEnabled()) {
				logger.debug("""
						Could not load %1$s [%2$s]. Specify custom %1$s classes or make the default %1$s classes \
						available.""".formatted(factoryType.getSimpleName(), factoryImplementationName), ex);
			}
		}
		// Workaround for https://github.com/oracle/graal/issues/6691
		else if (NativeDetector.inNativeImage() && ex instanceof IllegalStateException) {
			if (logger.isDebugEnabled()) {
				logger.debug("Skipping candidate %1$s [%2$s] due to an error when loading it in a native image."
						.formatted(factoryType.getSimpleName(), factoryImplementationName));
			}
		}
		else {
			if (ex instanceof RuntimeException runtimeException) {
				throw runtimeException;
			}
			if (ex instanceof Error error) {
				throw error;
			}
			throw new IllegalStateException(
				"Failed to load %s [%s]".formatted(factoryType.getSimpleName(), factoryImplementationName), ex);
		}
	}

}
