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

import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * 可将 JSON 格式字符串读入 {@link Map} 或 {@link List} 的解析器。
 *
 * @author Dave Syer
 * @since 1.0.0
 * @see JsonParserFactory
 * @see BasicJsonParser
 * @see JacksonJsonParser
 * @see GsonJsonParser
 */
public interface JsonParser {

	/**
	 * 将指定 JSON 字符串解析为 Map。
	 *
	 * @param json 待解析的 JSON
	 * @return 解析结果为 map
	 * @throws JsonParseException 若 JSON 无法解析
	 */
	Map<String, Object> parseMap(@Nullable String json) throws JsonParseException;

	/**
	 * 将指定 JSON 字符串解析为 List。
	 *
	 * @param json 待解析的 JSON
	 * @return 解析结果为 list
	 * @throws JsonParseException 若 JSON 无法解析
	 */
	List<Object> parseList(@Nullable String json) throws JsonParseException;

}
