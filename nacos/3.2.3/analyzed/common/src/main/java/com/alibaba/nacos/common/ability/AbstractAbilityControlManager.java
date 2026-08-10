/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.common.ability;

import com.alibaba.nacos.api.ability.constant.AbilityKey;
import com.alibaba.nacos.api.ability.constant.AbilityMode;
import com.alibaba.nacos.api.ability.constant.AbilityStatus;
import com.alibaba.nacos.api.ability.initializer.AbilityPostProcessor;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.spi.NacosServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * It is a capability control center, manager current node abilities or other control.
 * <p>能力控制中心抽象基类：维护当前节点各 {@link AbilityMode} 下的能力开关表，支持启用/禁用能力并通过 {@link NotifyCenter} 发布 {@link AbilityUpdateEvent} 通知订阅方。</p>
 *
 * @author Daydreamer
 * @date 2022/7/12 19:18
 **/
public abstract class AbstractAbilityControlManager {
    
    private static final Logger LOGGER =
        LoggerFactory.getLogger(AbstractAbilityControlManager.class);
    
    /**
     * current node support abilities.
     * <p>当前节点支持的能力表：外层键为 {@link AbilityMode}，内层为能力名到是否启用的映射。</p>
     */
    protected final Map<AbilityMode, Map<String, Boolean>> currentNodeAbilities =
        new ConcurrentHashMap<>();
    
    /** 注册能力变更事件发布器并初始化能力表 */
    protected AbstractAbilityControlManager() {
        NotifyCenter.registerToPublisher(AbilityUpdateEvent.class, 16384);
        initAbilityTable();
    }
    
    /**
     * initialize abilities.
     * <p>加载 SPI 后处理器、校验 {@link AbilityKey} 与模式一致，并填充 {@link #currentNodeAbilities}。</p>
     */
    private void initAbilityTable() {
        LOGGER.info("Ready to get current node abilities...");
        // 由子类提供各模式下的能力初始表
        Map<AbilityMode, Map<AbilityKey, Boolean>> abilities = initCurrentNodeAbilities();
        // 遍历各能力模式并执行后处理
        for (AbilityMode mode : AbilityMode.values()) {
            Map<AbilityKey, Boolean> abilitiesTable = abilities.get(mode);
            if (abilitiesTable == null) {
                continue;
            }
            // 校验能力键所属模式是否与当前模式一致
            // 供开发阶段尽早发现配置错误
            for (AbilityKey abilityKey : abilitiesTable.keySet()) {
                if (!mode.equals(abilityKey.getMode())) {
                    LOGGER.error(
                        "You should not contain a other mode: {} in a specify mode: {} abilities set, error key: {}, please check again.",
                        abilityKey.getMode(), mode, abilityKey);
                    throw new IllegalStateException(
                        "Except mode: " + mode + " but " + abilityKey + " mode: "
                            + abilityKey.getMode()
                            + ", please check again.");
                }
            }
            Collection<AbilityPostProcessor> processors =
                NacosServiceLoader.load(AbilityPostProcessor.class);
            for (AbilityPostProcessor processor : processors) {
                processor.process(mode, abilitiesTable);
            }
        }
        // 将校验后的能力表写入 currentNodeAbilities
        Set<AbilityMode> abilityModes = abilities.keySet();
        LOGGER.info("Ready to initialize current node abilities, support modes: {}", abilityModes);
        for (AbilityMode abilityMode : abilityModes) {
            this.currentNodeAbilities
                .put(abilityMode,
                    new ConcurrentHashMap<>(AbilityKey.mapStr(abilities.get(abilityMode))));
        }
        LOGGER.info("Initialize current abilities finish...");
    }
    
    /**
     * Turn on the ability whose key is <p>abilityKey</p>.
     * <p>开启指定 {@link AbilityKey} 对应的本节点能力。</p>
     *
     * @param abilityKey ability key{@link AbilityKey}
     */
    public void enableCurrentNodeAbility(AbilityKey abilityKey) {
        Map<String, Boolean> abilities = this.currentNodeAbilities.get(abilityKey.getMode());
        if (abilities != null) {
            doTurn(abilities, abilityKey, true);
        }
    }
    
