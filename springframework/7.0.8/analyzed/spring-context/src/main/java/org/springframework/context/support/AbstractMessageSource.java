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

package org.springframework.context.support;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.jspecify.annotations.Nullable;

import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.util.ObjectUtils;

/**
 * {@link HierarchicalMessageSource} 接口的抽象实现，提供消息变体的通用处理，
 * 便于为具体 MessageSource 实现特定策略。
 *
 * <p>子类必须实现抽象方法 {@link #resolveCode}。为高效解析无参数消息，
 * 还应覆盖 {@link #resolveCodeWithoutArguments}，在不涉及 MessageFormat 的情况下解析消息。
 *
 * <p><b>注意：</b>默认情况下，仅在为消息传入参数时才通过 MessageFormat 解析消息文本。
 * 无参数时消息文本原样返回。因此，仅对带实际参数的消息使用 MessageFormat 转义，
 * 其余消息保持未转义。若希望转义所有消息，将 {@code alwaysUseMessageFormat} 标志设为 {@code true}。
 *
 * <p>不仅支持以 MessageSourceResolvable 作为主消息，还支持解析本身为
 * MessageSourceResolvable 的消息参数。
 *
 * <p>本类不按代码缓存消息，子类可随时间动态更改消息。建议子类以感知修改的方式缓存消息，
 * 以支持热部署更新后的消息。
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @see #resolveCode(String, java.util.Locale)
 * @see #resolveCodeWithoutArguments(String, java.util.Locale)
 * @see #setAlwaysUseMessageFormat
 * @see java.text.MessageFormat
 */
public abstract class AbstractMessageSource extends MessageSourceSupport implements HierarchicalMessageSource {

	/** 父级 MessageSource，用于层级回退解析。 */
	private @Nullable MessageSource parentMessageSource;

	/** 与区域设置无关的通用消息属性表。 */
	private @Nullable Properties commonMessages;

	/** 找不到消息时是否使用消息代码作为默认消息。 */
	private boolean useCodeAsDefaultMessage = false;


	@Override
	public void setParentMessageSource(@Nullable MessageSource parent) {
		this.parentMessageSource = parent;
	}

	@Override
	public @Nullable MessageSource getParentMessageSource() {
		return this.parentMessageSource;
	}

	/**
	 * 指定与区域设置无关的通用消息，以消息代码为键、完整消息字符串（可含参数占位符）为值。
	 * <p>也可关联外部定义的 Properties 对象，例如通过
	 * {@link org.springframework.beans.factory.config.PropertiesFactoryBean} 定义。
	 */
	public void setCommonMessages(@Nullable Properties commonMessages) {
		this.commonMessages = commonMessages;
	}

	/**
	 * 返回定义与区域设置无关的通用消息的 Properties 对象（若有）。
	 */
	protected @Nullable Properties getCommonMessages() {
		return this.commonMessages;
	}

	/**
	 * 设置找不到消息时是否使用消息代码作为默认消息，而非抛出 NoSuchMessageException。
	 * 便于开发与调试。默认为 {@code false}。
	 * <p>注意：对于含多个代码的 MessageSourceResolvable（如 FieldError），
	 * 且 MessageSource 有父级时，<i>不要</i>在<i>父级</i>上激活 {@code useCodeAsDefaultMessage}：
	 * 否则父级会直接返回第一个代码作为消息，不再尝试后续代码。
	 * <p>若要在父级开启 {@code useCodeAsDefaultMessage} 的情况下工作，
	 * AbstractMessageSource 与 AbstractApplicationContext 包含特殊检查，
	 * 在可用时委托给内部 {@link #getMessageInternal} 方法。
	 * 一般建议仅在开发阶段使用 {@code useCodeAsDefaultMessage}，
	 * 生产环境不要依赖此行为。
	 * @see #getMessage(String, Object[], Locale)
	 * @see org.springframework.validation.FieldError
	 */
	public void setUseCodeAsDefaultMessage(boolean useCodeAsDefaultMessage) {
		this.useCodeAsDefaultMessage = useCodeAsDefaultMessage;
	}

	/**
	 * 返回找不到消息时是否使用消息代码作为默认消息，而非抛出 NoSuchMessageException。
	 * 便于开发与调试。默认为 {@code false}。
	 * <p>也可考虑覆盖 {@link #getDefaultMessage} 方法，
	 * 为无法解析的代码返回自定义回退消息。
	 * @see #getDefaultMessage(String)
	 */
	protected boolean isUseCodeAsDefaultMessage() {
		return this.useCodeAsDefaultMessage;
	}


