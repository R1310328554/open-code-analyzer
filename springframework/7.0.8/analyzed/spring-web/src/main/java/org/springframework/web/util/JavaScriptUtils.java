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

package org.springframework.web.util;

/* ===== [OCA 中文解析] =====
class JavaScriptUtils — 意图说明

class `JavaScriptUtils`：请结合所属模块与调用方理解其在整体架构中的职责。；源文件: `spring-web/src/main/java/org/springframework/web/util/JavaScriptUtils.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * Utility class for JavaScript escaping.
 * Escapes based on the JavaScript 1.5 recommendation.
 *
 * <p>Reference:
 * <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Grammar_and_types#string_literals">
 * JavaScript Guide</a> on Mozilla Developer Network.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Rossen Stoyanchev
 * @author Sebastien Deleuze
 * @since 1.1.1
 */
public abstract class JavaScriptUtils {

	/* ===== [OCA 中文解析] =====
方法 javaScriptEscape — 意图与阅读要点

方法 `javaScriptEscape` 复杂度较高（CCN≈19, NLOC≈63）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
	/**
	 * Turn JavaScript special characters into escaped characters.
	 * @param input the input string
	 * @return the string with escaped characters
	 */
	public static String javaScriptEscape(String input) {
		StringBuilder filtered = new StringBuilder(input.length());
		char prevChar = '\u0000';
		char c;
		for (int i = 0; i < input.length(); i++) {
			c = input.charAt(i);
			if (c == '"') {
				filtered.append("\\\"");
			}
			else if (c == '\'') {
				filtered.append("\\'");
			}
			else if (c == '\\') {
				filtered.append("\\\\");
			}
			else if (c == '/') {
				filtered.append("\\/");
			}
			else if (c == '\t') {
				filtered.append("\\t");
			}
			else if (c == '\n') {
				if (prevChar != '\r') {
					filtered.append("\\n");
				}
			}
			else if (c == '\r') {
				filtered.append("\\n");
			}
			else if (c == '\f') {
				filtered.append("\\f");
			}
			else if (c == '\b') {
				filtered.append("\\b");
			}
			// No '\v' in Java, use octal value for VT ascii char
			else if (c == '\013') {
				filtered.append("\\v");
			}
			else if (c == '<') {
				filtered.append("\\u003C");
			}
			else if (c == '>') {
				filtered.append("\\u003E");
			}
			// Unicode for PS (line terminator in ECMA-262)
			else if (c == '\u2028') {
				filtered.append("\\u2028");
			}
			// Unicode for LS (line terminator in ECMA-262)
			else if (c == '\u2029') {
				filtered.append("\\u2029");
			}
			else if (c == '`') {
				filtered.append("\\u0060");
			}
			else if (c == '$') {
				filtered.append("\\u0024");
			}
			else {
				filtered.append(c);
			}
			prevChar = c;

		}
		return filtered.toString();
	}

}
