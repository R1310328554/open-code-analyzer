package com.taobao.arthas.common;

/**
 * 按当前 OS/CPU 架构选择 Arthas JNI 本地库文件名（供 VmTool 等加载）。
 *
 * @author hengyunabc 2021-04-27
 */
public class VmToolUtils {
    /** 检测到的本地库文件名，不支持的平台可能为 null */
    private static String libName = null;
    static {
        if (OSUtils.isMac()) {
            libName = "libArthasJniLibrary.dylib";
        }
        if (OSUtils.isLinux()) {
            if (OSUtils.isArm32()) {
                libName = "libArthasJniLibrary-arm.so";
            } else if (OSUtils.isArm64()) {
                libName = "libArthasJniLibrary-aarch64.so";
            } else if (OSUtils.isX86_64()) {
                libName = "libArthasJniLibrary-x64.so";
            } else if (OSUtils.isLoongArch64()) {
                libName = "libArthasJniLibrary-loongarch64.so";
            }else {
                libName = "libArthasJniLibrary-" + OSUtils.arch() + ".so";
            }
        }
        if (OSUtils.isWindows()) {
            libName = "libArthasJniLibrary-x64.dll";
            if (OSUtils.isX86()) {
                libName = "libArthasJniLibrary-x86.dll";
            }
        }
    }

    /** 返回当前平台对应的 JNI 库名 */
    public static String detectLibName() {
        return libName;
    }
}
