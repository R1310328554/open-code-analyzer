package com.taobao.arthas.core.shell.term.impl.http.session;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.taobao.arthas.core.util.StringUtils;

/**
 * {@link HttpSession} 的轻量实现：随机 ID + {@link ConcurrentHashMap} 属性表。
 * <p>
 * 时间相关与失效逻辑为占位实现，满足 HTTP 鉴权与用户标识传递即可。
 *
 * @author hengyunabc 2021-03-03
 *
 */
public class SimpleHttpSession implements HttpSession {
    /** 线程安全的会话属性存储 */
    private Map<String, Object> attributes = new ConcurrentHashMap<String, Object>();

    /** 32 位随机会话 ID */
    private String id;

    public SimpleHttpSession() {
        id = StringUtils.randomString(32);
    }

    @Override
    public long getCreationTime() {
        return 0;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getLastAccessedTime() {
        return 0;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {

    }

    @Override
    public int getMaxInactiveInterval() {
        return 0;
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(this.attributes.keySet());
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public void invalidate() {

    }

    @Override
    public boolean isNew() {
        return false;
    }

}
