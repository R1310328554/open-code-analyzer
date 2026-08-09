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
package com.alibaba.csp.sentinel.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Dashboard 认证配置属性，绑定 {@code auth.*} 前缀。
 */
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    /** 是否启用 Dashboard 登录认证，默认 true。 */
    private boolean enabled = true;

    /** @return 认证是否启用 */
    public boolean isEnabled() {
        return enabled;
    }

    /** @param enabled 是否启用认证 */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
