/*
 * Copyright 2016 The Netty Project
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
package io.netty.channel.unix;

import static io.netty.channel.unix.LimitsStaticallyReferencedJniMethods.iovMax;
import static io.netty.channel.unix.LimitsStaticallyReferencedJniMethods.sizeOfjlong;
import static io.netty.channel.unix.LimitsStaticallyReferencedJniMethods.ssizeMax;
import static io.netty.channel.unix.LimitsStaticallyReferencedJniMethods.uioMaxIov;

/**
 * Unix 平台 I/O 相关常量：由 JNI 在类加载时读取 {@code sysconf} / 头文件定义。
 * <p>供 {@link IovArray}、{@link FileDescriptor#writev} 等限制单次 scatter/gather 规模。</p>
 */
public final class Limits {
    /** 单次 {@code writev} 允许的最大 iovec 条数 */
    public static final int IOV_MAX = iovMax();
    /** {@code UIO_MAXIOV}：用户态 iov 上限（可能与 IOV_MAX 不同） */
    public static final int UIO_MAX_IOV = uioMaxIov();
    /** 单次 read/write/writev 允许的最大字节数（通常 SSIZE_MAX） */
    public static final long SSIZE_MAX = ssizeMax();

    /** JNI {@code jlong} 字节宽度，供堆外结构体布局 */
    public static final int SIZEOF_JLONG = sizeOfjlong();

    /** 工具类不可实例化 */
    private Limits() { }
}
