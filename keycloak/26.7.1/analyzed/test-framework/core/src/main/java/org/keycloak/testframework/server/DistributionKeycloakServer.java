package org.keycloak.testframework.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.common.Version;
import org.keycloak.it.utils.Maven;
import org.keycloak.quarkus.runtime.Environment;
import org.keycloak.representations.info.ServerInfoRepresentation;
import org.keycloak.testframework.util.FileUtils;
import org.keycloak.testframework.util.ProcessUtils;
import org.keycloak.testframework.util.TmpDir;

import io.quarkus.fs.util.ZipUtils;
import org.jboss.logging.Logger;

/**
 * 基于 Quarkus 发行包（ZIP 解压）的 {@link KeycloakServer} 实现。
 * <p>
 * 解压本地或 Maven 解析的 dist、以子进程启动 {@code kc.sh/kc.bat}，
 * 支持进程复用、Provider 热部署与就绪探针等待。
 */
public class DistributionKeycloakServer implements KeycloakServer {

    /** 本类日志记录器。 */
    private static final Logger log = Logger.getLogger(DistributionKeycloakServer.class);

    /** 解压安装目录根路径。 */
    private static final File INSTALL_DIR = Path.of(TmpDir.resolveTmpDir().getAbsolutePath(), "kc-test-framework", "keycloak").toFile();
    /** 平台相关的 Keycloak 启动脚本名。 */
    private static final String CMD = "kc" + (Environment.isWindows() ? ".bat" : ".sh");

    /** 当前 Keycloak 安装主目录（{@code KEYCLOAK_HOME}）。 */
    private File keycloakHomeDir;
    /** 托管 Keycloak 子进程。 */
    private Process keycloakProcess;

    /** 是否在启动环境中启用 DEBUG。 */
    private final boolean debug;
    /** 是否尝试复用已在运行的托管实例。 */
    private final boolean reuse;
    /** 启动与就绪等待超时（秒）。 */
    private final long startTimeout;
    /** 当前实例是否以 HTTPS 模式运行。 */
    private boolean tlsEnabled = false;

    /**
     * @param debug 是否启用远程调试环境变量
     * @param reuse 是否复用已有进程
     * @param startTimeout 启动超时（秒）
     */
    public DistributionKeycloakServer(boolean debug, boolean reuse, long startTimeout) {
        this.debug = debug;
        this.reuse = reuse;
        this.startTimeout = startTimeout;
    }

