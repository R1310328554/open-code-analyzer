package com.taobao.arthas.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Arthas 使用情况异步上报工具。
 * <p>
 * 在独立守护线程中向配置的 statUrl 发送启动与命令执行统计，
 * 包含 IP、版本、agentId 与命令参数；失败静默忽略。
 * </p>
 * Created by zhuyong on 15/11/12.
 */
public class UserStatUtil {

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private static final byte[] SKIP_BYTE_BUFFER = new byte[DEFAULT_BUFFER_SIZE];

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            final Thread t = new Thread(r, "arthas-UserStat");
            t.setDaemon(true);
            return t;
        }
    });
    private static final String ip = IPUtils.getLocalIP();

    private static final String version = URLEncoder.encode(ArthasBanner.version().replace("\n", ""));

    private static volatile String statUrl = null;

    private static volatile String agentId = null;

    /** 统计上报 URL。 */
    public static String getStatUrl() {
        return statUrl;
    }

    /** 设置统计上报地址。 */
    public static void setStatUrl(String url) {
        statUrl = url;
    }

    /** 当前 Agent 实例 id。 */
    public static String getAgentId() {
        return agentId;
    }

    /** 设置 Agent id，随上报携带。 */
    public static void setAgentId(String id) {
        agentId = id;
    }

    /** 异步上报 Agent 启动事件。 */
    public static void arthasStart() {
        if (statUrl == null) {
            return;
        }
        RemoteJob job = new RemoteJob();
        job.appendQueryData("ip", ip);
        job.appendQueryData("version", version);
        if (agentId != null) {
            job.appendQueryData("agentId", agentId);
        }
        job.appendQueryData("command", "start");

        try {
            executorService.execute(job);
        } catch (Throwable t) {
            //
        }
    }

    /** 构造并异步提交命令使用上报任务。 */
    private static void arthasUsage(String cmd, String detail, String userId) {
        RemoteJob job = new RemoteJob();
        job.appendQueryData("ip", ip);
        job.appendQueryData("version", version);
        if (agentId != null) {
            job.appendQueryData("agentId", agentId);
        }
        if (userId != null) {
            job.appendQueryData("userId", URLEncoder.encode(userId));
        }
        job.appendQueryData("command", URLEncoder.encode(cmd));
        if (detail != null) {
            job.appendQueryData("arguments", URLEncoder.encode(detail));
        }

        try {
            executorService.execute(job);
        } catch (Throwable t) {
            //
        }
    }

    /**
     * 上报命令成功执行及完整参数字符串。
     *
     * @param cmd 命令名
     * @param args 参数列表
     * @param userId 用户标识，可为 null
     */
    public static void arthasUsageSuccess(String cmd, List<String> args, String userId) {
        if (statUrl == null) {
            return;
        }
        StringBuilder commandString = new StringBuilder(cmd);
        for (String arg : args) {
            commandString.append(" ").append(arg);
        }
        UserStatUtil.arthasUsage(cmd, commandString.toString(), userId);
    }

    /** 上报命令成功执行（无 userId）。 */
    public static void arthasUsageSuccess(String cmd, List<String> args) {
        arthasUsageSuccess(cmd, args, null);
    }

    public static void destroy() {
        // 直接关闭，没有回报的丢弃
        executorService.shutdownNow();
    }

    /** 后台 HTTP GET 上报任务，读尽响应体后丢弃。 */
    static class RemoteJob implements Runnable {
        private StringBuilder queryData = new StringBuilder();

        /** 追加 URL 查询参数键值对。 */
        public void appendQueryData(String key, String value) {
            if (key != null && value != null) {
                if (queryData.length() == 0) {
                    queryData.append(key).append("=").append(value);
                } else {
                    queryData.append("&").append(key).append("=").append(value);
                }
            }
        }

        @Override
        public void run() {
            String link = statUrl;
            if (link == null) {
                return;
            }
            InputStream inputStream = null;
            try {
                if (queryData.length() != 0) {
                    link = link + "?" + queryData;
                }
                URL url = new URL(link);
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(1000);
                connection.setReadTimeout(1000);
                connection.connect();
                inputStream = connection.getInputStream();
                //noinspection StatementWithEmptyBody
                while (inputStream.read(SKIP_BYTE_BUFFER) != -1) {
                    // 丢弃响应体，仅完成请求                    // do nothing
                }
            } catch (Throwable t) {
                // ignore
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
    }
}
