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
package org.jvnet.libpam;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;
import org.jvnet.libpam.impl.CLibrary.group;
import org.jvnet.libpam.impl.CLibrary.passwd;

import static org.jvnet.libpam.impl.CLibrary.libc;

/**
 * Unix 用户不可变表示，封装 passwd 结构与 supplementary 组列表。
 *
 * @author Kohsuke Kawaguchi
 */
public class UnixUser {
    private final String userName, gecos, dir, shell;
    private final int uid, gid;
    private final Set<String> groups;

    /*package*/ UnixUser(String userName, passwd pwd) throws PAMException {
        this.userName = userName;
        this.gecos = pwd.getPwGecos();
        this.dir = pwd.getPwDir();
        this.shell = pwd.getPwShell();
        this.uid = pwd.getPwUid();
        this.gid = pwd.getPwGid();

        int sz = 4; /*sizeof(gid_t)*/

        int ngroups = 64;
        Memory m = new Memory(ngroups * sz);
        IntByReference pngroups = new IntByReference(ngroups);
        try {
            if (libc.getgrouplist(userName, pwd.getPwGid(), m, pngroups) < 0) {
                // 组数量超出预分配，扩大缓冲区重试
                m = new Memory(pngroups.getValue() * sz);
                if (libc.getgrouplist(userName, pwd.getPwGid(), m, pngroups) < 0)
                    // 不应发生，兜底抛出异常
                    throw new PAMException("getgrouplist failed");
            }
            ngroups = pngroups.getValue();
        } catch (LinkageError e) {
            // 部分平台（如 Solaris）无 getgrouplist，改用 _getgroupsbymember
            ngroups = libc._getgroupsbymember(userName, m, ngroups, 0);
            if (ngroups < 0)
                throw new PAMException("_getgroupsbymember failed");
        }

        groups = new HashSet<String>();
        for (int i = 0; i < ngroups; i++) {
            int gid = m.getInt(i * sz);
            group grp = libc.getgrgid(gid);
            if (grp == null) {
                continue;
            }
            groups.add(grp.gr_name);
        }
    }

    public UnixUser(String userName) throws PAMException {
        this(userName, passwd.loadPasswd(userName));
    }

    /**
     * 模拟用拷贝构造器，仅供测试；签名可能变更。
     */
    protected UnixUser(String userName, String gecos, String dir, String shell, int uid, int gid, Set<String> groups) {
        this.userName = userName;
        this.gecos = gecos;
        this.dir = dir;
        this.shell = shell;
        this.uid = uid;
        this.gid = gid;
        this.groups = groups;
    }

    /** 返回 Unix 账户名，永不为 null。 */
    public String getUserName() {
        return userName;
    }

    /** 返回用户 UID。 */
    public int getUID() {
        return uid;
    }

    /** 返回用户主 GID。 */
    public int getGID() {
        return gid;
    }

    /** 返回 gecos 字段（真实姓名）。 */
    public String getGecos() {
        return gecos;
    }

    /** 返回用户主目录路径。 */
    public String getDir() {
        return dir;
    }

    /** 返回用户默认 shell。 */
    public String getShell() {
        return shell;
    }

    /**
     * 返回用户所属组名集合。
     *
     * @return 永不为 null 的不可修改集合
     */
    public Set<String> getGroups() {
        return Collections.unmodifiableSet(groups);
    }

    /** 检查指定 Unix 用户名是否存在。 */
    public static boolean exists(String name) {
        return libc.getpwnam(name) != null;
    }
}
