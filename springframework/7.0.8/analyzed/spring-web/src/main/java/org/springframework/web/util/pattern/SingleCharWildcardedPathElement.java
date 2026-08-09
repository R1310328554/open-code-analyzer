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

package org.springframework.web.util.pattern;

import org.springframework.http.server.PathContainer.Element;
import org.springframework.http.server.PathContainer.PathSegment;
import org.springframework.web.util.pattern.PathPattern.MatchingContext;

/* ===== [OCA 中文解析] =====
class SingleCharWildcardedPathElement — 意图说明

class `SingleCharWildcardedPathElement`：请结合所属模块与调用方理解其在整体架构中的职责。；源文件: `spring-web/src/main/java/org/springframework/web/util/pattern/SingleCharWildcardedPathElement.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * A literal path element that includes the single character wildcard '?' one
 * or more times (to basically many any character at that position).
 *
 * @author Andy Clement
 * @since 5.0
 */
class SingleCharWildcardedPathElement extends PathElement {

	// [OCA] 字段 `text`：类成员状态。
	private final char[] text;

	// [OCA] 字段 `len`：类成员状态。
	private final int len;

	// [OCA] 字段 `questionMarkCount`：类成员状态。
	private final int questionMarkCount;

	// [OCA] 字段 `caseSensitive`：类成员状态。
	private final boolean caseSensitive;


	public SingleCharWildcardedPathElement(
			int pos, char[] literalText, int questionMarkCount, boolean caseSensitive, char separator) {

		super(pos, separator);
		this.len = literalText.length;
		this.questionMarkCount = questionMarkCount;
		this.caseSensitive = caseSensitive;
		if (caseSensitive) {
			this.text = literalText;
		}
		else {
			this.text = new char[literalText.length];
			for (int i = 0; i < this.len; i++) {
				this.text[i] = Character.toLowerCase(literalText[i]);
			}
		}
	}


	@Override
	/* ===== [OCA 中文解析] =====
方法 matches — 意图与阅读要点

方法 `matches` 复杂度较高（CCN≈15, NLOC≈46）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
	public boolean matches(int pathIndex, MatchingContext matchingContext) {
		if (pathIndex >= matchingContext.pathLength) {
			// no more path left to match this element
			return false;
		}

		Element element = matchingContext.pathElements.get(pathIndex);
		if (!(element instanceof PathSegment pathSegment)) {
			return false;
		}
		String value = pathSegment.valueToMatch();
		if (value.length() != this.len) {
			// Not enough data to match this path element
			return false;
		}

		if (this.caseSensitive) {
			for (int i = 0; i < this.len; i++) {
				char ch = this.text[i];
				if ((ch != '?') && (ch != value.charAt((i)))) {
					return false;
				}
			}
		}
		else {
			for (int i = 0; i < this.len; i++) {
				char ch = this.text[i];
				char valCh = value.charAt(i);
				if (ch == valCh || ch == '?') {
					continue;
				}
				if (ch != Character.toLowerCase(valCh)) {
					return false;
				}
			}
		}

		pathIndex++;
		if (isNoMorePattern()) {
			if (matchingContext.determineRemainingPath) {
				matchingContext.remainingPathIndex = pathIndex;
				return true;
			}
			else {
				return (pathIndex == matchingContext.pathLength);
			}
		}
		else {
			return (this.next != null && this.next.matches(pathIndex, matchingContext));
		}
	}

	@Override
	public int getWildcardCount() {
		return this.questionMarkCount;
	}

	@Override
	public int getNormalizedLength() {
		return this.len;
	}

	@Override
	public char[] getChars() {
		return this.text;
	}


	@Override
	public String toString() {
		return "SingleCharWildcarded(" + String.valueOf(this.text) + ")";
	}

}
