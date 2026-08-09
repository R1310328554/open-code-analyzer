/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.dubbo3.origin;

import com.alibaba.csp.sentinel.context.Context;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;

/**
 * Dubbo Provider 过滤器的自定义来源解析器，结果写入 {@link Context#getOrigin()}。
 *
 * @author jingzian
 */
public interface DubboOriginParser {

    /**
     * 从 Dubbo 调用中解析来源（调用方）。
     *
     * @param invoker    Dubbo invoker
     * @param invocation Dubbo invocation
     * @return 解析出的来源标识
     */
    String parse(Invoker<?> invoker, Invocation invocation);

}
