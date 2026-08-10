/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.client.cli.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.Formatter;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.keycloak.common.util.StreamUtil;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isRegularFile;

/**
 * CLI 文件/流 I/O 与控制台输出工具。
 * <p>
 * 支持从文件或标准输入读取、流拷贝，以及创建仅属主可读写（POSIX/ACL）的配置文件。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class IoUtil {

    /**
     * 从文件路径或 {@code "-"}（标准输入）读取全部 UTF-8 文本。
     *
     * @param file 文件路径，{@code "-"} 表示 stdin
     * @return 文件或 stdin 内容
     */
    public static String readFileOrStdin(String file) {
        String content;
        if ("-".equals(file)) {
            content = readFully(System.in);
        } else {
            try (InputStream is = new FileInputStream(file)) {
                content = readFully(is);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("File not found: " + file);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read file: " + file, e);
            }
        }
        return content;
    }

    /** 以 UTF-8 读取输入流全部内容为字符串。 */
    public static String readFully(InputStream is) {
        try {
            return StreamUtil.readString(is, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read stream", e);
        }
    }

    /** 将输入流拷贝到输出流（8 KiB 缓冲），结束时刷新输出。 */
    public static void copyStream(InputStream is, OutputStream os) {

        byte [] buf = new byte[8192];

        int rc;
        try (InputStream input = is) {
            while ((rc = input.read(buf)) != -1) {
                os.write(buf, 0, rc);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read/write a stream: ", e);
        } finally {
            try {
                os.flush();
            } catch (IOException e) {
                throw new RuntimeException("Failed to write a stream: ", e);
            }
        }
    }

    /**
     * 确保配置文件及其父目录存在，并限制为仅属主可访问。
     * <p>
     * POSIX 系统设置 {@code 0600/0700} 权限；Windows 通过 ACL 仅保留属主条目。
     *
     * @param path 目标配置文件路径
     * @throws IOException 创建或权限设置失败时抛出
     */
    public static void ensureFile(Path path) throws IOException {

        FileSystem fs = FileSystems.getDefault();
        Set<String> supportedViews = fs.supportedFileAttributeViews();
        Path parent = path.getParent();

        if (!isDirectory(parent)) {
            createDirectories(parent);
            // 确保仅属主可读写目录
            if (supportedViews.contains("posix")) {
                setUnixPermissions(parent);
            } else if (supportedViews.contains("acl")) {
                setWindowsPermissions(parent);
            } else {
                warnErr("Failed to restrict access permissions on .keycloak directory: " + parent);
            }
        }
        if (!isRegularFile(path)) {
            createFile(path);
            // 确保仅属主可读写文件
            if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
                setUnixPermissions(path);
            } else if (supportedViews.contains("acl")) {
                setWindowsPermissions(path);
            } else {
                warnErr("Failed to restrict access permissions on config file: " + path);
            }
        }
    }

    /** 设置 Unix 属主读/写（目录额外可执行）权限。 */
    private static void setUnixPermissions(Path path) throws IOException {
        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        if (isDirectory(path)) {
            perms.add(PosixFilePermission.OWNER_EXECUTE);
        }
        Files.setPosixFilePermissions(path, perms);
    }

    /** 重建 Windows ACL，仅保留文件属主完整权限。 */
    private static void setWindowsPermissions(Path path) throws IOException {
        AclFileAttributeView view = Files.getFileAttributeView(path, AclFileAttributeView.class);
        UserPrincipal owner = view.getOwner();
        List<AclEntry> acl = view.getAcl();
        ListIterator<AclEntry> it = acl.listIterator();
        while (it.hasNext()) {
            AclEntry entry = it.next();
            if ("BUILTIN\\Administrators".equals(entry.principal().getName()) || "NT AUTHORITY\\SYSTEM".equals(entry.principal().getName())) {
                continue;
            }
            it.remove();
        }
        AclEntry entry = AclEntry.newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(owner)
                .setPermissions(AclEntryPermission.READ_DATA, AclEntryPermission.WRITE_DATA,
                        AclEntryPermission.APPEND_DATA, AclEntryPermission.READ_NAMED_ATTRS,
                        AclEntryPermission.WRITE_NAMED_ATTRS, AclEntryPermission.EXECUTE,
                        AclEntryPermission.READ_ATTRIBUTES, AclEntryPermission.WRITE_ATTRIBUTES,
                        AclEntryPermission.DELETE, AclEntryPermission.READ_ACL, AclEntryPermission.SYNCHRONIZE)
                .build();
        acl.add(entry);
        view.setAcl(acl);
    }

    /** 向标准输出打印一行。 */
    public static void printOut(String msg) {
        System.out.println(msg);
    }

    /** 向标准错误打印一行。 */
    public static void printErr(String msg) {
        System.err.println(msg);
    }

    /** 格式化输出到 stdout（前缀 WARN）。 */
    public static void printfOut(String format, String ... params) {
        System.out.println(new Formatter().format("WARN: " + format, params));
    }

    /** 向 stdout 打印 WARN 前缀警告。 */
    public static void warnOut(String msg) {
        System.out.println("WARN: " + msg);
    }

    /** 向 stderr 打印 WARN 前缀警告。 */
    public static void warnErr(String msg) {
        System.err.println("WARN: " + msg);
    }

    /** 格式化 WARN 输出到 stdout。 */
    public static void warnfOut(String format, String ... params) {
        System.out.println(new Formatter().format("WARN: " + format, params));
    }

    /** 格式化 WARN 输出到 stderr。 */
    public static void warnfErr(String format, String ... params) {
        System.err.println(new Formatter().format("WARN: " + format, params));
    }

    /** 向 stdout 打印 LOG 前缀日志行。 */
    public static void logOut(String msg) {
        System.out.println("LOG: " + msg);
    }
}
