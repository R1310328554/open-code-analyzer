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

package com.alibaba.nacos.naming.cluster;

import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.distributed.ProtocolManager;
import com.alibaba.nacos.core.distributed.distro.DistroProtocol;
import com.alibaba.nacos.naming.misc.GlobalConfig;
import com.alibaba.nacos.naming.misc.GlobalExecutor;
import com.alibaba.nacos.naming.misc.SwitchDomain;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;

/**
 * 检测并维护本机 Naming 节点运行状态。
 *
 * <p>周期性刷新 {@link ServerStatus}：优先读取 {@link SwitchDomain} 覆盖值，否则依据 CP 协议就绪与 {@link DistroProtocol} 初始化情况判定 UP/DOWN。</p>
 *
 * @author nkorange
 * @since 1.0.0
 */
@Service
public class ServerStatusManager {
    
    /** 全局命名配置（如数据预热开关）。 */
    private final GlobalConfig globalConfig;
    
    /** AP Distro 一致性协议实例。 */
    private final DistroProtocol distroProtocol;
    
    /** CP/AP 协议管理器，用于判断 Raft 是否就绪。 */
    private final ProtocolManager protocolManager;
    
    /** 运维开关域，可强制覆盖节点对外状态。 */
    private final SwitchDomain switchDomain;
    
    /** 当前节点状态，初始为 STARTING。 */
    private ServerStatus serverStatus = ServerStatus.STARTING;
    
    public ServerStatusManager(GlobalConfig globalConfig, DistroProtocol distroProtocol,
        ProtocolManager protocolManager, SwitchDomain switchDomain) {
        this.globalConfig = globalConfig;
        this.distroProtocol = distroProtocol;
        this.protocolManager = protocolManager;
        this.switchDomain = switchDomain;
    }
    
    /** 注册定时状态刷新任务。 */
    @PostConstruct
    public void init() {
        GlobalExecutor.registerServerStatusUpdater(new ServerStatusUpdater());
    }
    
    /** 根据开关覆盖或就绪检查结果刷新 {@link #serverStatus}。 */
    private void refreshServerStatus() {
        if (StringUtils.isNotBlank(switchDomain.getOverriddenServerStatus())) {
            serverStatus = ServerStatus.valueOf(switchDomain.getOverriddenServerStatus());
            return;
        }
        
        if (isReady()) {
            serverStatus = ServerStatus.UP;
        } else {
            serverStatus = ServerStatus.DOWN;
        }
    }
    
    /** 判断节点是否可对外提供服务（预热、CP、Distro 均就绪）。 */
    private boolean isReady() {
        if (!globalConfig.isDataWarmup()) {
            return true;
        }
        if (!protocolManager.isCpInit() || protocolManager.getCpProtocol() == null) {
            return false;
        }
        return protocolManager.getCpProtocol().isReady() && distroProtocol.isInitialized();
    }
    
    /** 获取当前节点运行状态。 */
    public ServerStatus getServerStatus() {
        return serverStatus;
    }
    
    /** 节点未就绪时返回可读错误提示（Distro 或 Raft 相关）。 */
    public Optional<String> getErrorMsg() {
        if (isReady()) {
            return Optional.empty();
        }
        if (!distroProtocol.isInitialized()) {
            return Optional.of(
                "Distro snapshot load failed, please see logs `protocol-distro.log` or `naming-distro.log` to see details.");
        }
        return Optional.of(
            "No leader for raft, please see logs `alipay-jraft.log` or `naming-raft.log` to see details.");
    }
    
    /** 定时触发 {@link #refreshServerStatus()} 的后台任务。 */
    public class ServerStatusUpdater implements Runnable {
        
        @Override
        public void run() {
            refreshServerStatus();
        }
    }
}
