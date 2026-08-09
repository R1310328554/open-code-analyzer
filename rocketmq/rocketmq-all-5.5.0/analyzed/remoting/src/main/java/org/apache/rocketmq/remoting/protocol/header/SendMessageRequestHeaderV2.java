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

package org.apache.rocketmq.remoting.protocol.header;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.common.action.RocketMQAction;
import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.common.resource.RocketMQResource;
import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.annotation.CFNullable;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.FastCodesHeader;
import org.apache.rocketmq.remoting.rpc.TopicQueueRequestHeader;
import org.apache.rocketmq.remoting.protocol.RequestCode;

/**
 * 发送消息请求头 V2：字段名缩短以加速 FastJson 反序列化。
 * 字段 a~n 分别对应 V1 中的 producerGroup、topic、defaultTopic 等。
 */
@RocketMQAction(value = RequestCode.SEND_MESSAGE_V2, action = Action.PUB)
public class SendMessageRequestHeaderV2 extends TopicQueueRequestHeader implements CommandCustomHeader, FastCodesHeader {
    /** 生产者组（字段 a，对应 V1 producerGroup）。 */
    @CFNotNull
    private String a; // producerGroup
    /** Topic 名称（字段 b）。 */
    @CFNotNull
    @RocketMQResource(ResourceType.TOPIC)
    private String b; // topic
    /** 默认 Topic 名（字段 c）。 */
    @CFNotNull
    private String c; // defaultTopic
    /** 默认队列数（字段 d）。 */
    @CFNotNull
    private Integer d; // defaultTopicQueueNums
    /** 队列 ID（字段 e）。 */
    @CFNotNull
    private Integer e; // queueId
    /** 系统标志位（字段 f）。 */
    @CFNotNull
    private Integer f; // sysFlag
    /** born 时间戳（字段 g）。 */
    @CFNotNull
    private Long g; // bornTimestamp
    /** 消息 flag（字段 h）。 */
    @CFNotNull
    private Integer h; // flag
    /** 用户属性（字段 i），可为空。 */
    @CFNullable
    private String i; // properties
    /** 重试消费次数（字段 j），可为空。 */
    @CFNullable
    private Integer j; // reconsumeTimes
    /** 单元化模式（字段 k），可为空。 */
    @CFNullable
    private Boolean k; // unitMode

    /** 最大重试次数（字段 l，对应 V1 maxReconsumeTimes）。 */
    private Integer l; // consumeRetryTimes

    /** 是否批量（字段 m），可为空。 */
    @CFNullable
    private Boolean m; // batch
    /** Broker 名称（字段 n），可为空。 */
    @CFNullable
    private String n; // brokerName

    /** 将 V2 请求头转换为 V1 格式。 */
    public static SendMessageRequestHeader createSendMessageRequestHeaderV1(final SendMessageRequestHeaderV2 v2) {
        SendMessageRequestHeader v1 = new SendMessageRequestHeader();
        v1.setProducerGroup(v2.a);
        v1.setTopic(v2.b);
        v1.setDefaultTopic(v2.c);
        v1.setDefaultTopicQueueNums(v2.d);
        v1.setQueueId(v2.e);
        v1.setSysFlag(v2.f);
        v1.setBornTimestamp(v2.g);
        v1.setFlag(v2.h);
        v1.setProperties(v2.i);
        v1.setReconsumeTimes(v2.j);
        v1.setUnitMode(v2.k);
        v1.setMaxReconsumeTimes(v2.l);
        v1.setBatch(v2.m);
        v1.setBrokerName(v2.n);
        return v1;
    }

    /** 将 V1 请求头转换为 V2 格式。 */
    public static SendMessageRequestHeaderV2 createSendMessageRequestHeaderV2(final SendMessageRequestHeader v1) {
        SendMessageRequestHeaderV2 v2 = new SendMessageRequestHeaderV2();
        v2.a = v1.getProducerGroup();
        v2.b = v1.getTopic();
        v2.c = v1.getDefaultTopic();
        v2.d = v1.getDefaultTopicQueueNums();
        v2.e = v1.getQueueId();
        v2.f = v1.getSysFlag();
        v2.g = v1.getBornTimestamp();
        v2.h = v1.getFlag();
        v2.i = v1.getProperties();
        v2.j = v1.getReconsumeTimes();
        v2.k = v1.isUnitMode();
        v2.l = v1.getMaxReconsumeTimes();
        v2.m = v1.isBatch();
        v2.n = v1.getBrokerName();
        return v2;
    }

    /** 校验请求头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {
    }

    /** 将短字段名键值对编码写入 ByteBuf。 */
    @Override
    public void encode(ByteBuf out) {
        writeIfNotNull(out, "a", a);
        writeIfNotNull(out, "b", b);
        writeIfNotNull(out, "c", c);
        writeIfNotNull(out, "d", d);
        writeIfNotNull(out, "e", e);
        writeIfNotNull(out, "f", f);
        writeIfNotNull(out, "g", g);
        writeIfNotNull(out, "h", h);
        writeIfNotNull(out, "i", i);
        writeIfNotNull(out, "j", j);
        writeIfNotNull(out, "k", k);
        writeIfNotNull(out, "l", l);
        writeIfNotNull(out, "m", m);
        writeIfNotNull(out, "n", n);
    }

