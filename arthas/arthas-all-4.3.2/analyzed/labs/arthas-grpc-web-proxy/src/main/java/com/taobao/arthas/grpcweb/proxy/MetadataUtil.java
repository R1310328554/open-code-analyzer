/*
 * Copyright 2020  Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.taobao.arthas.grpcweb.proxy;

import io.grpc.Metadata;
import io.netty.handler.codec.http.HttpHeaders;
import java.util.*;

/**
 * HTTP 头与 gRPC {@link Metadata} 的双向转换工具。
 *
 * <p>主要处理 {@code x-grpc-*} 自定义头；以 {@code -bin} 结尾的按二进制 Metadata 键处理。</p>
 */
class MetadataUtil {
    /** 二进制 Metadata 键后缀 */
    private static final String BINARY_HEADER_SUFFIX = "-bin";
    /** 需要复制到 gRPC Metadata 的 HTTP 头前缀 */
    private static final String GRPC_HEADER_PREFIX = "x-grpc-";
    /** 由 gRPC-Web 协议本身处理、无需透传的头名（小写） */
    private static final List<String> EXCLUDED = Arrays.asList("x-grpc-web", "content-type", "grpc-accept-encoding",
            "grpc-encoding");

    /**
     * 从 Netty HTTP 请求头提取 {@code x-grpc-*} 并写入 gRPC {@link Metadata}。
     *
     * @param headers 入站 HTTP 头
     * @return 供 Stub 附加的 Metadata
     */
    static Metadata getHtpHeaders(HttpHeaders headers) {
        Metadata httpHeaders = new Metadata();

        Set<String> headerNames = headers.names();
        if (headerNames == null) {
            return httpHeaders;
        }
        // 复制所有 x-grpc-* 头（排除 EXCLUDED 列表）
        // TODO: 是否需要复制全部 x-* 头而不仅是 x-grpc-*
        for (String headerName : headerNames) {
            if (EXCLUDED.contains(headerName.toLowerCase())) {
                continue;
            }
            if (headerName.toLowerCase().startsWith(GRPC_HEADER_PREFIX)) {
                // 同一头名可能有多值
                List<String> values = headers.getAll(headerName);
                if (values != null) {
                    for (String s : values) {
                        if (headerName.toLowerCase().endsWith(BINARY_HEADER_SUFFIX)) {
                            // 二进制 Metadata 键
                            httpHeaders.put(Metadata.Key.of(headerName, Metadata.BINARY_BYTE_MARSHALLER), s.getBytes());
                        } else {
                            // ASCII 字符串 Metadata 键
                            httpHeaders.put(Metadata.Key.of(headerName, Metadata.ASCII_STRING_MARSHALLER), s);
                        }
                    }
                }
            }
        }
        return httpHeaders;
    }

    /**
     * 将 gRPC {@link Metadata}（响应头或 trailer）转为 HTTP 头 Map，供写出 gRPC-Web 响应。
     *
     * @param trailer gRPC Metadata
     * @return 头名到头值的 Map
     */
    static Map<String, String> getHttpHeadersFromMetadata(Metadata trailer) {
        Map<String, String> map = new HashMap<>();
        for (String key : trailer.keys()) {
            if (EXCLUDED.contains(key.toLowerCase())) {
                continue;
            }
            if (key.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
                // TODO: 二进制值当前按 String 构造，后续可支持更丰富的类型
                byte[] value = trailer.get(Metadata.Key.of(key, Metadata.BINARY_BYTE_MARSHALLER));
                map.put(key, new String(value));
            } else {
                String value = trailer.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER));
                map.put(key, value);
            }
        }
        return map;
    }
}
