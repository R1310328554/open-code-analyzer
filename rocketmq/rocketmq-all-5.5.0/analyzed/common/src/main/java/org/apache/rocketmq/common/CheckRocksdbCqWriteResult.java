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

package org.apache.rocketmq.common;

/**
 * RocksDB CommitLog 队列写入校验结果。
 */
public class CheckRocksdbCqWriteResult {
    /** 校验结果描述文本。 */
    String checkResult;

    /** 校验状态码，对应 {@link CheckStatus}。 */
    int checkStatus;

    /** RocksDB CQ 写入校验状态枚举。 */
    public enum CheckStatus {
        /** 校验通过。 */
        CHECK_OK(0),
        /** 校验未通过。 */
        CHECK_NOT_OK(1),
        /** 校验进行中。 */
        CHECK_IN_PROGRESS(2),
        /** 校验过程出错。 */
        CHECK_ERROR(3);

        /** 状态整型值。 */
        private int value;

        CheckStatus(int value) {
            this.value = value;
        }

        /** 返回状态整型值。 */
        public int getValue() {
            return value;
        }
    }

    /** 获取校验结果描述。 */
    public String getCheckResult() {
        return checkResult;
    }

    /** 设置校验结果描述。 */
    public void setCheckResult(String checkResult) {
        this.checkResult = checkResult;
    }

    /** 获取校验状态码。 */
    public int getCheckStatus() {
        return checkStatus;
    }

    /** 设置校验状态码。 */
    public void setCheckStatus(int checkStatus) {
        this.checkStatus = checkStatus;
    }
}