    /** 解压/复用安装、部署 Provider、启动进程并等待就绪。 */
    @Override
    public void start(KeycloakServerConfigBuilder keycloakServerConfigBuilder, boolean tlsEnabled) {
        this.tlsEnabled = tlsEnabled;

        List<String> args = keycloakServerConfigBuilder.toArgs();

        try {
            boolean installationCreated = createInstallation();
            if (!reuse) {
                killPreviousProcess();
            }

            ProviderDeployer providerDeployer = new ProviderDeployer(log, keycloakHomeDir, keycloakServerConfigBuilder.toDependencies(), KeycloakServer.getDependencyHotDeployEnabled());

            if (!installationCreated && reuse && ping()) {
                checkRunning();

                File startupArgsFile = getServerArgsFile();
                String startedWithArgs = startupArgsFile.isFile() ? FileUtils.readStringFromFile(startupArgsFile) : null;
                String requestedArgs = String.join(" ", args);

                boolean dependenciesChanged = providerDeployer.updateDependencies();
                if (requestedArgs.equals(startedWithArgs) && !dependenciesChanged) {
                    log.trace("Re-using already running Keycloak");
                    return;
                } else {
                    if (killPreviousProcess()) {
                        log.trace("Killed existing Keycloak");
                    } else {
                        throw new RuntimeException("Running Keycloak not started with required arguments or providers, and could not kill the current process");
                    }
                }
            } else {
                providerDeployer.updateDependencies();
            }

            OutputHandler outputHandler = startKeycloak(args);

            waitForStart(outputHandler);
            ReadinessProbe.waitUntilReady(this, startTimeout);

            if (!Environment.isWindows()) {
                FileUtils.writeToFile(getPidFile(), ProcessUtils.getKeycloakPid(keycloakProcess));
            }
            FileUtils.writeToFile(getServerArgsFile(), String.join(" ", args));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** 校验端口上运行的是本框架托管的 Keycloak 而非外来进程。 */
    private void checkRunning() {
        if (!Environment.isWindows()) {
            ProcessBuilder pb = new ProcessBuilder("fuser", "-n", "tcp", tlsEnabled ? "8443" : "8080");
            try {
                Process process = pb.start();
                process.waitFor(1, TimeUnit.SECONDS);
                String pid = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
                String expectedPid = FileUtils.readStringFromFile(getPidFile());
                if (!pid.equals(expectedPid)) {
                    throw new RuntimeException("Process running on port is not a managed Keycloak server");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            ServerInfoRepresentation serverInfo;
            try {
                serverInfo = getServerInfo();
            } catch (Throwable t) {
                throw new RuntimeException("Non-managed Keycloak server or other process running on " + getBaseUrl());
            }
            File userDir = new File(serverInfo.getSystemInfo().getUserDir()).getParentFile();
            if (!userDir.equals(keycloakHomeDir)) {
                throw new RuntimeException("Non-managed Keycloak server running from " + userDir);
            }
        }
    }

    /** 在 {@code bin} 目录下启动 Keycloak 子进程并绑定输出处理器。 */
    private DistributionKeycloakServer.OutputHandler startKeycloak(List<String> args) {
        log.trace("Starting Keycloak");
        List<String> cmd = new LinkedList<>();
        if (Environment.isWindows()) {
            cmd.add(keycloakHomeDir.toPath().resolve("bin").resolve(CMD).toString());
        } else {
            cmd.add("./" + CMD);
        }
        cmd.addAll(args);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File(keycloakHomeDir, "bin"));

        if (debug) {
            pb.environment().put("DEBUG", "true");
        }

        OutputHandler outputHandler;
        try {
            keycloakProcess = pb.start();
            outputHandler = new OutputHandler(keycloakProcess);
            new Thread(outputHandler).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return outputHandler;
    }

    /** 非复用模式下终止子进程并清理 PID 文件。 */
    @Override
    public void stop() {
        if (!reuse) {
            ProcessUtils.killRunningProcess(keycloakProcess);

            File pidFile = getPidFile();
            if (pidFile.exists()) {
                FileUtils.delete(pidFile);
            }
        }
    }

    /** 读取 PID 文件并终止上一次托管的 Keycloak 进程。 */
    private boolean killPreviousProcess() {
        if (!Environment.isWindows()) {
            File pidFile = getPidFile();
            if (pidFile.exists()) {
                try {
                    String previousPid = FileUtils.readStringFromFile(pidFile);
                    if (ProcessUtils.killProcess(previousPid)) {
                        log.trace("Killed running managed Keycloak: " + previousPid);
                        FileUtils.delete(pidFile);
                        return true;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return false;
    }

    /** {@inheritDoc} 根据 TLS 返回 {@code https://localhost:8443} 或 HTTP 8080。 */
    @Override
    public String getBaseUrl() {
        if (tlsEnabled) {
            return "https://localhost:8443";
        } else {
            return "http://localhost:8080";
        }
    }

    /** {@inheritDoc} 返回 Quarkus 管理端基址（端口 9000）。 */
    @Override
    public String getManagementBaseUrl() {
        if (tlsEnabled) {
            return "https://localhost:9000";
        } else {
            return "http://localhost:9000";
        }
    }

    /** 解压发行 ZIP 到临时目录，必要时复用已有安装。 */
    private boolean createInstallation() throws IOException {
        File dist = resolveKeycloakDist();

        if (INSTALL_DIR.isDirectory()) {
            File[] f = INSTALL_DIR.listFiles();
            if (f != null && f.length == 1) {
                long fromZipLastModified = FileUtils.readLongFromFile(getZipLastModifiedFile(f[0]));
                if (fromZipLastModified != dist.lastModified()) {
                    log.trace("Deleting installation from a previous distribution");
                    FileUtils.delete(INSTALL_DIR);
                } else {
                    log.trace("Re-using previous installation");
                    keycloakHomeDir = f[0];
                    return false;
                }
            }
        }

        if (INSTALL_DIR.isDirectory()) {
            FileUtils.delete(INSTALL_DIR);
        }
        if (!INSTALL_DIR.mkdirs()) {
            throw new IOException("Failed to create directory " + INSTALL_DIR);
        }

        ZipUtils.unzip(dist.toPath(), INSTALL_DIR.toPath());

        File[] files = INSTALL_DIR.listFiles();
        if (files == null || files.length != 1) {
            throw new RuntimeException("Expected " + INSTALL_DIR.getAbsolutePath() + " to contain a single directory");
        }
        keycloakHomeDir = files[0];

        if (!Path.of(keycloakHomeDir.getPath(), "bin", CMD).toFile().setExecutable(true)) {
            throw new RuntimeException("Failed to make startup script executable");
        }

        FileUtils.writeToFile(getZipLastModifiedFile(keycloakHomeDir), dist.lastModified());
        return true;
    }

    /** 对基址发起短超时 HTTP(S) 连接以检测服务是否存活。 */
    private boolean ping() {
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(getBaseUrl()).openConnection();
            urlConnection.setConnectTimeout(1000);
            urlConnection.setReadTimeout(1000);
            if(urlConnection instanceof HttpsURLConnection httpsURLConnection) {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[] { new NullTrustManager() }, new SecureRandom());
                SSLSocketFactory socketFactory = sslContext.getSocketFactory();
                httpsURLConnection.setSSLSocketFactory(socketFactory);
            }
            urlConnection.connect();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** 等待日志中出现启动完成标志且 ping 成功，否则销毁进程并抛错。 */
    private void waitForStart(OutputHandler outputHandler) {
        boolean started = outputHandler.waitForStarted();
        if (started && ping()) {
            return;
        }
        keycloakProcess.destroy();
        throw new RuntimeException("Keycloak did not start within timeout: " + getErrorOutput());
    }

    /** @param dir 安装目录 @return 记录源 ZIP 修改时间的标记文件 */
    private File getZipLastModifiedFile(File dir) {
        return new File(dir, "zip-last-modified");
    }

    /** @return 托管进程 PID 持久化文件 */
    private File getPidFile() {
        return new File(keycloakHomeDir, "pid");
    }

    /** @return 记录上次启动参数的文件，用于复用判断 */
    private File getServerArgsFile() {
        return new File(keycloakHomeDir, "startup-args");
    }

    /** 通过临时 Admin 客户端拉取 {@link ServerInfoRepresentation}（Windows 校验用）。 */
    private ServerInfoRepresentation getServerInfo() {
        KeycloakBuilder kcb = KeycloakBuilder.builder()
                .serverUrl(getBaseUrl())
                .realm("master")
                .clientId("temp-admin")
                .clientSecret("mysecret")
                .grantType("client_credentials");

        Keycloak kc = kcb.build();
        ServerInfoRepresentation info = kc.serverInfo().getInfo();
        kc.close();
        return info;
    }

    /** @return 子进程 stderr 内容，用于启动失败诊断 */
    private String getErrorOutput() {
        try {
            return new String(keycloakProcess.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    /** 从工作目录向上查找本地构建 ZIP，否则通过 Maven 解析 dist 构件。 */
    private static File resolveKeycloakDist() {
        Path p = Path.of(System.getProperty("user.dir"));
        String dist = "quarkus/dist/target/" + "keycloak-" + Version.VERSION + ".zip";
        while (p.resolve("pom.xml").toFile().isFile()) {
            File zip = p.resolve(dist).toFile();
            if (zip.isFile()) {
                return zip;
            }
            p = p.getParent();
        }

        return Maven.resolveArtifact("org.keycloak", "keycloak-quarkus-dist").toFile();
    }

    /** 读取 Keycloak 标准输出、解析日志级别并通知启动 latch。 */
    private class OutputHandler implements Runnable {

        /** Quarkus/JBoss 日志行解析正则。 */
        private static final Pattern LOG_PATTERN = Pattern.compile("([^ ]*) ([^ ]*) ([A-Z]*)([ ]*)(.*)");
        /** 转发 Keycloak 进程日志的目标记录器。 */
        private static final Logger LOGGER = Logger.getLogger("managed.keycloak");

        /** 是否已检测到 "started in" 日志行。 */
        private boolean startedInPrinted = false;
        /** 被监控的子进程。 */
        private final Process process;

        /** 启动完成信号 latch。 */
        private CountDownLatch startupLatch = new CountDownLatch(1);

        /** @param process Keycloak 子进程 */
        private OutputHandler(Process process) {
            this.process = process;
        }

        /** 持续读取 stdout 直至进程结束或流关闭。 */
        @Override
        public void run() {
            InputStream is = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            try {
                for (String line = br.readLine(); process.isAlive() && line != null; line = br.readLine()) {
                    if (!startedInPrinted && line.matches(".*Keycloak.* started in.*")) {
                        startupLatch.countDown();
                    }

                    Matcher matcher = LOG_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        String levelString = matcher.group(3);
                        String message = matcher.group(5);
                        if (levelString != null && message != null) {
                            for (Logger.Level l : Logger.Level.values()) {
                                if (l.name().equals(levelString)) {
                                    LOGGER.log(l, message);
                                    break;
                                }
                            }
                        }
                    }
                    LOGGER.info(line);
                }
            } catch (IOException e) {
                // 读取异常时忽略
            } finally {
                if (startupLatch.getCount() != 0) {
                    startupLatch.countDown();
                }
            }
        }

        /** 阻塞等待启动 latch，超时后返回进程是否仍存活。 */
        public boolean waitForStarted() {
            try {
                startupLatch.await(startTimeout, TimeUnit.SECONDS);
                return process.isAlive();
            } catch (InterruptedException e) {
                return false;
            }
        }

    }

    /** ping 检测用：信任所有证书的 {@link X509TrustManager}（仅测试环境）。 */
    private static class NullTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

}
