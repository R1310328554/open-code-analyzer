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
package org.apache.rocketmq.tools.admin.api;

import java.util.List;

/**
 * Broker 批量操作结果：分别记录成功与失败的 Broker 标识列表。
 */
public class BrokerOperatorResult {

    /** 操作成功的 Broker 列表。 */
    private List<String> successList;

    /** 操作失败的 Broker 列表。 */
    private List<String> failureList;

    /** 返回成功列表。 */
    public List<String> getSuccessList() {
        return successList;
    }

    /** 设置成功列表。 */
    public void setSuccessList(List<String> successList) {
        this.successList = successList;
    }

    /** 返回失败列表。 */
    public List<String> getFailureList() {
        return failureList;
    }

    /** 设置失败列表。 */
    public void setFailureList(List<String> failureList) {
        this.failureList = failureList;
    }

    /** 返回包含 successList 与 failureList 的字符串表示。 */
    @Override
    public String toString() {
        return "BrokerOperatorResult{" +
            "successList=" + successList +
            ", failureList=" + failureList +
            '}';
    }
}
