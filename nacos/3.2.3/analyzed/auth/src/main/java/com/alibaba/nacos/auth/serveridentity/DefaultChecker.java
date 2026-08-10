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

package com.alibaba.nacos.auth.serveridentity;

import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.auth.config.NacosAuthConfig;

/**
 * Nacos 默认服务端身份校验器。
 *
 * <p>将请求中的身份值与 {@link NacosAuthConfig#getServerIdentityValue()} 做字符串相等比较；
 * SPI 未提供自定义实现时由 {@link ServerIdentityCheckerHolder} 回退使用本类。</p>
 *
 * @author xiweng.yy
 */
public class DefaultChecker implements ServerIdentityChecker {
    
    /** 认证配置，在 {@link #init} 时注入。 */
    private NacosAuthConfig authConfig;
    
    /** {@inheritDoc} */
    @Override
    public void init(NacosAuthConfig authConfigs) {
        this.authConfig = authConfigs;
    }
    
    /**
     * 比较请求身份值与配置中的服务端身份密钥。
     *
     * @param serverIdentity 请求携带的服务端身份
     * @param secured        目标 API 的安全注解（默认实现未使用）
     * @return 值相等返回 {@link ServerIdentityResult#success()}，否则 {@link ServerIdentityResult#noMatched()}
     */
    @Override
    public ServerIdentityResult check(ServerIdentity serverIdentity, Secured secured) {
        if (authConfig.getServerIdentityValue().equals(serverIdentity.getIdentityValue())) {
            return ServerIdentityResult.success();
        }
        return ServerIdentityResult.noMatched();
    }
}
