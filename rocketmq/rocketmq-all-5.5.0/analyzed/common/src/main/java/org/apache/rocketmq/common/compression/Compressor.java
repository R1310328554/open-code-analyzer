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

package org.apache.rocketmq.common.compression;

import java.io.IOException;

/**
 * 消息体压缩/解压 SPI：各算法实现本接口供 Broker/Client 调用。
 */
public interface Compressor {

    /**
     * 压缩消息体。
     *
     * @param src 待压缩字节数组
     * @param level 压缩级别，用于平衡压缩率与耗时
     * @return 压缩后的字节数组
     * @throws IOException 压缩 IO 异常
     */
    byte[] compress(byte[] src, int level) throws IOException;

    /**
     * 解压消息体。
     *
     * @param src 待解压字节数组
     * @return 解压后的字节数组
     * @throws IOException 解压 IO 异常
     */
    byte[] decompress(byte[] src) throws IOException;
}
