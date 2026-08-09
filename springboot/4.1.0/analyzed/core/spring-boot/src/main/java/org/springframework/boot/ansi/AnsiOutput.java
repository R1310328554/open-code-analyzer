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

package org.springframework.boot.ansi;

import java.io.Console;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 生成 ANSI 编码输出，并自动检测终端是否支持 ANSI。
 * <p>
 * 可通过 {@link Enabled} 强制启用/禁用，或在 Windows 上检测控制台与 OS 版本。
 *
 * @author Phillip Webb
 * @author Yong-Hyun Kim
 * @author Philemon Hilscher
 * @since 1.0.0
 */
public abstract class AnsiOutput {

	private static final String ENCODE_JOIN = ";";

	private static Enabled enabled = Enabled.DETECT;

	private static @Nullable Boolean consoleAvailable;

	private static @Nullable Boolean ansiCapable;

	private static final String OPERATING_SYSTEM_NAME = System.getProperty("os.name");

	private static final List<String> WINDOWS_ANSI_COMPATIBLE = List.of("Windows 11", "Windows Server 2025");

	private static final String ENCODE_START = "\033[";

	private static final String ENCODE_END = "m";

	private static final String RESET = "0;" + AnsiColor.DEFAULT;

	/**
	 * 设置 ANSI 输出是否启用。
	 *
	 * @param enabled 启用、禁用或自动检测
	 */
	public static void setEnabled(Enabled enabled) {
		Assert.notNull(enabled, "'enabled' must not be null");
		AnsiOutput.enabled = enabled;
	}

	/**
	 * 返回 ANSI 输出启用状态。
	 *
	 * @return 启用、禁用或自动检测
	 */
	public static Enabled getEnabled() {
		return AnsiOutput.enabled;
	}

	/**
	 * 设置 {@code System.console()} 是否已知可用。
	 *
	 * @param consoleAvailable 控制台是否可用，或 {@code null} 使用标准检测逻辑
	 */
	public static void setConsoleAvailable(@Nullable Boolean consoleAvailable) {
		AnsiOutput.consoleAvailable = consoleAvailable;
	}

	/**
	 * 若输出已启用，编码单个 {@link AnsiElement}。
	 *
	 * @param element 要编码的元素
	 * @return 编码后的字符串，未启用时返回空字符串
	 */
	public static String encode(AnsiElement element) {
		if (isEnabled()) {
			return ENCODE_START + element + ENCODE_END;
		}
		return "";
	}

	/**
	 * 由指定元素创建 ANSI 字符串；{@link AnsiElement} 会按需编码。
	 *
	 * @param elements 要编码的元素
	 * @return 编码后的字符串
	 */
	public static String toString(Object... elements) {
		StringBuilder sb = new StringBuilder();
		if (isEnabled()) {
			buildEnabled(sb, elements);
		}
		else {
			buildDisabled(sb, elements);
		}
		return sb.toString();
	}

	private static void buildEnabled(StringBuilder sb, Object[] elements) {
		boolean writingAnsi = false;
		boolean containsEncoding = false;
		for (Object element : elements) {
			if (element instanceof AnsiElement) {
				containsEncoding = true;
				if (!writingAnsi) {
					sb.append(ENCODE_START);
					writingAnsi = true;
				}
				else {
					sb.append(ENCODE_JOIN);
				}
			}
			else {
				if (writingAnsi) {
					sb.append(ENCODE_END);
					writingAnsi = false;
				}
			}
			sb.append(element);
		}
		if (containsEncoding) {
			sb.append(writingAnsi ? ENCODE_JOIN : ENCODE_START);
			sb.append(RESET);
			sb.append(ENCODE_END);
		}
	}

	private static void buildDisabled(StringBuilder sb, @Nullable Object[] elements) {
		for (Object element : elements) {
			if (!(element instanceof AnsiElement) && element != null) {
				sb.append(element);
			}
		}
	}

	private static boolean isEnabled() {
		if (enabled == Enabled.DETECT) {
			if (ansiCapable == null) {
				ansiCapable = detectIfAnsiCapable();
			}
			return ansiCapable;
		}
		return enabled == Enabled.ALWAYS;
	}

	private static boolean detectIfAnsiCapable() {
		try {
			if (Boolean.FALSE.equals(consoleAvailable)) {
				return false;
			}
			if (consoleAvailable == null) {
				Console console = System.console();
				if (console == null) {
					return false;
				}
				Method isTerminalMethod = ClassUtils.getMethodIfAvailable(Console.class, "isTerminal");
				if (isTerminalMethod != null) {
					Boolean isTerminal = (Boolean) isTerminalMethod.invoke(console);
					if (Boolean.FALSE.equals(isTerminal)) {
						return false;
					}
				}
			}
			if (OPERATING_SYSTEM_NAME.toLowerCase(Locale.ENGLISH).contains("win")) {
				return WINDOWS_ANSI_COMPATIBLE.contains(OPERATING_SYSTEM_NAME);
			}
			return true;
		}
		catch (Throwable ex) {
			return false;
		}
	}

	/**
	 * 传给 {@link AnsiOutput#setEnabled} 的取值，决定何时输出 ANSI 转义序列以着色应用输出。
	 */
	public enum Enabled {

		/**
		 * 尝试检测终端是否支持 ANSI 着色；{@link AnsiOutput} 的默认值。
		 */
		DETECT,

		/**
		 * 始终启用 ANSI 着色输出。
		 */
		ALWAYS,

		/**
		 * 始终禁用 ANSI 着色输出。
		 */
		NEVER

	}

}
