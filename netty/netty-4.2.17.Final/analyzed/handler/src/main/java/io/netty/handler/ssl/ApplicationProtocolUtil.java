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
package io.netty.handler.ssl;

import static io.netty.util.internal.ObjectUtil.checkNonEmpty;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for application protocol common operations.
 *
 * <p>将协议名 Iterable/可变参数转为非空 {@link List}，供 {@link ApplicationProtocolConfig} 等使用。</p>
 */
final class ApplicationProtocolUtil {
    /** 默认 ArrayList 初始容量。 */
    private static final int DEFAULT_LIST_SIZE = 2;

    private ApplicationProtocolUtil() {
    }

    /** 从 Iterable 构建协议列表，初始容量 {@link #DEFAULT_LIST_SIZE}。 */
    static List<String> toList(Iterable<String> protocols) {
        return toList(DEFAULT_LIST_SIZE, protocols);
    }

    /** 从 Iterable 构建协议列表，校验每项与结果均非空。 */
    static List<String> toList(int initialListSize, Iterable<String> protocols) {
        if (protocols == null) {
            return null;
        }

        List<String> result = new ArrayList<String>(initialListSize);
        for (String p : protocols) {
            result.add(checkNonEmpty(p, "p"));
        }

        return checkNonEmpty(result, "result");
    }

    /** 从可变参数构建协议列表。 */
    static List<String> toList(String... protocols) {
        return toList(DEFAULT_LIST_SIZE, protocols);
    }

    /** 从可变参数构建协议列表，指定初始容量。 */
    static List<String> toList(int initialListSize, String... protocols) {
        if (protocols == null) {
            return null;
        }

        List<String> result = new ArrayList<String>(initialListSize);
        for (String p : protocols) {
            result.add(checkNonEmpty(p, "p"));
        }

        return checkNonEmpty(result, "result");
    }
}
