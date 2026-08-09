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
package org.apache.rocketmq.store;

/**
 * 写消息结果：封装 PutMessageStatus、AppendMessageResult 及是否远程写入标志。
 */
public class PutMessageResult {
    /** 写消息状态码。 */
    private PutMessageStatus putMessageStatus;
    /** CommitLog 追加结果。 */
    private AppendMessageResult appendMessageResult;
    /** 是否为远程（Proxy）写入路径。 */
    private boolean remotePut = false;

    /** 本地写入结果构造。 */
    public PutMessageResult(PutMessageStatus putMessageStatus, AppendMessageResult appendMessageResult) {
        this.putMessageStatus = putMessageStatus;
        this.appendMessageResult = appendMessageResult;
    }

    /** 指定是否远程写入的构造。 */
    public PutMessageResult(PutMessageStatus putMessageStatus, AppendMessageResult appendMessageResult,
        boolean remotePut) {
        this.putMessageStatus = putMessageStatus;
        this.appendMessageResult = appendMessageResult;
        this.remotePut = remotePut;
    }

    /** 是否视为写入成功（含刷盘/同步超时等可接受状态）。 */
    public boolean isOk() {
        if (remotePut) {
            return putMessageStatus == PutMessageStatus.PUT_OK || putMessageStatus == PutMessageStatus.FLUSH_DISK_TIMEOUT
                || putMessageStatus == PutMessageStatus.FLUSH_SLAVE_TIMEOUT || putMessageStatus == PutMessageStatus.SLAVE_NOT_AVAILABLE;
        } else {
            return this.appendMessageResult != null && this.appendMessageResult.isOk();
        }

    }

    /** 返回追加结果。 */
    public AppendMessageResult getAppendMessageResult() {
        return appendMessageResult;
    }

    public void setAppendMessageResult(AppendMessageResult appendMessageResult) {
        this.appendMessageResult = appendMessageResult;
    }

    /** 返回写消息状态。 */
    public PutMessageStatus getPutMessageStatus() {
        return putMessageStatus;
    }

    public void setPutMessageStatus(PutMessageStatus putMessageStatus) {
        this.putMessageStatus = putMessageStatus;
    }

    /** 是否远程写入。 */
    public boolean isRemotePut() {
        return remotePut;
    }

    public void setRemotePut(boolean remotePut) {
        this.remotePut = remotePut;
    }

    /** 返回调试字符串。 */
    @Override
    public String toString() {
        return "PutMessageResult [putMessageStatus=" + putMessageStatus + ", appendMessageResult="
            + appendMessageResult + ", remotePut=" + remotePut + "]";
    }

}
