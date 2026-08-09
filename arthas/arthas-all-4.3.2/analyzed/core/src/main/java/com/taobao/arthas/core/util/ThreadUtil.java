package com.taobao.arthas.core.util;

import com.taobao.arthas.core.command.model.BlockingLockInfo;
import com.taobao.arthas.core.command.model.BusyThreadInfo;
import com.taobao.arthas.core.command.model.StackModel;
import com.taobao.arthas.core.command.model.ThreadNode;
import com.taobao.arthas.core.command.model.ThreadVO;
import com.taobao.arthas.core.view.Ansi;

import java.arthas.SpyAPI;
import java.lang.management.*;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 线程枚举、堆栈格式化与 EagleEye 链路信息提取工具。
 * <p>
 * 供 thread、stack、profiler 等命令获取 JVM 全量线程、阻塞锁分析、
 * 带 CPU 占用的堆栈文本，以及过滤 Spy 框架帧后的业务调用栈。
 *
 * @author hengyunabc 2015年12月7日 下午2:29:28
 */
abstract public class ThreadUtil {

    private static final BlockingLockInfo EMPTY_INFO = new BlockingLockInfo();

    private static ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    private static boolean detectedEagleEye = false;
    public static boolean foundEagleEye = false;

    /** 沿 ThreadGroup 父链向上找到根线程组。 */
    public static ThreadGroup getRoot() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        ThreadGroup parent;
        while ((parent = group.getParent()) != null) {
            group = parent;
        }
        return group;
    }

    /**
     * 获取所有线程
     */
    public static List<ThreadVO> getThreads() {
        ThreadGroup root = getRoot();
        Thread[] threads = new Thread[root.activeCount()];
        while (root.enumerate(threads, true) == threads.length) {
            threads = new Thread[threads.length * 2];
        }
        List<ThreadVO> list = new ArrayList<ThreadVO>(threads.length);
        for (Thread thread : threads) {
            if (thread != null) {
                ThreadVO threadVO = createThreadVO(thread);
                list.add(threadVO);
            }
        }
        return list;
    }

    /** 将 {@link Thread} 转为命令层 {@link ThreadVO}。 */
    private static ThreadVO createThreadVO(Thread thread) {
        ThreadGroup group = thread.getThreadGroup();
        ThreadVO threadVO = new ThreadVO();
        threadVO.setId(thread.getId());
        threadVO.setName(thread.getName());
        threadVO.setGroup(group == null ? "" : group.getName());
        threadVO.setPriority(thread.getPriority());
        threadVO.setState(thread.getState());
        threadVO.setInterrupted(thread.isInterrupted());
        threadVO.setDaemon(thread.isDaemon());
        return threadVO;
    }

    /**
     * 获取所有线程List
     * 
     * @return
     */
    public static List<Thread> getThreadList() {
        List<Thread> result = new ArrayList<Thread>();
        ThreadGroup root = getRoot();
        Thread[] threads = new Thread[root.activeCount()];
        while (root.enumerate(threads, true) == threads.length) {
            threads = new Thread[threads.length * 2];
        }
        for (Thread thread : threads) {
            if (thread != null) {
                result.add(thread);
            }
        }
        return result;
    }


    /**
     * 找出阻塞线程数最多的锁及其持有线程。
     * <p>时间复杂度 O(线程数)，空间复杂度 O(锁数量)。</p>
     *
     * @return 阻塞锁信息；未找到时返回空对象
     */
    public static BlockingLockInfo findMostBlockingLock() {
        ThreadInfo[] infos = threadMXBean.dumpAllThreads(threadMXBean.isObjectMonitorUsageSupported(),
                threadMXBean.isSynchronizerUsageSupported());

        // 锁 identityHashCode -> 等待该锁的线程数
        Map<Integer, Integer> blockCountPerLock = new HashMap<Integer, Integer>();
        // 锁 identityHashCode -> 当前持有该锁的 ThreadInfo
        Map<Integer, ThreadInfo> ownerThreadPerLock = new HashMap<Integer, ThreadInfo>();

        for (ThreadInfo info: infos) {
            if (info == null) {
                continue;
            }

            LockInfo lockInfo = info.getLockInfo();
            if (lockInfo != null) {
                // 当前线程正在等待某锁/条件
                if (blockCountPerLock.get(lockInfo.getIdentityHashCode()) == null) {
                    blockCountPerLock.put(lockInfo.getIdentityHashCode(), 0);
                }
                int blockedCount = blockCountPerLock.get(lockInfo.getIdentityHashCode());
                blockCountPerLock.put(lockInfo.getIdentityHashCode(), blockedCount + 1);
            }

            for (MonitorInfo monitorInfo: info.getLockedMonitors()) {
                // 当前线程持有的对象监视器
                if (ownerThreadPerLock.get(monitorInfo.getIdentityHashCode()) == null) {
                    ownerThreadPerLock.put(monitorInfo.getIdentityHashCode(), info);
                }
            }

            for (LockInfo lockedSync: info.getLockedSynchronizers()) {
                // 当前线程持有的可拥有同步器（如 ReentrantLock）
                if (ownerThreadPerLock.get(lockedSync.getIdentityHashCode()) == null) {
                    ownerThreadPerLock.put(lockedSync.getIdentityHashCode(), info);
                }
            }
        }

        // 选取阻塞线程数最多且确有持有者的那把锁
        int mostBlockingLock = 0; // System.identityHashCode(null) == 0
        int maxBlockingCount = 0;
        for (Map.Entry<Integer, Integer> entry: blockCountPerLock.entrySet()) {
            if (entry.getValue() > maxBlockingCount && ownerThreadPerLock.get(entry.getKey()) != null) {
                // 该锁已被其他线程显式持有
                maxBlockingCount = entry.getValue();
                mostBlockingLock = entry.getKey();
            }
        }

        if (mostBlockingLock == 0) {
            // 未发现符合条件的阻塞锁
            return EMPTY_INFO;
        }

        BlockingLockInfo blockingLockInfo = new BlockingLockInfo();
        blockingLockInfo.setThreadInfo(ownerThreadPerLock.get(mostBlockingLock));
        blockingLockInfo.setLockIdentityHashCode(mostBlockingLock);
        blockingLockInfo.setBlockingThreadCount(blockCountPerLock.get(mostBlockingLock));
        return blockingLockInfo;
    }


    /** 格式化 {@link ThreadInfo} 完整堆栈（无 CPU/锁高亮）。 */
    public static String getFullStacktrace(ThreadInfo threadInfo) {
        return getFullStacktrace(threadInfo, -1, -1, -1, 0, 0);
    }

    /** 格式化持有最多阻塞锁的线程堆栈并高亮锁信息。 */
    public static String getFullStacktrace(BlockingLockInfo blockingLockInfo) {
        return getFullStacktrace(blockingLockInfo.getThreadInfo(), -1, -1, -1, blockingLockInfo.getLockIdentityHashCode(),
                blockingLockInfo.getBlockingThreadCount());
    }


    /**
     * 完全从 ThreadInfo 中 copy 过来
     * @param threadInfo the thread info object
     * @param cpuUsage will be ignore if cpuUsage < 0 or cpuUsage > 100
     * @param lockIdentityHashCode 阻塞了其他线程的锁的identityHashCode
     * @param blockingThreadCount 阻塞了其他线程的数量
     * @return the string representation of the thread stack
     */
    public static String getFullStacktrace(ThreadInfo threadInfo, double cpuUsage, long deltaTime, long time, int lockIdentityHashCode,
                                           int blockingThreadCount) {
        StringBuilder sb = new StringBuilder("\"" + threadInfo.getThreadName() + "\"" + " Id="
                + threadInfo.getThreadId());

        if (cpuUsage >= 0 && cpuUsage <= 100) {
            sb.append(" cpuUsage=").append(cpuUsage).append("%");
        }
        if (deltaTime >= 0 ) {
            sb.append(" deltaTime=").append(deltaTime).append("ms");
        }
        if (time >= 0 ) {
            sb.append(" time=").append(time).append("ms");
        }

        sb.append(" ").append(threadInfo.getThreadState());

        if (threadInfo.getLockName() != null) {
            sb.append(" on ").append(threadInfo.getLockName());
        }
        if (threadInfo.getLockOwnerName() != null) {
            sb.append(" owned by \"").append(threadInfo.getLockOwnerName()).append("\" Id=").append(threadInfo.getLockOwnerId());
        }
        if (threadInfo.isSuspended()) {
            sb.append(" (suspended)");
        }
        if (threadInfo.isInNative()) {
            sb.append(" (in native)");
        }
        sb.append('\n');
        int i = 0;
        for (StackTraceElement ste : threadInfo.getStackTrace()) {
            sb.append("\tat ").append(ste.toString());
            sb.append('\n');
            if (i == 0 && threadInfo.getLockInfo() != null) {
                Thread.State ts = threadInfo.getThreadState();
                switch (ts) {
                    case BLOCKED:
                        sb.append("\t-  blocked on ").append(threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    case WAITING:
                        sb.append("\t-  waiting on ").append(threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    case TIMED_WAITING:
                        sb.append("\t-  waiting on ").append(threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    default:
                }
            }

            for (MonitorInfo mi : threadInfo.getLockedMonitors()) {
                if (mi.getLockedStackDepth() == i) {
                    sb.append("\t-  locked ").append(mi);
                    if (mi.getIdentityHashCode() == lockIdentityHashCode) {
                        Ansi highlighted = Ansi.ansi().fg(Ansi.Color.RED);
                        highlighted.a(" <---- but blocks ").a(blockingThreadCount).a(" other threads!");
                        sb.append(highlighted.reset().toString());
                    }
                    sb.append('\n');
                }
            }
            ++i;
        }
        if (i < threadInfo.getStackTrace().length) {
            sb.append("\t...");
            sb.append('\n');
        }

        LockInfo[] locks = threadInfo.getLockedSynchronizers();
        if (locks.length > 0) {
            sb.append("\n\tNumber of locked synchronizers = ").append(locks.length);
            sb.append('\n');
            for (LockInfo li : locks) {
                sb.append("\t- ").append(li);
                if (li.getIdentityHashCode() == lockIdentityHashCode) {
                    sb.append(" <---- but blocks ").append(blockingThreadCount);
                    sb.append(" other threads!");
                }
                sb.append('\n');
            }
        }
        sb.append('\n');
        return sb.toString().replace("\t", "    ");
    }

    /** 格式化 {@link BusyThreadInfo} 堆栈，可标注阻塞其他线程的锁。 */
    public static String getFullStacktrace(BusyThreadInfo threadInfo, int lockIdentityHashCode, int blockingThreadCount) {
        if (threadInfo == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder("\"" + threadInfo.getName() + "\"");
        if (threadInfo.getId() > 0) {
            sb.append(" Id=").append(threadInfo.getId());
        } else {
            sb.append(" [Internal]");
        }
        double cpuUsage = threadInfo.getCpu();
        if (cpuUsage >= 0 && cpuUsage <= 100) {
            sb.append(" cpuUsage=").append(cpuUsage).append("%");
        }
        if (threadInfo.getDeltaTime() >= 0 ) {
            sb.append(" deltaTime=").append(threadInfo.getDeltaTime()).append("ms");
        }
        if (threadInfo.getTime() >= 0 ) {
            sb.append(" time=").append(threadInfo.getTime()).append("ms");
        }

        if (threadInfo.getState() == null) {
            sb.append("\n\n");
            return sb.toString();
        }

        sb.append(" ").append(threadInfo.getState());

        if (threadInfo.getLockName() != null) {
            sb.append(" on ").append(threadInfo.getLockName());
        }
        if (threadInfo.getLockOwnerName() != null) {
            sb.append(" owned by \"").append(threadInfo.getLockOwnerName()).append("\" Id=").append(threadInfo.getLockOwnerId());
        }
        if (threadInfo.isSuspended()) {
            sb.append(" (suspended)");
        }
        if (threadInfo.isInNative()) {
            sb.append(" (in native)");
        }
        sb.append('\n');
        int i = 0;
        for (; i < threadInfo.getStackTrace().length; i++) {
            StackTraceElement ste = threadInfo.getStackTrace()[i];
            sb.append("\tat ").append(ste.toString());
            sb.append('\n');
            if (i == 0 && threadInfo.getLockInfo() != null) {
                Thread.State ts = threadInfo.getState();
                switch (ts) {
                    case BLOCKED:
                        sb.append("\t-  blocked on ").append(threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    case WAITING:
                        sb.append("\t-  waiting on ").append(threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    case TIMED_WAITING:
                        sb.append("\t-  waiting on ").append(threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    default:
                }
            }

            for (MonitorInfo mi : threadInfo.getLockedMonitors()) {
                if (mi.getLockedStackDepth() == i) {
                    sb.append("\t-  locked ").append(mi);
                    if (mi.getIdentityHashCode() == lockIdentityHashCode) {
                        Ansi highlighted = Ansi.ansi().fg(Ansi.Color.RED);
                        highlighted.a(" <---- but blocks ").a(blockingThreadCount).a(" other threads!");
                        sb.append(highlighted.reset().toString());
                    }
                    sb.append('\n');
                }
            }
        }
        if (i < threadInfo.getStackTrace().length) {
            sb.append("\t...");
            sb.append('\n');
        }

        LockInfo[] locks = threadInfo.getLockedSynchronizers();
        if (locks.length > 0) {
            sb.append("\n\tNumber of locked synchronizers = ").append(locks.length);
            sb.append('\n');
            for (LockInfo li : locks) {
                sb.append("\t- ").append(li);
                if (li.getIdentityHashCode() == lockIdentityHashCode) {
                    sb.append(" <---- but blocks ").append(blockingThreadCount);
                    sb.append(" other threads!");
                }
                sb.append('\n');
            }
        }
        sb.append('\n');
        return sb.toString().replace("\t", "    ");
    }

    /**
     * </pre>
     * java.lang.Thread.getStackTrace(Thread.java:1559),
     * com.taobao.arthas.core.util.ThreadUtil.getThreadStack(ThreadUtil.java:349),
     * com.taobao.arthas.core.command.monitor200.StackAdviceListener.before(StackAdviceListener.java:33),
     * com.taobao.arthas.core.advisor.AdviceListenerAdapter.before(AdviceListenerAdapter.java:49),
     * com.taobao.arthas.core.advisor.SpyImpl.atEnter(SpyImpl.java:42),
     * java.arthas.SpyAPI.atEnter(SpyAPI.java:40),
     * demo.MathGame.print(MathGame.java), demo.MathGame.run(MathGame.java:25),
     * demo.MathGame.main(MathGame.java:16)
     * </pre>
     */
    private static int MAGIC_STACK_DEPTH = 0;

    /** 定位 {@link SpyAPI} 在堆栈中的深度，用于裁剪 Arthas 探针帧。 */
    private static int findTheSpyAPIDepth(StackTraceElement[] stackTraceElementArray) {
        if (MAGIC_STACK_DEPTH > 0) {
            return MAGIC_STACK_DEPTH;
        }
        if (MAGIC_STACK_DEPTH > stackTraceElementArray.length) {
            return 0;
        }
        for (int i = 0; i < stackTraceElementArray.length; ++i) {
            if (SpyAPI.class.getName().equals(stackTraceElementArray[i].getClassName())) {
                MAGIC_STACK_DEPTH = i + 1;
                break;
            }
        }
        return MAGIC_STACK_DEPTH;
    }

    /**
     * 获取方法执行堆栈信息
     *
     * @return 方法堆栈信息
     */
    public static StackModel getThreadStackModel(ClassLoader loader, Thread currentThread) {
        StackModel stackModel = new StackModel();
        stackModel.setThreadName(currentThread.getName());
        stackModel.setThreadId(Long.toString(currentThread.getId()));
        stackModel.setDaemon(currentThread.isDaemon());
        stackModel.setPriority(currentThread.getPriority());
        stackModel.setClassloader(getTCCL(currentThread));

        getEagleeyeTraceInfo(loader, currentThread, stackModel);


        // 裁剪 Spy 探针帧后的业务堆栈
        StackTraceElement[] stackTraceElementArray = currentThread.getStackTrace();
        int magicStackDepth = findTheSpyAPIDepth(stackTraceElementArray);
        StackTraceElement[] actualStackFrames = new StackTraceElement[stackTraceElementArray.length - magicStackDepth];
        System.arraycopy(stackTraceElementArray, magicStackDepth , actualStackFrames, 0, actualStackFrames.length);
        stackModel.setStackTrace(actualStackFrames);
        return stackModel;
    }

    /** 构建含 traceId/rpcId 的 {@link ThreadNode}（不含完整堆栈）。 */
    public static ThreadNode getThreadNode(ClassLoader loader, Thread currentThread) {
        ThreadNode threadNode = new ThreadNode();
        threadNode.setThreadId(currentThread.getId());
        threadNode.setThreadName(currentThread.getName());
        threadNode.setDaemon(currentThread.isDaemon());
        threadNode.setPriority(currentThread.getPriority());
        threadNode.setClassloader(getTCCL(currentThread));

        // 附加 EagleEye trace 信息
        StackModel stackModel = new StackModel();
        getEagleeyeTraceInfo(loader, currentThread, stackModel);
        threadNode.setTraceId(stackModel.getTraceId());
        threadNode.setRpcId(stackModel.getRpcId());
        return threadNode;
    }

    /** 生成 thread 命令输出的线程标题行（名称、id、TCCL、trace 等）。 */
    public static String getThreadTitle(StackModel stackModel) {
        StringBuilder sb = new StringBuilder("thread_name=");
        sb.append(stackModel.getThreadName())
                .append(";id=").append(stackModel.getThreadId())
                .append(";is_daemon=").append(stackModel.isDaemon())
                .append(";priority=").append(stackModel.getPriority())
                .append(";TCCL=").append(stackModel.getClassloader());
        if (stackModel.getTraceId() != null) {
            sb.append(";trace_id=").append(stackModel.getTraceId());
        }
        if (stackModel.getRpcId() != null) {
            sb.append(";rpc_id=").append(stackModel.getRpcId());
        }
        return sb.toString();
    }

    /** 返回线程上下文 ClassLoader 的类名@hash 描述。 */
    private static String getTCCL(Thread currentThread) {
        ClassLoader contextClassLoader = currentThread.getContextClassLoader();
        if (null == contextClassLoader) {
            return "null";
        } else {
            return contextClassLoader.getClass().getName() +
                    "@" +
                    Integer.toHexString(contextClassLoader.hashCode());
        }
    }

    /** 反射调用 EagleEye 获取 traceId 与 rpcId（若 classpath 存在）。 */
    private static void getEagleeyeTraceInfo(ClassLoader loader, Thread currentThread, StackModel stackModel) {
        if(loader == null) {
            return;
        }
        Class<?> eagleEyeClass = null;
        if (!detectedEagleEye) {
            try {
                eagleEyeClass = loader.loadClass("com.taobao.eagleeye.EagleEye");
                foundEagleEye = true;
            } catch (Throwable e) {
                // ignore
            }
            detectedEagleEye = true;
        }

        if (!foundEagleEye) {
            return;
        }

        try {
            if (eagleEyeClass == null) {
                eagleEyeClass = loader.loadClass("com.taobao.eagleeye.EagleEye");
            }
            Method getTraceIdMethod = eagleEyeClass.getMethod("getTraceId");
            String traceId = (String) getTraceIdMethod.invoke(null);
            stackModel.setTraceId(traceId);
            Method getRpcIdMethod = eagleEyeClass.getMethod("getRpcId");
            String rpcId = (String) getRpcIdMethod.invoke(null);
            stackModel.setRpcId(rpcId);
        } catch (Throwable e) {
            // ignore
        }
    }

}
