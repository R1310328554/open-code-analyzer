/*
 * The MIT License
 *
 * Copyright (c) 2009, Sun Microsystems, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jvnet.libpam.impl;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;

import static org.jvnet.libpam.impl.CLibrary.libc;

/**
 * libpam.so JNA 绑定，映射 PAM 认证 API 及相关结构体。
 * <p>
 * 在线参考：http://www.opengroup.org/onlinepubs/008329799/apdxa.htm（pam_appl.h）
 *
 * @author Kohsuke Kawaguchi
 */
public interface PAMLibrary extends Library {
    /** PAM 会话句柄 */
    class pam_handle_t extends PointerType {
        public pam_handle_t() {
        }

        public pam_handle_t(Pointer pointer) {
            super(pointer);
        }
    }

    /** PAM 向应用发送的提示/错误消息 */
    class pam_message extends Structure {
        /** 消息类型（如 {@link #PAM_PROMPT_ECHO_OFF}） */
        public int msg_style;
        /** 消息文本 */
        public String msg;

        /**
         * 绑定到指定指针处的内存区域并读取结构内容。
         *
         * @param src 原生内存指针
         */
        public pam_message(Pointer src) {
            useMemory(src);
            read();
        }

        protected List getFieldOrder() {
            return Arrays.asList("msg_style", "msg");
        }
    }

    /** 应用返回给 PAM 的响应 */
    class pam_response extends Structure {
        /**
         * 实际为字符串，但须由 conversation 回调通过 malloc 分配、由调用方释放，
         * 故此处绑定为 {@link Pointer}。
         * <p>
         * 手册未明确说明，但 NetBSD 文档与实测均要求使用 strdup，否则 libpam 会崩溃。
         * 参见 http://www.netbsd.org/docs/guide/en/chap-pam.html#pam-sample-conv
         */
        public Pointer resp;
        /** 响应返回码 */
        public int resp_retcode;

        /**
         * 绑定到指定指针处的内存区域并读取结构内容。
         *
         * @param src 原生内存指针
         */
        public pam_response(Pointer src) {
            useMemory(src);
            read();
        }

        public pam_response() {
        }

        /**
         * 设置响应字符串（内部通过 {@link CLibrary#strdup(String)} 分配）。
         *
         * @param msg 响应内容
         */
        public void setResp(String msg) {
            this.resp = libc.strdup(msg);
        }

        protected List getFieldOrder() {
            return Arrays.asList("resp", "resp_retcode");
        }

        /** 单个 pam_response 结构体的字节大小 */
        public static final int SIZE = new pam_response().size();
    }

    /** PAM conversation 回调结构 */
    class pam_conv extends Structure {
        public interface PamCallback extends Callback {
            /**
             * PAM conversation 回调。resp 及其字符串成员均须由 malloc 分配，供调用方释放。
             * 参见 http://www.netbsd.org/docs/guide/en/chap-pam.html#pam-sample-conv
             */
            int callback(int num_msg, Pointer msg, Pointer resp, Pointer _ptr);
        }

        /** conversation 回调函数 */
        public PamCallback conv;
        /** 应用私有数据指针 */
        public Pointer _ptr;

        public pam_conv(PamCallback conv) {
            this.conv = conv;
        }

        protected List getFieldOrder() {
            return Arrays.asList("conv", "_ptr");
        }
    }

    /** 启动 PAM 认证会话 */
    int pam_start(String service, String user, pam_conv conv, PointerByReference/* pam_handle_t** */ pamh_p);

    /** 结束 PAM 会话 */
    int pam_end(pam_handle_t handle, int pam_status);

    /** 设置 PAM 会话项（如用户名） */
    int pam_set_item(pam_handle_t handle, int item_type, String item);

    /** 获取 PAM 会话项 */
    int pam_get_item(pam_handle_t handle, int item_type, PointerByReference item);

    /** 执行用户认证 */
    int pam_authenticate(pam_handle_t handle, int flags);

    /** 设置用户凭证 */
    int pam_setcred(pam_handle_t handle, int flags);

    /** 账户管理（如账户是否过期） */
    int pam_acct_mgmt(pam_handle_t handle, int flags);

    /** 将 PAM 错误码转为可读字符串 */
    String pam_strerror(pam_handle_t handle, int pam_error);

    /** PAM 项类型：用户名 */
    final int PAM_USER = 2;

    // 错误码
    /** 操作成功 */
    final int PAM_SUCCESS = 0;
    /** conversation 回调失败 */
    final int PAM_CONV_ERR = 6;


    /** 获取响应时不回显输入 */
    final int PAM_PROMPT_ECHO_OFF = 1; /* Echo off when getting response */
    /** 获取响应时回显输入 */
    final int PAM_PROMPT_ECHO_ON = 2; /* Echo on when getting response */
    /** 错误消息 */
    final int PAM_ERROR_MSG = 3; /* Error message */
    /** 纯文本提示信息 */
    final int PAM_TEXT_INFO = 4; /* Textual information */

    /** libpam 单例 */
    public static final PAMLibrary libpam = (PAMLibrary) Native.loadLibrary("pam", PAMLibrary.class);
}
