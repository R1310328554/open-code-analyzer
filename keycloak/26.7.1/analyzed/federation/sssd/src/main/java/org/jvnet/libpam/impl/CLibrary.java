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

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import org.jvnet.libpam.PAMException;

/**
 * C 标准库（libc）JNA 绑定，提供 passwd/group 查询及内存分配等底层调用。
 * <p>
 * 按运行平台自动加载对应实现（{@link BSDCLibrary}、{@link LinuxCLibrary} 等）。
 *
 * @author Kohsuke Kawaguchi
 */
public interface CLibrary extends Library {
    /**
     * passwd 结构基类。对比 http://linux.die.net/man/3/getpwnam 与 macOS 实现可见字段布局跨平台差异较大，
     * 因此无法在所有平台上可靠读取真实姓名等扩展字段。
     */
    public class passwd extends Structure {
        /** 用户名 */
        public String pw_name;
        /** 加密后的密码 */
        public String pw_passwd;
        /** 用户 ID */
        public int pw_uid;
        /** 主组 ID */
        public int pw_gid;

        // ... 各平台还有大量额外字段，由子类扩展

        /**
         * 加载指定用户的 passwd 记录。
         *
         * @param userName 用户名
         * @return passwd 结构
         * @throws PAMException 无可用用户信息时抛出
         */
        public static passwd loadPasswd(String userName) throws PAMException {
            passwd pwd = libc.getpwnam(userName);
            if (pwd == null) {
                throw new PAMException("No user information is available");
            }
            return pwd;
        }

        public String getPwName() {
            return pw_name;
        }

        public String getPwPasswd() {
            return pw_passwd;
        }

        public int getPwUid() {
            return pw_uid;
        }

        public int getPwGid() {
            return pw_gid;
        }

        public String getPwGecos() {
            return null;
        }

        public String getPwDir() {
            return null;
        }

        public String getPwShell() {
            return null;
        }

        protected List getFieldOrder() {
            return Arrays.asList("pw_name", "pw_passwd", "pw_uid", "pw_gid");
        }
    }

    /** group 结构，仅映射组名等必要字段 */
    public class group extends Structure {
        /** 组名 */
        public String gr_name;
        // ... 其余字段对本库无实际用途

        protected List getFieldOrder() {
            return Arrays.asList("gr_name");
        }
    }

    /** 分配并清零 count * size 字节内存 */
    Pointer calloc(int count, int size);

    /** 复制字符串到堆内存（需调用方释放） */
    Pointer strdup(String s);

    /** 按用户名查询 passwd 结构 */
    passwd getpwnam(String username);

    /**
     * 列出用户所属 supplementary 组 ID。Linux 与多数 BSD 支持，Solaris 不支持。
     * 参见 http://www.gnu.org/software/hello/manual/gnulib/getgrouplist.html
     */
    int getgrouplist(String user, int/*gid_t*/ group, Memory groups, IntByReference ngroups);

    /**
     * Solaris 上等效的 getgrouplist 替代实现。
     * 参见 http://mail.opensolaris.org/pipermail/sparks-discuss/2008-September/000528.html
     */
    int _getgroupsbymember(String user, Memory groups, int maxgids, int numgids);

    /** 按 GID 查询 group 结构 */
    group getgrgid(int/*gid_t*/ gid);

    /** 按组名查询 group 结构 */
    group getgrnam(String name);

    // 其他用户/组相关函数可能也有用
    // 参见 http://www.gnu.org/software/libc/manual/html_node/Users-and-Groups.html#Users-and-Groups


    /** 按当前平台自动选择的 libc 单例 */
    public static final CLibrary libc = Instance.init();

    /** 延迟按平台加载对应 CLibrary 实现 */
    static class Instance {
        private static CLibrary init() {
            if (Platform.isMac() || Platform.isOpenBSD()) {
                return (CLibrary) Native.loadLibrary("c", BSDCLibrary.class);
            } else if (Platform.isFreeBSD()) {
                return (CLibrary) Native.loadLibrary("c", FreeBSDCLibrary.class);
            } else if (Platform.isSolaris()) {
                return (CLibrary) Native.loadLibrary("c", SolarisCLibrary.class);
            } else if (Platform.isLinux()) {
                return (CLibrary) Native.loadLibrary("c", LinuxCLibrary.class);
            } else {
                return (CLibrary) Native.loadLibrary("c", CLibrary.class);
            }
        }
    }
}
