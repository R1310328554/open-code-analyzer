package org.keycloak.testframework.events;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jboss.logging.Logger;

/**
 * 测试用嵌入式 Syslog 接收服务器。
 * <p>
 * 在随机可用端口监听 TCP 连接，解析每行 syslog 并分发给注册的 {@link SysLogListener}。
 */
public class SysLogServer {

    /** 本类日志记录器。 */
    private static final Logger LOGGER = Logger.getLogger(SysLogServer.class);
    /** 并发处理连接的最大线程数。 */
    private static final int MAX_THREADS = 5;
    /** 监听 socket。 */
    private final ServerSocket serverSocket;
    /** 活跃的工作线程列表。 */
    private final List<Thread> threads = Collections.synchronizedList(new LinkedList<>());
    /** 已注册的 syslog 监听器。 */
    private final Set<SysLogListener> listeners = new HashSet<>();
    /** 服务器是否仍在运行。 */
    private boolean running = true;

    /** 在随机可用端口启动 syslog 服务器。 */
    public SysLogServer() throws IOException {
        serverSocket = new ServerSocket(0);
        startThread();
    }

    /** 关闭服务器并等待所有工作线程结束。 */
    public void stop() throws InterruptedException, IOException {
        LOGGER.tracev("Shutdown, threads={0}", threads.size());
        running = false;
        serverSocket.close();
        for (Thread t : threads) {
            t.join();
        }
    }

    /** 注册 syslog 监听器。 */
    public void addListener(SysLogListener listener) {
        listeners.add(listener);
    }

    /** 移除 syslog 监听器。 */
    public void removeListener(SysLogListener listener) {
        listeners.remove(listener);
    }

    /** @return {@code host:port} 形式的 syslog 端点，供 Keycloak 配置使用 */
    public String getEndpoint() {
        return "localhost:" + serverSocket.getLocalPort();
    }

    /** 若未达线程上限则启动新的 socket 处理线程。 */
    protected void startThread() {
        if (running && threads.size() < MAX_THREADS) {
            Thread thread = new Thread(new BasicSocketHandler());
            thread.start();
            threads.add(thread);

            LOGGER.tracev("Started new thread, running threads={0}", threads.size());
        }
    }

    /** 接受连接并逐行读取 syslog 消息的处理逻辑。 */
    private class BasicSocketHandler implements Runnable {

        @Override
        public void run() {
            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    LOGGER.trace("Socket accepted");
                    startThread();

                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    for (String l = br.readLine(); l != null; l = br.readLine()) {
                        try {
                            SysLog sysLog = SysLog.parse(l);
// TODO 嵌入式 Keycloak 测试时，testsuite 客户端侧日志也会经 syslog 发送，可能导致干扰
//                            LOGGER.tracev("New message={0}", sysLog.getMessage());
                            listeners.forEach(listener -> listener.onLog(sysLog));
                        } catch (Throwable t) {
                            LOGGER.tracev("Failed to parse message={0}", l);
                        }
                    }
                    socket.close();
                    LOGGER.trace("Socket closed");
                } catch (Throwable t) {
                    if (!serverSocket.isClosed()) {
                        LOGGER.trace(t.getMessage(), t);
                    }
                }
            }
        }
    }

}
