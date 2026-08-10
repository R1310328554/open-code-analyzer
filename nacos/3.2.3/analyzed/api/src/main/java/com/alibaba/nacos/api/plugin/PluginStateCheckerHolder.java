/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.plugin;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link PluginStateChecker} 静态持有者。
 *
 * <p>桥接单例模式插件 Manager 与 Spring 管理的 UnifiedPluginManager；
 * 使用 {@link AtomicReference} 保证线程安全。</p>
 *
 * @author WangzJi
 * @since 3.2.0
 */
public class PluginStateCheckerHolder {
    
    /** 全局 PluginStateChecker 实例引用。 */
    private static final AtomicReference<PluginStateChecker> INSTANCE = new AtomicReference<>();
    
    /** 私有构造，禁止实例化。 */
    private PluginStateCheckerHolder() {
    }
    
    /**
     * 设置 PluginStateChecker 实例。
     *
     * @param checker PluginStateChecker 实例
     */
    public static void setInstance(PluginStateChecker checker) {
        INSTANCE.set(checker);
    }
    
    /**
     * 获取 PluginStateChecker 实例。
     *
     * @return 包含实例的 Optional，未设置时为空
     */
    public static Optional<PluginStateChecker> getInstance() {
        return Optional.ofNullable(INSTANCE.get());
    }
    
    /**
     * 检查插件是否已启用。
     * 未设置 checker 时返回 true（向后兼容）。
     *
     * @param pluginType 插件类型字符串
     * @param pluginName 插件名称
     * @return 已启用或未设置 checker 时返回 true
     */
    public static boolean isPluginEnabled(String pluginType, String pluginName) {
        PluginStateChecker checker = INSTANCE.get();
        if (checker == null) {
            // 无 checker 时默认视为启用，保持旧版行为
            return true;
        }
        return checker.isPluginEnabled(pluginType, pluginName);
    }
}
