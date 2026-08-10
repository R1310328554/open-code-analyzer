/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.plugin.condition;

import com.alibaba.nacos.sys.env.EnvUtil;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Spring 条件：Nacos 以集群模式运行时匹配（非 standalone）。
 * <p>用于仅在集群部署下注册的 Bean，如插件状态 Raft 同步组件。</p>
 * Condition that matches when Nacos is running in cluster mode.
 *
 * @author WangzJi
 * @since 3.2.0
 */
/**
 * 集群模式条件实现，委托 {@link EnvUtil#getStandaloneMode()} 取反判断。
 */
public class ConditionOnClusterMode implements Condition {
    
    /**
     * {@inheritDoc} — 非单机模式（集群）时返回 true。
     *
     * @param context Spring 条件上下文
     * @param metadata 注解元数据
     * @return 是否匹配集群模式
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return !EnvUtil.getStandaloneMode();
    }
}
