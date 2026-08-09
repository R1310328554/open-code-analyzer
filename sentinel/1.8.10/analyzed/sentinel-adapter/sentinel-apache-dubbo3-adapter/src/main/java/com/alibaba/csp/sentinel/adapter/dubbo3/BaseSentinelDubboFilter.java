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
package com.alibaba.csp.sentinel.adapter.dubbo3;


import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;

/**
 * {@link SentinelDubboProviderFilter} 与 {@link SentinelDubboConsumerFilter} 的基类。
 *
 * @author Zechao Zheng
 */
public abstract class BaseSentinelDubboFilter {


    /**
     * 获取 Dubbo RPC 方法资源名。
     *
     * @param invoker Dubbo invoker
     * @param invocation Dubbo invocation
     * @param prefix 资源名前缀
     * @return 方法资源名
     */
    abstract String getMethodName(Invoker invoker, Invocation invocation, String prefix);

    /**
     * 获取 Dubbo RPC 接口资源名。
     *
     * @param invoker Dubbo invoker
     * @param prefix 资源名前缀
     * @return 接口资源名
     */
    abstract String getInterfaceName(Invoker invoker, String prefix);


}
