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

import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.dashboard.config.DashboardConfig;

/**
 * 已注册 Sentinel 应用及其机器集合。
 * <p>维护心跳状态，并依据 {@link DashboardConfig} 判定是否在侧栏展示或应被移除。
 */
public class AppInfo {

    /** 应用名称。 */
    private String app = "";

    /** 应用类型标识（客户端上报）。 */
    private Integer appType = 0;

    /** 当前应用下已注册的机器集合（线程安全）。 */
    private Set<MachineInfo> machines = ConcurrentHashMap.newKeySet();

    public AppInfo() {}

    public AppInfo(String app) {
        this.app = app;
    }

    public AppInfo(String app, Integer appType) {
        this.app = app;
        this.appType = appType;
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

    /**
     * 获取当前机器集合的副本，避免外部直接修改内部 Set。
     *
     * @return 机器信息副本
     */
    public Set<MachineInfo> getMachines() {
        return new HashSet<>(machines);
    }

    @Override
    public String toString() {
        return "AppInfo{" + "app='" + app + ", machines=" + machines + '}';
    }

    /** 添加或更新机器（同 app/ip/port 先移除再插入）。 */
    public boolean addMachine(MachineInfo machineInfo) {
        machines.remove(machineInfo);
        return machines.add(machineInfo);
    }

    /** 按 IP 与端口移除本应用下的机器。 */
    public synchronized boolean removeMachine(String ip, int port) {
        Iterator<MachineInfo> it = machines.iterator();
        while (it.hasNext()) {
            MachineInfo machine = it.next();
            if (machine.getIp().equals(ip) && machine.getPort() == port) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    /** 按 IP 与端口查找机器。 */
    public Optional<MachineInfo> getMachine(String ip, int port) {
        return machines.stream()
            .filter(e -> e.getIp().equals(ip) && e.getPort().equals(port))
            .findFirst();
    }

    /** 按 IP 查找第一台匹配机器（端口未指定时）。 */
    public Optional<MachineInfo> getMachine(String ip) {
        return machines.stream()
            .filter(e -> e.getIp().equals(ip))
            .findFirst();
    }

    /** 依据阈值判断应用是否仍“存活”（存在健康机器或最近心跳未超时）。 */
    private boolean heartbeatJudge(final int threshold) {
        if (machines.size() == 0) {
            return false;
        }
        if (threshold > 0) {
            long healthyCount = machines.stream()
                .filter(MachineInfo::isHealthy)
                .count();
            if (healthyCount == 0) {
                // 无健康机器时，看最近一次心跳是否仍在阈值内。
                return machines.stream()
                    .max(Comparator.comparingLong(MachineInfo::getLastHeartbeat))
                    .map(e -> System.currentTimeMillis() - e.getLastHeartbeat() < threshold)
                    .orElse(false);
            }
        }
        return true;
    }

    /**
     * 是否应在 Dashboard 侧栏展示。
     * <p>无健康机器且最后心跳超过 {@link DashboardConfig#getHideAppNoMachineMillis()} 时隐藏。
     *
     * @return true 表示应展示
     */
    public boolean isShown() {
        return heartbeatJudge(DashboardConfig.getHideAppNoMachineMillis());
    }

    /**
     * 是否应被从注册表移除（“死亡”应用）。
     *
     * @return true 表示应移除
     */
    public boolean isDead() {
        return !heartbeatJudge(DashboardConfig.getRemoveAppNoMachineMillis());
    }
}
