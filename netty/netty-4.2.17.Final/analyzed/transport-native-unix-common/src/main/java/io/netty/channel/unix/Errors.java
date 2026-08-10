/*
 * Copyright 2015 The Netty Project
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

import io.netty.util.internal.EmptyArrays;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.NotYetConnectedException;

import static io.netty.channel.unix.ErrorsStaticallyReferencedJniMethods.errnoEAGAIN;
import static io.netty.channel.unix.ErrorsStaticallyReferencedJniMethods.errnoEBADF;
import static io.netty.channel.unix.ErrorsStaticallyReferencedJniMethods.errnoECONNRESET;
import static io.netty.channel.unix.ErrorsStaticallyReferencedJniMethods.errnoEINPROGRESS;
import static io.netty.channel.unix.ErrorsStaticallyReferencedJniMethods.errnoENOENT;
import static io.netty.channel.unix.ErrorsStaticallyReferencedJniMethods.errnoENOTCONN;
import static io.netty.channel.unix.ErrorsStaticallyReferencedJniMethods.errnoEPIPE;
import static io.netty.channel.unix.ErrorsStaticallyReferencedJniMethods.errnoEWOULDBLOCK;
import static io.netty.channel.unix.ErrorsStaticallyReferencedJniMethods.errorEALREADY;
import static io.netty.channel.unix.ErrorsStaticallyReferencedJniMethods.errorECONNREFUSED;
import static io.netty.channel.unix.ErrorsStaticallyReferencedJniMethods.errorEHOSTUNREACH;
import static io.netty.channel.unix.ErrorsStaticallyReferencedJniMethods.errorEISCONN;
import static io.netty.channel.unix.ErrorsStaticallyReferencedJniMethods.errorENETUNREACH;
import static io.netty.channel.unix.ErrorsStaticallyReferencedJniMethods.strError;

/**
 * <strong>Internal usage only!</strong>
 * <p>Static members which call JNI methods must be defined in {@link ErrorsStaticallyReferencedJniMethods}.
 * <p>原生 I/O errno 映射与异常工厂：JNI 返回负 errno，此处预缓存 {@code strerror} 并转换为  {@link IOException} / {@link ConnectException} 等 Java 异常。</p>
 */
public final class Errors {
    // JNI 失败时返回负 errno，常量预取正值再取负以便比较
    /** 路径不存在（负 errno） */
    public static final int ERRNO_ENOENT_NEGATIVE = -errnoENOENT();
    /** 套接字未连接 */
    public static final int ERRNO_ENOTCONN_NEGATIVE = -errnoENOTCONN();
    /** 无效文件描述符（通常表示已关闭） */
    public static final int ERRNO_EBADF_NEGATIVE = -errnoEBADF();
    /** 向已关闭管道/套接字写入 */
    public static final int ERRNO_EPIPE_NEGATIVE = -errnoEPIPE();
    /** 连接被对端重置 */
    public static final int ERRNO_ECONNRESET_NEGATIVE = -errnoECONNRESET();
    /** 资源暂不可用，非阻塞下应稍后重试 */
    public static final int ERRNO_EAGAIN_NEGATIVE = -errnoEAGAIN();
    /** 非阻塞操作会阻塞（常与 EAGAIN 等价） */
    public static final int ERRNO_EWOULDBLOCK_NEGATIVE = -errnoEWOULDBLOCK();
    /** 非阻塞 connect 进行中 */
    public static final int ERRNO_EINPROGRESS_NEGATIVE = -errnoEINPROGRESS();
    public static final int ERROR_ECONNREFUSED_NEGATIVE = -errorECONNREFUSED();
    public static final int ERROR_EISCONN_NEGATIVE = -errorEISCONN();
    public static final int ERROR_EALREADY_NEGATIVE = -errorEALREADY();
    public static final int ERROR_ENETUNREACH_NEGATIVE = -errorENETUNREACH();
    public static final int ERROR_EHOSTUNREACH_NEGATIVE = -errorEHOSTUNREACH();

    /**
     * Holds the mappings for errno codes to String messages.
     * This eliminates the need to call back into JNI to get the right String message on an exception
     * and thus is faster.
     * <p>启动时批量 {@code strError} 填充，异常路径避免重复 JNI 调用。</p>
     *
     * Choose an array length which should give us enough space in the future even when more errno codes
     * will be added.
     */
    private static final String[] ERRORS = new String[2048];

    /**
     * <strong>Internal usage only!</strong>
     * <p>原生 I/O 失败时携带负 errno 与可选的精简堆栈。</p>
     */
    public static final class NativeIoException extends IOException {
        private static final long serialVersionUID = 8222160204268655526L;
        private final int expectedErr;
        private final boolean fillInStackTrace;

        /** 构造并默认填充堆栈 */
        public NativeIoException(String method, int expectedErr) {
            this(method, expectedErr, true);
        }

