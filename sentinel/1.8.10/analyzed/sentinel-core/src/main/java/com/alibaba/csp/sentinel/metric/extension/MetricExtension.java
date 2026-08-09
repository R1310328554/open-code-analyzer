/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.metric.extension;

import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * 为 Sentinel 内部统计提供扩展点的接口。
 * <p>
 * 所有方法均在业务逻辑同一线程中调用，请勿执行耗时操作，否则会阻塞业务线程。
 * </p>
 *
 * @author Carpenter Lee
 * @since 1.6.1
 */
public interface MetricExtension {

    /**
     * 累加资源通过（放行）次数。
     *
     * @param n        待累加次数
     * @param resource 资源名
     * @param args     资源附加参数；若资源为方法名，则为方法参数
     */
    void addPass(String resource, int n, Object... args);

    /**
     * 累加资源被限流/阻断次数。
     *
     * @param n              待累加次数
     * @param resource       资源名
     * @param origin         调用方来源
     * @param blockException 关联的阻断异常
     * @param args           资源附加参数；若资源为方法名，则为方法参数
     */
    void addBlock(String resource, int n, String origin, BlockException blockException, Object... args);

    /**
     * 累加资源调用成功完成次数。
     *
     * @param n        待累加次数
     * @param resource 资源名
     * @param args     资源附加参数；若资源为方法名，则为方法参数
     */
    void addSuccess(String resource, int n, Object... args);

    /**
     * 累加资源调用异常次数。
     *
     * @param n         待累加次数
     * @param resource  资源名
     * @param throwable 关联异常
     */
    void addException(String resource, int n, Throwable throwable);

    /**
     * 累加资源响应时间。
     *
     * @param rt       响应时间（毫秒）
     * @param resource 资源名
     * @param args     资源附加参数；若资源为方法名，则为方法参数
     */
    void addRt(String resource, long rt, Object... args);

    /**
     * 增加资源当前并发线程数。
     *
     * @param resource 资源名
     * @param args     资源附加参数；若资源为方法名，则为方法参数
     */
    void increaseThreadNum(String resource, Object... args);

    /**
     * 减少资源当前并发线程数。
     *
     * @param resource 资源名
     * @param args     资源附加参数；若资源为方法名，则为方法参数
     */
    void decreaseThreadNum(String resource, Object... args);
}
