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
package org.apache.rocketmq.client.hook;

/**
 * 消息过滤钩子：在消费监听器之前对拉取到的消息批次做二次过滤或改写，
 * 常用于灰度、租户隔离等场景。
 */
public interface FilterMessageHook {
    /** 返回钩子唯一名称。 */
    String hookName();

    /** 执行过滤逻辑，可修改 context 中的 msgList。 */
    void filterMessage(final FilterMessageContext context);
}
