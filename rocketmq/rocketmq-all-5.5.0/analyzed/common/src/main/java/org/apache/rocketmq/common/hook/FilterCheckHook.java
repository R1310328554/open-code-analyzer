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

package org.apache.rocketmq.common.hook;

import java.nio.ByteBuffer;

/**
 * 消息过滤校验钩子：在消费端对消息体进行额外匹配检查。
 */
public interface FilterCheckHook {
    /** 返回钩子名称，用于注册与日志标识。 */
    String hookName();

    /**
     * 判断消息体是否通过过滤条件。
     *
     * @param isUnitMode 是否单元化模式
     * @param byteBuffer 消息体字节缓冲
     * @return 匹配返回 true
     */
    boolean isFilterMatched(final boolean isUnitMode, final ByteBuffer byteBuffer);
}
