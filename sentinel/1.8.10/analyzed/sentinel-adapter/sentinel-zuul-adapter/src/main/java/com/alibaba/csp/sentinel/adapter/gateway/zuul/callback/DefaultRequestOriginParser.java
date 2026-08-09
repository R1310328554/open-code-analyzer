package com.alibaba.csp.sentinel.adapter.gateway.zuul.callback;

import javax.servlet.http.HttpServletRequest;

/**
 * {@link RequestOriginParser} 的默认实现，返回空字符串作为请求来源。
 *
 * @author tiger
 */
public class DefaultRequestOriginParser implements RequestOriginParser {

    @Override
    public String parseOrigin(HttpServletRequest request) {
        return "";
    }
}
