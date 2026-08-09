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
package org.apache.rocketmq.broker.metrics;

import com.google.common.base.Objects;
import org.apache.rocketmq.remoting.protocol.LanguageCode;
import org.apache.rocketmq.remoting.protocol.heartbeat.ConsumeType;

/**
 * 消费者连接属性：用于指标标签去重的 group、语言、版本与消费模式组合键。
 */
public class ConsumerAttr {
    String group;
    LanguageCode language;
    int version;
    ConsumeType consumeMode;

    /** 构造消费者属性快照。 */
    public ConsumerAttr(String group, LanguageCode language, int version, ConsumeType consumeMode) {
        this.group = group;
        this.language = language;
        this.version = version;
        this.consumeMode = consumeMode;
    }

    /** 按 group、language、version、consumeMode 判等。 */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ConsumerAttr attr = (ConsumerAttr) o;
        return version == attr.version && Objects.equal(group, attr.group) && language == attr.language && consumeMode == attr.consumeMode;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(group, language, version, consumeMode);
    }
}
