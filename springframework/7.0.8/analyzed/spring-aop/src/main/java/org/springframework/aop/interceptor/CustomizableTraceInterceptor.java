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

package org.springframework.aop.interceptor;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

/**
 * {@code MethodInterceptor} 实现允许使用占位符进行高度可定制的方法级跟踪。
 * <p>Trace 消息写入方法入口处，如果方法调用成功则写入方法出口处。如果调用导致异常，则会写入异常消息。这些跟踪消息的内容是完全可定制的，并且可以使用特殊的占位符来允许您在
 * 日志消息中包含运行时信息。可用的占位符有：
 * <p><ul> <li>{@code $[methodName]} - 替换为正在调用的方法的名称</li> <li>{@code $[targetClassName]} -
 * 替换为调用目标的类的名称</li> <li>{@code $[targetClassShortName]} - 替换为调用目标的类的短名称incall</li>
 * <li>{@code $[returnValue]} - 替换为调用 </li> 返回的值 <li>{@code $[argumentTypes]} -
 * 替换为方法参数的短类名的逗号分隔列表</li> <li>{@code $[arguments]} - 替换为方法参数的 {@code String}
 * 表示形式的逗号分隔列表</li> <li>{@code $[exception]} - 替换为调用期间引发的任何 {@code Throwable} 的 {@code
 * String} 表示形式</li> <li>{@code $[invocationTime]} - 替换为所采取的时间（以毫秒为单位）通过方法调用</li> </ul>
 * <p> 对于哪些消息中可以使用哪些占位符存在限制：有关有效占位符的详细信息，请参阅各个消息属性。
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 1.2
 * @see #setEnterMessage
 * @see #setExitMessage
 * @see #setExceptionMessage
 * @see SimpleTraceInterceptor
 */
@SuppressWarnings("serial")
public class CustomizableTraceInterceptor extends AbstractTraceInterceptor {

	/**
	 * {@code $[methodName]} 占位符。替换为正在调用的方法的名称。
	 */
	public static final String PLACEHOLDER_METHOD_NAME = "$[methodName]";

	/**
	 * {@code $[targetClassName]} 占位符。替换为方法调用目标的 {@code Class} 的完全限定名称。
	 */
	public static final String PLACEHOLDER_TARGET_CLASS_NAME = "$[targetClassName]";

	/**
	 * {@code $[targetClassShortName]} 占位符。替换为方法调用目标的 {@code Class} 的短名称。
	 */
	public static final String PLACEHOLDER_TARGET_CLASS_SHORT_NAME = "$[targetClassShortName]";

	/**
	 * {@code $[returnValue]} 占位符。替换为方法调用返回值的 {@code String} 表示形式。
	 */
	public static final String PLACEHOLDER_RETURN_VALUE = "$[returnValue]";

	/**
	 * {@code $[argumentTypes]} 占位符。替换为方法调用的参数类型的逗号分隔列表。参数类型被写为短类名。
	 */
	public static final String PLACEHOLDER_ARGUMENT_TYPES = "$[argumentTypes]";

	/**
	 * {@code $[arguments]} 占位符。替换为方法调用的参数值的逗号分隔列表。依赖于每个参数类型的 {@code toString()} 方法。
	 */
	public static final String PLACEHOLDER_ARGUMENTS = "$[arguments]";

	/**
	 * {@code $[exception]} 占位符。替换为方法调用期间引发的任何 {@code Throwable} 的 {@code String} 表示形式。
	 */
	public static final String PLACEHOLDER_EXCEPTION = "$[exception]";

	/**
	 * {@code $[invocationTime]} 占位符。替换为调用所花费的时间（以毫秒为单位）。
	 */
	public static final String PLACEHOLDER_INVOCATION_TIME = "$[invocationTime]";

	/**
	 * 用于编写方法入口消息的默认消息。
	 */
	private static final String DEFAULT_ENTER_MESSAGE = "Entering method '" +
			PLACEHOLDER_METHOD_NAME + "' of class [" + PLACEHOLDER_TARGET_CLASS_NAME + "]";

