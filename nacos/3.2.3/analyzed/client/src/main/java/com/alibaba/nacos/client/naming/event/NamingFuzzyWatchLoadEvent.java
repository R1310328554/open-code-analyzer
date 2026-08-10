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

package com.alibaba.nacos.client.naming.event;

import com.alibaba.nacos.common.notify.Event;

/**
 * 命名模糊监听负载/超限通知事件。
 *
 * <p>当模糊匹配模式或匹配服务数超限时，由 {@link NamingFuzzyWatchContext} 发布，通知 {@link com.alibaba.nacos.api.naming.listener.FuzzyWatchLoadWatcher} 回调。</p>
 *
 * @author shiyiyue
 * @date 2025/01/13
 */
public class NamingFuzzyWatchLoadEvent extends Event {
    
    /** 事件作用域。 */
    private String eventScope;
    
    /** 触发通知的 groupKey 模糊匹配模式。 */
    /**
     * The groupKeyPattern of configuration.
      * <p>模糊监听负载事件；详见类级说明。</p>
     */
    private String groupKeyPattern;
    
    /** 通知类型码（如模式超限、匹配数超限）。 */
    private int code;
    
    /**
     * 私有构造，通过工厂方法创建。
     *
     * @param code            通知类型码
     * @param groupKeyPattern groupKey 匹配模式
     */
    private NamingFuzzyWatchLoadEvent(int code, String groupKeyPattern, String eventScope) {
        this.code = code;
        this.groupKeyPattern = groupKeyPattern;
        this.eventScope = eventScope;
    }
    
    /**
     * 构建模糊监听负载通知事件。
     *
     * @param groupKeyPattern groupKey 模式
     * @return 新事件实例
     */
    public static NamingFuzzyWatchLoadEvent buildEvent(int code, String groupKeyPattern,
        String scope) {
        return new NamingFuzzyWatchLoadEvent(code, groupKeyPattern, scope);
    }
    
    /** 返回事件作用域。 */
    @Override
    public String scope() {
        return eventScope;
    }
    
    /** 获取 groupKey 匹配模式。 */
    public String getGroupKeyPattern() {
        return groupKeyPattern;
    }
    
    /** 获取通知类型码。 */
    public int getCode() {
        return code;
    }
}
