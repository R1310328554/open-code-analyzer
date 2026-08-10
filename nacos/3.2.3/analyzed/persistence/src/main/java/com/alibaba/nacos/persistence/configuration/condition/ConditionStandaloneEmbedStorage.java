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

package com.alibaba.nacos.persistence.configuration.condition;

import com.alibaba.nacos.persistence.configuration.DatasourceConfiguration;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 判断是否启用单机嵌入式存储的 Spring {@link Condition}。
 *
 * <p>当 {@link DatasourceConfiguration#isEmbeddedStorage()} 为 true 且处于 {@link EnvUtil#getStandaloneMode()} 单机模式时匹配，用于条件化注册 Derby 相关 Bean。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ConditionStandaloneEmbedStorage implements Condition {
    
    @Override
    /** 嵌入式存储开启且为单机模式时返回 true。 */
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return DatasourceConfiguration.isEmbeddedStorage() && EnvUtil.getStandaloneMode();
    }
}
