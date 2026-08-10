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

package com.alibaba.nacos.persistence.repository.embedded.hook;

import java.util.HashSet;
import java.util.Set;

/**
 * 嵌入式 Apply 钩子注册中心（单例）。
 *
 * <p>集中管理所有 {@link EmbeddedApplyHook} 实例，供存储层在日志 Apply 后统一遍历触发。</p>
 *
 * @author xiweng.yy
 */
public class EmbeddedApplyHookHolder {
    
    private static final EmbeddedApplyHookHolder INSTANCE = new EmbeddedApplyHookHolder();
    
    private final Set<EmbeddedApplyHook> hooks;
    
    private EmbeddedApplyHookHolder() {
        hooks = new HashSet<>();
    }
    
    /** 获取全局单例持有者。 */
    public static EmbeddedApplyHookHolder getInstance() {
        return INSTANCE;
    }
    
    /** 注册 Apply 完成钩子。 */
    public void register(EmbeddedApplyHook hook) {
        this.hooks.add(hook);
    }
    
    /** 返回已注册的全部钩子集合。 */
    public Set<EmbeddedApplyHook> getAllHooks() {
        return this.hooks;
    }
}
