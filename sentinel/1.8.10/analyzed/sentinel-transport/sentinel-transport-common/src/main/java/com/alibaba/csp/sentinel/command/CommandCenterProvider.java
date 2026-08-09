/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.command;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.transport.CommandCenter;
import com.alibaba.csp.sentinel.spi.SpiLoader;

/**
 * {@link CommandCenter} 单例提供者：类加载时通过 {@link SpiLoader}
 * 解析优先级最高的传输层命令中心实现。
 *
 * @author cdfive
 * @since 1.5.0
 */
public final class CommandCenterProvider {

    private static CommandCenter commandCenter = null;

    static {
        resolveInstance();
    }

    /** 从 SPI 加载并缓存 {@link CommandCenter} 实例。 */
    private static void resolveInstance() {
        CommandCenter resolveCommandCenter = SpiLoader.of(CommandCenter.class).loadHighestPriorityInstance();

        if (resolveCommandCenter == null) {
            RecordLog.warn("[CommandCenterProvider] WARN: No existing CommandCenter found");
        } else {
            commandCenter = resolveCommandCenter;
            RecordLog.info("[CommandCenterProvider] CommandCenter resolved: {}", resolveCommandCenter.getClass()
                .getCanonicalName());
        }
    }

    /**
     * 获取已解析的 {@link CommandCenter} 实例。
     *
     * @return resolved {@code CommandCenter} instance
     */
    public static CommandCenter getCommandCenter() {
        return commandCenter;
    }

    private CommandCenterProvider() {}
}