        public NativeIoException(String method, int expectedErr, boolean fillInStackTrace) {
            super(method + "(..) failed with error(" + expectedErr + "): " + errnoString(-expectedErr));
            this.expectedErr = expectedErr;
            this.fillInStackTrace = fillInStackTrace;
        }

        /** 返回预期的负 errno 值 */
        public int expectedErr() {
            return expectedErr;
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            if (fillInStackTrace) {
                return super.fillInStackTrace();
            }
            return this;
        }
    }

    static final class NativeConnectException extends ConnectException {
        private static final long serialVersionUID = -5532328671712318161L;
        private final int expectedErr;
        NativeConnectException(String method, int expectedErr) {
            super(method + "(..) failed with error(" + expectedErr + "):" + errnoString(-expectedErr));
            this.expectedErr = expectedErr;
        }

        int expectedErr() {
            return expectedErr;
        }
    }

    static {
        for (int i = 0; i < ERRORS.length; i++) {
            // This is ok as strerror returns 'Unknown error i' when the message is not known.
            ERRORS[i] = strError(i);
        }
    }

    public static boolean handleConnectErrno(String method, int err) throws IOException {
        if (err == ERRNO_EINPROGRESS_NEGATIVE || err == ERROR_EALREADY_NEGATIVE) {
            // connect 未完成，需等待可写事件；CentOS8 TCP FastOpen 上可能见 EALREADY
            return false;
        }
        throw newConnectException0(method, err);
    }

    /**
     * @deprecated Use {@link #handleConnectErrno(String, int)}.
     * @param method The native method name which caused the errno.
     * @param err the negative value of the errno.
     * @throws IOException The errno translated into an exception.
     * <p>将 connect errno 转为 Java 连接异常（已废弃，请用 {@link #handleConnectErrno}）。</p>
     */
    @Deprecated
    public static void throwConnectException(String method, int err) throws IOException {
        if (err == ERROR_EALREADY_NEGATIVE) {
            throw new ConnectionPendingException();
        }
        throw newConnectException0(method, err);
    }

    private static String errnoString(int err) {
        // 优先查预缓存表，超出范围再 JNI strError
        if (err < ERRORS.length - 1) {
            return ERRORS[err];
        }
        return strError(err);
    }

    private static IOException newConnectException0(String method, int err) {
        if (err == ERROR_ENETUNREACH_NEGATIVE || err == ERROR_EHOSTUNREACH_NEGATIVE) {
            return new NoRouteToHostException();
        }
        if (err == ERROR_EISCONN_NEGATIVE) {
            throw new AlreadyConnectedException();
        }
        if (err == ERRNO_ENOENT_NEGATIVE) {
            return new FileNotFoundException();
        }
        return new ConnectException(method + "(..) failed with error(" + err + "): " + errnoString(-err));
    }

    public static NativeIoException newConnectionResetException(String method, int errnoNegative) {
        NativeIoException exception = new NativeIoException(method, errnoNegative, false);
        exception.setStackTrace(EmptyArrays.EMPTY_STACK_TRACE);
        return exception;
    }

    public static NativeIoException newIOException(String method, int err) {
        return new NativeIoException(method, err);
    }

    @Deprecated
    public static int ioResult(String method, int err, NativeIoException resetCause,
                               ClosedChannelException closedCause) throws IOException {
        // 内核暂不可服务（EAGAIN/EWOULDBLOCK），返回 0 表示稍后重试
        if (err == ERRNO_EAGAIN_NEGATIVE || err == ERRNO_EWOULDBLOCK_NEGATIVE) {
            return 0;
        }
        if (err == resetCause.expectedErr()) {
            throw resetCause;
        }
        if (err == ERRNO_EBADF_NEGATIVE) {
            throw closedCause;
        }
        if (err == ERRNO_ENOTCONN_NEGATIVE) {
            throw new NotYetConnectedException();
        }
        if (err == ERRNO_ENOENT_NEGATIVE) {
            throw new FileNotFoundException();
        }

        // TODO: 其余 errno 可进一步预实例化 IOException；复杂错误仍保留堆栈
        throw newIOException(method, err);
    }

    public static int ioResult(String method, int err) throws IOException {
        // network stack saturated... try again later
        if (err == ERRNO_EAGAIN_NEGATIVE || err == ERRNO_EWOULDBLOCK_NEGATIVE) {
            return 0;
        }
        if (err == ERRNO_EBADF_NEGATIVE) {
            throw new ClosedChannelException();
        }
        if (err == ERRNO_ENOTCONN_NEGATIVE) {
            throw new NotYetConnectedException();
        }
        if (err == ERRNO_ENOENT_NEGATIVE) {
            throw new FileNotFoundException();
        }

        throw new NativeIoException(method, err, false);
    }

    private Errors() { }
}