	/**
	 * 用于编写方法退出消息的默认消息。
	 */
	private static final String DEFAULT_EXIT_MESSAGE = "Exiting method '" +
			PLACEHOLDER_METHOD_NAME + "' of class [" + PLACEHOLDER_TARGET_CLASS_NAME + "]";

	/**
	 * 用于写入异常消息的默认消息。
	 */
	private static final String DEFAULT_EXCEPTION_MESSAGE = "Exception thrown in method '" +
			PLACEHOLDER_METHOD_NAME + "' of class [" + PLACEHOLDER_TARGET_CLASS_NAME + "]";

	/**
	 * {@code Pattern} 用于匹配占位符。
	 */
	private static final Pattern PATTERN = Pattern.compile("\\$\\[\\p{Alpha}+]");

	/**
	 * 允许的占位符的 {@code Set}。
	 */
	static final Set<String> ALLOWED_PLACEHOLDERS = Set.of(
			PLACEHOLDER_METHOD_NAME,
			PLACEHOLDER_TARGET_CLASS_NAME,
			PLACEHOLDER_TARGET_CLASS_SHORT_NAME,
			PLACEHOLDER_RETURN_VALUE,
			PLACEHOLDER_ARGUMENT_TYPES,
			PLACEHOLDER_ARGUMENTS,
			PLACEHOLDER_EXCEPTION,
			PLACEHOLDER_INVOCATION_TIME);


	/**
	 * 方法输入的消息。
	 */
	private String enterMessage = DEFAULT_ENTER_MESSAGE;

	/**
	 * 方法退出的消息。
	 */
	private String exitMessage = DEFAULT_EXIT_MESSAGE;

	/**
	 * 方法执行期间发生异常的消息。
	 */
	private String exceptionMessage = DEFAULT_EXCEPTION_MESSAGE;


	/**
	 * 设置用于方法条目日志消息的模板。此模板可以包含以下任意占位符： <ul> <li>{@code $[targetClassName]}</li> <li>{@code $[ta
	 * rgetClassShortName]}</li> <li>{@code $[argumentTypes]}</li> <li>{@code $[arguments]}</li
	 * > </ul>
	 */
	public void setEnterMessage(String enterMessage) throws IllegalArgumentException {
		Assert.hasText(enterMessage, "enterMessage must not be empty");
		checkForInvalidPlaceholders(enterMessage);
		Assert.doesNotContain(enterMessage, PLACEHOLDER_RETURN_VALUE,
				"enterMessage cannot contain placeholder " + PLACEHOLDER_RETURN_VALUE);
		Assert.doesNotContain(enterMessage, PLACEHOLDER_EXCEPTION,
				"enterMessage cannot contain placeholder " + PLACEHOLDER_EXCEPTION);
		Assert.doesNotContain(enterMessage, PLACEHOLDER_INVOCATION_TIME,
				"enterMessage cannot contain placeholder " + PLACEHOLDER_INVOCATION_TIME);
		this.enterMessage = enterMessage;
	}

	/**
	 * 设置用于方法退出日志消息的模板。此模板可以包含以下任意占位符： <ul> <li>{@code $[targetClassName]}</li> <li>{@code $[ta
	 * rgetClassShortName]}</li> <li>{@code $[argumentTypes]}</li> <li>{@code $[arguments]}</li
	 * > <li>{@code $[returnValue]}</li> <li>{@code $[invocationTime]}</li> </ul>
	 */
	public void setExitMessage(String exitMessage) {
		Assert.hasText(exitMessage, "exitMessage must not be empty");
		checkForInvalidPlaceholders(exitMessage);
		Assert.doesNotContain(exitMessage, PLACEHOLDER_EXCEPTION,
				"exitMessage cannot contain placeholder" + PLACEHOLDER_EXCEPTION);
		this.exitMessage = exitMessage;
	}

