package org.keycloak.testsuite.utils.tls;

import java.io.FileInputStream;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * TLS/SSL 上下文初始化工具，用于集成测试中的 HTTPS 客户端与服务器。
 */
public class TLSUtils {

   /** 工具类不可实例化。 */
   private TLSUtils() {}

   /** 接受所有服务器证书的额外信任管理器（测试专用）。 */
   private static final TrustManager TRUST_ALL_MANAGER = new X509TrustManager() {
      public X509Certificate[] getAcceptedIssuers() {
         return null;
      }

      public void checkClientTrusted(X509Certificate[] certs, String authType) {

      }

      public void checkServerTrusted(X509Certificate[] certs, String authType) {
      }
   };

   /**
    * 加载密钥库与信任库并初始化 TLS {@link SSLContext}。
    *
    * @return 已初始化的 SSL 上下文
    */
   public static SSLContext initializeTLS() {
      try {
         String keystorePath = System.getProperty("dependency.keystore");;
         if (keystorePath == null) {
            keystorePath = Paths.get(TLSUtils.class.getResource("/keycloak.jks").toURI()).toAbsolutePath().toString(); // 未通过 Maven 运行时从 IDE 直接执行
         }

         KeyStore keystore = KeyStore.getInstance("jks");
         try (FileInputStream is = new FileInputStream(keystorePath)) {
            keystore.load(is, "secret".toCharArray());
         }
         KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
         keyManagerFactory.init(keystore, "secret".toCharArray());
         KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

         String truststorePath = System.getProperty("dependency.truststore");;
         if (truststorePath == null) {
            truststorePath = Paths.get(TLSUtils.class.getResource("/keycloak.truststore").toURI()).toAbsolutePath().toString(); // 未通过 Maven 运行时从 IDE 直接执行
         }

         // 等效于 REQUEST CLIENT AUTH：客户端无证书时不失败，但会发起证书挑战
         KeyStore truststore = KeyStore.getInstance("jks");
         try (FileInputStream is = new FileInputStream(truststorePath)) {
            truststore.load(is, "secret".toCharArray());
         }
         TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
         trustManagerFactory.init(truststore);
         TrustManager[] trustManagers = new TrustManager[trustManagerFactory.getTrustManagers().length + 1];
         for (int i = 0; i < trustManagerFactory.getTrustManagers().length; ++i) {
            trustManagers[i] = trustManagerFactory.getTrustManagers()[i];
         }
         trustManagers[trustManagers.length - 1] = TRUST_ALL_MANAGER;

         SSLContext sslContext;
         sslContext = SSLContext.getInstance("TLS");
         sslContext.init(keyManagers, trustManagers, null);
         return sslContext;
      } catch (Exception e) {
         throw new IllegalStateException("Could not initialize TLS", e);
      }
   }
}