	@Override
	public final @Nullable String getMessage(String code, Object @Nullable [] args, @Nullable String defaultMessage, @Nullable Locale locale) {
		String msg = getMessageInternal(code, args, locale);
		if (msg != null) {
			return msg;
		}
		if (defaultMessage == null) {
			return getDefaultMessage(code);
		}
		return renderDefaultMessage(defaultMessage, args, locale);
	}

	@Override
	public final String getMessage(String code, Object @Nullable [] args, @Nullable Locale locale) throws NoSuchMessageException {
		String msg = getMessageInternal(code, args, locale);
		if (msg != null) {
			return msg;
		}
		String fallback = getDefaultMessage(code);
		if (fallback != null) {
			return fallback;
		}
		if (locale == null ) {
			throw new NoSuchMessageException(code);
		}
		else {
			throw new NoSuchMessageException(code, locale);
		}
	}

	@Override
	public final String getMessage(MessageSourceResolvable resolvable, @Nullable Locale locale) throws NoSuchMessageException {
		String[] codes = resolvable.getCodes();
		if (codes != null) {
			for (String code : codes) {
				String message = getMessageInternal(code, resolvable.getArguments(), locale);
				if (message != null) {
					return message;
				}
			}
		}
		String defaultMessage = getDefaultMessage(resolvable, locale);
		if (defaultMessage != null) {
			return defaultMessage;
		}
		String code = !ObjectUtils.isEmpty(codes) ? codes[codes.length - 1] : "";
		if (locale == null ) {
			throw new NoSuchMessageException(code);
		}
		else {
			throw new NoSuchMessageException(code, locale);
		}
	}


	/**
	 * 在给定 Locale 中解析给定代码和参数为消息，未找到时返回 {@code null}。
	 * <i>不会</i>回退到以代码作为默认消息。由 {@code getMessage} 方法调用。
	 * @param code 要查找的代码，例如 {@code calculator.noRateSet}
	 * @param args 将填入消息内参数占位符的参数数组
	 * @param locale 执行查找的区域设置
	 * @return 解析后的消息，或 {@code null} 表示未找到
	 * @see #getMessage(String, Object[], String, Locale)
	 * @see #getMessage(String, Object[], Locale)
	 * @see #getMessage(MessageSourceResolvable, Locale)
	 * @see #setUseCodeAsDefaultMessage
	 */
	protected @Nullable String getMessageInternal(@Nullable String code, Object @Nullable [] args, @Nullable Locale locale) {
		if (code == null) {
			return null;
		}
		if (locale == null) {
			locale = Locale.getDefault();
		}
		Object[] argsToUse = args;

		if (!isAlwaysUseMessageFormat() && ObjectUtils.isEmpty(args)) {
			// 优化路径：无参数可应用，无需 MessageFormat
			String message = resolveCodeWithoutArguments(code, locale);
			if (message != null) {
				return message;
			}
		}

		else {
			// 提前解析参数：消息定义在父 MessageSource 中，
			// 而可解析参数定义在子 MessageSource 中的场景
			argsToUse = resolveArguments(args, locale);

			MessageFormat messageFormat = resolveCode(code, locale);
			if (messageFormat != null) {
				synchronized (messageFormat) {
					return messageFormat.format(argsToUse);
				}
			}
		}

		// 检查给定消息代码的与区域设置无关的通用消息
		Properties commonMessages = getCommonMessages();
		if (commonMessages != null) {
			String commonMessage = commonMessages.getProperty(code);
			if (commonMessage != null) {
				return formatMessage(commonMessage, args, locale);
			}
		}

		// 未找到 -> 若有父级则向父级查找
		return getMessageFromParent(code, argsToUse, locale);
	}

	/**
	 * 尝试从父级 {@code MessageSource} 获取给定消息（若有）。
	 * @param code 要查找的代码，例如 {@code calculator.noRateSet}
	 * @param args 将填入消息内参数占位符的参数数组
	 * @param locale 执行查找的区域设置
	 * @return 解析后的消息，或 {@code null} 表示未找到
	 * @see #getParentMessageSource()
	 */
	protected @Nullable String getMessageFromParent(String code, Object @Nullable [] args, Locale locale) {
		MessageSource parent = getParentMessageSource();
		if (parent != null) {
			if (parent instanceof AbstractMessageSource abstractMessageSource) {
				// 调用内部方法，避免在激活 useCodeAsDefaultMessage 时取回默认代码
				return abstractMessageSource.getMessageInternal(code, args, locale);
			}
			else {
				// 查询父 MessageSource，父级未找到则返回 null
				return parent.getMessage(code, args, null, locale);
			}
		}
		// 父级也未找到
		return null;
	}