	/**
	 * 设置用于方法异常日志消息的模板。此模板可以包含以下任意占位符： <ul> <li>{@code $[targetClassName]}</li> <li>{@code $[ta
	 * rgetClassShortName]}</li> <li>{@code $[argumentTypes]}</li> <li>{@code $[arguments]}</li
	 * > <li>{@code $[exception]}</li> </ul>
	 */
	public void setExceptionMessage(String exceptionMessage) {
		Assert.hasText(exceptionMessage, "exceptionMessage must not be empty");
		checkForInvalidPlaceholders(exceptionMessage);
		Assert.doesNotContain(exceptionMessage, PLACEHOLDER_RETURN_VALUE,
				"exceptionMessage cannot contain placeholder " + PLACEHOLDER_RETURN_VALUE);
		this.exceptionMessage = exceptionMessage;
	}


	/**
	 * 根据 {@code enterMessage} 的值在调用之前写入日志消息。如果调用成功，则退出时会根据值 {@code exitMessage} 写入一条日志消息。如果调用期
	 * 间发生异常，则会根据 {@code exceptionMessage} 的值写入一条消息。
	 * @see #setEnterMessage
	 * @see #setExitMessage
	 * @see #setExceptionMessage
	 */
	@Override
	protected @Nullable Object invokeUnderTrace(MethodInvocation invocation, Log logger) throws Throwable {
		String name = ClassUtils.getQualifiedMethodName(invocation.getMethod());
		StopWatch stopWatch = new StopWatch(name);
		Object returnValue = null;
		boolean exitThroughException = false;
		try {
			stopWatch.start(name);
			writeToLog(logger,
					replacePlaceholders(this.enterMessage, invocation, null, null, -1));
			returnValue = invocation.proceed();
			return returnValue;
		}
		catch (Throwable ex) {
			if (stopWatch.isRunning()) {
				stopWatch.stop();
			}
			exitThroughException = true;
			writeToLog(logger, replacePlaceholders(
					this.exceptionMessage, invocation, null, ex, stopWatch.getTotalTimeMillis()), ex);
			throw ex;
		}
		finally {
			if (!exitThroughException) {
				if (stopWatch.isRunning()) {
					stopWatch.stop();
				}
				writeToLog(logger, replacePlaceholders(
						this.exitMessage, invocation, returnValue, null, stopWatch.getTotalTimeMillis()));
			}
		}
	}

	/**
	 * 将给定消息中的占位符替换为提供的值或从提供的值派生的值。
	 * @param message 包含要替换的占位符的消息模板
	 * @param methodInvocation 正在记录 {@code MethodInvocation}。用于导出除 {@code $[exception]} 和 {@code $[returnValue]} 之外的所有占位符的值。
	 * @param returnValue 调用返回的任何值。用于替换 {@code $[returnValue]} 占位符。可能是 {@code null}。
	 * @param throwable 调用期间引发的任何 {@code Throwable}。 {@code Throwable.toString()} 的值被替换为 {@code $[exception]} 占位符。可能是{@code null}。
	 * @param invocationTime 代替 {@code $[invocationTime]} 占位符写入的值
	 * @return 格式化输出写入日志
	 */
	protected String replacePlaceholders(String message, MethodInvocation methodInvocation,
			@Nullable Object returnValue, @Nullable Throwable throwable, long invocationTime) {

		Object target = methodInvocation.getThis();
		Assert.state(target != null, "Target must not be null");

		StringBuilder output = new StringBuilder();
		Matcher matcher = PATTERN.matcher(message);
		while (matcher.find()) {
			String match = matcher.group();
			switch (match) {
				case PLACEHOLDER_METHOD_NAME -> matcher.appendReplacement(output,
						Matcher.quoteReplacement(methodInvocation.getMethod().getName()));
				case PLACEHOLDER_TARGET_CLASS_NAME -> {
					String className = getClassForLogging(target).getName();
					matcher.appendReplacement(output, Matcher.quoteReplacement(className));
				}
				case PLACEHOLDER_TARGET_CLASS_SHORT_NAME -> {
					String shortName = ClassUtils.getShortName(getClassForLogging(target));
					matcher.appendReplacement(output, Matcher.quoteReplacement(shortName));
				}
				case PLACEHOLDER_ARGUMENTS -> matcher.appendReplacement(output,
						Matcher.quoteReplacement(StringUtils.arrayToCommaDelimitedString(methodInvocation.getArguments())));
				case PLACEHOLDER_ARGUMENT_TYPES -> appendArgumentTypes(methodInvocation, matcher, output);
				case PLACEHOLDER_RETURN_VALUE -> appendReturnValue(methodInvocation, matcher, output, returnValue);
				case PLACEHOLDER_EXCEPTION -> {
					if (throwable != null) {
						matcher.appendReplacement(output, Matcher.quoteReplacement(throwable.toString()));
					}
				}
				case PLACEHOLDER_INVOCATION_TIME -> matcher.appendReplacement(output, Long.toString(invocationTime));
				default -> {
					// 由于占位符已提前检查，因此不应发生这种情况。
					throw new IllegalArgumentException("Unknown placeholder [" + match + "]");
				}
			}
		}
		matcher.appendTail(output);

		return output.toString();
	}

