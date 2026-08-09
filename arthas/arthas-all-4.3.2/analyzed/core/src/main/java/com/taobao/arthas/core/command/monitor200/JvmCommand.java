package com.taobao.arthas.core.command.monitor200;

import com.taobao.arthas.core.command.Constants;
import com.taobao.arthas.core.command.model.JvmModel;
import com.taobao.arthas.core.shell.command.AnnotatedCommand;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.middleware.cli.annotations.Description;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.cli.annotations.Summary;

import java.lang.management.*;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code jvm} 命令：一次性采集目标 JVM 的运行时、类加载、编译、GC、内存、
 * 操作系统、线程与文件描述符等 MXBean 信息，封装为 {@link JvmModel}。
 * <p>
 * 各分组通过 {@link JvmModel#addItem} 追加键值；JDK9+ 可能无法读取 boot classpath。
 *
 * @author vlinux on 15/6/6.
 */
@Name("jvm")
@Summary("Display the target JVM information")
@Description(Constants.WIKI + Constants.WIKI_HOME + "jvm")
public class JvmCommand extends AnnotatedCommand {

    /** JVM 运行时 MXBean（启动参数、VM 版本等） */
    private final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    /** 类加载统计 MXBean */
    private final ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
    /** JIT 编译器 MXBean（可能为 null） */
    private final CompilationMXBean compilationMXBean = ManagementFactory.getCompilationMXBean();
    /** 各垃圾收集器 MXBean 列表 */
    private final Collection<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
    /** 内存管理器（关联 MemoryPool）列表 */
    private final Collection<MemoryManagerMXBean> memoryManagerMXBeans = ManagementFactory.getMemoryManagerMXBeans();
    /** 堆/非堆汇总内存 MXBean */
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    //    private final Collection<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
    /** 操作系统 MXBean */
    private final OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
    /** 线程 MXBean（含死锁检测） */
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    /** 依次填充各 MXBean 分组并输出 JvmModel */
    @Override
    public void process(CommandProcess process) {

        JvmModel jvmModel = new JvmModel();

        addRuntimeInfo(jvmModel);

        addClassLoading(jvmModel);

        addCompilation(jvmModel);

        if (!garbageCollectorMXBeans.isEmpty()) {
            addGarbageCollectors(jvmModel);
        }

        if (!memoryManagerMXBeans.isEmpty()) {
            addMemoryManagers(jvmModel);
        }

        addMemory(jvmModel);

        addOperatingSystem(jvmModel);

        addThread(jvmModel);

        addFileDescriptor(jvmModel);

        process.appendResult(jvmModel);
        process.end();
    }

    /** 反射调用 com.sun.management 扩展方法获取文件描述符上限与已打开数 */
    private void addFileDescriptor(JvmModel jvmModel) {
        String group = "FILE-DESCRIPTOR";
        jvmModel.addItem(group,"MAX-FILE-DESCRIPTOR-COUNT", invokeFileDescriptor(operatingSystemMXBean, "getMaxFileDescriptorCount"))
                .addItem(group,"OPEN-FILE-DESCRIPTOR-COUNT", invokeFileDescriptor(operatingSystemMXBean, "getOpenFileDescriptorCount"));
    }

    /** 反射调用 HotSpot 扩展 OS MXBean 方法，失败返回 -1 */
    private long invokeFileDescriptor(OperatingSystemMXBean os, String name) {
        try {
            final Method method = os.getClass().getDeclaredMethod(name);
            method.setAccessible(true);
            return (Long) method.invoke(os);
        } catch (Exception e) {
            return -1;
        }
    }

    private void addRuntimeInfo(JvmModel jvmModel) {
        String bootClassPath = "";
        try {
            bootClassPath = runtimeMXBean.getBootClassPath();
        } catch (Exception e) {
            // under jdk9 will throw UnsupportedOperationException, ignore
        }
        String group = "RUNTIME";
        jvmModel.addItem(group,"MACHINE-NAME", runtimeMXBean.getName());
        jvmModel.addItem(group, "JVM-START-TIME", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(runtimeMXBean.getStartTime())));
        jvmModel.addItem(group, "MANAGEMENT-SPEC-VERSION", runtimeMXBean.getManagementSpecVersion());
        jvmModel.addItem(group, "SPEC-NAME", runtimeMXBean.getSpecName());
        jvmModel.addItem(group, "SPEC-VENDOR", runtimeMXBean.getSpecVendor());
        jvmModel.addItem(group, "SPEC-VERSION", runtimeMXBean.getSpecVersion());
        jvmModel.addItem(group, "VM-NAME", runtimeMXBean.getVmName());
        jvmModel.addItem(group, "VM-VENDOR", runtimeMXBean.getVmVendor());
        jvmModel.addItem(group, "VM-VERSION", runtimeMXBean.getVmVersion());
        jvmModel.addItem(group, "INPUT-ARGUMENTS", runtimeMXBean.getInputArguments());
        jvmModel.addItem(group, "CLASS-PATH", runtimeMXBean.getClassPath());
        jvmModel.addItem(group, "BOOT-CLASS-PATH", bootClassPath);
        jvmModel.addItem(group, "LIBRARY-PATH", runtimeMXBean.getLibraryPath());
    }

    private void addClassLoading(JvmModel jvmModel) {
        String group = "CLASS-LOADING";
        jvmModel.addItem(group, "LOADED-CLASS-COUNT", classLoadingMXBean.getLoadedClassCount());
        jvmModel.addItem(group, "TOTAL-LOADED-CLASS-COUNT", classLoadingMXBean.getTotalLoadedClassCount());
        jvmModel.addItem(group, "UNLOADED-CLASS-COUNT", classLoadingMXBean.getUnloadedClassCount());
        jvmModel.addItem(group, "IS-VERBOSE", classLoadingMXBean.isVerbose());
    }

    private void addCompilation(JvmModel jvmModel) {
        if (compilationMXBean == null) {
            return;
        }
        String group = "COMPILATION";
        jvmModel.addItem(group, "NAME", compilationMXBean.getName());
        if (compilationMXBean.isCompilationTimeMonitoringSupported()) {
            jvmModel.addItem(group, "TOTAL-COMPILE-TIME", compilationMXBean.getTotalCompilationTime(), "time (ms)");
        }
    }

    private void addGarbageCollectors(JvmModel jvmModel) {
        String group = "GARBAGE-COLLECTORS";
        for (GarbageCollectorMXBean gcMXBean : garbageCollectorMXBeans) {
            Map<String, Object> gcInfo = new LinkedHashMap<String, Object>();
            gcInfo.put("name", gcMXBean.getName());
            gcInfo.put("collectionCount", gcMXBean.getCollectionCount());
            gcInfo.put("collectionTime", gcMXBean.getCollectionTime());

            jvmModel.addItem(group, gcMXBean.getName(), gcInfo, "count/time (ms)");
        }
    }

    private void addMemoryManagers(JvmModel jvmModel) {
        String group = "MEMORY-MANAGERS";
        for (final MemoryManagerMXBean memoryManagerMXBean : memoryManagerMXBeans) {
            if (memoryManagerMXBean.isValid()) {
                final String name = memoryManagerMXBean.isValid()
                        ? memoryManagerMXBean.getName()
                        : memoryManagerMXBean.getName() + "(Invalid)";
                jvmModel.addItem(group, name, memoryManagerMXBean.getMemoryPoolNames());
            }
        }
    }

    private void addMemory(JvmModel jvmModel) {
        String group = "MEMORY";
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        Map<String, Object> heapMemoryInfo = getMemoryUsageInfo("heap", heapMemoryUsage);
        jvmModel.addItem(group, "HEAP-MEMORY-USAGE", heapMemoryInfo, "memory in bytes");

        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
        Map<String, Object> nonheapMemoryInfo = getMemoryUsageInfo("nonheap", nonHeapMemoryUsage);
        jvmModel.addItem(group,"NO-HEAP-MEMORY-USAGE", nonheapMemoryInfo, "memory in bytes");

        jvmModel.addItem(group,"PENDING-FINALIZE-COUNT", memoryMXBean.getObjectPendingFinalizationCount());
    }

    private Map<String, Object> getMemoryUsageInfo(String name, MemoryUsage heapMemoryUsage) {
        Map<String, Object> memoryInfo = new LinkedHashMap<String, Object>();
        memoryInfo.put("name", name);
        memoryInfo.put("init", heapMemoryUsage.getInit());
        memoryInfo.put("used", heapMemoryUsage.getUsed());
        memoryInfo.put("committed", heapMemoryUsage.getCommitted());
        memoryInfo.put("max", heapMemoryUsage.getMax());
        return memoryInfo;
    }

    private void addOperatingSystem(JvmModel jvmModel) {
        String group = "OPERATING-SYSTEM";
        jvmModel.addItem(group,"OS", operatingSystemMXBean.getName())
                .addItem(group,"ARCH", operatingSystemMXBean.getArch())
                .addItem(group,"PROCESSORS-COUNT", operatingSystemMXBean.getAvailableProcessors())
                .addItem(group,"LOAD-AVERAGE", operatingSystemMXBean.getSystemLoadAverage())
                .addItem(group,"VERSION", operatingSystemMXBean.getVersion());
    }

    private void addThread(JvmModel jvmModel) {
        String group = "THREAD";
        jvmModel.addItem(group, "COUNT", threadMXBean.getThreadCount())
                .addItem(group, "DAEMON-COUNT", threadMXBean.getDaemonThreadCount())
                .addItem(group, "PEAK-COUNT", threadMXBean.getPeakThreadCount())
                .addItem(group, "STARTED-COUNT", threadMXBean.getTotalStartedThreadCount())
                .addItem(group, "DEADLOCK-COUNT",getDeadlockedThreadsCount(threadMXBean));
    }

    /** 调用 findDeadlockedThreads，无死锁时返回 0 */
    private int getDeadlockedThreadsCount(ThreadMXBean threads) {
        final long[] ids = threads.findDeadlockedThreads();
        if (ids == null) {
            return 0;
        } else {
            return ids.length;
        }
    }
}
