package com.taobao.arthas.mcp.server.tool.execution;

import com.taobao.arthas.mcp.server.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ToolExecutionExceptionProcessor} 的默认实现。
 * <p>
 * 默认将工具异常消息转为字符串返回给模型；若配置 {@code alwaysThrow=true} 则重新抛出供上层处理。
 */
public class DefaultToolExecutionExceptionProcessor implements ToolExecutionExceptionProcessor {

	private final static Logger logger = LoggerFactory.getLogger(DefaultToolExecutionExceptionProcessor.class);

	private static final boolean DEFAULT_ALWAYS_THROW = false;

	/** 为 true 时不吞异常，直接向上抛出 {@link ToolExecutionException}。 */
	private final boolean alwaysThrow;

	public DefaultToolExecutionExceptionProcessor(boolean alwaysThrow) {
		this.alwaysThrow = alwaysThrow;
	}

	@Override
	public String process(ToolExecutionException exception) {
		Assert.notNull(exception, "exception cannot be null");
		if (this.alwaysThrow) {
			throw exception;
		}
		logger.debug("Exception thrown by tool: {}. Message: {}", exception.getToolDefinition().getName(),
				exception.getMessage());
		return exception.getMessage();
	}

	public static Builder builder() {
		return new Builder();
	}

	/** 建造者，用于配置是否在处理阶段重新抛出异常。 */
	public static class Builder {

		private boolean alwaysThrow = DEFAULT_ALWAYS_THROW;

		public Builder alwaysThrow(boolean alwaysThrow) {
			this.alwaysThrow = alwaysThrow;
			return this;
		}

		public DefaultToolExecutionExceptionProcessor build() {
			return new DefaultToolExecutionExceptionProcessor(this.alwaysThrow);
		}

	}

}
