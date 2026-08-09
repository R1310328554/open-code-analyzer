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

package org.springframework.boot.convert;

/**
 * 使用宽松规则将 String 转换为 {@link java.lang.Enum}。具体规则：
 * <ul>
 * <li>大小写不敏感匹配</li>
 * <li>忽略 {@code '_'}、{@code '$'} 及其他特殊字符</li>
 * <li>允许将 {@code "false"} 与 {@code "true"} 映射到枚举 {@code ON} 与 {@code OFF}</li>
 * </ul>
 *
 * @author Phillip Webb
 */
final class LenientStringToEnumConverterFactory extends LenientObjectToEnumConverterFactory<String> {

}
