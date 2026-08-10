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

package com.alibaba.nacos.client.utils;

import com.alibaba.nacos.client.auth.ram.utils.SpasAdapter;
import com.alibaba.nacos.common.utils.JacksonUtils;

/**
 * Async do pre init to load some cost component.
 * <p>异步预初始化工具：在后台线程预热 Jackson {@code ObjectMapper} 与 RAM {@link SpasAdapter} 凭证读取，缩短首次 RPC/鉴权请求的冷启动延迟。</p>
 *
 * <ul>
 *     <li>JacksonUtil</li>
 *     <li>SpasAdapter</li>
 * </ul>
 *
 * @author xiweng.yy
 */
public class PreInitUtils {
    
    /**
     * Async pre load cost component.
     * <p>启动守护线程执行 {@link JacksonUtils#createEmptyJsonNode()} 与 {@link SpasAdapter#getAk()}，不阻塞主线程。</p>
     */
    public static void asyncPreLoadCostComponent() {
        Thread preLoadThread = new Thread(() -> {
            // 触发 Jackson 静态 ObjectMapper 初始化（通常耗时数百毫秒）
            JacksonUtils.createEmptyJsonNode();
            // 预热 RAM 插件从环境/系统读取 AK 的路径，避免首次鉴权卡顿
            SpasAdapter.getAk();
        });
        preLoadThread.start();
    }
}
