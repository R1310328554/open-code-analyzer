package com.taobao.arthas.common;

import java.io.File;
import java.util.Locale;

/**
 * 操作系统与 CPU 架构探测：启动时解析 {@code os.name}/{@code os.arch}，供端口、JNI、ANSI 等分支使用。
 *
 * @author hengyunabc 2018-11-08
 */
public class OSUtils {
    /** 小写化的 {@code os.name} */
    private static final String OPERATING_SYSTEM_NAME = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
    /** 小写化的 {@code os.arch} */
    private static final String OPERATING_SYSTEM_ARCH = System.getProperty("os.arch").toLowerCase(Locale.ENGLISH);
    private static final String UNKNOWN = "unknown";

    /** 当前平台枚举 */
    static PlatformEnum platform;

    /** 归一化后的架构标识，如 x86_64、aarch_64 */
    static String arch;

    static {
        if (OPERATING_SYSTEM_NAME.startsWith("linux")) {
            platform = PlatformEnum.LINUX;
        } else if (OPERATING_SYSTEM_NAME.startsWith("mac") || OPERATING_SYSTEM_NAME.startsWith("darwin")) {
            platform = PlatformEnum.MACOSX;
        } else if (OPERATING_SYSTEM_NAME.startsWith("windows")) {
            platform = PlatformEnum.WINDOWS;
        } else {
            platform = PlatformEnum.UNKNOWN;
        }

        arch = normalizeArch(OPERATING_SYSTEM_ARCH);
    }

    private OSUtils() {
    }

    /** 是否为 Windows */
    public static boolean isWindows() {
        return platform == PlatformEnum.WINDOWS;
    }

    /** 是否为 Linux */
    public static boolean isLinux() {
        return platform == PlatformEnum.LINUX;
    }

    /** 是否为 macOS */
    public static boolean isMac() {
        return platform == PlatformEnum.MACOSX;
    }

    /**
     * 是否在 Cygwin 或 MinGW 环境下运行（Windows 上仍可使用 ANSI 颜色等 Unix 特性）。
     */
    public static boolean isCygwinOrMinGW() {
        if (isWindows()) {
            if ((System.getenv("MSYSTEM") != null && System.getenv("MSYSTEM").startsWith("MINGW"))
                            || "/bin/bash".equals(System.getenv("SHELL"))) {
                return true;
            }
        }
        return false;
    }

	/** 返回归一化架构字符串 */
	public static String arch() {
		return arch;
	}

	public static boolean isArm32() {
		return "arm_32".equals(arch);
	}

	public static boolean isArm64() {
		return "aarch_64".equals(arch);
	}

	public static boolean isX86() {
    	return "x86_32".equals(arch);
	}

	public static boolean isX86_64() {
		return "x86_64".equals(arch);
	}

       /** 是否为龙芯 LoongArch64 */
       public static boolean isLoongArch64() {
               return "loongarch_64".equals(arch);
       }


	/** 将多种 os.arch 写法映射为统一内部名称 */
	private static String normalizeArch(String value) {
		value = normalize(value);
		if (value.matches("^(x8664|amd64|ia32e|em64t|x64)$")) {
			return "x86_64";
		}
		if (value.matches("^(x8632|x86|i[3-6]86|ia32|x32)$")) {
			return "x86_32";
		}
		if (value.matches("^(ia64w?|itanium64)$")) {
			return "itanium_64";
		}
		if ("ia64n".equals(value)) {
			return "itanium_32";
		}
		if (value.matches("^(sparc|sparc32)$")) {
			return "sparc_32";
		}
		if (value.matches("^(sparcv9|sparc64)$")) {
			return "sparc_64";
		}
		if (value.matches("^(arm|arm32)$")) {
			return "arm_32";
		}
		if ("aarch64".equals(value)) {
			return "aarch_64";
		}
		if (value.matches("^(mips|mips32)$")) {
			return "mips_32";
		}
		if (value.matches("^(mipsel|mips32el)$")) {
			return "mipsel_32";
		}
		if ("mips64".equals(value)) {
			return "mips_64";
		}
		if ("mips64el".equals(value)) {
			return "mipsel_64";
		}
		if (value.matches("^(ppc|ppc32)$")) {
			return "ppc_32";
		}
		if (value.matches("^(ppcle|ppc32le)$")) {
			return "ppcle_32";
		}
		if ("ppc64".equals(value)) {
			return "ppc_64";
		}
		if ("ppc64le".equals(value)) {
			return "ppcle_64";
		}
		if ("s390".equals(value)) {
			return "s390_32";
		}
		if ("s390x".equals(value)) {
			return "s390_64";
		}
		return value;
	}

	/** 是否使用 musl libc（通过动态链接器路径判断） */
	public static boolean isMuslLibc() {
		File ld_musl_x86_64_file = new File("/lib/ld-musl-x86_64.so.1");
		File ld_musl_aarch64_file = new File("/lib/ld-musl-aarch64.so.1");

		if(ld_musl_x86_64_file.exists() || ld_musl_aarch64_file.exists()){
			return true;
		}

		return false;
	}

	private static String normalize(String value) {
		if (value == null) {
			return "";
		}
		return value.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
	}
}
