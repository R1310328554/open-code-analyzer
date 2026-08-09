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
package com.alibaba.csp.sentinel.dashboard.discovery;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;

import com.alibaba.csp.sentinel.util.StringUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 机器发现门面组件，委托 {@link SimpleMachineDiscovery} 管理应用与机器注册表。
 * <p>Spring 容器启动后通过 {@link #init()} 注入具体发现实现。
 */
@Component
public class AppManagement implements MachineDiscovery {

    @Autowired
    private ApplicationContext context;

    private MachineDiscovery machineDiscovery;

    /** 初始化时绑定 {@link SimpleMachineDiscovery} 实现。 */
    @PostConstruct
    public void init() {
        machineDiscovery = context.getBean(SimpleMachineDiscovery.class);
    }

    @Override
    public Set<AppInfo> getBriefApps() {
        return machineDiscovery.getBriefApps();
    }

    @Override
    public long addMachine(MachineInfo machineInfo) {
        return machineDiscovery.addMachine(machineInfo);
    }
    
    @Override
    public boolean removeMachine(String app, String ip, int port) {
        return machineDiscovery.removeMachine(app, ip, port);
    }

    @Override
    public List<String> getAppNames() {
        return machineDiscovery.getAppNames();
    }

    @Override
    public AppInfo getDetailApp(String app) {
        return machineDiscovery.getDetailApp(app);
    }
    
    @Override
    public void removeApp(String app) {
        machineDiscovery.removeApp(app);
    }

    /** 校验指定 IP 是否属于该应用下已注册的机器。 */
    public boolean isValidMachineOfApp(String app, String ip) {
        if (StringUtil.isEmpty(app)) {
            return false;
        }
        return Optional.ofNullable(getDetailApp(app))
            .flatMap(a -> a.getMachine(ip))
            .isPresent();
    }

}
