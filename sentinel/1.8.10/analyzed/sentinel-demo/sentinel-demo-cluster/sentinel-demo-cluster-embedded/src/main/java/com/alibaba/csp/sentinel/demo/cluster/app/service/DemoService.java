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
package com.alibaba.csp.sentinel.demo.cluster.app.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import org.springframework.stereotype.Service;

/**
 * 集群演示业务服务：{@link SentinelResource} 绑定 blockHandler 处理限流。
 *
 * @author Eric Zhao
 */
@Service
public class DemoService {

    /** 问候资源，被限流时调用 sayHelloBlockHandler。 */
    @SentinelResource(blockHandler = "sayHelloBlockHandler")
    public String sayHello(String name) {
        return "Hello, " + name;
    }

    /** sayHello 的 blockHandler：打印异常并返回友好提示。 */
    public String sayHelloBlockHandler(String name, BlockException ex) {
        ex.printStackTrace();
        return String.format("Oops, <%s> blocked by Sentinel", name);
    }
}
