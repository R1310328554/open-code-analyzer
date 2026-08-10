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

package org.keycloak.representations.info;

/**
 * JVM 堆内存使用情况的 REST 表示，供 {@link ServerInfoRepresentation} 展示服务器内存快照。
 */
public class MemoryInfoRepresentation {

    /** 堆内存上限（字节）。 */
    protected long total;
    /** 格式化的堆内存上限字符串。 */
    protected String totalFormated;
    /** 已使用堆内存（字节）。 */
    protected long used;
    /** 格式化的已用内存字符串。 */
    protected String usedFormated;
    /** 剩余可用堆内存（字节）。 */
    protected long free;
    /** 剩余内存占上限的百分比。 */
    protected long freePercentage;
    /** 格式化的剩余内存字符串。 */
    protected String freeFormated;

    /**
     * 基于当前 JVM {@link Runtime} 快照创建内存信息表示。
     *
     * @return 填充完毕的内存信息对象
     */
    public static MemoryInfoRepresentation create() {
        MemoryInfoRepresentation rep = new MemoryInfoRepresentation();
        Runtime runtime = Runtime.getRuntime();
        rep.total = runtime.maxMemory();
        rep.totalFormated = formatMemory(rep.total);
        rep.used = runtime.totalMemory() - runtime.freeMemory();
        rep.usedFormated = formatMemory(rep.used);
        rep.free = rep.total - rep.used;
        rep.freeFormated = formatMemory(rep.free);
        rep.freePercentage = rep.free * 100 / rep.total;
        return rep;
    }

    /** @return 堆内存上限（字节） */
    public long getTotal() {
        return total;
    }

    /** @return 格式化的堆内存上限 */
    public String getTotalFormated() {
        return totalFormated;
    }

    /** @return 剩余堆内存（字节） */
    public long getFree() {
        return free;
    }

    /** @return 格式化的剩余内存 */
    public String getFreeFormated() {
        return freeFormated;
    }

    /** @return 已用堆内存（字节） */
    public long getUsed() {
        return used;
    }

    /** @return 格式化的已用内存 */
    public String getUsedFormated() {
        return usedFormated;
    }

    /** @return 剩余内存百分比 */
    public long getFreePercentage() {
        return freePercentage;
    }

    /**
     * 将字节数格式化为 B、kB 或 MB 可读字符串。
     *
     * @param bytes 字节数
     * @return 格式化后的内存大小
     */
    private static String formatMemory(long bytes) {
        if (bytes > 1024L * 1024L) {
            return bytes / (1024L * 1024L) + " MB";
        } else if (bytes > 1024L) {
            return bytes / (1024L) + " kB";
        } else {
            return bytes + " B";
        }
    }

}
