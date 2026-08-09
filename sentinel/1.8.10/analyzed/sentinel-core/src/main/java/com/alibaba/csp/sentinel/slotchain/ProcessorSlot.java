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
package com.alibaba.csp.sentinel.slotchain;

import com.alibaba.csp.sentinel.context.Context;

/**
 * 处理器槽接口：封装一段处理逻辑，并提供 entry/exit 完成后的链式通知机制。
 *
 * @author qinan.qn
 * @author jialiang.linjl
 * @author leyou(lihao)
 * @author Eric Zhao
 */
public interface ProcessorSlot<T> {

    /**
     * 本槽位的入口处理。
     *
     * @param context         当前 {@link Context}
     * @param resourceWrapper 当前资源
     * @param param           泛型参数，通常为 {@link com.alibaba.csp.sentinel.node.Node}
     * @param count           所需令牌数
     * @param prioritized     是否为优先级入口
     * @param args            原始调用的参数
     * @throws Throwable 被阻断或发生意外错误
     */
    void entry(Context context, ResourceWrapper resourceWrapper, T param, int count, boolean prioritized,
               Object... args) throws Throwable;

    /**
     * 表示本槽位 {@link #entry(Context, ResourceWrapper, Object, int, boolean, Object...)} 处理完成，
     * 继续触发后续槽位。
     *
     * @param context         当前 {@link Context}
     * @param resourceWrapper 当前资源
     * @param obj             相关对象（如 Node）
     * @param count           所需令牌数
     * @param prioritized     是否为优先级入口
     * @param args            原始调用的参数
     * @throws Throwable 被阻断或发生意外错误
     */
    void fireEntry(Context context, ResourceWrapper resourceWrapper, Object obj, int count, boolean prioritized,
                   Object... args) throws Throwable;

    /**
     * 本槽位的退出处理。
     *
     * @param context         当前 {@link Context}
     * @param resourceWrapper 当前资源
     * @param count           所需令牌数
     * @param args            原始调用的参数
     */
    void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args);

    /**
     * 表示本槽位 {@link #exit(Context, ResourceWrapper, int, Object...)} 处理完成，
     * 继续触发后续槽位。
     *
     * @param context         当前 {@link Context}
     * @param resourceWrapper 当前资源
     * @param count           所需令牌数
     * @param args            原始调用的参数
     */
    void fireExit(Context context, ResourceWrapper resourceWrapper, int count, Object... args);
}
