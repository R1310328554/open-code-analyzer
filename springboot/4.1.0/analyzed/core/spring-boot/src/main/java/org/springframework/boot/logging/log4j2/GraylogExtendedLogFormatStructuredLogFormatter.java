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

package org.springframework.boot.logging.log4j2;

import java.math.BigDecimal;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.net.Severity;
import org.apache.logging.log4j.core.time.Instant;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.jspecify.annotations.Nullable;

import org.springframework.boot.json.JsonWriter;
import org.springframework.boot.json.JsonWriter.Members;
import org.springframework.boot.json.WritableJson;
import org.springframework.boot.logging.StackTracePrinter;
import org.springframework.boot.logging.structured.CommonStructuredLogFormat;
import org.springframework.boot.logging.structured.ContextPairs;
import org.springframework.boot.logging.structured.ContextPairs.Joiner;
import org.springframework.boot.logging.structured.GraylogExtendedLogFormatProperties;
import org.springframework.boot.logging.structured.JsonWriterStructuredLogFormatter;
import org.springframework.boot.logging.structured.StructuredLogFormatter;
import org.springframework.boot.logging.structured.StructuredLoggingJsonMembersCustomizer;
import org.springframework.core.env.Environment;
import org.springframework.core.log.LogMessage;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 用于 {@link CommonStructuredLogFormat#GRAYLOG_EXTENDED_LOG_FORMAT} 的
 * Log4j2 {@link StructuredLogFormatter} 实现，支持 GELF 1.1 版本。
 *
 * @author Samuel Lissner
 * @author Moritz Halbritter
 * @author Phillip Webb
 */
class GraylogExtendedLogFormatStructuredLogFormatter extends JsonWriterStructuredLogFormatter<LogEvent> {

	private static final Log logger = LogFactory.getLog(GraylogExtendedLogFormatStructuredLogFormatter.class);

	/**
	 * 字段名允许的字符为任意 word 字符（字母、数字、下划线）、连字符与点号。
	 */
	private static final Pattern FIELD_NAME_VALID_PATTERN = Pattern.compile("^[\\w.\\-]*$");

	/**
	 * 库不应允许将 id 作为附加字段（"_id"）发送；Graylog 服务端会自动省略该字段。
	 */
	private static final Set<String> ADDITIONAL_FIELD_ILLEGAL_KEYS = Set.of("id", "_id");

	GraylogExtendedLogFormatStructuredLogFormatter(Environment environment,
			@Nullable StackTracePrinter stackTracePrinter, ContextPairs contextPairs,
			@Nullable StructuredLoggingJsonMembersCustomizer<?> customizer) {
		super((members) -> jsonMembers(environment, stackTracePrinter, contextPairs, members), customizer);
	}

	private static void jsonMembers(Environment environment, @Nullable StackTracePrinter stackTracePrinter,
			ContextPairs contextPairs, JsonWriter.Members<LogEvent> members) {
		Extractor extractor = new Extractor(stackTracePrinter);
		members.add("version", "1.1");
		members.add("short_message", LogEvent::getMessage)
			.as(GraylogExtendedLogFormatStructuredLogFormatter::getMessageText);
		members.add("timestamp", LogEvent::getInstant)
			.as(GraylogExtendedLogFormatStructuredLogFormatter::formatTimeStamp);
		members.add("level", GraylogExtendedLogFormatStructuredLogFormatter::convertLevel);
		members.add("_level_name", LogEvent::getLevel).as(Level::name);
		members.add("_process_pid", environment.getProperty("spring.application.pid", Long.class)).whenNotNull();
		members.add("_process_thread_name", LogEvent::getThreadName);
		GraylogExtendedLogFormatProperties.get(environment).jsonMembers(members);
		members.add("_log_logger", LogEvent::getLoggerName);
		Predicate<@Nullable ReadOnlyStringMap> mapIsEmpty = (map) -> map == null || map.isEmpty();
		members.from(LogEvent::getContextData)
			.whenNot(mapIsEmpty)
			.usingPairs(contextPairs.flat(additionalFieldJoiner(),
					GraylogExtendedLogFormatStructuredLogFormatter::addContextDataPairs));
		Function<@Nullable LogEvent, @Nullable Object> getThrown = (event) -> (event != null) ? event.getThrown()
				: null;
		members.add()
			.whenNotNull(getThrown)
			.usingMembers((thrownMembers) -> throwableMembers(thrownMembers, extractor));
	}

	private static String getMessageText(Message message) {
		// Always return text as a blank message will lead to a error as of Graylog v6
		String formattedMessage = message.getFormattedMessage();
		return (!StringUtils.hasText(formattedMessage)) ? "(blank)" : formattedMessage;
	}

	/**
	 * GELF 要求 "自 UNIX epoch 起的秒数，可选<b>毫秒小数位</b>"。
	 * 为满足该要求，将毫秒精度的 POSIX 时间戳格式化为字符串，
	 * 例如 "1725459730385" -> "1725459730.385"。
	 *
	 * @param timeStamp 日志消息的时间戳
	 * @return 毫秒精度的格式化时间戳字符串
	 */
	private static WritableJson formatTimeStamp(Instant timeStamp) {
		return (out) -> out.append(new BigDecimal(timeStamp.getEpochMillisecond()).movePointLeft(3).toPlainString());
	}

	/**
	 * 将 log4j2 事件级别转换为 Syslog 事件级别代码。
	 *
	 * @param event 日志事件
	 * @return 表示 syslog 日志级别代码的整数
	 * @see Log4j2 的 Severity 类，其中包含转换逻辑
	 */
	private static int convertLevel(LogEvent event) {
		return Severity.getSeverity(event.getLevel()).getCode();
	}

	private static void throwableMembers(Members<LogEvent> members, Extractor extractor) {
		members.add("full_message", extractor::messageAndStackTrace);
		members.add("_error_type", LogEvent::getThrown).whenNotNull().as(ObjectUtils::nullSafeClassName);
		members.add("_error_stack_trace", extractor::stackTrace);
		members.add("_error_message", (event) -> event.getThrown().getMessage());
	}

	private static void addContextDataPairs(ContextPairs.Pairs<ReadOnlyStringMap> contextPairs) {
		contextPairs.add((contextData, pairs) -> contextData.forEach(pairs::accept));
	}

	private static Joiner additionalFieldJoiner() {
		return (prefix, name) -> {
			name = prefix + name;
			if (!FIELD_NAME_VALID_PATTERN.matcher(name).matches()) {
				logger.warn(LogMessage.format("'%s' is not a valid field name according to GELF standard", name));
				return null;
			}
			if (ADDITIONAL_FIELD_ILLEGAL_KEYS.contains(name)) {
				logger.warn(LogMessage.format("'%s' is an illegal field name according to GELF standard", name));
				return null;
			}
			return (!name.startsWith("_")) ? "_" + name : name;
		};
	}

}
