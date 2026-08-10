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
 * <p>打破 JNI 注册与类静态初始化循环依赖：errno 常量由独立类在 {@link Errors} 静态块中调用； 本类仅声明 native 方法，不含会触发 JNI 的静态字段。</p>
 * Static members which call JNI methods must not be declared in this class!
 */
final class ErrorsStaticallyReferencedJniMethods {

    private ErrorsStaticallyReferencedJniMethods() { }

    /** ENOENT：无此文件或目录 */
    static native int errnoENOENT();
    /** EBADF：坏文件描述符 */
    static native int errnoEBADF();
    /** EPIPE：断开的管道 */
    static native int errnoEPIPE();
    /** ECONNRESET：连接被重置 */
    static native int errnoECONNRESET();
    /** ENOTCONN：套接字未连接 */
    static native int errnoENOTCONN();
    /** EAGAIN：资源暂不可用 */
    static native int errnoEAGAIN();
    /** EWOULDBLOCK：操作会阻塞 */
    static native int errnoEWOULDBLOCK();
    /** EINPROGRESS：操作正在进行中 */
    static native int errnoEINPROGRESS();
    static native int errorECONNREFUSED();
    static native int errorEISCONN();
    static native int errorEALREADY();
    static native int errorENETUNREACH();
    static native int errorEHOSTUNREACH();
    /** 对应 {@code strerror(3)}，返回 errno 描述字符串 */
    static native String strError(int err);
}
