/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.proxy.common.utils;

import io.grpc.Attributes;
import io.grpc.Metadata;
import io.grpc.ServerCall;

/**
 * gRPC 辅助工具：安全地向 Metadata 写入头、从 ServerCall 读取 Attributes。
 */
public class GrpcUtils {

    /** 工具类禁止实例化。 */
    private GrpcUtils() {
    }

    /** 若 headers 中尚未存在 key 且 value 非空，则写入该头。 */
    public static <T> void putHeaderIfNotExist(Metadata headers, Metadata.Key<T> key, T value) {
        // headers 为空时静默跳过
        if (headers == null) {
            return;
        }
        if (!headers.containsKey(key) && value != null) {
            headers.put(key, value);
        }
    }

    /** 从 ServerCall 的 Attributes 中读取指定键，缺失时返回 null。 */
    public static <R, W, T> T getAttribute(ServerCall<R, W> call, Attributes.Key<T> key) {
        Attributes attributes = call.getAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.get(key);
    }
}
