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
 * Nacos 服务端身份校验器接口。
 *
 * <p>用于 Nacos 内部/管理 API 的服务端身份互信校验；实现类可通过 SPI 扩展，
 * 未找到自定义实现时使用 {@link DefaultChecker}。</p>
 *
 * @author xiweng.yy
 */
public interface ServerIdentityChecker {
    
    /**
     * 初始化校验器，注入认证配置。
     *
     * @param authConfig Nacos 认证配置
     */
    void init(NacosAuthConfig authConfig);
    
    /**
     * 校验请求携带的服务端身份是否与配置匹配。
     *
     * @param serverIdentity 请求中提取的服务端身份
     * @param secured        目标 API 的 {@link Secured} 注解
     * @return 校验结果，见 {@link ServerIdentityResult}
     */
    ServerIdentityResult check(ServerIdentity serverIdentity, Secured secured);
}
