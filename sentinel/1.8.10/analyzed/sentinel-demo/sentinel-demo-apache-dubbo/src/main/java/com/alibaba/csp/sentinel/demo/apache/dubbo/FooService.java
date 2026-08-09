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
package com.alibaba.csp.sentinel.demo.apache.dubbo;

/**
 * Dubbo 演示 RPC 服务接口。
 *
 * @author Eric Zhao
 */
public interface FooService {

    /** 问候调用，作为流控/熔断主要测试资源。 */
    String sayHello(String name);

    /** 备用 RPC 方法，Consumer 被限流时可作为降级调用。 */
    String doAnother();

    /** 异常/超时测试：biz 为 true 抛业务异常，timeout 为 true 模拟慢调用。 */
    String exceptionTest(boolean biz, boolean timeout);
}
