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

import io.netty.buffer.ByteBuf;

/**
 * 文件上传接口，实现可为内存、临时磁盘文件等。
 * <p>
 * 多数方法语义借鉴 {@code java.io.File}；扩展 {@link HttpData} 提供
 * 文件名、Content-Type、Content-Transfer-Encoding 等 multipart 元数据。
 */
public interface FileUpload extends HttpData {
    /** 返回客户端（浏览器）提供的原始文件名。 */

    String getFilename();

    /** 设置原始文件名；会校验是否可安全用于 HTTP 请求，非法字符抛异常。 */

    void setFilename(String filename);

    /** 设置浏览器提供的 Content-Type，不可为 {@code null}。 */

    void setContentType(String contentType);

    /** 返回浏览器 Content-Type，未定义时 {@code null}。 */

    String getContentType();

    /** 设置 Content-Transfer-Encoding（7bit、8bit 或 binary）。 */

    void setContentTransferEncoding(String contentTransferEncoding);

    /** 返回 Content-Transfer-Encoding。 */

    String getContentTransferEncoding();

    @Override
    FileUpload copy();

    @Override
    FileUpload duplicate();

    @Override
    FileUpload retainedDuplicate();

    @Override
    FileUpload replace(ByteBuf content);

    @Override
    FileUpload retain();

    @Override
    FileUpload retain(int increment);

    @Override
    FileUpload touch();

    @Override
    FileUpload touch(Object hint);
}
