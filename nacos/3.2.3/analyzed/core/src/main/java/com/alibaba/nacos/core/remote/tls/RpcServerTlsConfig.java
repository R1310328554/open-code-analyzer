/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.remote.tls;

import com.alibaba.nacos.common.remote.TlsConfig;

/**
 * RPC 服务端的 TLS 配置，继承 {@link com.alibaba.nacos.common.remote.TlsConfig} 的通用 TLS 属性。
 *
 * <p>额外提供 SSL 上下文热刷新器名称与兼容模式开关。</p>
 *
 * @author githubcheng2978.
 */
public class RpcServerTlsConfig extends TlsConfig {
    
    /** SSL 上下文刷新器的 SPI 实现类名（{@link RpcServerSslContextRefresher}）。 */
    private String sslContextRefresher = "";
    
    /** 是否启用 TLS 兼容模式（默认 {@code true}）。 */
    private Boolean compatibility = true;
    
    /**
     * 获取兼容模式是否启用。
     *
     * @return {@code true} 表示启用兼容模式
     */
    public Boolean getCompatibility() {
        return compatibility;
    }
    
    /**
     * 设置是否启用兼容模式。
     *
     * @param compatibility {@code true} 启用兼容模式
     */
    public void setCompatibility(Boolean compatibility) {
        this.compatibility = compatibility;
    }
    
    /**
     * 获取 SSL 上下文刷新器 SPI 名称。
     *
     * @return 刷新器实现类名
     */
    public String getSslContextRefresher() {
        return sslContextRefresher;
    }
    
    /**
     * 设置 SSL 上下文刷新器 SPI 名称。
     *
     * @param sslContextRefresher 刷新器实现类名
     */
    public void setSslContextRefresher(String sslContextRefresher) {
        this.sslContextRefresher = sslContextRefresher;
    }
}
