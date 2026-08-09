package com.taobao.arthas.common;

/**
 * Arthas 支持的操作系统平台枚举。
 *
 */
public enum PlatformEnum {
    /**
     * Microsoft Windows
     */
    WINDOWS,
    /**
     * Linux 及其发行版
     */
    LINUX,
    /**
     * macOS (OS X)
     */
    MACOSX,

    /** 未识别平台 */
    UNKNOWN
}
