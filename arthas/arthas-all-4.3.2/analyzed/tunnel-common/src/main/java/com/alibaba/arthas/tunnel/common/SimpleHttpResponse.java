package com.alibaba.arthas.tunnel.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 轻量 HTTP 响应模型，用于 tunnel 内 HTTP 代理在 client 与 server 间序列化传递。
 * 反序列化时通过白名单限制可加载的类，降低不安全反序列化风险。
 *
 * @author hengyunabc 2020-10-22
 *
 */
public class SimpleHttpResponse implements Serializable {
    private static final long serialVersionUID = 1L;

        /** 允许反序列化的类名白名单 */
    private static final List<String> whitelist = Arrays.asList(byte[].class.getName(), String.class.getName(),
            Map.class.getName(), HashMap.class.getName(), SimpleHttpResponse.class.getName());

        /** HTTP 状态码，默认 200 */
    private int status = 200;

        /** 响应头 */
    private Map<String, String> headers = new HashMap<String, String>();

        /** 响应体字节内容 */
    private byte[] content;

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

        /** 序列化为字节数组，便于 Base64 后经 WebSocket 传输 */
    public static byte[] toBytes(SimpleHttpResponse response) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(response);
            out.flush();
            return bos.toByteArray();
        }
    }

        /** 从字节反序列化；非白名单类名将抛出异常 */
    public static SimpleHttpResponse fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try (ObjectInputStream in = new ObjectInputStream(bis) {
            protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                if (!whitelist.contains(desc.getName())) {
                    throw new InvalidClassException("Unauthorized deserialization attempt", desc.getName());
                }
                return super.resolveClass(desc);
            }
        }) {
            return (SimpleHttpResponse) in.readObject();
        }
    }

}
