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
package com.alibaba.csp.sentinel.transport.endpoint;

/**
 * Dashboard 通信协议枚举，当前支持 HTTP 与 HTTPS。
 *
 * @author Leo Li
 * @author Yanming Zhou
 */
public enum Protocol {
    /** 明文 HTTP。 */
    HTTP,
    /** TLS HTTPS。 */
    HTTPS;

    /** @return 小写协议名（http/https）。 */
    public String getProtocol() {
        return name().toLowerCase();
    }
}
