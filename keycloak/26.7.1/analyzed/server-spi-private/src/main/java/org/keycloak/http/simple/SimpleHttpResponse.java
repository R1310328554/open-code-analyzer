package org.keycloak.http.simple;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import org.keycloak.connections.httpclient.SafeInputStream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;

/**
 * HTTP 响应包装：惰性读取实体、支持 gzip 解压与响应大小上限。
 * <p>实现 {@link java.lang.AutoCloseable}，应在用毕后关闭以释放连接。</p>
 */
public class SimpleHttpResponse implements AutoCloseable {

    private final HttpResponse response;
    private final long maxConsumedResponseSize;
    private final ObjectMapper objectMapper;
    private int statusCode = -1;
    private String responseString;
    private ContentType contentType;

    public SimpleHttpResponse(HttpResponse response, long maxConsumedResponseSize, ObjectMapper objectMapper) {
        this.response = response;
        this.maxConsumedResponseSize = maxConsumedResponseSize;
        this.objectMapper = objectMapper;
    }

    private void readResponse() throws IOException {
        if (statusCode == -1) {
            statusCode = response.getStatusLine().getStatusCode();

            InputStream is;
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                is = entity.getContent();
                contentType = ContentType.getOrDefault(entity);
                Charset charset = contentType.getCharset();
                try {
                    HeaderIterator it = response.headerIterator();
                    while (it.hasNext()) {
                        Header header = it.nextHeader();
                        if (header.getName().equals("Content-Encoding") && header.getValue().equals("gzip")) {
                            is = new GZIPInputStream(is);
                        }
                    }

                    is = new SafeInputStream(is, maxConsumedResponseSize);

                    try (InputStreamReader reader = charset == null ? new InputStreamReader(is, StandardCharsets.UTF_8) :
                            new InputStreamReader(is, charset)) {

                        StringWriter writer = new StringWriter();

                        char[] buffer = new char[1024 * 4];
                        for (int n = reader.read(buffer); n != -1; n = reader.read(buffer)) {
                            writer.write(buffer, 0, n);
                        }

                        responseString = writer.toString();
                    }
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
            }
        }
    }

    /** @return HTTP 状态码 */
    public int getStatus() throws IOException {
        readResponse();
        return response.getStatusLine().getStatusCode();
    }

    /** 将响应体解析为 {@link com.fasterxml.jackson.databind.JsonNode}。 */
    public JsonNode asJson() throws IOException {
        return objectMapper.readTree(asString());
    }

    public <T> T asJson(Class<T> type) throws IOException {
        return objectMapper.readValue(asString(), type);
    }

    public <T> T asJson(TypeReference<T> type) throws IOException {
        return objectMapper.readValue(asString(), type);
    }

    /** @return 响应体字符串（首次调用时读取并缓存） */
    public String asString() throws IOException {
        readResponse();
        return responseString;
    }

    /** @return 指定响应头的第一个值，不存在时为 {@code null} */
    public String getFirstHeader(String name) throws IOException {
        readResponse();
        Header[] headers = response.getHeaders(name);

        if (headers != null && headers.length > 0) {
            return headers[0].getValue();
        }

        return null;
    }

    /** @return 指定响应头的全部值列表，不存在时为 {@code null} */
    public List<String> getHeader(String name) throws IOException {
        readResponse();
        Header[] headers = response.getHeaders(name);

        if (headers != null && headers.length > 0) {
            return Stream.of(headers).map(Header::getValue).collect(Collectors.toList());
        }

        return null;
    }

    public Header[] getAllHeaders() throws IOException {
        readResponse();
        return response.getAllHeaders();
    }

    /** @return 响应实体的 {@link org.apache.http.entity.ContentType} */
    public ContentType getContentType() throws IOException {
        readResponse();
        return contentType;
    }

    public Charset getContentTypeCharset() throws IOException {
        readResponse();
        if (contentType != null) {
            Charset charset = contentType.getCharset();
            if (charset != null) {
                return charset;
            }
        }
        return StandardCharsets.UTF_8;
    }

    /** 确保响应已读取并释放底层 HTTP 连接。 */
    public void close() throws IOException {
        readResponse();
    }
}
