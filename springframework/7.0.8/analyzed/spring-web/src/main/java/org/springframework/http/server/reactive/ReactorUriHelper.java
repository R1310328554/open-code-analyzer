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

package org.springframework.http.server.reactive;

import java.net.URI;
import java.net.URISyntaxException;

import reactor.netty.http.server.HttpServerRequest;

import org.springframework.util.Assert;

/* ===== [OCA 中文解析] =====
class ReactorUriHelper — 意图说明

class `ReactorUriHelper`：请结合所属模块与调用方理解其在整体架构中的职责。；源文件: `spring-web/src/main/java/org/springframework/http/server/reactive/ReactorUriHelper.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * Helper class to create {@link URI} from a Reactor Netty request.
 *
 * @author Arjen Poutsma
 * @since 6.0.8
 */
abstract class ReactorUriHelper {

	/* ===== [OCA 中文解析] =====
方法 createUri — 意图与阅读要点

方法 `createUri` 复杂度较高（CCN≈9, NLOC≈20）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。

	===== [OCA 中文解析结束] ===== */

	public static URI createUri(HttpServerRequest request) throws URISyntaxException {
		Assert.notNull(request, "HttpServerRequest must not be null");

		StringBuilder builder = new StringBuilder();
		String scheme = request.scheme();
		builder.append(scheme);
		builder.append("://");

		appendHostName(request, builder);

		int port = request.hostPort();
		if ((scheme.equals("http") || scheme.equals("ws")) && port != 80 ||
				(scheme.equals("https") || scheme.equals("wss")) && port != 443) {
			builder.append(':');
			builder.append(port);
		}

		// Reactor Netty has config whether to extract and apply forwarded headers.
		// We apply the prefix manually as it affects the contextPath too.

		String prefix = request.forwardedPrefix();
		if (prefix != null && !prefix.isEmpty()) {
			builder.append(prefix);
		}

		appendRequestUri(request, builder);

		return new URI(builder.toString());
	}

	/* ===== [OCA 中文解析] =====
方法 appendHostName — 意图与阅读要点

方法 `appendHostName` 复杂度较高（CCN≈8, NLOC≈29）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。

	===== [OCA 中文解析结束] ===== */

	private static void appendHostName(HttpServerRequest request, StringBuilder builder) {
		String hostName = request.hostName();
		boolean ipv6 = hostName.indexOf(':') != -1;
		boolean brackets = ipv6 && !hostName.startsWith("[") && !hostName.endsWith("]");
		if (brackets) {
			builder.append('[');
		}
		if (encoded(hostName, ipv6)) {
			builder.append(hostName);
		}
		else {
			for (int i=0; i < hostName.length(); i++) {
				char c = hostName.charAt(i);
				if (isAllowedInHost(c, ipv6)) {
					builder.append(c);
				}
				else {
					builder.append('%');
					char hex1 = Character.toUpperCase(Character.forDigit((c >> 4) & 0xF, 16));
					char hex2 = Character.toUpperCase(Character.forDigit(c & 0xF, 16));
					builder.append(hex1);
					builder.append(hex2);
				}
			}
		}
		if (brackets) {
			builder.append(']');
		}
	}

	private static boolean encoded(String hostName, boolean ipv6) {
		int length = hostName.length();
		for (int i = 0; i < length; i++) {
			char c = hostName.charAt(i);
			if (c == '%') {
				if ((i + 2) < length) {
					char hex1 = hostName.charAt(i + 1);
					char hex2 = hostName.charAt(i + 2);
					int u = Character.digit(hex1, 16);
					int l = Character.digit(hex2, 16);
					if (u == -1 || l == -1) {
						return false;
					}
					i += 2;
				}
				else {
					return false;
				}
			}
			else if (!isAllowedInHost(c, ipv6)) {
				return false;
			}
		}
		return true;
	}

	/* ===== [OCA 中文解析] =====
方法 isAllowedInHost — 意图与阅读要点

方法 `isAllowedInHost` 复杂度较高（CCN≈25, NLOC≈9）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。

	===== [OCA 中文解析结束] ===== */

	private static boolean isAllowedInHost(char c, boolean ipv6) {
		return (c >= 'a' && c <= 'z') || // alpha
				(c >= 'A' && c <= 'Z') || // alpha
				(c >= '0' && c <= '9') || // digit
				'-' == c || '.' == c || '_' == c || '~' == c || // unreserved
				'!' == c || '$' == c || '&' == c || '\'' == c || '(' == c || ')' == c || // sub-delims
				'*' == c || '+' == c || ',' == c || ';' == c || '=' == c ||
				(ipv6 && ('[' == c || ']' == c || ':' == c)); // ipv6
	}

	/* ===== [OCA 中文解析] =====
方法 appendRequestUri — 意图与阅读要点

方法 `appendRequestUri` 复杂度较高（CCN≈13, NLOC≈23）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。

	===== [OCA 中文解析结束] ===== */

	private static void appendRequestUri(HttpServerRequest request, StringBuilder builder) {
		String uri = request.uri();
		int length = uri.length();
		for (int i = 0; i < length; i++) {
			char c = uri.charAt(i);
			if (c == '/' || c == '?' || c == '#') {
				break;
			}
			if (c == ':' && (i + 2 < length)) {
				if (uri.charAt(i + 1) == '/' && uri.charAt(i + 2) == '/') {
					for (int j = i + 3; j < length; j++) {
						c = uri.charAt(j);
						if (c == '/' || c == '?' || c == '#') {
							builder.append(uri, j, length);
							return;
						}
					}
					return;
				}
			}
		}
		builder.append(uri);
	}
}
