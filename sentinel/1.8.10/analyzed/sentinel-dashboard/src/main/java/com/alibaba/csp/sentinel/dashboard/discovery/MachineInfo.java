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

import java.util.Objects;

import com.alibaba.csp.sentinel.dashboard.config.DashboardConfig;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * Sentinel 客户端机器实例信息，含心跳时间与客户端版本。
 * <p>实现 {@link Comparable} 以便在集合中按端口、应用名、IP 排序。
 */
public class MachineInfo implements Comparable<MachineInfo> {

    /** 所属应用名。 */
    private String app = "";
    /** 应用类型。 */
    private Integer appType = 0;
    /** 主机名。 */
    private String hostname = "";
    /** 机器 IP。 */
    private String ip = "";
    /** 客户端端口，-1 表示尚未就绪。 */
    private Integer port = -1;
    /** 最后一次心跳时间戳（毫秒）。 */
    private long lastHeartbeat;
    /** 心跳协议版本号。 */
    private long heartbeatVersion;

    /** Sentinel 客户端版本号（0.2.0 起上报）。 */
    private String version;

    /** 快速构造仅含 app/ip/port 的机器信息。 */
    public static MachineInfo of(String app, String ip, Integer port) {
        MachineInfo machineInfo = new MachineInfo();
        machineInfo.setApp(app);
        machineInfo.setIp(ip);
        machineInfo.setPort(port);
        return machineInfo;
    }

    /** @return {@code ip:port} 形式的主机端口字符串 */
    public String toHostPort() {
        return ip + ":" + port;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public Integer getAppType() {
        return appType;
    }

    public void setAppType(Integer appType) {
        this.appType = appType;
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

    public long getHeartbeatVersion() {
        return heartbeatVersion;
    }
    
    public void setHeartbeatVersion(long heartbeatVersion) {
        this.heartbeatVersion = heartbeatVersion;
    }

    public String getVersion() {
        return version;
    }

    public MachineInfo setVersion(String version) {
        this.version = version;
        return this;
    }
    
    /** 距上次心跳是否在 {@link DashboardConfig#getUnhealthyMachineMillis()} 以内。 */
    public boolean isHealthy() {
        long delta = System.currentTimeMillis() - lastHeartbeat;
        return delta < DashboardConfig.getUnhealthyMachineMillis();
    }
    
    /**
     * 是否超过自动移除阈值，应被从注册表清理。
     *
     * @return true 表示已“死亡”且应移除
     */
    public boolean isDead() {
        if (DashboardConfig.getAutoRemoveMachineMillis() > 0) {
            long delta = System.currentTimeMillis() - lastHeartbeat;
            return delta > DashboardConfig.getAutoRemoveMachineMillis();
        }
        return false;
    }
    
    public long getLastHeartbeat() {
        return lastHeartbeat;
    }
    
    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    /** 先比端口，再比应用名（忽略大小写），最后比 IP。 */
    @Override
    public int compareTo(MachineInfo o) {
        if (this == o) {
            return 0;
        }
        if (!port.equals(o.getPort())) {
            return port.compareTo(o.getPort());
        }
        if (!StringUtil.equals(app, o.getApp())) {
            return app.compareToIgnoreCase(o.getApp());
        }
        return ip.compareToIgnoreCase(o.getIp());
    }

    @Override
    public String toString() {
        return new StringBuilder("MachineInfo {")
            .append("app='").append(app).append('\'')
            .append(",appType='").append(appType).append('\'')
            .append(", hostname='").append(hostname).append('\'')
            .append(", ip='").append(ip).append('\'')
            .append(", port=").append(port)
            .append(", heartbeatVersion=").append(heartbeatVersion)
            .append(", lastHeartbeat=").append(lastHeartbeat)
            .append(", version='").append(version).append('\'')
            .append(", healthy=").append(isHealthy())
            .append('}').toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof MachineInfo)) { return false; }
        MachineInfo that = (MachineInfo)o;
        return Objects.equals(app, that.app) &&
            Objects.equals(ip, that.ip) &&
            Objects.equals(port, that.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(app, ip, port);
    }

    /**
     * 生成日志友好的单行描述。
     *
     * @return {@code app|ip|port|version}
     */
    public String toLogString() {
        return app + "|" + ip + "|" + port + "|" + version;
    }
}
