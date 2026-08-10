/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.storage;

/**
 * 存储不可用异常：外部存储因连接故障、服务宕机或基础设施问题暂时不可访问时抛出。
 * <p>存储 provider 可借此向 {@code UserStorageManager} 发出优雅降级信号，跳过不可用 provider 并继续其他可用存储或本地存储。</p>
 *
 * Exception thrown by user storage providers to indicate that the external storage
 * system is temporarily unavailable due to connectivity issues, server downtime,
 * or other infrastructure problems.
 * 
 * <p>This exception allows storage providers to signal graceful degradation scenarios
 * where the UserStorageManager should skip the unavailable provider and continue
 * with other available providers or local storage.</p>
 *
 */
public class StorageUnavailableException extends RuntimeException {

    /** 构造无消息的存储不可用异常。 */
    public StorageUnavailableException() {
        super();
    }

    /** 构造带消息的存储不可用异常。
     * @param message 异常描述 */
    public StorageUnavailableException(String message) {
        super(message);
    }

    /** 构造带消息与原因的存储不可用异常。
     * @param message 异常描述
     * @param cause 根本原因 */
    public StorageUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    /** 构造由给定原因引起的存储不可用异常。
     * @param cause 根本原因 */
    public StorageUnavailableException(Throwable cause) {
        super(cause != null ? cause.getMessage() : null, cause);
    }
}
