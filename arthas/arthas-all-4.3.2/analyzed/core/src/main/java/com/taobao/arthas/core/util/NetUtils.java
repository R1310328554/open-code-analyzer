package com.taobao.arthas.core.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.taobao.arthas.common.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

/**
 * HTTP 与 Socket 网络请求工具，用于 Agent 侧调用远程 API 及 Pandora QOS 探测。
 *
 * @author ralf0131 on 2015-11-11 15:39.
 */
public class NetUtils {

    private static final String QOS_HOST = "localhost";
    private static final int QOS_PORT = 12201;
    private static final String QOS_RESPONSE_START_LINE = "pandora>[QOS Response]";
    private static final int INTERNAL_SERVER_ERROR = 500;
    private static final int CONNECT_TIMEOUT = 1000;
    private static final int READ_TIMEOUT = 3000;

    /**
     * 发起 HTTP GET 请求并解析响应（类似 Apache HttpClient 语义）。
     * <p>
     * 优先 Accept JSON；500 时尝试解析 {@code errorMsg} 字段；IO 失败时
     * {@link Response#isSuccess()} 为 false。
     *
     * @param urlString 请求 URL
     * @return 封装成功标志与正文内容的 Response
     */
    public static Response request(String urlString) {
        HttpURLConnection urlConnection = null;
        InputStream in = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
            urlConnection.setReadTimeout(READ_TIMEOUT);;
            // 优先接受 JSON，其次 plain text
            urlConnection.setRequestProperty("Accept", "application/json,text/plain;q=0.2");
            in = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            int statusCode = urlConnection.getResponseCode();
            String result = sb.toString().trim();
            if (statusCode == INTERNAL_SERVER_ERROR) {
                JSONObject errorObj = JSON.parseObject(result);
                if (errorObj.containsKey("errorMsg")) {
                    return new Response(errorObj.getString("errorMsg"), false);
                }
                return new Response(result, false);
            }
            return new Response(result);
        } catch (IOException e) {
            return new Response(e.getMessage(), false);
        } finally {
            IOUtils.close(in);
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    /**
     * @deprecated 基于 HttpURLConnection，无法妥善处理非 200 状态码
     * @param url 请求 URL
     * @return 响应正文；异常时返回 null
     */
    public static String simpleRequest(String url) {
        BufferedReader br = null;
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestProperty("Accept", "application/json");
            int responseCode = con.getResponseCode();

            br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            String result = sb.toString().trim();
            if (responseCode == 500) {
                JSONObject errorObj = JSON.parseObject(result);
                if (errorObj.containsKey("errorMsg")) {
                    return errorObj.getString("errorMsg");
                }
                return result;
            } else {
                return result;
            }

        } catch (Exception e) {
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * 通过原始 Socket 访问 Pandora QOS（仅 tomcat monitor &lt;= 1.0.1 等旧场景）。
     * <p>
     * 向 {@code localhost:12201} 发送简易 HTTP GET，并从
     * {@value #QOS_RESPONSE_START_LINE} 之后截取有效正文（兼容非标准 HTTP 响应）。
     *
     * @param path 相对路径，如 {@code /pandora/ls} 或 {@code /pandora/find?arg0=RPCProtocolService}
     * @return QOS 响应封装
     */
    public static Response requestViaSocket(String path) {
        BufferedReader br = null;
        try {
            Socket s = new Socket(QOS_HOST, QOS_PORT);
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            pw.println("GET " + path + " HTTP/1.1");
            pw.println("Host: " + QOS_HOST + ":" + QOS_PORT);
            pw.println("");
            pw.flush();

            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;
            boolean start = false;
            while ((line = br.readLine()) != null) {
                if (start) {
                    sb.append(line).append("\n");
                }
                if (line.equals(QOS_RESPONSE_START_LINE)) {
                    start = true;
                }
            }
            String result = sb.toString().trim();
            return new Response(result);
        } catch (Exception e) {
            return new Response(e.getMessage(), false);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /** HTTP/Socket 请求结果：是否成功及响应正文 */
    public static class Response {

        private boolean success;
        private String content;

        /** 指定成功标志与内容 */
        public Response(String content, boolean success) {
            this.success = success;
            this.content = content;
        }

        /** 默认视为成功的响应 */
        public Response(String content) {
            this.content = content;
            this.success = true;
        }

        /** @return 请求是否成功完成 */
        public boolean isSuccess() {
            return success;
        }

        /** @return 响应正文或错误信息 */
        public String getContent() {
            return content;
        }
    }


    /**
     * 探测指定主机端口是否可建立 TCP 连接（监听中）。
     *
     * @param host 主机名或 IP
     * @param port 端口号
     * @return true 表示端口可达
     */
    public static boolean serverListening(String host, int port) {
        Socket s = null;
        try {
            s = new Socket(host, port);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }


}
