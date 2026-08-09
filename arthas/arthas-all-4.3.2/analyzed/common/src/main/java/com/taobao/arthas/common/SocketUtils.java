package com.taobao.arthas.common;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ServerSocketFactory;

/**
 * TCP 端口可用性检测、随机选端口及占用进程 PID 查询（跨 Windows/Unix）。
 *
 * @author hengyunabc 2018-11-07
 */
public class SocketUtils {

    /**
     * 查找可用端口时的默认最小端口号。
     */
    public static final int PORT_RANGE_MIN = 1024;

    /**
     * 查找可用端口时的默认最大端口号。
     */
    public static final int PORT_RANGE_MAX = 65535;

    private static final Random random = new Random(System.currentTimeMillis());

    private SocketUtils() {
    }

    /**
     * 查找监听指定 TCP 端口的进程 PID；超时或失败返回 -1。
     *
     * @param port 端口号
     */
    public static long findTcpListenProcess(int port) {
        // 5 秒超时，避免 netstat/lsof 阻塞
        final int TIMEOUT_SECONDS = 5;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Long> future = executor.submit(new Callable<Long>() {
                @Override
                public Long call() throws Exception {
                    return doFindTcpListenProcess(port);
                }
            });

            try {
                return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                return -1;
            } catch (Exception e) {
                return -1;
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private static long doFindTcpListenProcess(int port) {
        try {
            if (OSUtils.isWindows()) {
                return findTcpListenProcessOnWindows(port);
            }

            if (OSUtils.isLinux() || OSUtils.isMac()) {
                return findTcpListenProcessOnUnix(port);
            }
        } catch (Throwable e) {
            // ignore
        }
        return -1;
    }

    /** 解析 Windows {@code netstat -ano} 输出 */
    private static long findTcpListenProcessOnWindows(int port) {
        String[] command = { "netstat", "-ano", "-p", "TCP" };
        List<String> lines = ExecutingCommand.runNative(command);
        for (String line : lines) {
            if (line.contains("LISTENING")) {
                // TCP 0.0.0.0:49168 0.0.0.0:0 LISTENING 476
                String[] strings = line.trim().split("\\s+");
                if (strings.length == 5) {
                    if (strings[1].endsWith(":" + port)) {
                        return Long.parseLong(strings[4]);
                    }
                }
            }
        }
        return -1;
    }

    /** 通过 lsof 查找 Unix 上监听端口的 PID */
    private static long findTcpListenProcessOnUnix(int port) {
        String pid = ExecutingCommand.getFirstAnswer("lsof -t -s TCP:LISTEN -i TCP:" + port);
        if (pid != null && !pid.trim().isEmpty()) {
            try {
                return Long.parseLong(pid.trim());
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return -1;
    }

    /** 尝试在 localhost 绑定端口以判断是否可用 */
    public static boolean isTcpPortAvailable(int port) {
        try {
            ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(port, 1,
                    InetAddress.getByName("localhost"));
            serverSocket.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 在 [{@value #PORT_RANGE_MIN}, {@value #PORT_RANGE_MAX}] 内随机选取可用 TCP 端口。
     * 
     * @return 可用端口号
     * @throws IllegalStateException 范围内无可用端口
     */
    public static int findAvailableTcpPort() {
        return findAvailableTcpPort(PORT_RANGE_MIN);
    }

    /**
     * 在 [{@code minPort}, {@value #PORT_RANGE_MAX}] 内随机选取可用 TCP 端口。
     * 
     * @param minPort 最小端口
     * @return 可用端口号
     * @throws IllegalStateException 范围内无可用端口
     */
    public static int findAvailableTcpPort(int minPort) {
        return findAvailableTcpPort(minPort, PORT_RANGE_MAX);
    }

    /**
     * 在 [{@code minPort}, {@code maxPort}] 内随机选取可用 TCP 端口。
     * 
     * @param minPort 最小端口
     * @param maxPort 最大端口
     * @return 可用端口号
     * @throws IllegalStateException 范围内无可用端口
     */
    public static int findAvailableTcpPort(int minPort, int maxPort) {
        return findAvailablePort(minPort, maxPort);
    }

    /**
     * 随机试探直至找到可绑定的端口。
     * 
     * @param minPort 最小端口
     * @param maxPort 最大端口
     * @return 可用端口号
     * @throws IllegalStateException 范围内无可用端口
     */
    private static int findAvailablePort(int minPort, int maxPort) {

        int portRange = maxPort - minPort;
        int candidatePort;
        int searchCounter = 0;
        do {
            if (searchCounter > portRange) {
                throw new IllegalStateException(
                        String.format("Could not find an available tcp port in the range [%d, %d] after %d attempts",
                                minPort, maxPort, searchCounter));
            }
            candidatePort = findRandomPort(minPort, maxPort);
            searchCounter++;
        } while (!isTcpPortAvailable(candidatePort));

        return candidatePort;
    }

    /**
     * 在 [{@code minPort}, {@code maxPort}] 内生成伪随机端口。
     * 
     * @param minPort 最小端口
     * @param maxPort 最大端口
     * @return 随机端口
     */
    private static int findRandomPort(int minPort, int maxPort) {
        int portRange = maxPort - minPort;
        return minPort + random.nextInt(portRange + 1);
    }
}
