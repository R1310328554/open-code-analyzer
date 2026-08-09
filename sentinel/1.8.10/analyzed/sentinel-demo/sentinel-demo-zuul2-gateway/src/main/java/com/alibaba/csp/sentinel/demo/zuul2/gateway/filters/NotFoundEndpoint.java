package com.alibaba.csp.sentinel.demo.zuul2.gateway.filters;

import com.netflix.zuul.filters.http.HttpSyncEndpoint;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.message.http.HttpResponseMessage;
import com.netflix.zuul.message.http.HttpResponseMessageImpl;
import org.apache.http.HttpStatus;

/** 404 同步 Endpoint：路径未匹配时返回 HTTP NOT FOUND。 */
public class NotFoundEndpoint extends HttpSyncEndpoint {

    /** 构造 404 响应并缓冲响应体。 */
    @Override
    public HttpResponseMessage apply(HttpRequestMessage request) {
        HttpResponseMessage response = new HttpResponseMessageImpl(request.getContext(), request, HttpStatus.SC_NOT_FOUND);
        response.finishBufferedBodyIfIncomplete();
        return response;
    }
}