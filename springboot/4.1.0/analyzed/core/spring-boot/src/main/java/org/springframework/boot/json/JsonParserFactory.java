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

package org.springframework.boot.json;

import org.springframework.util.ClassUtils;

/**
 * 创建 {@link JsonParser} 的工厂。
 * 按类路径可用性依次尝试 Jackson、Gson，最后回退到 {@link BasicJsonParser}。
 *
 * @author Dave Syer
 * @since 1.0.0
 * @see JacksonJsonParser
 * @see GsonJsonParser
 * @see BasicJsonParser
 */
public abstract class JsonParserFactory {

	/**
	 * 类路径上“最佳” JSON 解析器的静态工厂。
	 * 依次尝试 Jackson、Gson，最后回退到 {@link BasicJsonParser}。
	 *
	 * @return a {@link JsonParser} JSON 解析器
	 */
	public static JsonParser getJsonParser() {
		if (ClassUtils.isPresent("tools.jackson.databind.ObjectMapper", null)) {
			return new JacksonJsonParser();
		}
		if (ClassUtils.isPresent("com.google.gson.Gson", null)) {
			return new GsonJsonParser();
		}
		return new BasicJsonParser();
	}

}
