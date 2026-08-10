package org.keycloak.testframework.remote.runonserver;

import java.io.IOException;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.keycloak.testframework.remote.providers.runonserver.FetchOnServer;
import org.keycloak.testframework.remote.providers.runonserver.FetchOnServerWrapper;
import org.keycloak.testframework.remote.providers.runonserver.RunOnServer;
import org.keycloak.testframework.remote.providers.runonserver.RunOnServerException;
import org.keycloak.testframework.remote.providers.runonserver.SerializationUtil;
import org.keycloak.util.JsonSerialization;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

/**
 * 通过 HTTP 调用 Keycloak 测试端点，在服务端执行序列化代码并获取返回值。
 * <p>
 * 支持 {@link org.keycloak.testframework.remote.providers.runonserver.RunOnServer} 断言执行
 * 以及 {@link org.keycloak.testframework.remote.providers.runonserver.FetchOnServer} 取值操作。
 */
public class RunOnServerClient {

    private static final String RUN_ON_SERVER_ENDPOINT = "/testing-run-on-server";
    private final HttpClient httpClient;
    private final String url;
    private final int executionId;

    /**
     * 构造客户端并绑定目标 realm 的 RunOnServer 端点。
     *
     * @param httpClient 用于发送 HTTP 请求的客户端
     * @param realmUrl 目标 realm 的基础 URL
     * @param executionId 执行上下文标识，用于隔离并发测试
     */
    public RunOnServerClient(HttpClient httpClient, String realmUrl, int executionId) {
        this.httpClient = httpClient;
        this.url = realmUrl + RUN_ON_SERVER_ENDPOINT;
        this.executionId = executionId;
    }

    /**
     * 使用包装器从 Keycloak 服务器获取指定类型的返回值。
     *
     * @param wrapper 包含待执行代码与返回类型的包装器
     * @param <T> 返回值类型
     * @return 服务端执行结果
     * @throws RunOnServerException 远程执行失败时抛出
     */
    public <T> T fetch(FetchOnServerWrapper<T> wrapper) throws RunOnServerException {
        return fetch(wrapper.getRunOnServer(), wrapper.getResultClass());
    }

    /**
     * 在服务端执行函数并返回解码后的字符串（与服务端方法返回值一致）。
     *
     * @param function 待执行的取值函数
     * @return 服务端返回的字符串
     * @throws RunOnServerException 远程执行失败时抛出
     */
    public String fetchString(FetchOnServer function) throws RunOnServerException {
        return fetch(function, String.class);
    }

    /**
     * 在服务端执行函数并将 JSON 结果反序列化为指定类型。
     *
     * @param function 待执行的取值函数
     * @param clazz 期望的返回类型
     * @param <T> 返回值类型
     * @return 反序列化后的对象，服务端无返回值时为 {@code null}
     * @throws RunOnServerException 远程执行失败时抛出
     */
    public <T> T fetch(FetchOnServer function, Class<T> clazz) throws RunOnServerException {
        try {
            String s = fetchStringInternal(function);
            return s == null ? null : JsonSerialization.readValue(s, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 内部方法：序列化函数、调用 RunOnServer 端点并解析响应字符串。
     *
     * @param function 待执行的取值函数
     * @return 服务端原始字符串响应
     * @throws RunOnServerException 远程执行失败或断言失败时抛出
     */
    private String fetchStringInternal(FetchOnServer function) throws RunOnServerException {
        String encoded = SerializationUtil.encode(function);

        String result = runOnServer(encoded);
        if (result != null && !result.isEmpty() && result.trim().startsWith("EXCEPTION:")) {
            Throwable t = SerializationUtil.decodeException(result);
            if (t instanceof AssertionError) {
                throw (AssertionError) t;
            } else {
                throw new RunOnServerException(t);
            }
        } else {
            return result;
        }
    }

    /**
     * 在 Keycloak 服务器上执行代码，可包含服务端断言以验证服务器侧状态。
     *
     * @param function 待执行的 RunOnServer 函数
     * @throws RunOnServerException 远程执行失败时抛出
     */
    public void run(RunOnServer function) throws RunOnServerException {
        String encoded = SerializationUtil.encode(function);

        String result = runOnServer(encoded);
        if (result != null && !result.isEmpty() && result.trim().startsWith("EXCEPTION:")) {
            Throwable t = SerializationUtil.decodeException(result);
            if (t instanceof AssertionError) {
                throw (AssertionError) t;
            } else {
                throw new RunOnServerException(t);
            }
        }
    }

    private String runOnServer(String encoded) throws RunOnServerException {
        try {
            HttpPost request = new HttpPost(url + "?executionId=" + executionId);
            request.setHeader("Content-type", "text/plain;charset=utf-8");
            request.setEntity(new StringEntity(encoded));

            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == Response.Status.OK.getStatusCode()) {
                return EntityUtils.toString(response.getEntity());
            } else if (statusCode == Response.Status.NO_CONTENT.getStatusCode()) {
                return null;
            } else {
                throw new WebApplicationException(String.format("Unexpected response status for RunOnServer: %s", statusCode));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
