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
 * 单 Topic 消费性能指标：拉取/消费 RT、TPS 及失败计数。
 */
public class ConsumeStatus {
    /** 拉取平均响应时间（毫秒）。 */
    private double pullRT;
    /** 拉取吞吐量（条/秒）。 */
    private double pullTPS;
    /** 消费平均耗时（毫秒）。 */
    private double consumeRT;
    /** 消费成功 TPS。 */
    private double consumeOKTPS;
    /** 消费失败 TPS。 */
    private double consumeFailedTPS;

    /** 近一小时消费失败消息数。 */
    private long consumeFailedMsgs;

    /** 返回拉取 RT。 */
    public double getPullRT() {
        return pullRT;
    }

    /** 设置拉取 RT。 */
    public void setPullRT(double pullRT) {
        this.pullRT = pullRT;
    }

    /** 返回拉取 TPS。 */
    public double getPullTPS() {
        return pullTPS;
    }

    /** 设置拉取 TPS。 */
    public void setPullTPS(double pullTPS) {
        this.pullTPS = pullTPS;
    }

    /** 返回消费 RT。 */
    public double getConsumeRT() {
        return consumeRT;
    }

    /** 设置消费 RT。 */
    public void setConsumeRT(double consumeRT) {
        this.consumeRT = consumeRT;
    }

    /** 返回消费成功 TPS。 */
    public double getConsumeOKTPS() {
        return consumeOKTPS;
    }

    /** 设置消费成功 TPS。 */
    public void setConsumeOKTPS(double consumeOKTPS) {
        this.consumeOKTPS = consumeOKTPS;
    }

    /** 返回消费失败 TPS。 */
    public double getConsumeFailedTPS() {
        return consumeFailedTPS;
    }

    /** 设置消费失败 TPS。 */
    public void setConsumeFailedTPS(double consumeFailedTPS) {
        this.consumeFailedTPS = consumeFailedTPS;
    }

    /** 返回失败消息数。 */
    public long getConsumeFailedMsgs() {
        return consumeFailedMsgs;
    }

    /** 设置失败消息数。 */
    public void setConsumeFailedMsgs(long consumeFailedMsgs) {
        this.consumeFailedMsgs = consumeFailedMsgs;
    }
}
