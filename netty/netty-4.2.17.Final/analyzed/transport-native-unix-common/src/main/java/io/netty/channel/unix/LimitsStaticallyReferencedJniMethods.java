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

/**
 * This class is necessary to break the following cyclic dependency:
 * <ol>
 * <li>JNI_OnLoad</li>
 * <li>JNI Calls FindClass because RegisterNatives (used to register JNI methods) requires a class</li>
 * <li>FindClass loads the class, but static members variables of that class attempt to call a JNI method which has not
 * yet been registered.</li>
 * <li>java.lang.UnsatisfiedLinkError is thrown because native method has not yet been registered.</li>
 * </ol>
 * <p>打破 JNI 注册与 {@link Limits} 静态初始化循环依赖：平台 I/O 上限常量由  {@link Limits} 在静态块中调用；本类仅声明 native 方法，不含会触发 JNI 的静态字段。</p>
 * Static members which call JNI methods must not be declared in this class!
 */
final class LimitsStaticallyReferencedJniMethods {
    private LimitsStaticallyReferencedJniMethods() { }

    /** {@code SSIZE_MAX}：单次 read/write/writev 允许的最大字节数 */
    static native long ssizeMax();
    /** {@code IOV_MAX}：单次 writev 允许的最大 iovec 条数 */
    static native int iovMax();
    /** {@code UIO_MAXIOV}：用户态 iov 上限 */
    static native int uioMaxIov();
    /** JNI {@code jlong} 字节宽度，供堆外结构体布局 */
    static native int sizeOfjlong();
    /** {@code sockaddr_un.sun_path} 最大长度（含 NUL） */
    static native int udsSunPathSize();
}
