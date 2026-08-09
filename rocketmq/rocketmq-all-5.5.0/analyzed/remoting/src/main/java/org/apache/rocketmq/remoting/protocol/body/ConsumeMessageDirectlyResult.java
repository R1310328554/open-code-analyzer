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

import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * 直接消费（管理端触发）结果：顺序/自动提交标志、消费结论与耗时。
 */
public class ConsumeMessageDirectlyResult extends RemotingSerializable {
    /** 是否顺序消费模式。 */
    private boolean order = false;
    /** 是否自动提交消费位点。 */
    private boolean autoCommit = true;
    /** 消费处理结果码。 */
    private CMResult consumeResult;
    /** 附加说明或异常信息。 */
    private String remark;
    /** 消费耗时（毫秒）。 */
    private long spentTimeMills;

    /** 是否顺序消费。 */
    public boolean isOrder() {
        return order;
    }

    /** 设置顺序消费标志。 */
    public void setOrder(boolean order) {
        this.order = order;
    }

    /** 是否自动提交。 */
    public boolean isAutoCommit() {
        return autoCommit;
    }

    /** 设置自动提交标志。 */
    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    /** 返回备注信息。 */
    public String getRemark() {
        return remark;
    }

    /** 设置备注信息。 */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /** 返回消费结果码。 */
    public CMResult getConsumeResult() {
        return consumeResult;
    }

    /** 设置消费结果码。 */
    public void setConsumeResult(CMResult consumeResult) {
        this.consumeResult = consumeResult;
    }

    /** 返回耗时毫秒数。 */
    public long getSpentTimeMills() {
        return spentTimeMills;
    }

    /** 设置耗时毫秒数。 */
    public void setSpentTimeMills(long spentTimeMills) {
        this.spentTimeMills = spentTimeMills;
    }

    /** 返回便于日志排查的字符串。 */
    @Override
    public String toString() {
        return "ConsumeMessageDirectlyResult [order=" + order + ", autoCommit=" + autoCommit
            + ", consumeResult=" + consumeResult + ", remark=" + remark + ", spentTimeMills="
            + spentTimeMills + "]";
    }
}
