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

package org.apache.rocketmq.remoting.protocol.body;

/**
 * ConsumeQueue 单条索引条目：物理偏移、大小、Tags 哈希及扩展 JSON。
 */
public class ConsumeQueueData {

    /** CommitLog 物理起始偏移。 */
    private long physicOffset;
    /** CommitLog 消息体字节长度。 */
    private int physicSize;
    /** Tags 字符串哈希码。 */
    private long tagsCode;
    /** 扩展属性 JSON 字符串。 */
    private String extendDataJson;
    /** 事务/过滤相关位图。 */
    private String bitMap;
    /** 是否已执行表达式求值。 */
    private boolean eval;
    /** 关联消息摘要或调试文本。 */
    private String msg;

    /** 返回物理偏移。 */
    public long getPhysicOffset() {
        return physicOffset;
    }

    /** 设置物理偏移。 */
    public void setPhysicOffset(long physicOffset) {
        this.physicOffset = physicOffset;
    }

    /** 返回物理大小。 */
    public int getPhysicSize() {
        return physicSize;
    }

    /** 设置物理大小。 */
    public void setPhysicSize(int physicSize) {
        this.physicSize = physicSize;
    }

    /** 返回 Tags 哈希码。 */
    public long getTagsCode() {
        return tagsCode;
    }

    /** 设置 Tags 哈希码。 */
    public void setTagsCode(long tagsCode) {
        this.tagsCode = tagsCode;
    }

    /** 返回扩展 JSON。 */
    public String getExtendDataJson() {
        return extendDataJson;
    }

    /** 设置扩展 JSON。 */
    public void setExtendDataJson(String extendDataJson) {
        this.extendDataJson = extendDataJson;
    }

    /** 返回位图字符串。 */
    public String getBitMap() {
        return bitMap;
    }

    /** 设置位图字符串。 */
    public void setBitMap(String bitMap) {
        this.bitMap = bitMap;
    }

    /** 是否已求值。 */
    public boolean isEval() {
        return eval;
    }

    /** 设置求值标志。 */
    public void setEval(boolean eval) {
        this.eval = eval;
    }

    /** 返回消息摘要。 */
    public String getMsg() {
        return msg;
    }

    /** 设置消息摘要。 */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /** 返回调试字符串。 */
    @Override
    public String toString() {
        return "ConsumeQueueData{" +
            "physicOffset=" + physicOffset +
            ", physicSize=" + physicSize +
            ", tagsCode=" + tagsCode +
            ", extendDataJson='" + extendDataJson + '\'' +
            ", bitMap='" + bitMap + '\'' +
            ", eval=" + eval +
            ", msg='" + msg + '\'' +
            '}';
    }
}
