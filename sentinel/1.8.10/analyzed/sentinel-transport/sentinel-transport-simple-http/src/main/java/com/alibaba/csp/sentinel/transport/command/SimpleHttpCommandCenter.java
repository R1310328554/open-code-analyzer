/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.transport.command;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandHandlerProvider;
import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.transport.log.CommandCenterLog;
import com.alibaba.csp.sentinel.transport.CommandCenter;
import com.alibaba.csp.sentinel.transport.command.http.HttpEventTask;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.util.StringUtil;

/***
 * 简易 HTTP 命令中心：基于 {@link ServerSocket} 接受 Dashboard 下发的控制命令。
 * 在独立线程中绑定端口，业务请求由线程池异步处理 {@link HttpEventTask}。
 *
 * @author youji.zj
 */
public class SimpleHttpCommandCenter implements CommandCenter {

    /** 端口未初始化时的占位值。 */
    private static final int PORT_UNINITIALIZED = -1;

    /** 接受连接后 Socket 读超时（毫秒）。 */
    private static final int DEFAULT_SERVER_SO_TIMEOUT = 3000;
    /** 未配置端口时的默认监听端口。 */
    private static final int DEFAULT_PORT = 8719;

    @SuppressWarnings("rawtypes")
    /** 命令名到 {@link CommandHandler} 的全局注册表。 */
    private static final Map<String, CommandHandler> handlerMap = new ConcurrentHashMap<String, CommandHandler>();

    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    /** 负责端口绑定与 accept 循环的单线程池。 */
    private ExecutorService executor = Executors.newSingleThreadExecutor(
        new NamedThreadFactory("sentinel-command-center-executor", true));
    /** 处理 {@link HttpEventTask} 的业务线程池。 */
    private ExecutorService bizExecutor;

    /** 当前监听的 ServerSocket，stop 时关闭。 */
    private ServerSocket socketReference;

    @Override
    @SuppressWarnings("rawtypes")
    public void beforeStart() throws Exception {
        // 注册 SPI 加载的全部命令处理器
        Map<String, CommandHandler> handlers = CommandHandlerProvider.getInstance().namedHandlers();
        registerCommands(handlers);
    }

    @Override
    public void start() throws Exception {
        int nThreads = Runtime.getRuntime().availableProcessors();
        this.bizExecutor = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(10),
            new NamedThreadFactory("sentinel-command-center-service-executor", true),
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    CommandCenterLog.info("命令任务被拒绝，线程池已满");
                    throw new RejectedExecutionException();
                }
            });

        Runnable serverInitTask = new Runnable() {
            int port;

            {
                try {
                    port = Integer.parseInt(TransportConfig.getPort());
                } catch (Exception e) {
                    port = DEFAULT_PORT;
                }
            }

            @Override
            public void run() {
                boolean success = false;
                ServerSocket serverSocket = getServerSocketFromBasePort(port);

                if (serverSocket != null) {
                    CommandCenterLog.info("[CommandCenter] 开始在端口 " + serverSocket.getLocalPort() + " 监听");
                    socketReference = serverSocket;
                    executor.submit(new ServerThread(serverSocket));
                    success = true;
                    port = serverSocket.getLocalPort();
                } else {
                    CommandCenterLog.info("[CommandCenter] 端口绑定失败，HTTP 命令中心不可用");
                }

                if (!success) {
                    port = PORT_UNINITIALIZED;
                }

                TransportConfig.setRuntimePort(port);
                executor.shutdown();
            }

        };

        new Thread(serverInitTask).start();
    }

    /**
     * 从 basePort 起递增尝试绑定可用端口（每 3 次失败 port+1）。<br>
     * 端口被占用时自动递增重试。
     *
     * @param basePort 起始端口
     * @return 绑定成功的 ServerSocket，全部失败时返回 null
     */
    private static ServerSocket getServerSocketFromBasePort(int basePort) {
        int tryCount = 0;
        while (true) {
            try {
                ServerSocket server = new ServerSocket(basePort + tryCount / 3, 100);
                server.setReuseAddress(true);
                return server;
            } catch (IOException e) {
                tryCount++;
                try {
                    TimeUnit.MILLISECONDS.sleep(30);
                } catch (InterruptedException e1) {
                    break;
                }
            }
        }
        return null;
    }

    @Override
    public void stop() throws Exception {
        if (socketReference != null) {
            try {
                socketReference.close();
            } catch (IOException e) {
                CommandCenterLog.warn("Error when releasing the server socket", e);
            }
        }

        if (bizExecutor != null) {
            bizExecutor.shutdownNow();
        }
        executor.shutdownNow();
        TransportConfig.setRuntimePort(PORT_UNINITIALIZED);
        handlerMap.clear();
    }

    /**
     * 获取已注册命令名称集合。
     */
    public static Set<String> getCommands() {
        return handlerMap.keySet();
    }

    /** accept 循环线程：接收连接并提交 {@link HttpEventTask}。 */
    class ServerThread extends Thread {

        private ServerSocket serverSocket;

        ServerThread(ServerSocket s) {
            this.serverSocket = s;
            setName("sentinel-courier-server-accept-thread");
        }

        @Override
        public void run() {
            while (true) {
                Socket socket = null;
                try {
                    socket = this.serverSocket.accept();
                    setSocketSoTimeout(socket);
                    HttpEventTask eventTask = new HttpEventTask(socket);
                    bizExecutor.submit(eventTask);
                } catch (Exception e) {
                    CommandCenterLog.info("Server error", e);
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (Exception e1) {
                            CommandCenterLog.info("Error when closing an opened socket", e1);
                        }
                    }
                    try {
                        // 避免异常时日志刷屏
                        Thread.sleep(10);
                    } catch (InterruptedException e1) {
                        // 中断 accept 循环
                        break;
                    }
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public static CommandHandler getHandler(String commandName) {
        return handlerMap.get(commandName);
    }

    @SuppressWarnings("rawtypes")
    public static void registerCommands(Map<String, CommandHandler> handlerMap) {
        if (handlerMap != null) {
            for (Entry<String, CommandHandler> e : handlerMap.entrySet()) {
                registerCommand(e.getKey(), e.getValue());
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public static void registerCommand(String commandName, CommandHandler handler) {
        if (StringUtil.isEmpty(commandName)) {
            return;
        }

        if (handlerMap.containsKey(commandName)) {
            CommandCenterLog.warn("注册失败（命令重复）: " + commandName);
            return;
        }

        handlerMap.put(commandName, handler);
    }

    /**
     * 设置 Socket 读超时，避免 accept 后线程永久阻塞（默认 3 秒）。
     */
    private void setSocketSoTimeout(Socket socket) throws SocketException {
        if (socket != null) {
            socket.setSoTimeout(DEFAULT_SERVER_SO_TIMEOUT);
        }
    }
}
