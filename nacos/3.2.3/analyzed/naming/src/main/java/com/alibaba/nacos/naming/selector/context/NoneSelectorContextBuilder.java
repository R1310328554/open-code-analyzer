/*
 *  Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.alibaba.nacos.naming.selector.context;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.selector.context.SelectorContextBuilder;

import java.util.List;

/**
 * 空选择器上下文构建器。
 *
 * <p>无需额外资源的选择器直接以提供者列表作为上下文。</p>
 *
 * @author chenglu
 * @date 2021-08-04 13:31
 */
public class NoneSelectorContextBuilder<T extends Instance>
    implements SelectorContextBuilder<List<T>, String, List<T>> {
    
    /** 上下文类型：NONE。 */
    private static final String CONTEXT_TYPE = "NONE";
    
    @Override
    /** 忽略 consumer，直接返回 provider 列表。 */
    public List<T> build(String consumer, List<T> provider) {
        return provider;
    }
    
    @Override
    /** 返回上下文类型 NONE。 */
    public String getContextType() {
        return CONTEXT_TYPE;
    }
}