    /** 从字段映射解码并填充各短名字段。 */
    @Override
    public void decode(HashMap<String, String> fields) throws RemotingCommandException {

        String str = getAndCheckNotNull(fields, "a");
        if (str != null) {
            a = str;
        }

        str = getAndCheckNotNull(fields, "b");
        if (str != null) {
            b = str;
        }

        str = getAndCheckNotNull(fields, "c");
        if (str != null) {
            c = str;
        }

        str = getAndCheckNotNull(fields, "d");
        if (str != null) {
            d = Integer.parseInt(str);
        }

        str = getAndCheckNotNull(fields, "e");
        if (str != null) {
            e = Integer.parseInt(str);
        }

        str = getAndCheckNotNull(fields, "f");
        if (str != null) {
            f = Integer.parseInt(str);
        }

        str = getAndCheckNotNull(fields, "g");
        if (str != null) {
            g = Long.parseLong(str);
        }

        str = getAndCheckNotNull(fields, "h");
        if (str != null) {
            h = Integer.parseInt(str);
        }

        str = fields.get("i");
        if (str != null) {
            i = str;
        }

        str = fields.get("j");
        if (str != null) {
            j = Integer.parseInt(str);
        }

        str = fields.get("k");
        if (str != null) {
            k = Boolean.parseBoolean(str);
        }

        str = fields.get("l");
        if (str != null) {
            l = Integer.parseInt(str);
        }

        str = fields.get("m");
        if (str != null) {
            m = Boolean.parseBoolean(str);
        }

        str = fields.get("n");
        if (str != null) {
            n = str;
        }
    }

    /** 返回字段 a（producerGroup）。 */
    public String getA() {
        return a;
    }

    /** 设置字段 a。 */
    public void setA(String a) {
        this.a = a;
    }

    /** 返回字段 b（topic）。 */
    public String getB() {
        return b;
    }

    /** 设置字段 b。 */
    public void setB(String b) {
        this.b = b;
    }

    /** 返回字段 c（defaultTopic）。 */
    public String getC() {
        return c;
    }

    /** 设置字段 c。 */
    public void setC(String c) {
        this.c = c;
    }

    /** 返回字段 d（defaultTopicQueueNums）。 */
    public Integer getD() {
        return d;
    }

    /** 设置字段 d。 */
    public void setD(Integer d) {
        this.d = d;
    }

    /** 返回字段 e（queueId）。 */
    public Integer getE() {
        return e;
    }

    /** 设置字段 e。 */
    public void setE(Integer e) {
        this.e = e;
    }

    /** 返回字段 f（sysFlag）。 */
    public Integer getF() {
        return f;
    }

    /** 设置字段 f。 */
    public void setF(Integer f) {
        this.f = f;
    }

    /** 返回字段 g（bornTimestamp）。 */
    public Long getG() {
        return g;
    }

    /** 设置字段 g。 */
    public void setG(Long g) {
        this.g = g;
    }

    /** 返回字段 h（flag）。 */
    public Integer getH() {
        return h;
    }

    /** 设置字段 h。 */
    public void setH(Integer h) {
        this.h = h;
    }

    /** 返回字段 i（properties）。 */
    public String getI() {
        return i;
    }

    /** 设置字段 i。 */
    public void setI(String i) {
        this.i = i;
    }

    /** 返回字段 j（reconsumeTimes）。 */
    public Integer getJ() {
        return j;
    }

    /** 设置字段 j。 */
    public void setJ(Integer j) {
        this.j = j;
    }

    /** 返回字段 k（unitMode）。 */
    public Boolean isK() {
        return k;
    }

    /** 设置字段 k。 */
    public void setK(Boolean k) {
        this.k = k;
    }

    /** 返回字段 l（maxReconsumeTimes）。 */
    public Integer getL() {
        return l;
    }

    /** 设置字段 l。 */
    public void setL(final Integer l) {
        this.l = l;
    }

    /** 返回字段 m（batch）。 */
    public Boolean isM() {
        return m;
    }

    /** 设置字段 m。 */
    public void setM(Boolean m) {
        this.m = m;
    }

    /** 返回含全部短字段的调试字符串。 */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("a", a)
            .add("b", b)
            .add("c", c)
            .add("d", d)
            .add("e", e)
            .add("f", f)
            .add("g", g)
            .add("h", h)
            .add("i", i)
            .add("j", j)
            .add("k", k)
            .add("l", l)
            .add("m", m)
            .add("n", n)
            .toString();
    }

    /** 返回队列 ID（字段 e）。 */
    @Override
    public Integer getQueueId() {
        return e;
    }

    /** 设置队列 ID（写入字段 e）。 */
    @Override
    public void setQueueId(Integer queueId) {
        this.e = queueId;
    }

    /** 返回 Topic 名称（字段 b）。 */
    @Override
    public String getTopic() {
        return b;
    }

    /** 设置 Topic 名称（写入字段 b）。 */
    @Override
    public void setTopic(String topic) {
        this.b = topic;
    }
}
