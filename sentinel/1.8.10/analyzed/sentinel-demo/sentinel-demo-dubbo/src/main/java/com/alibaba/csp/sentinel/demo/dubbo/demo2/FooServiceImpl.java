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
package com.alibaba.csp.sentinel.demo.dubbo.demo2;

import java.time.LocalDateTime;

import com.alibaba.csp.sentinel.demo.dubbo.FooService;
import com.alibaba.dubbo.config.annotation.Service;

/**
 * {@link com.alibaba.csp.sentinel.demo.dubbo.FooService} Provider 实现（demo2）。
 *
 * @author Eric Zhao
 */
@Service
public class FooServiceImpl implements FooService {

    /** 返回带当前时间的问候语。 */
    @Override
    public String sayHello(String name) {
        return String.format("Hello, %s at %s", name, LocalDateTime.now());
    }

    /** 返回当前时间字符串，Consumer 压测时不受 sayHello 流控影响。 */
    @Override
    public String doAnother() {
        return LocalDateTime.now().toString();
    }
}
