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
package org.apache.rocketmq.acl.common;

/** ACL 校验失败时抛出的运行时异常，携带 status 与 code。 */
public class AclException extends RuntimeException {
    private static final long serialVersionUID = -7256002576788700354L;

    private String status;
    private int code;

    /** @param status 错误状态标识
     *  @param code 错误码 */
    public AclException(String status, int code) {
        super();
        this.status = status;
        this.code = code;
    }

    /** @param status 错误状态标识
     *  @param code 错误码
     *  @param message 错误描述 */
    public AclException(String status, int code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    /** @param message 错误描述 */
    public AclException(String message) {
        super(message);
    }

    /** @param message 错误描述
     *  @param throwable 根因 */
    public AclException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /** @param status 错误状态标识
     *  @param code 错误码
     *  @param message 错误描述
     *  @param throwable 根因 */
    public AclException(String status, int code, String message, Throwable throwable) {
        super(message, throwable);
        this.status = status;
        this.code = code;
    }

    /** 返回 ACL 错误状态标识。 */
    public String getStatus() {
        return status;
    }

    /** 设置 ACL 错误状态标识。 */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 返回 ACL 错误码。 */
    public int getCode() {
        return code;
    }

    /** 设置 ACL 错误码。 */
    public void setCode(int code) {
        this.code = code;
    }
}
