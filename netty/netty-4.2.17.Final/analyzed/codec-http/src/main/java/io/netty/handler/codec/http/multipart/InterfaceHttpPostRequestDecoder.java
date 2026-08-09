/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.http.multipart;

import io.netty.handler.codec.http.HttpContent;

import java.util.List;

/**
 * POST 请求体解码器接口，支持 {@code application/x-www-form-urlencoded} 与 multipart。
 * <p>
 * 完成后<strong>必须</strong>调用 {@link #destroy()} 释放临时文件与缓冲。
 */
public interface InterfaceHttpPostRequestDecoder {
    /** 是否为 {@code multipart/form-data} 请求。 */

    boolean isMultipart();

    /** 设置已读字节丢弃阈值；越小内存占用越低但拷贝越多，{@code 0} 禁用。 */

    void setDiscardThreshold(int discardThreshold);

    /** 返回缓冲丢弃阈值（字节）。 */

    int getDiscardThreshold();

    /** 返回正文全部 {@link InterfaceHttpData}；分块传输须先 {@link #offer} 完所有块。 */

    List<InterfaceHttpData> getBodyHttpDatas();

    /** 按名称（忽略大小写）返回全部匹配的数据项。 */

    List<InterfaceHttpData> getBodyHttpDatas(String name);

    /** 返回首个匹配名称的数据项（忽略大小写）。 */

    InterfaceHttpData getBodyHttpData(String name);

    /** 喂入新的 {@link HttpContent} 分块并推进解码状态机。 */

    InterfaceHttpPostRequestDecoder offer(HttpContent content);

    /** 是否还有已完整解码、可供 {@link #next()} 消费的数据项。 */

    boolean hasNext();

    /** 返回下一个完整数据项；用毕须 {@link InterfaceHttpData#release()}。 */

    InterfaceHttpData next();

    /** 返回当前正在部分解码的数据项；完整项通过 {@link #hasNext()}/{@link #next()} 获取。 */

    InterfaceHttpData currentPartialHttpData();

    /** 销毁解码器并释放全部资源，之后不可再使用。 */

    void destroy();

    /** 清理当前请求关联的磁盘临时 {@link InterfaceHttpData}。 */

    void cleanFiles();

    /** 从待清理列表移除指定 {@link InterfaceHttpData}（如已持久化到业务路径）。 */

    void removeHttpDataFromClean(InterfaceHttpData data);
}
