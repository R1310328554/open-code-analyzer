/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.istio.util;

import com.alibaba.nacos.common.utils.UuidUtils;

/**
 * xDS/MCP 推送 nonce 生成器。
 *
 * <p>基于 UUID 生成无连字符的随机串，供 ADS/Delta 协议 ACK 追踪。</p>
 *
 * @author special.fy
 */
public class NonceGenerator {
    
    /** 生成 32 位十六进制 nonce 字符串。 */
    public static String generateNonce() {
        return UuidUtils.generateUuid().replace("-", "");
    }
}
