package org.keycloak.testframework.remote.timeoffset;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.keycloak.common.util.Time;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;

/**
 * 通过 HTTP 端点同步 Keycloak 服务器与本地测试进程的时间偏移。
 * <p>
 * 调用 {@link org.keycloak.common.util.Time#setOffset(int)} 更新客户端侧时间，
 * 并向 {@code /testing-timeoffset} 发送 JSON 以更新服务端（可选包含缓存）。
 */
public class TimeOffSet {
    private int currentOffset;
    private final String KEY_OFFSET = "offset";
    private final String CACHES = "caches";
    private final String TIME_OFFSET_ENDPOINT = "/testing-timeoffset";
    private final HttpClient httpClient;
    private final String serverUrl;
    private boolean enableForCaches;

    /**
     * 创建时间偏移控制器；若初始偏移非零则立即应用。
     *
     * @param httpClient 用于调用服务端端点的 HTTP 客户端
     * @param serverUrl Keycloak 服务器基础 URL
     * @param initOffset 初始偏移秒数
     * @param enableForCaches 是否同步到底层缓存
     */
    public TimeOffSet(HttpClient httpClient, String serverUrl, int initOffset, boolean enableForCaches) {
        this.httpClient = httpClient;
        this.serverUrl = serverUrl;
        this.enableForCaches = enableForCaches;
        if (initOffset != 0) {
            set(initOffset);
        }
        currentOffset = initOffset;
    }

    /**
     * 启用缓存时间偏移同步；若已设置非零偏移则刷新服务端状态。
     */
    public void enableForCaches() {
        this.enableForCaches = true;
        if (currentOffset != 0) {
            set(currentOffset); // Refresh the server (in case that timeOffset was already set there)
        }
    }

    /**
     * 设置 Keycloak 服务器与本地测试的时间偏移（秒）。
     *
     * @param offset 偏移秒数，正数表示将时钟拨快
     * @throws RuntimeException HTTP 调用失败或响应非 200 时抛出
     */
    public void set(int offset) throws RuntimeException {
        currentOffset = offset;

        // set for tests
        Time.setOffset(currentOffset);

        // set for KC server
        var time = Map.of(KEY_OFFSET, currentOffset, CACHES, enableForCaches);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(time);

            HttpPut request = new HttpPut(serverUrl + TIME_OFFSET_ENDPOINT);
            request.setEntity(new StringEntity(json));
            request.setHeader("Content-type", "application/json");

            HttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() != Response.Status.OK.getStatusCode()) {
                var statusLine = response.getStatusLine();
                throw new WebApplicationException(String.format("Unexpected response status for TimeOffSet: %d %s", statusLine.getStatusCode(), statusLine.getReasonPhrase()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 与 {@link #set(int)} 相同，但接受 {@link Duration} 参数。
     *
     * @param duration 时间偏移量，不可为 {@code null}
     */
    public void set(Duration duration) {
        Objects.requireNonNull(duration, "duration can not be null");
        set(Math.toIntExact(duration.toSeconds()));
    }

    /**
     * 获取当前已应用的时间偏移（秒）。
     *
     * @return 当前偏移秒数
     */
    public int get() {
        return currentOffset;
    }

    /** 是否已应用非零时间偏移。 */
    public boolean hasChanged() {
        return currentOffset != 0;
    }
}
