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

package com.alibaba.nacos.naming.interceptor;

/**
 * 可被 Naming 拦截器链拦截的对象接口。
 *
 * <p>实现类在拦截链执行完毕后根据是否被拦截调用 {@link #passIntercept()} 或 {@link #afterIntercept()} 继续或中止后续逻辑。</p>
 *
 * @author xiweng.yy
 */
public interface Interceptable {
    
    /**
     * 无拦截器命中时调用，表示对象可正常执行后续逻辑。
     */
    void passIntercept();
    
    /**
     * 被某拦截器命中时调用，通常用于跳过默认执行路径。
     */
    void afterIntercept();
}
