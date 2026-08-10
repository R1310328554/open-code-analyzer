/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl.condition;

import com.alibaba.nacos.plugin.auth.constant.Constants;
import com.alibaba.nacos.plugin.auth.impl.constant.AuthSystemTypes;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 当 {@code nacos.core.auth.system.type=ldap} 时生效的 Spring 条件。
 *
 * <p>用于 LDAP 认证相关配置类与 Bean 的条件装配，避免在非 LDAP 模式下加载 LDAP 组件。</p>
 *
 * @author karsonto
 */
public class ConditionOnLdapAuth implements Condition {
    
    /** 判断当前认证系统类型是否为 LDAP。 */
    @Override
    public boolean matches(ConditionContext conditionContext,
        AnnotatedTypeMetadata annotatedTypeMetadata) {
        return AuthSystemTypes.LDAP.name()
            .equalsIgnoreCase(EnvUtil.getProperty(Constants.Auth.NACOS_CORE_AUTH_SYSTEM_TYPE));
    }
}
