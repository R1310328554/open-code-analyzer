/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl.constant;

/**
 * Nacos 支持的鉴权系统类型枚举。
 *
 * <p>对应配置项 {@code nacos.core.auth.system.type}， 决定加载内置 Nacos 鉴权还是 LDAP 集成。</p>
 *
 * @author nkorange
 * @author mai.jh
 * @since 1.2.0
 */
public enum AuthSystemTypes {
    
    /** Nacos 内置用户名密码 + JWT 鉴权体系。 */

    NACOS,
    /** 外部 LDAP 目录服务鉴权。 */

    LDAP
}
