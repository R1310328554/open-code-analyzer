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

package com.alibaba.nacos.naming.healthcheck.heartbeat;

import com.alibaba.nacos.naming.core.v2.metadata.NamingMetadataManager;
import com.alibaba.nacos.naming.core.v2.metadata.ServiceMetadata;
import com.alibaba.nacos.naming.misc.UtilsAndCommons;
import com.alibaba.nacos.sys.utils.ApplicationUtils;

import java.util.Optional;

/**
 * 服务级客户端心跳检查开关拦截器，优先级最高。
 *
 * <p>从 {@link ServiceMetadata} 扩展字段读取 {@link UtilsAndCommons#ENABLE_CLIENT_BEAT}，为 true 时整条心跳检查链通过。</p>
 *
 * @author xiweng.yy
 */
public class ServiceEnableBeatCheckInterceptor extends AbstractBeatCheckInterceptor {
    
    /** 查询服务元数据中的 ENABLE_CLIENT_BEAT 开关。 */
    @Override
    public boolean intercept(InstanceBeatCheckTask object) {
        NamingMetadataManager metadataManager =
            ApplicationUtils.getBean(NamingMetadataManager.class);
        Optional<ServiceMetadata> metadata =
            metadataManager.getServiceMetadata(object.getService());
        if (metadata.isPresent()
            && metadata.get().getExtendData().containsKey(UtilsAndCommons.ENABLE_CLIENT_BEAT)) {
            return Boolean.parseBoolean(
                metadata.get().getExtendData().get(UtilsAndCommons.ENABLE_CLIENT_BEAT));
        }
        return false;
    }
    
    /** 心跳拦截链中最高优先级（最先执行）。 */
    @Override
    public int order() {
        return Integer.MIN_VALUE;
    }
}
