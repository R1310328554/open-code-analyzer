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
package org.apache.rocketmq.client.consumer;

/** POP 长轮询通知结果。 */
public class NotifyResult {
    /** 是否有新消息。 */
    private boolean hasMsg;
    /** 长轮询池是否已满。 */
    private boolean pollingFull;

    /** 是否有新消息。 */
    public boolean isHasMsg() {
        return hasMsg;
    }

    /** 长轮询池是否已满。 */
    public boolean isPollingFull() {
        return pollingFull;
    }

    /** 设置是否有新消息。 */
    public void setHasMsg(boolean hasMsg) {
        this.hasMsg = hasMsg;
    }

    /** 设置长轮询池是否已满。 */
    public void setPollingFull(boolean pollingFull) {
        this.pollingFull = pollingFull;
    }

    @Override public String toString() {
        return "NotifyResult{" +
            "hasMsg=" + hasMsg +
            ", pollingFull=" + pollingFull +
            '}';
    }
}
