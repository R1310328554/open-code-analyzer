/*
 * Copyright 2020 The Netty Project
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
package io.netty.handler.codec.http.multipart;

import java.io.File;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JVM 关闭钩子：在进程退出时删除已注册的临时 multipart 磁盘文件。
 */
final class DeleteFileOnExitHook {
    private static final Set<String> FILES = ConcurrentHashMap.newKeySet();

    private DeleteFileOnExitHook() {
    }

    static {
        // 须在其它 shutdown hook 之后执行，以便应用 hook 仍可向列表添加文件
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                runHook();
            }
        });
    }

    /**
     * 文件已手动删除时从池中移除，减小集合占用。
     *
     * @param file tmp file path
     */
    public static void remove(String file) {
        FILES.remove(file);
    }

    /**
     * 注册临时文件路径，进程退出时由 {@link #runHook()} 删除。
     *
     * @param file tmp file path
     */
    public static void add(String file) {
        FILES.add(file);
    }

    /**
     * Check in the hook files.
     *
     * @param file target file
     * @return true or false
     */
    public static boolean checkFileExist(String file) {
        return FILES.contains(file);
    }

    /**
     * 关闭钩子入口：遍历并删除所有已注册文件。
     */
    static void runHook() {
        for (String filename : FILES) {
            new File(filename).delete();
        }
    }
}
