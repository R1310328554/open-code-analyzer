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
import io.netty.buffer.ByteBufHolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * {@link InterfaceHttpData} 的扩展接口，同时实现 {@link ByteBufHolder}。
 * <p>
 * 表示 multipart 或表单中的具名数据体（属性或文件），支持分块写入、
 * 内存/磁盘存储、大小限制与 {@link ByteBuf} 生命周期管理。
 */
public interface HttpData extends InterfaceHttpData, ByteBufHolder {

    /** 返回本数据项允许的最大字节数。 */

    long getMaxSize();

    /** 设置最大字节限制，{@code -1} 表示不限；超限抛异常，通常由 {@link HttpDataFactory} 配置。 */

    void setMaxSize(long maxSize);

    /** 检查新累计大小是否超过上限（以字节计）。 */

    void checkSize(long newSize) throws IOException;

    /** 用 {@link ByteBuf} 覆盖原有内容；{@code buffer} 所有权转移给本对象。 */

    void setContent(ByteBuf buffer) throws IOException;

    /** 追加 {@link ByteBuf} 内容；{@code last=true} 表示最后一块并完成解码。 */

    void addContent(ByteBuf buffer, boolean last) throws IOException;

    /** 从 {@link File} 读取并覆盖内容。 */

    void setContent(File file) throws IOException;

    /** 从 {@link InputStream} 读取并覆盖内容。 */

    void setContent(InputStream inputStream) throws IOException;

    /** 是否已接收并存储全部数据块。 */

    boolean isCompleted();

    /** 返回当前已存储内容的字节长度。 */

    long length();

    /** 返回请求中声明的 Content-Length；无声明时为 0，解码过程中不变。 */

    long definedLength();

    /** 删除底层存储（含临时磁盘文件）。 */

    void delete();

    /** 以字节数组返回全部内容；磁盘存储时会大量分配堆内存。 */

    byte[] get() throws IOException;

    /** 以 {@link ByteBuf} 返回全部内容；磁盘存储时可能大量分配内存。 */

    ByteBuf getByteBuf() throws IOException;

    /** 从当前读位置返回最多 {@code length} 字节的块；读完后返回空缓冲并重置位置。 */

    ByteBuf getChunk(int length) throws IOException;

    /** 以默认字符集解码为字符串。 */

    String getString() throws IOException;

    /** 以指定 {@link Charset} 解码为字符串。 */

    String getString(Charset encoding) throws IOException;

    /** 设置浏览器声明的字符集，不可为 {@code null}。 */

    void setCharset(Charset charset);

    /** 返回浏览器字符集，未定义时 {@code null}。 */

    Charset getCharset();

    /** 将内容写入目标文件；成功后脱离工厂清理列表。 */

    boolean renameTo(File dest) throws IOException;

    /** 内容是否完全在内存中（{@code true} 表示非磁盘临时文件）。 */

    boolean isInMemory();

    /** 返回关联的磁盘 {@link File}；纯内存实现会抛 {@link IOException}。 */

    File getFile() throws IOException;

    @Override
    HttpData copy();

    @Override
    HttpData duplicate();

    @Override
    HttpData retainedDuplicate();

    @Override
    HttpData replace(ByteBuf content);

    @Override
    HttpData retain();

    @Override
    HttpData retain(int increment);

    @Override
    HttpData touch();

    @Override
    HttpData touch(Object hint);
}
