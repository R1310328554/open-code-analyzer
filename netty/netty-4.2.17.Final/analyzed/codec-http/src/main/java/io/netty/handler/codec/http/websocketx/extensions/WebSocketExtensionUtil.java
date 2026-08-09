/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.http.websocketx.extensions;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * WebSocket 扩展握手工具类：解析/合并 {@code Sec-WebSocket-Extensions} 头。
 * <p>支持 RFC 6455 扩展参数语法（逗号分隔扩展名、分号分隔键值对）。
 */
public final class WebSocketExtensionUtil {

    /** 多个扩展名之间的分隔符 */
    private static final String EXTENSION_SEPARATOR = ",";
    /** 扩展名与其参数之间的分隔符 */
    private static final String PARAMETER_SEPARATOR = ";";
    private static final char PARAMETER_EQUAL = '=';

    private static final Pattern PARAMETER = Pattern.compile("^([^=]+)(=[\\\"]?([^\\\"]+)[\\\"]?)?$");

    /** 判断 HTTP 头是否表示 WebSocket 升级请求/响应（Upgrade + Connection） */
    static boolean isWebsocketUpgrade(HttpHeaders headers) {
        // contains 检查不分配迭代器；多数请求非升级，先做快速否定
        //so we do the contains check first before checking for specific values
        return headers.contains(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET, true) &&
               headers.containsValue(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE, true);
    }

    /**
     * 解析 {@code Sec-WebSocket-Extensions} 头值为扩展数据列表。
     *
     * @param extensionHeader 原始扩展头字符串
     * @return 按出现顺序排列的 {@link WebSocketExtensionData}
     */
        String[] rawExtensions = extensionHeader.split(EXTENSION_SEPARATOR);
        if (rawExtensions.length > 0) {
            List<WebSocketExtensionData> extensions = new ArrayList<>(rawExtensions.length);
            for (String rawExtension : rawExtensions) {
                String[] extensionParameters = rawExtension.split(PARAMETER_SEPARATOR);
                String name = extensionParameters[0].trim();
                Map<String, String> parameters;
                if (extensionParameters.length > 1) {
                    parameters = new LinkedHashMap<>(extensionParameters.length - 1);
                    for (int i = 1; i < extensionParameters.length; i++) {
                        String parameter = extensionParameters[i].trim();
                        Matcher parameterMatcher = PARAMETER.matcher(parameter);
                        if (parameterMatcher.matches() && parameterMatcher.group(1) != null) {
                            parameters.put(parameterMatcher.group(1), parameterMatcher.group(3));
                        }
                    }
                } else {
                    parameters = Collections.emptyMap();
                }
                extensions.add(new WebSocketExtensionData(name, parameters));
            }
            return extensions;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * 将用户已有扩展头与协商成功的扩展合并，生成响应头值。
     * <p>同名扩展以用户参数优先覆盖服务端默认值。
     *
     * @param userDefinedHeaderValue 响应中已有的扩展头（可为 null）
     * @param extraExtensions 握手协商后待追加的扩展
     * @return 合并后的 {@code Sec-WebSocket-Extensions} 值
     */
    static String computeMergeExtensionsHeaderValue(String userDefinedHeaderValue,
                                                    List<WebSocketExtensionData> extraExtensions) {
        List<WebSocketExtensionData> userDefinedExtensions =
          userDefinedHeaderValue != null ?
            extractExtensions(userDefinedHeaderValue) :
            Collections.emptyList();

        for (WebSocketExtensionData userDefined: userDefinedExtensions) {
            WebSocketExtensionData matchingExtra = null;
            int i;
            for (i = 0; i < extraExtensions.size(); i ++) {
                WebSocketExtensionData extra = extraExtensions.get(i);
                if (extra.name().equals(userDefined.name())) {
                    matchingExtra = extra;
                    break;
                }
            }
            if (matchingExtra == null) {
                extraExtensions.add(userDefined);
            } else {
                // 用户自定义参数优先级高于服务端默认值
                Map<String, String> mergedParameters = new LinkedHashMap<>(matchingExtra.parameters());
                mergedParameters.putAll(userDefined.parameters());
                extraExtensions.set(i, new WebSocketExtensionData(matchingExtra.name(), mergedParameters));
            }
        }

        StringBuilder sb = new StringBuilder(150);

        for (WebSocketExtensionData data: extraExtensions) {
            sb.append(data.name());
            for (Entry<String, String> parameter : data.parameters().entrySet()) {
                sb.append(PARAMETER_SEPARATOR);
                sb.append(parameter.getKey());
                if (parameter.getValue() != null) {
                    sb.append(PARAMETER_EQUAL);
                    sb.append(parameter.getValue());
                }
            }
            sb.append(EXTENSION_SEPARATOR);
        }

        if (!extraExtensions.isEmpty()) {
            sb.setLength(sb.length() - EXTENSION_SEPARATOR.length());
        }

        return sb.toString();
    }

    /** 工具类不可实例化 */
    private WebSocketExtensionUtil() {
        // 无实例字段
    }
}