    protected void doTurn(Map<String, Boolean> abilities, AbilityKey key, boolean turn) {
        LOGGER.info("Turn current node ability: {}, turn: {}", key, turn);
        abilities.put(key.getName(), turn);
        // 构造并发布能力变更事件
        AbilityUpdateEvent abilityUpdateEvent = new AbilityUpdateEvent();
        abilityUpdateEvent.setTable(Collections.unmodifiableMap(abilities));
        abilityUpdateEvent.setOn(turn);
        abilityUpdateEvent.setAbilityKey(key);
        NotifyCenter.publishEvent(abilityUpdateEvent);
    }
    
    /**
     * Turn off the ability whose key is <p>abilityKey</p> {@link AbilityKey}.
     * <p>关闭指定 {@link AbilityKey} 对应的本节点能力。</p>
     *
     * @param abilityKey ability key
     */
    public void disableCurrentNodeAbility(AbilityKey abilityKey) {
        Map<String, Boolean> abilities = this.currentNodeAbilities.get(abilityKey.getMode());
        if (abilities != null) {
            doTurn(abilities, abilityKey, false);
        }
    }
    
    /**
     * . Whether current node support
     * <p>查询本节点是否运行（支持）指定能力，未知时返回 {@link AbilityStatus#UNKNOWN}。</p>
     *
     * @param abilityKey ability key from {@link AbilityKey}
     * @return whether support
     */
    public AbilityStatus isCurrentNodeAbilityRunning(AbilityKey abilityKey) {
        Map<String, Boolean> abilities = currentNodeAbilities.get(abilityKey.getMode());
        if (abilities != null) {
            Boolean support = abilities.get(abilityKey.getName());
            if (support != null) {
                return support ? AbilityStatus.SUPPORTED : AbilityStatus.NOT_SUPPORTED;
            }
        }
        return AbilityStatus.UNKNOWN;
    }
    
    /**
     * . Init current node abilities
     * <p>由子类实现：返回各 {@link AbilityMode} 下本节点初始能力开关表。</p>
     *
     * @return current node abilities
     */
    protected abstract Map<AbilityMode, Map<AbilityKey, Boolean>> initCurrentNodeAbilities();
    
    /**
     * . Return the abilities current node
     * <p>返回指定模式下本节点能力表的只读视图，无数据时返回空 Map。</p>
     *
     * @return current abilities
     */
    public Map<String, Boolean> getCurrentNodeAbilities(AbilityMode mode) {
        Map<String, Boolean> abilities = currentNodeAbilities.get(mode);
        if (abilities != null) {
            return Collections.unmodifiableMap(abilities);
        }
        return Collections.emptyMap();
    }
    
    /**
     * A legal nacos application has a ability control manager. If there are more than one, the one with higher priority
     * is preferred
     * <p>能力管理器优先级；存在多个实现时数值更大者优先被 {@link NacosAbilityManagerHolder} 选用。</p>
     *
     * @return priority
     */
    public abstract int getPriority();
    
    /**
     * notify when current node ability changing.
     * <p>本节点能力开关变更时通过 {@link NotifyCenter} 发布的事件。</p>
     */
    public static class AbilityUpdateEvent extends Event {
        
        /** 序列化版本号 */
        private static final long serialVersionUID = -1232411212311111L;
        
        /** 变更的能力键 */
        private AbilityKey abilityKey;
        
        /** 变更后是否为开启状态 */
        private boolean isOn;
        
        /** 变更后的能力表快照（只读语义由调用方保证） */
        private Map<String, Boolean> table;
        
        private AbilityUpdateEvent() {
        }
        
        public Map<String, Boolean> getAbilityTable() {
            return table;
        }
        
        public void setTable(Map<String, Boolean> abilityTable) {
            this.table = abilityTable;
        }
        
        public AbilityKey getAbilityKey() {
            return abilityKey;
        }
        
        public void setAbilityKey(AbilityKey abilityKey) {
            this.abilityKey = abilityKey;
        }
        
        public boolean isOn() {
            return isOn;
        }
        
        public void setOn(boolean on) {
            isOn = on;
        }
    }
}
