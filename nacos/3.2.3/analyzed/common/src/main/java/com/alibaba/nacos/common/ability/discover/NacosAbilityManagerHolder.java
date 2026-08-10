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

package com.alibaba.nacos.common.ability.discover;

import com.alibaba.nacos.common.ability.AbstractAbilityControlManager;
import com.alibaba.nacos.common.spi.NacosServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is used to discover {@link AbstractAbilityControlManager} implements. All the ability operation will be
 * finish in this singleton.
 * <p>通过 SPI 发现并持有全局唯一的 {@link AbstractAbilityControlManager} 实例，按 {@link AbstractAbilityControlManager#getPriority()} 选取优先级最高实现。</p>
 *
 * @author Daydreamer
 * @date 2022/7/14 19:58
 **/
public class NacosAbilityManagerHolder {
    
    /**
     * . private constructor
     * <p>私有构造，禁止外部实例化。</p>
     */
    private NacosAbilityManagerHolder() {
    }
    
    /** 本类日志记录器 */
    private static final Logger LOGGER = LoggerFactory.getLogger(NacosAbilityManagerHolder.class);
    
    /**
     * . singleton
     * <p>懒加载的全局能力管理器实例。</p>
     */
    private static AbstractAbilityControlManager abstractAbilityControlManager;
    
    /**
     * . get nacos ability control manager
     * <p>线程安全地获取能力管理器单例，首次调用时触发 SPI 初始化。</p>
     *
     * @return BaseAbilityControlManager
     */
    public static synchronized AbstractAbilityControlManager getInstance() {
        if (null == abstractAbilityControlManager) {
            initAbilityControlManager();
        }
        return abstractAbilityControlManager;
    }
    
    /**
     * . Return the target type of ability manager
     * <p>将单例强转为指定子类型，便于调用方使用扩展 API。</p>
     *
     * @param clazz clazz
     * @param <T>   target type
     * @return AbilityControlManager
     */
    public static <T extends AbstractAbilityControlManager> T getInstance(Class<T> clazz) {
        return clazz.cast(abstractAbilityControlManager);
    }
    
    private static void initAbilityControlManager() {
        // 通过 SPI 加载所有 AbstractAbilityControlManager 实现
        Collection<AbstractAbilityControlManager> load = null;
        load = NacosServiceLoader.load(AbstractAbilityControlManager.class);
        // 按优先级升序排序，服务端实现通常优先级更高
        List<AbstractAbilityControlManager> collect = load.stream()
            .sorted(Comparator.comparingInt(AbstractAbilityControlManager::getPriority))
            .collect(Collectors.toList());
        // 取排序后最后一个（优先级最高）作为全局实例
        if (load.size() > 0) {
            abstractAbilityControlManager = collect.get(collect.size() - 1);
            LOGGER.info("[AbilityControlManager] Successfully initialize AbilityControlManager");
        }
    }
}
