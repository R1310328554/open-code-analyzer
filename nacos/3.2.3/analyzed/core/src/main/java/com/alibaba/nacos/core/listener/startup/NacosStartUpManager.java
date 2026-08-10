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

package com.alibaba.nacos.core.listener.startup;

import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.common.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Nacos 启动阶段管理器（单例），通过 SPI 加载各 {@link NacosStartUp} 实现并按阶段切换当前上下文。
 * <p>记录已启动阶段列表，失败时可逆序清理。</p>
 * Nacos start up phase manager.
 *
 * @author xiweng.yy
 */
public class NacosStartUpManager {
    
    /** 全局单例实例。 */
    private static final NacosStartUpManager INSTANCE = new NacosStartUpManager();
    
    /** 当前正在执行的启动阶段名称。 */
    private String currentStartUpPhase;
    
    /** 阶段名 → 启动实现 的映射表。 */
    private final Map<String, NacosStartUp> startUpMap;
    
    /** 按启动顺序记录已成功进入的阶段（用于逆序回滚）。 */
    private final List<NacosStartUp> startedList;
    
    /** 通过 SPI 加载所有 {@link NacosStartUp} 实现并建立阶段映射。 */
    private NacosStartUpManager() {
        startUpMap = new HashMap<>();
        for (NacosStartUp each : NacosServiceLoader.load(NacosStartUp.class)) {
            startUpMap.put(each.startUpPhase(), each);
        }
        startedList = new ArrayList<>(startUpMap.size());
    }
    
    /** 按阶段名查找启动实现，未知阶段返回 null。 */
    private NacosStartUp getStartUp(String phase) {
        return startUpMap.get(phase);
    }
    
    /**
     * 标记进入新的启动阶段，并将该阶段加入已启动列表。
     * @param phase phase name.
     * @throws IllegalArgumentException when phase is unknown.
     */
    public static void start(String phase) {
        NacosStartUp startUp = INSTANCE.getStartUp(phase);
        if (null == startUp) {
            throw new IllegalArgumentException("Unknown nacos start up phase " + phase);
        }
        INSTANCE.currentStartUpPhase = phase;
        INSTANCE.startedList.add(startUp);
    }
    
    /**
     * 获取当前启动阶段的 {@link NacosStartUp} 实现。
     * @return current start up phase.
     * @throws IllegalStateException when nacos not start up.
     */
    public static NacosStartUp getCurrentStartUp() {
        if (StringUtils.isBlank(INSTANCE.currentStartUpPhase)) {
            throw new IllegalStateException("Nacos don't start up.");
        }
        return INSTANCE.getStartUp(INSTANCE.currentStartUpPhase);
    }
    
    /**
     * 返回已启动阶段的逆序列表，便于失败时从后向前清理资源。
     * @return reversed nacos start up
     */
    public static List<NacosStartUp> getReverseStartedList() {
        List<NacosStartUp> result = new ArrayList<>(INSTANCE.startedList);
        Collections.reverse(result);
        return result;
    }
}
