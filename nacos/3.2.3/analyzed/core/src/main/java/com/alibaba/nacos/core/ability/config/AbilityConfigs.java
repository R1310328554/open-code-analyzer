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

package com.alibaba.nacos.core.ability.config;

import com.alibaba.nacos.api.ability.constant.AbilityKey;
import com.alibaba.nacos.api.ability.register.impl.ServerAbilities;
import com.alibaba.nacos.common.JustForTest;
import com.alibaba.nacos.common.ability.AbstractAbilityControlManager;
import com.alibaba.nacos.common.ability.discover.NacosAbilityManagerHolder;
import com.alibaba.nacos.common.event.ServerConfigChangeEvent;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.notify.listener.Subscriber;
import com.alibaba.nacos.common.utils.ConcurrentHashSet;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 服务端能力动态配置订阅者：监听 {@link ServerConfigChangeEvent}，从环境变量刷新各 {@link AbilityKey} 开关。
 * Dynamically load ability from config.
 *
 * @author Daydreamer
 * @date 2022/8/31 12:27
 */
@Configuration
public class AbilityConfigs extends Subscriber<ServerConfigChangeEvent> {
    
    /** 能力配置项环境变量前缀。 */
    public static final String PREFIX = "nacos.core.ability.";
    
    /** 日志记录器。 */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbilityConfigs.class);
    
    /** 可动态刷新的服务端能力键集合。 */
    private final Set<AbilityKey> serverAbilityKeys = new ConcurrentHashSet<>();
    
    /** 能力控制管理器，用于启停当前节点能力。 */
    private AbstractAbilityControlManager abilityHandlerRegistry =
        NacosAbilityManagerHolder.getInstance();
    
    /** 构造时加载静态能力键并注册配置变更订阅。 */
    public AbilityConfigs() {
        // 初始化可配置的能力键集合
        serverAbilityKeys.addAll(ServerAbilities.getStaticAbilities().keySet());
        NotifyCenter.registerSubscriber(this);
    }
    
    /** 配置变更时扫描环境变量并刷新能力开关。 */
    @Override
    public void onEvent(ServerConfigChangeEvent event) {
        // 从环境变量读取各能力配置
        Map<AbilityKey, Boolean> newValues = new HashMap<>(serverAbilityKeys.size());
        serverAbilityKeys.forEach(abilityKey -> {
            String key = PREFIX + abilityKey.getName();
            try {
                // 按 PREFIX + abilityKey 扫描
                Boolean property = EnvUtil.getProperty(key, Boolean.class);
                if (property != null) {
                    newValues.put(abilityKey, property);
                }
            } catch (Exception e) {
                LOGGER.warn(
                    "Update ability config from env failed, use old val, ability : {} , because : {}",
                    key, e);
            }
        });
        // 批量刷新能力状态
        refresh(newValues);
    }
    
    /** 根据新值启用或禁用当前节点对应能力。 */
    private void refresh(Map<AbilityKey, Boolean> newValues) {
        newValues.forEach((abilityKey, val) -> {
            // 根据布尔值调用 enable/disable
            if (val) {
                abilityHandlerRegistry.enableCurrentNodeAbility(abilityKey);
            } else {
                abilityHandlerRegistry.disableCurrentNodeAbility(abilityKey);
            }
        });
    }
    
    /** 订阅 {@link ServerConfigChangeEvent} 类型。 */
    @Override
    public Class<? extends Event> subscribeType() {
        return ServerConfigChangeEvent.class;
    }
    
    /** 测试用：获取服务端能力键集合。 */
    @JustForTest
    protected Set<AbilityKey> getServerAbilityKeys() {
        return serverAbilityKeys;
    }
    
    /** 测试用：注入能力控制管理器。 */
    @JustForTest
    protected void setAbilityHandlerRegistry(AbstractAbilityControlManager abilityHandlerRegistry) {
        this.abilityHandlerRegistry = abilityHandlerRegistry;
    }
    
}
