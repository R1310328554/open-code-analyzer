/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.netty.handler.codec.mqtt;

import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Payload of the {@link MqttSubAckMessage}
 * <p>SUBACK 载荷：与 SUBSCRIBE 中各主题过滤器一一对应，按序给出授权 QoS 或 MQTT 5 原因码。
 * 3.1.1 下每字节 0–2 表示 QoS，0x80 表示失败；5.0 则扩展为完整 {@link MqttReasonCodes.SubAck} 语义。</p>
 */
public class MqttSubAckPayload {

    /** 强类型原因码列表，构造后不可变。 */
    private final List<MqttReasonCodes.SubAck> reasonCodes;

    public MqttSubAckPayload(int... reasonCodes) {
        ObjectUtil.checkNotNull(reasonCodes, "reasonCodes");

        List<MqttReasonCodes.SubAck> list = new ArrayList<MqttReasonCodes.SubAck>(reasonCodes.length);
        for (int v: reasonCodes) {
            list.add(MqttReasonCodes.SubAck.valueOf((byte) (v & 0xFF)));
        }
        this.reasonCodes = Collections.unmodifiableList(list);
    }

    public MqttSubAckPayload(MqttReasonCodes.SubAck... reasonCodes) {
        ObjectUtil.checkNotNull(reasonCodes, "reasonCodes");

        List<MqttReasonCodes.SubAck> list = new ArrayList<MqttReasonCodes.SubAck>(reasonCodes.length);
        list.addAll(Arrays.asList(reasonCodes));
        this.reasonCodes = Collections.unmodifiableList(list);
    }

    public MqttSubAckPayload(Iterable<Integer> reasonCodes) {
        ObjectUtil.checkNotNull(reasonCodes, "reasonCodes");
        List<MqttReasonCodes.SubAck> list = new ArrayList<MqttReasonCodes.SubAck>();
        for (Integer v: reasonCodes) {
            if (v == null) {
                break; // 遇 null 提前终止，兼容部分迭代器语义
            }
            list.add(MqttReasonCodes.SubAck.valueOf(v.byteValue()));
        }
        this.reasonCodes = Collections.unmodifiableList(list);
    }

    /**
     * 兼容 MQTT 3.x 的 QoS 级别视图：值 &gt; 2 时映射为 {@link MqttQoS#FAILURE}。
     */
    public List<Integer> grantedQoSLevels() {
        List<Integer> qosLevels = new ArrayList<Integer>(reasonCodes.size());
        for (MqttReasonCodes.SubAck code: reasonCodes) {
            if ((code.byteValue() & 0xFF) > MqttQoS.EXACTLY_ONCE.value()) {
                qosLevels.add(MqttQoS.FAILURE.value());
            } else {
                qosLevels.add(code.byteValue() & 0xFF);
            }
        }
        return qosLevels;
    }

    /** 以无符号整型返回各原因码原始字节值。 */
    public List<Integer> reasonCodes() {
        return typedReasonCodesToOrdinal();
    }

    private List<Integer> typedReasonCodesToOrdinal() {
        List<Integer> intCodes = new ArrayList<Integer>(reasonCodes.size());
        for (MqttReasonCodes.SubAck code: reasonCodes) {
            intCodes.add(code.byteValue() & 0xFF);
        }
        return intCodes;
    }

    public List<MqttReasonCodes.SubAck> typedReasonCodes() {
        return reasonCodes;
    }

    @Override
    public String toString() {
        return new StringBuilder(StringUtil.simpleClassName(this))
            .append('[')
            .append("reasonCodes=").append(reasonCodes)
            .append(']')
            .toString();
    }
}
