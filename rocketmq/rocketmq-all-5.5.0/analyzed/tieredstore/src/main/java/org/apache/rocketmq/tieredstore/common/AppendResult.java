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
package org.apache.rocketmq.tieredstore.common;

public enum AppendResult {

    /**

     * 追加写入成功。
     
     */
    SUCCESS,

    /**

     * 追加缓冲区已满。
     
     */
    /** 追加缓冲区已满。 */
    BUFFER_FULL,

    /**

     * 文件已满，无法继续写入。
     
     */
    /** 文件已满。 */
    FILE_FULL,

    /**

     * 文件已关闭，无法接受数据。
     
     */
    /** 文件已关闭。 */
    FILE_CLOSED,

    /**

     * 追加过程中发生未知错误。
     
     */
    /** 未知错误。 */
    UNKNOWN_ERROR
}
