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
package org.apache.rocketmq.tieredstore.exception;

/**
 * 分层存储错误码枚举。
 */
public enum TieredStoreErrorCode {

    /** 非法偏移量。 */
    ILLEGAL_OFFSET,

    /** 非法参数。 */
    ILLEGAL_PARAM,

    /** 下载长度不正确。 */
    DOWNLOAD_LENGTH_NOT_CORRECT,

    /** 存储系统中无新数据。 */
    NO_NEW_DATA,

    /** 存储提供方错误。 */
    STORAGE_PROVIDER_ERROR,

    /** 输入/输出错误。 */
    IO_ERROR,

    /** 文件段已封存，不可再写入。 */
    SEGMENT_SEALED,

    /** 未知错误。 */
    UNKNOWN
}