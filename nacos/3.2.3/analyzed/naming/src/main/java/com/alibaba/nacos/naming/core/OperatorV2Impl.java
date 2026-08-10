/*
 * Copyright 1999-$toady.year Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.core;

import com.alibaba.nacos.naming.cluster.ServerStatusManager;
import com.alibaba.nacos.naming.constants.ClientConstants;
import com.alibaba.nacos.naming.core.v2.client.impl.IpPortBasedClient;
import com.alibaba.nacos.naming.core.v2.client.manager.ClientManager;
import com.alibaba.nacos.naming.misc.Loggers;
import com.alibaba.nacos.naming.misc.SwitchDomain;
import com.alibaba.nacos.naming.misc.SwitchManager;
import com.alibaba.nacos.naming.model.vo.MetricsInfoVo;
import com.alibaba.nacos.naming.monitor.MetricsMonitor;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;

/**
 * Naming V2 运维操作实现。
 *
 * <p>提供开关域查询与更新、运行时指标采集以及日志级别调整，指标统计基于 {@link ClientManager} 中的客户端类型与归属信息。</p>
 *
 * @author Nacos
 */

@Service
public class OperatorV2Impl implements Operator {
    
    /** 命名模块开关域配置。 */
    @Resource
    private SwitchDomain switchDomain;
    
    /** 开关项读写管理器。 */
    @Resource
    private SwitchManager switchManager;
    
    /** 集群节点状态管理器。 */
    @Resource
    private ServerStatusManager serverStatusManager;
    
    /** V2 客户端管理器，用于统计连接型与 IP:Port 型客户端数量。 */
    @Resource
    private ClientManager clientManager;
    
    /** 返回当前命名开关域快照。 */
    @Override
    public SwitchDomain switches() {
        return switchDomain;
    }
    
    /** 更新指定开关项的值。 */
    @Override
    public void updateSwitch(String entry, String value, boolean debug) throws Exception {
        switchManager.update(entry, value, debug);
    }
    
    /** 采集命名模块运行时指标；{@code onlyStatus} 为 true 时仅返回节点状态。 */
    @Override
    public MetricsInfoVo metrics(boolean onlyStatus) {
        MetricsInfoVo metricsInfoVo = new MetricsInfoVo();
        metricsInfoVo.setStatus(serverStatusManager.getServerStatus().name());
        if (onlyStatus) {
            return metricsInfoVo;
        }
        
        // 连接型客户端计数（clientId 不含 IP:Port 分隔符）
        int connectionBasedClient = 0;
        // 临时 IP:Port 型客户端计数
        int ephemeralIpPortClient = 0;
        // 持久 IP:Port 型客户端计数
        int persistentIpPortClient = 0;
        // 当前节点负责的客户端数量
        int responsibleClientCount = 0;
        Collection<String> allClientId = clientManager.allClientId();
        for (String clientId : allClientId) {
            if (clientId.contains(IpPortBasedClient.ID_DELIMITER)) {
                if (clientId.endsWith(ClientConstants.PERSISTENT_SUFFIX)) {
                    persistentIpPortClient += 1;
                } else {
                    ephemeralIpPortClient += 1;
                }
            } else {
                connectionBasedClient += 1;
            }
            if (clientManager.isResponsibleClient(clientManager.getClient(clientId))) {
                responsibleClientCount += 1;
            }
        }
        
        metricsInfoVo.setServiceCount(MetricsMonitor.getDomCountMonitor().get());
        metricsInfoVo.setInstanceCount(MetricsMonitor.getIpCountMonitor().get());
        metricsInfoVo.setSubscribeCount(MetricsMonitor.getSubscriberCount().get());
        metricsInfoVo.setClientCount(allClientId.size());
        metricsInfoVo.setConnectionBasedClientCount(connectionBasedClient);
        metricsInfoVo.setEphemeralIpPortClientCount(ephemeralIpPortClient);
        metricsInfoVo.setPersistentIpPortClientCount(persistentIpPortClient);
        metricsInfoVo.setResponsibleClientCount(responsibleClientCount);
        metricsInfoVo.setCpu(EnvUtil.getCpu());
        metricsInfoVo.setLoad(EnvUtil.getLoad());
        metricsInfoVo.setMem(EnvUtil.getMem());
        
        return metricsInfoVo;
    }
    
    /** 动态调整指定命名日志器的输出级别。 */
    @Override
    public void setLogLevel(String logName, String logLevel) {
        Loggers.setLogLevel(logName, logLevel);
    }
}
