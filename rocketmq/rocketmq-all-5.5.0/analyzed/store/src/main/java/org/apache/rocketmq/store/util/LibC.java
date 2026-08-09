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
package org.apache.rocketmq.store.util;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;

/**
 * JNA 封装的 libc 接口：提供 mlock、madvise、msync 等内存映射系统调用。
 */
public interface LibC extends Library {
    /** libc 单例实例。 */
    LibC INSTANCE = (LibC) Native.loadLibrary(Platform.isWindows() ? "msvcrt" : "c", LibC.class);

    /** madvise 正常访问模式。 */
    int MADV_NORMAL = 0;
    /** madvise 随机访问模式。 */
    int MADV_RANDOM = 1;
    /** madvise 预读提示。 */
    int MADV_WILLNEED = 3;
    /** madvise 释放页缓存提示。 */
    int MADV_DONTNEED = 4;

    int MCL_CURRENT = 1;
    int MCL_FUTURE = 2;
    int MCL_ONFAULT = 4;

    /** 异步同步内存到磁盘。 */
    /* sync memory asynchronously */
    int MS_ASYNC = 0x0001;
    /** 使映射与缓存失效。 */
    /* invalidate mappings & caches */
    int MS_INVALIDATE = 0x0002;
    /** 同步刷盘内存映射。 */
    /* synchronous memory sync */
    /** msync 同步刷盘标志。 */
    int MS_SYNC = 0x0004;

    int mlock(Pointer var1, NativeLong var2);

    int munlock(Pointer var1, NativeLong var2);

    int madvise(Pointer var1, NativeLong var2, int var3);

    Pointer memset(Pointer p, int v, long len);

    int mlockall(int flags);

    int msync(Pointer p, NativeLong length, int flags);

    int mincore(Pointer p, NativeLong length, byte[] vec);

    int getpagesize();
}
