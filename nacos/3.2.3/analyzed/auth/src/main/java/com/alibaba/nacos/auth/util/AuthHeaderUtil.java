/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.auth.util;

import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.auth.config.NacosAuthConfig;
import com.alibaba.nacos.common.http.param.Header;
import com.alibaba.nacos.common.utils.StringUtils;

/**
 * 认证请求头工具类。
 *
 * <p>在集群节点间 HTTP 调用或 gRPC 请求中自动附加服务端身份键值对，
 * 使对端可通过 {@link com.alibaba.nacos.auth.serveridentity.ServerIdentityChecker} 快速互信。</p>
 *
 * @author xiweng.yy
 */
public class AuthHeaderUtil {
    
    /**
     * 向 HTTP 请求头写入服务端身份信息。
     *
     * <p>仅当 {@link NacosAuthConfig#isSupportServerIdentity()} 为 true 且身份键非空时写入。</p>
     *
     * @param header     HTTP 请求头
     * @param authConfig Nacos 认证配置
     */
    public static void addIdentityToHeader(Header header, NacosAuthConfig authConfig) {
        if (!authConfig.isSupportServerIdentity()) {
            return;
        }
        if (StringUtils.isNotBlank(authConfig.getServerIdentityKey())) {
            header.addParam(authConfig.getServerIdentityKey(), authConfig.getServerIdentityValue());
        }
    }
    
    /**
     * 向 gRPC 远程请求头写入服务端身份信息。
     *
     * <p>仅当 {@link NacosAuthConfig#isSupportServerIdentity()} 为 true 且身份键非空时写入。</p>
     *
     * @param request    gRPC 远程请求
     * @param authConfig Nacos 认证配置
     */
    public static void addIdentityToHeader(Request request, NacosAuthConfig authConfig) {
        if (!authConfig.isSupportServerIdentity()) {
            return;
        }
        if (StringUtils.isNotBlank(authConfig.getServerIdentityKey())) {
            request.putHeader(authConfig.getServerIdentityKey(),
                authConfig.getServerIdentityValue());
        }
    }
    
}
