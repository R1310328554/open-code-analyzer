/*
 *  The MIT License
 * 
 *  Copyright 2011, Sun Microsystems, Inc.
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 * 
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package org.jvnet.libpam.impl;

/**
 * BSD 系（macOS、OpenBSD 等）C 标准库 JNA 绑定，扩展 {@link CLibrary} 以返回 {@link BSDPasswd}。
 *
 * @author Sebastian Sdorra
 */
public interface BSDCLibrary extends CLibrary {

    /**
     * 按用户名查询 passwd 结构。
     *
     * @param username 用户名
     * @return 对应 passwd 记录，不存在时返回 null
     */
    BSDPasswd getpwnam(String username);

}
