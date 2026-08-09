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

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;

import static com.alibaba.dubbo.common.Constants.CONSUMER;

/**
 * 将当前消费者的应用名写入每次调用的 attachment，供 Provider 端解析调用来源。
 *
 * @author Eric Zhao
 */
@Activate(group = CONSUMER)
public class DubboAppContextFilter implements Filter {

    /** 在调用前将消费者应用名写入 RpcContext attachment。 */
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String application = invoker.getUrl().getParameter(Constants.APPLICATION_KEY);
        if (application != null) {
            RpcContext.getContext().setAttachment(DubboUtils.DUBBO_APPLICATION_KEY, application);
        }
        return invoker.invoke(invocation);
    }
}
