package com.taobao.arthas.core.command.monitor200;

import com.taobao.arthas.core.command.model.ThreadVO;
import sun.management.HotspotThreadMBean;
import sun.management.ManagementFactoryHelper;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 线程 CPU 采样器：两次调用 {@link #sample} 间隔 {@link #pause} 毫秒，
 * 用 {@link ThreadMXBean#getThreadCpuTime} 差值除以墙钟时间估算 CPU 使用率。
 * 可选通过 HotspotThreadMBean 纳入 JVM 内部线程（GC 等）。
 *
 * @author gongdewei 2020/4/23
 */
public class ThreadSampler {

    private static ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private static HotspotThreadMBean hotspotThreadMBean;
    private static boolean hotspotThreadMBeanEnable = true;

    /** 上次采样时各线程累计 CPU 时间（纳秒） */
    private Map<ThreadVO, Long> lastCpuTimes = new HashMap<ThreadVO, Long>();

    private long lastSampleTimeNanos;
    private boolean includeInternalThreads = true;


    /**
     * 首次采样仅记录基准 CPU 时间并排序；第二次起计算 delta 与 CPU%。
     * 返回的 ThreadVO 已填充 time、deltaTime、cpu 字段。
     */
    public List<ThreadVO> sample(Collection<ThreadVO> originThreads) {

        List<ThreadVO> threads = new ArrayList<ThreadVO>(originThreads);

        // Sample CPU
        if (lastCpuTimes.isEmpty()) {
            lastSampleTimeNanos = System.nanoTime();
            for (ThreadVO thread : threads) {
                if (thread.getId() > 0) {
                    long cpu = threadMXBean.getThreadCpuTime(thread.getId());
                    lastCpuTimes.put(thread, cpu);
                    thread.setTime(cpu / 1000000);
                }
            }

            // add internal threads
            Map<String, Long> internalThreadCpuTimes = getInternalThreadCpuTimes();
            if (internalThreadCpuTimes != null) {
                for (Map.Entry<String, Long> entry : internalThreadCpuTimes.entrySet()) {
                    String key = entry.getKey();
                    ThreadVO thread = createThreadVO(key);
                    thread.setTime(entry.getValue() / 1000000);
                    threads.add(thread);
                    lastCpuTimes.put(thread, entry.getValue());
                }
            }

            //sort by time
            Collections.sort(threads, new Comparator<ThreadVO>() {
                @Override
                public int compare(ThreadVO o1, ThreadVO o2) {
                    long l1 = o1.getTime();
                    long l2 = o2.getTime();
                    if (l1 < l2) {
                        return 1;
                    } else if (l1 > l2) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
            return threads;
        }

        // Resample
        long newSampleTimeNanos = System.nanoTime();
        Map<ThreadVO, Long> newCpuTimes = new HashMap<ThreadVO, Long>(threads.size());
        for (ThreadVO thread : threads) {
            if (thread.getId() > 0) {
                long cpu = threadMXBean.getThreadCpuTime(thread.getId());
                newCpuTimes.put(thread, cpu);
            }
        }
        // internal threads
        Map<String, Long> newInternalThreadCpuTimes = getInternalThreadCpuTimes();
        if (newInternalThreadCpuTimes != null) {
            for (Map.Entry<String, Long> entry : newInternalThreadCpuTimes.entrySet()) {
                ThreadVO threadVO = createThreadVO(entry.getKey());
                threads.add(threadVO);
                newCpuTimes.put(threadVO, entry.getValue());
            }
        }

        // Compute delta time
        final Map<ThreadVO, Long> deltas = new HashMap<ThreadVO, Long>(threads.size());
        for (ThreadVO thread : newCpuTimes.keySet()) {
            Long t = lastCpuTimes.get(thread);
            if (t == null) {
                t = 0L;
            }
            long time1 = t;
            long time2 = newCpuTimes.get(thread);
            if (time1 == -1) {
                time1 = time2;
            } else if (time2 == -1) {
                time2 = time1;
            }
            long delta = time2 - time1;
            deltas.put(thread, delta);
        }

        long sampleIntervalNanos = newSampleTimeNanos - lastSampleTimeNanos;

        // Compute cpu usage
        final HashMap<ThreadVO, Double> cpuUsages = new HashMap<ThreadVO, Double>(threads.size());
        for (ThreadVO thread : threads) {
            double cpu = sampleIntervalNanos == 0 ? 0 : (Math.rint(deltas.get(thread) * 10000.0 / sampleIntervalNanos) / 100.0);
            cpuUsages.put(thread, cpu);
        }

        // Sort by CPU time : should be a rendering hint...
        Collections.sort(threads, new Comparator<ThreadVO>() {
            @Override
            public int compare(ThreadVO o1, ThreadVO o2) {
                long l1 = deltas.get(o1);
                long l2 = deltas.get(o2);
                if (l1 < l2) {
                    return 1;
                } else if (l1 > l2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        for (ThreadVO thread : threads) {
            //nanos to mills
            long timeMills = newCpuTimes.get(thread) / 1000000;
            long deltaTime = deltas.get(thread) / 1000000;
            double cpu = cpuUsages.get(thread);

            thread.setCpu(cpu);
            thread.setTime(timeMills);
            thread.setDeltaTime(deltaTime);
        }
        lastCpuTimes = newCpuTimes;
        lastSampleTimeNanos = newSampleTimeNanos;

        return threads;
    }

    /** 读取 HotSpot 内部线程 CPU；反射失败则永久关闭以免反复抛错 */
    private Map<String, Long> getInternalThreadCpuTimes() {
        if (hotspotThreadMBeanEnable && includeInternalThreads) {
            try {
                if (hotspotThreadMBean == null) {
                    hotspotThreadMBean = ManagementFactoryHelper.getHotspotThreadMBean();
                }
                return hotspotThreadMBean.getInternalThreadCpuTimes();
            } catch (Throwable e) {
                //ignore ex
                hotspotThreadMBeanEnable = false;
            }
        }
        return null;
    }

    private ThreadVO createThreadVO(String name) {
        ThreadVO threadVO = new ThreadVO();
        threadVO.setId(-1);
        threadVO.setName(name);
        threadVO.setPriority(-1);
        threadVO.setDaemon(true);
        threadVO.setInterrupted(false);
        return threadVO;
    }

    /** 两次 sample 之间的等待间隔（毫秒） */
    public void pause(long mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    public boolean isIncludeInternalThreads() {
        return includeInternalThreads;
    }

    public void setIncludeInternalThreads(boolean includeInternalThreads) {
        this.includeInternalThreads = includeInternalThreads;
    }

}
