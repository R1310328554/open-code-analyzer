/*
 * Copyright The async-profiler authors
 * SPDX-License-Identifier: Apache-2.0
 */

package one.profiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * async-profiler 进程内性能分析 Java API，封装本地库 libasyncProfiler.so。
 * <p>
 * 单例模式：首次调用 {@link #getInstance()} 时按平台加载本地库
 * （支持 -agentpath 预加载、系统属性指定路径或内嵌 so 解压）。
 */
public class AsyncProfiler implements AsyncProfilerMXBean {
    private static AsyncProfiler instance;

    private AsyncProfiler() {
    }

    /** 使用默认路径加载本地库并返回单例。 */
    public static AsyncProfiler getInstance() {
        return getInstance(null);
    }

    /**
     * 获取单例；可显式指定本地库绝对路径。
     *
     * @param libPath 本地库路径，null 时走自动探测逻辑
     * @return AsyncProfiler 单例
     */
    public static synchronized AsyncProfiler getInstance(String libPath) {
        if (instance != null) {
            return instance;
        }

        AsyncProfiler profiler = new AsyncProfiler();
        if (libPath != null) {
            System.load(libPath);
        } else {
            try {
                // 若已通过 -agentpath 预加载，getVersion 不会抛 UnsatisfiedLinkError
                profiler.getVersion();
            } catch (UnsatisfiedLinkError e) {
                String libraryPath = System.getProperty("one.profiler.libraryPath");
                if (libraryPath != null && !libraryPath.isEmpty()) {
                    System.load(new File(libraryPath).getAbsolutePath());
                } else {
                    File file = extractEmbeddedLib();
                    if (file != null) {
                        try {
                            System.load(file.getAbsolutePath());
                        } finally {
                            file.delete();
                        }
                    } else {
                        System.loadLibrary("asyncProfiler");
                    }
                }

            }
        }

        instance = profiler;
        return profiler;
    }

    /** 从 classpath 资源解压对应平台的 libasyncProfiler.so 到临时文件。 */
    private static File extractEmbeddedLib() {
        String resourceName = "/" + getPlatformTag() + "/libasyncProfiler.so";
        InputStream in = AsyncProfiler.class.getResourceAsStream(resourceName);
        if (in == null) {
            return null;
        }

        try {
            String extractPath = System.getProperty("one.profiler.extractPath");
            File file = File.createTempFile("libasyncProfiler-", ".so",
                    extractPath == null || extractPath.isEmpty() ? null : new File(extractPath));
            try (FileOutputStream out = new FileOutputStream(file)) {
                byte[] buf = new byte[32000];
                for (int bytes; (bytes = in.read(buf)) >= 0; ) {
                    out.write(buf, 0, bytes);
                }
            }
            return file;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /** 根据 os.name / os.arch 返回内嵌 so 的资源目录标签。 */
    private static String getPlatformTag() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();
        if (os.contains("linux")) {
            if (arch.equals("amd64") || arch.equals("x86_64") || arch.contains("x64")) {
                return "linux-x64";
            } else if (arch.equals("aarch64") || arch.contains("arm64")) {
                return "linux-arm64";
            } else if (arch.equals("aarch32") || arch.contains("arm")) {
                return "linux-arm32";
            } else if (arch.contains("86")) {
                return "linux-x86";
            } else if (arch.contains("ppc64")) {
                return "linux-ppc64le";
            }
        } else if (os.contains("mac")) {
            return "macos";
        }
        throw new UnsupportedOperationException("Unsupported platform: " + os + "-" + arch);
    }

    /**
     * 启动采样分析并重置已采集数据。
     *
     * @param event 采样事件类型，见 {@link Events}
     * @param interval 采样间隔（如 CPU 事件为纳秒）
     * @throws IllegalStateException 分析器已在运行时再次 start
     */
    @Override
    public void start(String event, long interval) throws IllegalStateException {
        if (event == null) {
            throw new NullPointerException();
        }
        start0(event, interval, true);
    }

    /**
     * 启动或恢复采样，不重置已有样本；事件与间隔可与上次会话不同。
     *
     * @param event 采样事件类型，见 {@link Events}
     * @param interval 采样间隔
     * @throws IllegalStateException 分析器已在运行时再次 resume
     */
    @Override
    public void resume(String event, long interval) throws IllegalStateException {
        if (event == null) {
            throw new NullPointerException();
        }
        start0(event, interval, false);
    }

    /**
     * 停止采样（不导出结果）。
     *
     * @throws IllegalStateException 分析器未在运行
     */
    @Override
    public void stop() throws IllegalStateException {
        stop0();
    }

    /**
     * 获取当前会话已采集的样本数。
     *
     * @return 样本数量
     */
    @Override
    public native long getSamples();

    /**
     * 获取 profiler 代理版本号，如 "1.0"。
     *
     * @return 版本字符串
     */
    @Override
    public String getVersion() {
        try {
            return execute0("version");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 执行与 agent 兼容的分析命令（逗号分隔参数，定义见 arguments.cpp）。
     *
     * @param command 分析命令字符串
     * @return 命令执行结果
     * @throws IllegalArgumentException 命令解析失败
     * @throws IOException 输出文件创建失败
     */
    @Override
    public String execute(String command) throws IllegalArgumentException, IllegalStateException, IOException {
        if (command == null) {
            throw new NullPointerException();
        }
        return execute0(command);
    }

    /**
     * 以 collapsed stacktraces 格式导出火焰图数据。
     *
     * @param counter 输出使用的计数指标
     * @return 文本格式的 profile
     */
    @Override
    public String dumpCollapsed(Counter counter) {
        try {
            return execute0("collapsed," + counter.name().toLowerCase());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 导出采集到的调用栈文本。
     *
     * @param maxTraces 最大栈条数，0 表示不限制
     * @return 文本格式的 profile
     */
    @Override
    public String dumpTraces(int maxTraces) {
        try {
            return execute0(maxTraces == 0 ? "traces" : "traces=" + maxTraces);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 导出 flat profile（最热方法直方图）。
     *
     * @param maxMethods 最大方法数，0 表示不限制
     * @return 文本格式的 profile
     */
    @Override
    public String dumpFlat(int maxMethods) {
        try {
            return execute0(maxMethods == 0 ? "flat" : "flat=" + maxMethods);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 以 OTLP 格式导出采集数据。
     * <p>
     * 此 API 不稳定，可能在后续版本中变更或移除。
     *
     * @return OTLP 二进制 profile
     */
    @Override
    public byte[] dumpOtlp() {
        try {
            return execute1("otlp");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 将指定线程加入采样集合（需启用 filter 选项）。
     *
     * @param thread 待纳入采样的线程
     */
    public void addThread(Thread thread) {
        filterThread(thread, true);
    }

    /**
     * 将指定线程移出采样集合（需启用 filter 选项）。
     *
     * @param thread 待排除的线程
     */
    public void removeThread(Thread thread) {
        filterThread(thread, false);
    }

    /** 对目标线程加锁后调用 native filterThread0，避免与线程状态变更竞态。 */
    private void filterThread(Thread thread, boolean enable) {
        if (thread == null || thread == Thread.currentThread()) {
            filterThread0(null, enable);
        } else {
            // 加锁避免与线程状态变更产生竞态
            synchronized (thread) {
                Thread.State state = thread.getState();
                if (state != Thread.State.NEW && state != Thread.State.TERMINATED) {
                    filterThread0(thread, enable);
                }
            }
        }
    }

    private native void start0(String event, long interval, boolean reset) throws IllegalStateException;

    private native void stop0() throws IllegalStateException;

    private native String execute0(String command) throws IllegalArgumentException, IllegalStateException, IOException;

    private native byte[] execute1(String command) throws IllegalArgumentException, IllegalStateException, IOException;

    private native void filterThread0(Thread thread, boolean enable);
}
