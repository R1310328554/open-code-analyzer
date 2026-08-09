package com.alibaba.csp.sentinel.demo.servlet.controller;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 演示 Servlet：按 pathInfo 路由 /foo 与 /bar 请求。
 *
 * @author zhangxunwei
 * @date 2024/6/24
 */
public class DefaultServlet implements Servlet {
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    /** 根据 pathInfo 前缀分发到 foo/bar 或返回 404。 */
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        String path = ((HttpServletRequest) servletRequest).getPathInfo();

        if (path.startsWith("/foo")) {
            handleFoo(servletRequest, servletResponse);
        } else if (path.startsWith("/bar")) {
            handleBar(servletRequest, servletResponse);
        } else {
            notFound(servletRequest, servletResponse);
        }
    }

    /** 返回 404 与路径未找到提示。 */
    private void notFound(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        httpServletResponse.setStatus(404);
        httpServletResponse.setContentType("text/plain");
        httpServletResponse.getWriter().write(httpServletRequest.getServletPath() + " not found.");
        httpServletResponse.getWriter().close();
    }

    /** 处理 /bar 请求，返回 "bar"。 */
    private void handleBar(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        httpServletResponse.setStatus(200);
        httpServletResponse.setContentType("text/plain");
        httpServletResponse.getWriter().write("bar");
        httpServletResponse.getWriter().close();
    }

    /** 处理 /foo/{id} 请求，返回 "Hello {id}"。 */
    private void handleFoo(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        String path = httpServletRequest.getPathInfo();
        String id = path.replaceAll("/foo/(\\d+)", "$1");

        httpServletResponse.setStatus(200);
        httpServletResponse.setContentType("text/plain");
        httpServletResponse.getWriter().write("Hello " + id);
        httpServletResponse.getWriter().close();
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {
    }
}
