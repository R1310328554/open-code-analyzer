/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.container;

import java.util.Properties;

/**
 * Broker 启动生命周期钩子：在容器内 Broker 启动前后插入自定义逻辑。
 */
public interface BrokerBootHook {
    /**
     * 钩子名称，用于日志与去重识别。
     *
     * @return 钩子名称
     */
    String hookName();

    /**
     * Broker 启动前执行的代码。
     *
     * @param innerBrokerController 待启动的内部 Broker 控制器
     * @param properties            Broker 配置属性
     * @throws Exception 钩子执行失败时抛出
     */
    void executeBeforeStart(InnerBrokerController innerBrokerController, Properties properties) throws Exception;

    /**
     * Broker 启动完成后执行的代码。
     *
     * @param innerBrokerController 已启动的内部 Broker 控制器
     * @param properties            Broker 配置属性
     * @throws Exception 钩子执行失败时抛出
     */
    void executeAfterStart(InnerBrokerController innerBrokerController, Properties properties) throws Exception;
}