	/**
	 * 将方法返回值的 {@code String} 表示形式添加到提供的 {@code StringBuilder} 中。正确处理 {@code null} 和 {@code
	 * void} 结果。
	 * @param methodInvocation 返回值的 {@code MethodInvocation}
	 * @param matcher 包含匹配占位符的 {@code Matcher}
	 * @param output 将输出写入的 {@code StringBuilder}
	 * @param returnValue 方法调用返回的值。
	 */
	private static void appendReturnValue(
			MethodInvocation methodInvocation, Matcher matcher, StringBuilder output, @Nullable Object returnValue) {

		if (methodInvocation.getMethod().getReturnType() == void.class) {
			matcher.appendReplacement(output, "void");
		}
		else if (returnValue == null) {
			matcher.appendReplacement(output, "null");
		}
		else {
			matcher.appendReplacement(output, Matcher.quoteReplacement(returnValue.toString()));
		}
	}

	/**
	 * 将方法参数类型的短 {@code Class} 名称的逗号分隔列表添加到输出。例如，如果方法具有签名 {@code put(java.lang.String,
	 * java.lang.Object)}，则返回的值将为 {@code String, Object}。
	 * @param methodInvocation 正在记录 {@code MethodInvocation}。将从相应的 {@code Method} 中检索参数。
	 * @param matcher 包含输出状态的 {@code Matcher}
	 * @param output 包含输出的 {@code StringBuilder}
	 */
	private static void appendArgumentTypes(MethodInvocation methodInvocation, Matcher matcher, StringBuilder output) {
		Class<?>[] argumentTypes = methodInvocation.getMethod().getParameterTypes();
		String[] argumentTypeShortNames = new String[argumentTypes.length];
		for (int i = 0; i < argumentTypeShortNames.length; i++) {
			argumentTypeShortNames[i] = ClassUtils.getShortName(argumentTypes[i]);
		}
		matcher.appendReplacement(output,
				Matcher.quoteReplacement(StringUtils.arrayToCommaDelimitedString(argumentTypeShortNames)));
	}

	/**
	 * 检查提供的 {@code String} 是否有任何未指定为此类常量的占位符，如果有，则抛出 {@code IllegalArgumentException}。
	 */
	private static void checkForInvalidPlaceholders(String message) throws IllegalArgumentException {
		Matcher matcher = PATTERN.matcher(message);
		while (matcher.find()) {
			String match = matcher.group();
			if (!ALLOWED_PLACEHOLDERS.contains(match)) {
				throw new IllegalArgumentException("Placeholder [" + match + "] is not valid");
			}
		}
	}

}
