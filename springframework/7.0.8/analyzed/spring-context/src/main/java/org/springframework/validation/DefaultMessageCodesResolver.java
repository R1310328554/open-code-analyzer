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

package org.springframework.validation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

/**
 * {@link MessageCodesResolver} 接口的默认实现。
 *
 * <p>对对象错误将按以下顺序创建两条消息码
 *（使用 {@link Format#PREFIX_ERROR_CODE 前缀}
 * {@link #setMessageCodeFormatter(MessageCodeFormatter) 格式化器} 时）：
 * <ul>
 * <li>1.: code + "." + object name
 * <li>2.: code
 * </ul>
 *
 * <p>对字段规格将按以下顺序创建四条消息码：
 * <ul>
 * <li>1.: code + "." + object name + "." + field
 * <li>2.: code + "." + field
 * <li>3.: code + "." + field type
 * <li>4.: code
 * </ul>
 *
 * <p>例如 errorCode 为 "typeMismatch"、object name 为 "user"、field 为 "age" 时：
 * <ul>
 * <li>1. 尝试 "typeMismatch.user.age"
 * <li>2. 尝试 "typeMismatch.age"
 * <li>3. 尝试 "typeMismatch.int"
 * <li>4. 尝试 "typeMismatch"
 * </ul>
 *
 * <p>因此该解析算法可用于为 "required"、"typeMismatch" 等绑定错误
 * 显示不同粒度的消息：
 * <ul>
 * <li>对象 + 字段级别（"age" 字段，但仅限 "user" 对象）；
 * <li>字段级别（所有 "age" 字段，不限对象名）；
 * <li>或通用级别（任意对象上的所有字段）。
 * </ul>
 *
 * <p>对于数组、{@link List} 或 {@link java.util.Map} 属性，
 * 会同时生成针对特定元素与整个集合的 codes。
 * 假设 object "user" 中数组 "groups" 的字段 "name"：
 * <ul>
 * <li>1. 尝试 "typeMismatch.user.groups[0].name"
 * <li>2. 尝试 "typeMismatch.user.groups.name"
 * <li>3. 尝试 "typeMismatch.groups[0].name"
 * <li>4. 尝试 "typeMismatch.groups.name"
 * <li>5. 尝试 "typeMismatch.name"
 * <li>6. 尝试 "typeMismatch.java.lang.String"
 * <li>7. 尝试 "typeMismatch"
 * </ul>
 *
 * <p>默认 {@code errorCode} 置于构造消息串的开头。
 * 可通过 {@link #setMessageCodeFormatter(MessageCodeFormatter) messageCodeFormatter}
 * 属性指定替代的 {@link MessageCodeFormatter 拼接格式}。
 *
 * <p>若要在资源包中将所有 codes 归入特定类别，
 * 例如 "validation.typeMismatch.name" 而非默认 "typeMismatch.name"，
 * 可指定要应用的 {@link #setPrefix prefix}。
 *
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @author Chris Beams
 * @since 1.0.1
 */
@SuppressWarnings("serial")
public class DefaultMessageCodesResolver implements MessageCodesResolver, Serializable {

	/**
	 * 本实现解析消息码时使用的分隔符。
	 */
	public static final String CODE_SEPARATOR = ".";

	private static final MessageCodeFormatter DEFAULT_FORMATTER = Format.PREFIX_ERROR_CODE;


	private String prefix = "";

	private MessageCodeFormatter formatter = DEFAULT_FORMATTER;


	/**
	 * 指定本解析器构建的任意 code 要应用的前缀。
	 * <p>默认无前缀。例如指定 "validation." 可得到
	 * "validation.typeMismatch.name" 等错误码。
	 */
	public void setPrefix(@Nullable String prefix) {
		this.prefix = (prefix != null ? prefix : "");
	}

	/**
	 * 返回本解析器构建的任意 code 要应用的前缀。
	 * <p>无前缀时返回空字符串。
	 */
	protected String getPrefix() {
		return this.prefix;
	}

	/**
	 * 指定本解析器构建消息码的格式。
	 * <p>默认为 {@link Format#PREFIX_ERROR_CODE}。
	 * @since 3.2
	 * @see Format
	 */
	public void setMessageCodeFormatter(@Nullable MessageCodeFormatter formatter) {
		this.formatter = (formatter != null ? formatter : DEFAULT_FORMATTER);
	}


