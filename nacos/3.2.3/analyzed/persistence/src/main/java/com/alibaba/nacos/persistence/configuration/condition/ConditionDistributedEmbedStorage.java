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
 * 分布式内嵌存储条件：embeddedStorage 为 true 且非 standalone。
 *
 * <p>用于装配集群模式下 Derby 分布式存储相关 Bean。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ConditionDistributedEmbedStorage implements Condition {
    
    /** 内嵌存储开启且运行在非单机模式时匹配。 */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return DatasourceConfiguration.isEmbeddedStorage() && !EnvUtil.getStandaloneMode();
    }
}
