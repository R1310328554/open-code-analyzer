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

package com.alibaba.nacos.naming.monitor;

import com.alibaba.nacos.core.monitor.topn.BaseTopNCounter;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.misc.UtilsAndCommons;

/**
 * 服务变更次数 TopN 计数器。
 *
 * <p>继承 {@link BaseTopNCounter}，以「命名空间@@分组@@服务名」为键统计各服务变更频率，供 {@link NamingDynamicMeterRefreshService} 定时刷新到 Micrometer。</p>
 *
 * @author xiweng.yy
 */
public class ServiceTopNCounter extends BaseTopNCounter<Service> {
    
    public ServiceTopNCounter() {
        super();
    }
    
    /** 将 {@link Service} 转为 TopN 计数键（namespace@@groupedServiceName）。 */
    @Override
    protected String keyToString(Service service) {
        return service.getNamespace() + UtilsAndCommons.NAMESPACE_SERVICE_CONNECTOR
            + service.getGroupedServiceName();
    }
}