	@Override
	public String[] resolveMessageCodes(String errorCode, String objectName) {
		return resolveMessageCodes(errorCode, objectName, "", null);
	}

	/**
	 * 为给定 code 与 field 构建 code 列表：
	 * 对象/字段特定 code、字段特定 code、纯 error code。
	 * <p>数组、List 与 Map 会同时解析特定元素与整个集合。
	 * <p>生成 codes 的详情见 {@link DefaultMessageCodesResolver 类级 JavaDoc}。
	 * @return code 列表
	 */
	@Override
	public String[] resolveMessageCodes(String errorCode, String objectName, String field, @Nullable Class<?> fieldType) {
		Set<String> codeList = new LinkedHashSet<>();
		List<String> fieldList = new ArrayList<>();
		buildFieldList(field, fieldList);
		addCodes(codeList, errorCode, objectName, fieldList);
		int dotIndex = field.lastIndexOf('.');
		if (dotIndex != -1) {
			buildFieldList(field.substring(dotIndex + 1), fieldList);
		}
		addCodes(codeList, errorCode, null, fieldList);
		if (fieldType != null) {
			addCode(codeList, errorCode, null, fieldType.getName());
		}
		addCode(codeList, errorCode, null, null);
		return StringUtils.toStringArray(codeList);
	}

	private void addCodes(Collection<String> codeList, String errorCode, @Nullable String objectName, Iterable<String> fields) {
		for (String field : fields) {
			addCode(codeList, errorCode, objectName, field);
		}
	}

	private void addCode(Collection<String> codeList, String errorCode, @Nullable String objectName, @Nullable String field) {
		codeList.add(postProcessMessageCode(this.formatter.format(errorCode, objectName, field)));
	}

	/**
	 * 将给定 {@code field} 的带键与不带键条目均添加到给定字段列表。
	 */
	protected void buildFieldList(String field, List<String> fieldList) {
		fieldList.add(field);
		String plainField = field;
		int keyIndex = plainField.lastIndexOf('[');
		while (keyIndex != -1) {
			int endKeyIndex = plainField.indexOf(']', keyIndex);
			if (endKeyIndex != -1) {
				plainField = plainField.substring(0, keyIndex) + plainField.substring(endKeyIndex + 1);
				fieldList.add(plainField);
				keyIndex = plainField.lastIndexOf('[');
			}
			else {
				keyIndex = -1;
			}
		}
	}

	/**
	 * 对本解析器构建的给定消息码进行后处理。
	 * <p>默认实现应用指定前缀（若有）。
	 * @param code 本解析器构建的消息码
	 * @return 最终返回的消息码
	 * @see #setPrefix
	 */
	protected String postProcessMessageCode(String code) {
		return getPrefix() + code;
	}


	/**
	 * 常见消息码格式。
	 * @see MessageCodeFormatter
	 * @see DefaultMessageCodesResolver#setMessageCodeFormatter(MessageCodeFormatter)
	 */
	public enum Format implements MessageCodeFormatter {

		/**
		 * 将 errorCode 前缀到生成的消息码开头，例如：
		 * {@code errorCode + "." + object name + "." + field}
		 */
		PREFIX_ERROR_CODE {
			@Override
			public String format(String errorCode, @Nullable String objectName, @Nullable String field) {
				return toDelimitedString(errorCode, objectName, field);
			}
		},

		/**
		 * 将 errorCode 后缀到生成的消息码末尾，例如：
		 * {@code object name + "." + field + "." + errorCode}
		 */
		POSTFIX_ERROR_CODE {
			@Override
			public String format(String errorCode, @Nullable String objectName, @Nullable String field) {
				return toDelimitedString(objectName, field, errorCode);
			}
		};

		/**
		 * 拼接给定元素，以 {@link DefaultMessageCodesResolver#CODE_SEPARATOR} 分隔，
		 * 完全跳过零长度或 null 元素。
		 */
		public static String toDelimitedString(@Nullable String... elements) {
			StringJoiner rtn = new StringJoiner(CODE_SEPARATOR);
			for (String element : elements) {
				if (StringUtils.hasLength(element)) {
					rtn.add(element);
				}
			}
			return rtn.toString();
		}
	}

}
