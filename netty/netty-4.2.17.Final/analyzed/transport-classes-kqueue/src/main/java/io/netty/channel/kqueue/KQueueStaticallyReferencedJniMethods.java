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
package io.netty.channel.kqueue;

/**
 * This class is necessary to break the following cyclic dependency:
 * <ol>
 * <li>JNI_OnLoad</li>
 * <li>JNI Calls FindClass because RegisterNatives (used to register JNI methods) requires a class</li>
 * <li>FindClass loads the class, but static members variables of that class attempt to call a JNI method which has not
 * yet been registered.</li>
 * <li>java.lang.UnsatisfiedLinkError is thrown because native method has not yet been registered.</li>
 * </ol>
 * Static members which call JNI methods must not be declared in this class!
 * <p>打破 JNI_OnLoad 与 FindClass 的循环依赖： 本类仅声明 native 方法，不在静态字段中调用 JNI。</p>
 */
final class KQueueStaticallyReferencedJniMethods {
    private KQueueStaticallyReferencedJniMethods() { }

    /** EV_ADD 常量（JNI 从 native 读取） */
    static native short evAdd();
    static native short evEnable();
    static native short evDisable();
    static native short evDelete();
    static native short evClear();
    static native short evEOF();
    static native short evError();

    // EVFILT_SOCK 的 NOTE_* fflags，与内核/userspace 共享
    static native short noteReadClosed();
    static native short noteConnReset();
    static native short noteDisconnected();

    static native short evfiltRead();
    static native short evfiltWrite();
    static native short evfiltUser();
    static native short evfiltSock();

    // connectx(2) 连接标志
    static native int connectResumeOnReadWrite();
    static native int connectDataIdempotent();

    // sysctl 探测 TCP FastOpen 客户端/服务端支持
    static native int fastOpenClient();
    static native int fastOpenServer();
}
