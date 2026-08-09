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
package com.alibaba.csp.sentinel.dashboard.domain.vo;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;

/**
 * 机器信息视图对象，供 Dashboard API 返回客户端实例列表。
 * <p>由 {@link MachineInfo} 转换而来，含心跳与健康状态。
 *
 * @author leyou
 */
public class MachineInfoVo {

    /** 所属应用名。 */
    private String app;
    /** 主机名。 */
    private String hostname;
    /** 机器 IP。 */
    private String ip;
    /** 客户端端口。 */
    private int port;
    /** 心跳协议版本号。 */
    private long heartbeatVersion;
    /** 最后一次心跳时间戳（毫秒）。 */
    private long lastHeartbeat;
    /** 当前是否健康（心跳未超时）。 */
    private boolean healthy;

    /** Sentinel 客户端版本号。 */
    private String version;

    /** 批量将 {@link MachineInfo} 列表转换为 VO 列表。 */
    public static List<MachineInfoVo> fromMachineInfoList(List<MachineInfo> machines) {
        List<MachineInfoVo> list = new ArrayList<>();
        for (MachineInfo machine : machines) {
            list.add(fromMachineInfo(machine));
        }
        return list;
    }

    /** 从单条 {@link MachineInfo} 拷贝字段构建 VO。 */
    public static MachineInfoVo fromMachineInfo(MachineInfo machine) {
        MachineInfoVo vo = new MachineInfoVo();
        vo.setApp(machine.getApp());
        vo.setHostname(machine.getHostname());
        vo.setIp(machine.getIp());
        vo.setPort(machine.getPort());
        vo.setLastHeartbeat(machine.getLastHeartbeat());
        vo.setHeartbeatVersion(machine.getHeartbeatVersion());
        vo.setVersion(machine.getVersion());
        vo.setHealthy(machine.isHealthy());
        return vo;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }
    
    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }
    
    public void setHeartbeatVersion(long heartbeatVersion) {
        this.heartbeatVersion = heartbeatVersion;
    }
    
    public long getHeartbeatVersion() {
        return heartbeatVersion;
    }

    public String getVersion() {
        return version;
    }

    public MachineInfoVo setVersion(String version) {
        this.version = version;
        return this;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }
}
