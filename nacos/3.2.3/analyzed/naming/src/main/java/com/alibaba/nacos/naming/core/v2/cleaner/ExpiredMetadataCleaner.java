/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.core.v2.cleaner;

import com.alibaba.nacos.naming.core.v2.metadata.ExpiredMetadataInfo;
import com.alibaba.nacos.naming.core.v2.metadata.NamingMetadataManager;
import com.alibaba.nacos.naming.core.v2.metadata.NamingMetadataOperateService;
import com.alibaba.nacos.naming.misc.GlobalConfig;
import com.alibaba.nacos.naming.misc.GlobalExecutor;
import com.alibaba.nacos.naming.misc.Loggers;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 过期元数据清理器。
 *
 * <p>定时扫描 {@link NamingMetadataManager} 中的过期元数据记录，删除超时的服务级或实例级元数据。</p>
 *
 * @author xiweng.yy
 */
@Component
public class ExpiredMetadataCleaner extends AbstractNamingCleaner {
    
    /** 清理器类型标识。 */
    private static final String EXPIRED_METADATA = "expiredMetadata";
    
    /** 首次执行前的延迟毫秒数。 */
    private static final int INITIAL_DELAY = 5000;
    
    /** 元数据内存管理器。 */
    private final NamingMetadataManager metadataManager;
    
    /** 元数据删除操作服务。 */
    private final NamingMetadataOperateService metadataOperateService;
    
    /** 注册定时过期元数据清理任务。 */
    public ExpiredMetadataCleaner(NamingMetadataManager metadataManager,
        NamingMetadataOperateService metadataOperateService) {
        this.metadataManager = metadataManager;
        this.metadataOperateService = metadataOperateService;
        GlobalExecutor.scheduleExpiredClientCleaner(this, INITIAL_DELAY,
            GlobalConfig.getExpiredMetadataCleanInterval(),
            TimeUnit.MILLISECONDS);
    }
    
    @Override
    public String getType() {
        return EXPIRED_METADATA;
    }
    
    @Override
    public void doClean() {
        long currentTime = System.currentTimeMillis();
        for (ExpiredMetadataInfo each : metadataManager.getExpiredMetadataInfos()) {
            if (currentTime - each.getCreateTime() > GlobalConfig.getExpiredMetadataExpiredTime()) {
                removeExpiredMetadata(each);
            }
        }
    }
    
    /** 根据过期信息删除服务或实例元数据。 */
    private void removeExpiredMetadata(ExpiredMetadataInfo expiredInfo) {
        Loggers.SRV_LOG.info("Remove expired metadata {}", expiredInfo);
        if (null == expiredInfo.getMetadataId()) {
            if (metadataManager.containServiceMetadata(expiredInfo.getService())) {
                metadataOperateService.deleteServiceMetadata(expiredInfo.getService());
            }
        } else {
            if (metadataManager.containInstanceMetadata(expiredInfo.getService(),
                expiredInfo.getMetadataId())) {
                metadataOperateService.deleteInstanceMetadata(expiredInfo.getService(),
                    expiredInfo.getMetadataId());
            }
        }
    }
}
