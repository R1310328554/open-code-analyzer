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

package org.apache.rocketmq.proxy.service.relay;

/**
 * Proxy 中继操作结果：封装响应码、备注与载荷。
 */
public class ProxyRelayResult<T> {
    /** Remoting 响应码。 */
    private int code;
    /** 响应备注信息。 */
    private String remark;
    /** 业务结果载荷。 */
    private T result;

    /** 构造中继结果三元组。 */
    public ProxyRelayResult(int code, String remark, T result) {
        this.code = code;
        this.remark = remark;
        this.result = result;
    }

    /** 返回响应码。 */
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    /** 返回响应备注。 */
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    /** 返回业务载荷。 */
    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
