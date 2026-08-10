/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.common;

/**
 * 客户端连接信息抽象。
 *
 * <p>描述 HTTP/TCP 层对端与本地端点的地址与端口，供审计、限流等场景使用。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ClientConnection {

    /**
     * @return 远端 IP 地址字符串；不可用时返回 {@code null}
     */
    default String getRemoteAddr() {
        return null;
    }
    /**
     * @return 远端主机名或 IP；若经代理则取自代理头，不可用时返回 {@code null}
     */
    default String getRemoteHost() {
        return null;
    }
    
    /**
     * @return 远端端口；不可用时返回 {@code 0}
     */
    default int getRemotePort() {
        return 0;
    }

    /**
     * @return 本地 IP 地址字符串；不可用时返回 {@code null}
     */
    default String getLocalAddr() {
        return null;
    }
    
    /**
     * @return 本地端口；不可用时返回 {@code 0}
     */
    default int getLocalPort() {
        return 0;
    }
}
