/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.demo.annotation.cdi.interceptor;

import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * CDI 演示用 BlockException 处理工具类，供 {@code @SentinelResourceBinding} 的 blockHandler 引用。
 *
 * @author Eric Zhao
 */
public final class ExceptionUtil {

    public static void handleException(BlockException ex) {
        // 被限流时调用的 blockHandler：参数列表与原方法一致，末尾追加 BlockException；
        // 返回类型须与原方法相同；默认须与原方法同类，跨类引用时需 blockHandlerClass 且方法为 static。
        System.out.println("Oops: " + ex.getClass().getCanonicalName());
    }
}