	/**
	 * 为给定 {@code MessageSourceResolvable} 获取默认消息。
	 * <p>本实现在可用时完整渲染默认消息，或当主消息代码用作默认消息时
	 * 直接返回纯默认消息 {@code String}。
	 * @param resolvable 要解析默认消息的值对象
	 * @param locale 当前区域设置
	 * @return 默认消息，或 {@code null} 表示无
	 * @since 4.3.6
	 * @see #renderDefaultMessage(String, Object[], Locale)
	 * @see #getDefaultMessage(String)
	 */
	protected @Nullable String getDefaultMessage(MessageSourceResolvable resolvable, @Nullable Locale locale) {
		String defaultMessage = resolvable.getDefaultMessage();
		String[] codes = resolvable.getCodes();
		if (defaultMessage != null) {
			if (resolvable instanceof DefaultMessageSourceResolvable defaultMessageSourceResolvable &&
					!defaultMessageSourceResolvable.shouldRenderDefaultMessage()) {
				// 给定默认消息不含参数占位符（且未为 alwaysUseMessageFormat 转义）-> 原样返回
				return defaultMessage;
			}
			if (!ObjectUtils.isEmpty(codes) && defaultMessage.equals(codes[0])) {
				// 即使 alwaysUseMessageFormat=true，也不格式化 code-as-default-message
				return defaultMessage;
			}
			return renderDefaultMessage(defaultMessage, resolvable.getArguments(), locale);
		}
		return (!ObjectUtils.isEmpty(codes) ? getDefaultMessage(codes[0]) : null);
	}

	/**
	 * 为给定代码返回回退默认消息（若有）。
	 * <p>默认行为：若激活 {@code useCodeAsDefaultMessage} 则返回代码本身，
	 * 否则不返回回退。无回退时，调用方通常会从 {@code getMessage} 收到 {@code NoSuchMessageException}。
	 * @param code 无法解析且未收到显式默认消息的代码
	 * @return 要使用的默认消息，或 {@code null} 表示无
	 * @see #setUseCodeAsDefaultMessage
	 */
	protected @Nullable String getDefaultMessage(String code) {
		if (isUseCodeAsDefaultMessage()) {
			return code;
		}
		return null;
	}


	/**
	 * 遍历给定对象数组，找出其中的 MessageSourceResolvable 并解析。
	 * <p>允许消息参数本身为 MessageSourceResolvable。
	 * @param args 消息参数数组
	 * @param locale 用于解析的区域设置
	 * @return 解析了所有 MessageSourceResolvable 后的参数数组
	 */
	@Override
	protected Object[] resolveArguments(Object @Nullable [] args, @Nullable Locale locale) {
		if (ObjectUtils.isEmpty(args)) {
			return super.resolveArguments(args, locale);
		}
		List<Object> resolvedArgs = new ArrayList<>(args.length);
		for (Object arg : args) {
			if (arg instanceof MessageSourceResolvable messageSourceResolvable) {
				resolvedArgs.add(getMessage(messageSourceResolvable, locale));
			}
			else {
				resolvedArgs.add(arg);
			}
		}
		return resolvedArgs.toArray();
	}

	/**
	 * 子类可覆盖此方法，以优化方式解析无参数消息，即不通过 MessageFormat。
	 * <p>默认实现<i>会</i>使用 MessageFormat，通过委托给 {@link #resolveCode}。
	 * 建议子类替换为优化解析。
	 * <p>遗憾的是，{@code java.text.MessageFormat} 实现效率不高，
	 * 尤其不会检测消息模式是否根本不含参数占位符。因此建议对无参数消息绕过 MessageFormat。
	 * @param code 要解析的消息代码
	 * @param locale 要解析代码的区域设置（建议子类支持国际化）
	 * @return 消息字符串，或 {@code null} 表示未找到
	 * @see #resolveCode
	 * @see java.text.MessageFormat
	 */
	protected @Nullable String resolveCodeWithoutArguments(String code, Locale locale) {
		MessageFormat messageFormat = resolveCode(code, locale);
		if (messageFormat != null) {
			synchronized (messageFormat) {
				return messageFormat.format(new Object[0]);
			}
		}
		return null;
	}

	/**
	 * 子类必须实现此方法以解析消息。
	 * <p>返回 MessageFormat 实例而非消息字符串，以便子类适当缓存 MessageFormat。
	 * <p><b>建议子类为无参数消息提供优化解析，不涉及 MessageFormat。</b>
	 * 详见 {@link #resolveCodeWithoutArguments} 的 JavaDoc。
	 * @param code 要解析的消息代码
	 * @param locale 要解析代码的区域设置（建议子类支持国际化）
	 * @return 消息的 MessageFormat，或 {@code null} 表示未找到
	 * @see #resolveCodeWithoutArguments(String, java.util.Locale)
	 */
	protected abstract @Nullable MessageFormat resolveCode(String code, Locale locale);

}
