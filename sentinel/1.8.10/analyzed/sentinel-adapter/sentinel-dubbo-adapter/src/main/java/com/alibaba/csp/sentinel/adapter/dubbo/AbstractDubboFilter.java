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
package com.alibaba.csp.sentinel.adapter.dubbo;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;

/**
 * Dubbo 旧版适配器 Filter 抽象基类，提供接口与方法级 Sentinel 资源名构建。
 *
 * @author leyou
 */
abstract class AbstractDubboFilter implements Filter {

    /** 构建方法级资源名：接口名:方法名(参数类型列表)。 */
    protected String getMethodResourceName(Invoker<?> invoker, Invocation invocation) {
        StringBuilder buf = new StringBuilder(64);
        buf.append(invoker.getInterface().getName())
            .append(":")
            .append(invocation.getMethodName())
            .append("(");
        boolean isFirst = true;
        for (Class<?> clazz : invocation.getParameterTypes()) {
            if (!isFirst) {
                buf.append(",");
            }
            buf.append(clazz.getName());
            isFirst = false;
        }
        buf.append(")");
        return buf.toString();
    }

    /** 构建带前缀的方法级资源名。 */
    protected String getMethodResourceName(Invoker<?> invoker, Invocation invocation, String prefix) {
        if (StringUtil.isBlank(prefix)) {
            return getMethodResourceName(invoker, invocation);
        }
        StringBuilder buf = new StringBuilder(64);
        return buf.append(prefix)
            .append(getMethodResourceName(invoker, invocation))
            .toString();
    }

    /** 获取 Dubbo 接口全限定名作为资源名。 */
    protected String getInterfaceName(Invoker<?> invoker) {
        return invoker.getInterface().getName();
    }

    /** 获取带前缀的接口级资源名。 */
    protected String getInterfaceName(Invoker<?> invoker, String prefix) {
        if (StringUtil.isBlank(prefix)) {
            return getInterfaceName(invoker);
        }
        return prefix + getInterfaceName(invoker);
    }
}
