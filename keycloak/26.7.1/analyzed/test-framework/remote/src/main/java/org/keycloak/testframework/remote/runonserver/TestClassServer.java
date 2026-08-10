package org.keycloak.testframework.remote.runonserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * 在嵌入式 HTTP 服务器上暴露测试类字节码与 JSON 资源。
 * <p>
 * Keycloak 服务端通过 {@link #CONTEXT_PATH} 按需下载类文件，以支持 RunOnServer 远程执行。
 */
public class TestClassServer {

    /** 测试类资源的 HTTP 上下文路径。 */
    public static final String CONTEXT_PATH = "/test-classes/";

    private final HttpServer httpServer;

    /** 默认允许下载的包名前缀（测试框架与 JUnit 相关包）。 */
    public static final Set<String> DEFAULT_PERMITTED_PACKAGES = Set.of(
        "org.keycloak.testframework",
        "org.junit",
        "org.opentest4j"
    );

    private final Set<String> permittedPackages;

    /**
     * 在指定 HTTP 服务器上注册测试类路径处理器。
     *
     * @param httpServer 嵌入式 HTTP 服务器
     */
    TestClassServer(HttpServer httpServer) {
        this.httpServer = httpServer;
        permittedPackages = new HashSet<>(DEFAULT_PERMITTED_PACKAGES);

        httpServer.createContext(CONTEXT_PATH, new TestClassPathHandler());
    }

    /**
     * 追加允许通过 HTTP 下载的包名前缀。
     *
     * @param permittedPackages 额外允许的包前缀集合
     */
    public void addPermittedPackages(Set<String> permittedPackages) {
        this.permittedPackages.addAll(permittedPackages);
    }

    /** 从 HTTP 服务器移除测试类上下文。 */
    public void close() {
        httpServer.removeContext(CONTEXT_PATH);
    }

    private class TestClassPathHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String resource = httpExchange.getRequestURI().getPath().substring(CONTEXT_PATH.length() - 1);

            Headers respHeaders = httpExchange.getResponseHeaders();
            respHeaders.set("Content-Type", "application/x-java-applet;charset=utf-8");

            if (!isPermittedPackage(resource) || !(resource.endsWith(".class") || resource.endsWith(".json"))) {
                httpExchange.sendResponseHeaders(403, 0);
            } else {
                try (InputStream resourceStream = TestClassServer.class.getResourceAsStream(resource)) {
                    if (resourceStream != null) {
                        byte[] bytes = resourceStream.readAllBytes();
                        httpExchange.sendResponseHeaders(200, bytes.length);
                        httpExchange.getResponseBody().write(bytes);
                    } else {
                        httpExchange.sendResponseHeaders(404, 0);
                    }
                }
            }
            httpExchange.close();
        }

        private boolean isPermittedPackage(String className) {
            String c = className.substring(1).replace('/', '.');
            for (String p : permittedPackages) {
                if (c.startsWith(p)) {
                    return true;
                }
            }
            return false;
        }
    }

}
