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

package com.alibaba.nacos.config.server.service;

import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.config.server.model.event.ConfigDataChangeEvent;
import com.alibaba.nacos.persistence.configuration.DatasourceConfiguration;
import com.alibaba.nacos.sys.env.EnvUtil;

/**
 * 配置变更事件发布器：在集群非嵌入式存储模式下，通过 {@link NotifyCenter} 广播 {@link ConfigDataChangeEvent}。
 * 嵌入式集群模式下单节点不重复发布，避免多副本重复通知。
 * ConfigChangePublisher.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ConfigChangePublisher {
    
    /**
     * 发布配置数据变更事件（嵌入式集群且非 standalone 时直接返回）。
     *
     * Notify ConfigChange.
     *
     * @param event ConfigDataChangeEvent instance.
     */
    public static void notifyConfigChange(ConfigDataChangeEvent event) {
        if (DatasourceConfiguration.isEmbeddedStorage() && !EnvUtil.getStandaloneMode()) {
            return;
        }
        NotifyCenter.publishEvent(event);
    }
    
}
