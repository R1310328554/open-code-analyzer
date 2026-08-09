
package com.taobao.arthas.core.shell.term.impl.http.session;

import java.util.Enumeration;

/**
 * HTTP 会话抽象，语义对齐 Servlet {@code HttpSession}，供 Netty HTTP 鉴权与属性存储。
 *
 * @author hengyunabc 2021-03-03
 *
 */
public interface HttpSession {

    /**
     * 返回会话创建时间（毫秒，自 1970-01-01 GMT 起）。
     *
     * @return 创建时间戳
     * @exception IllegalStateException 会话已失效时调用
     */
    public long getCreationTime();

    /**
     * 返回本会话的唯一标识符（由容器分配，实现相关）。
     *
     * @return 会话 ID 字符串
     * @exception IllegalStateException 会话已失效时调用
     */
    public String getId();

    /**
     * 返回客户端最后一次携带本会话的请求到达容器的时间（毫秒）。
     * <p>
     * 应用内 get/set 属性不会刷新该时间。
     *
     * @return 最后访问时间戳
     * @exception IllegalStateException 会话已失效时调用
     */
    public long getLastAccessedTime();

    /**
     * 设置会话最大空闲间隔（秒）；0 或负数表示永不过期。
     *
     * @param interval 空闲秒数
     */
    public void setMaxInactiveInterval(int interval);

    /**
     * 返回最大空闲间隔（秒），见 {@link #setMaxInactiveInterval}。
     *
     * @return 空闲秒数
     * @see #setMaxInactiveInterval
     */
    public int getMaxInactiveInterval();

    /**
     * 按名称获取绑定属性；不存在则返回 {@code null}。
     *
     * @param name 属性名
     * @return 绑定对象或 null
     * @exception IllegalStateException 会话已失效时调用
     */
    public Object getAttribute(String name);

    /**
     * 返回所有已绑定属性名的枚举。
     *
     * @return 属性名枚举
     * @exception IllegalStateException 会话已失效时调用
     */
    public Enumeration<String> getAttributeNames();

    /**
     * 将对象绑定到指定名称；同名则替换。传入 null 等效于 {@link #removeAttribute(String)}。
     *
     * @param name  属性名，不可为 null
     * @param value 绑定值
     * @exception IllegalStateException 会话已失效时调用
     */
    public void setAttribute(String name, Object value);

    /**
     * 移除指定名称的绑定属性；不存在则无操作。
     *
     * @param name 属性名
     * @exception IllegalStateException 会话已失效时调用
     */
    public void removeAttribute(String name);

    /**
     * 使会话失效并解绑所有属性。
     *
     * @exception IllegalStateException 会话已失效时再次调用
     */
    public void invalidate();

    /**
     * 客户端是否尚未加入本会话（如禁用 Cookie 时每次请求视为新会话）。
     *
     * @return 客户端未加入时为 true
     * @exception IllegalStateException 会话已失效时调用
     */
    public boolean isNew();
}
