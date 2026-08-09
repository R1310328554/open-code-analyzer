#!/usr/bin/env python3
"""Chinese-annotate Spring Boot 4.1.0 wave-13b batch [20:40] (ssl/pem/env-postprocessors)."""
from __future__ import annotations
import json, re, shutil, subprocess, sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "springboot/4.1.0"
ORIGINAL, ANALYZED, QUEUE = VER / "original", VER / "analyzed", VER / "_reports/class-queue"
BATCH_FILES = Path("/tmp/springboot_w13b.txt").read_text(encoding="utf-8").strip().splitlines()

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
"SslManagerBundle.java": [
("/**\n * A bundle of key and trust managers that can be used to establish an SSL connection.\n * Instances are usually created {@link #from(SslStoreBundle, SslBundleKey) from} an\n * {@link SslStoreBundle}.\n *\n * @author Scott Frederick\n * @author Moritz Halbritter\n * @since 3.1.0\n * @see SslStoreBundle\n * @see SslBundle#getManagers()\n */",
 "/**\n * 可用于建立 SSL 连接的密钥管理器与信任管理器 bundle。\n * 实例通常 {@link #from(SslStoreBundle, SslBundleKey) 由}\n * {@link SslStoreBundle} 创建。\n *\n * @author Scott Frederick\n * @author Moritz Halbritter\n * @since 3.1.0\n * @see SslStoreBundle\n * @see SslBundle#getManagers()\n */"),
("/**\n\t * Return the {@code KeyManager} instances used to establish identity.\n\t * @return the key managers\n\t */",
 "/**\n\t * 返回用于建立身份的 {@code KeyManager} 实例。\n\t * @return the key managers 密钥管理器数组\n\t */"),
("/**\n\t * Return the {@code KeyManagerFactory} used to establish identity.\n\t * @return the key manager factory\n\t */",
 "/**\n\t * 返回用于建立身份的 {@code KeyManagerFactory}。\n\t * @return the key manager factory 密钥管理器工厂\n\t */"),
("/**\n\t * Return the {@link TrustManager} instances used to establish trust.\n\t * @return the trust managers\n\t */",
 "/**\n\t * 返回用于建立信任的 {@link TrustManager} 实例。\n\t * @return the trust managers 信任管理器数组\n\t */"),
("/**\n\t * Return the {@link TrustManagerFactory} used to establish trust.\n\t * @return the trust manager factory\n\t */",
 "/**\n\t * 返回用于建立信任的 {@link TrustManagerFactory}。\n\t * @return the trust manager factory 信任管理器工厂\n\t */"),
("/**\n\t * Factory method to create a new {@link SSLContext} for the {@link #getKeyManagers()\n\t * key managers} and {@link #getTrustManagers() trust managers} managed by this\n\t * instance.\n\t * @param protocol the standard name of the SSL protocol. See\n\t * {@link SSLContext#getInstance(String)}\n\t * @return a new {@link SSLContext} instance\n\t */",
 "/**\n\t * 工厂方法：为本实例管理的 {@link #getKeyManagers() 密钥管理器}\n\t * 与 {@link #getTrustManagers() 信任管理器} 创建新的 {@link SSLContext}。\n\t * @param protocol the standard name of the SSL protocol SSL 协议标准名称，参见\n\t * {@link SSLContext#getInstance(String)}\n\t * @return a new {@link SSLContext} instance 新的 {@link SSLContext} 实例\n\t */"),
("/**\n\t * Factory method to create a new {@link SslManagerBundle} instance.\n\t * @param keyManagerFactory the key manager factory\n\t * @param trustManagerFactory the trust manager factory\n\t * @return a new {@link SslManagerBundle} instance\n\t */",
 "/**\n\t * 工厂方法：创建新的 {@link SslManagerBundle} 实例。\n\t * @param keyManagerFactory the key manager factory 密钥管理器工厂\n\t * @param trustManagerFactory the trust manager factory 信任管理器工厂\n\t * @return a new {@link SslManagerBundle} instance 新的 {@link SslManagerBundle} 实例\n\t */"),
("/**\n\t * Factory method to create a new {@link SslManagerBundle} backed by the given\n\t * {@link SslBundle} and {@link SslBundleKey}.\n\t * @param storeBundle the SSL store bundle\n\t * @param key the key reference\n\t * @return a new {@link SslManagerBundle} instance\n\t */",
 "/**\n\t * 工厂方法：基于给定 {@link SslStoreBundle} 与 {@link SslBundleKey}\n\t * 创建新的 {@link SslManagerBundle}。\n\t * @param storeBundle the SSL store bundle SSL 存储 bundle\n\t * @param key the key reference 密钥引用\n\t * @return a new {@link SslManagerBundle} instance 新的 {@link SslManagerBundle} 实例\n\t */"),
("/**\n\t * Factory method to create a new {@link SslManagerBundle} using the given\n\t * {@link TrustManagerFactory} and the default {@link KeyManagerFactory}.\n\t * @param trustManagerFactory the trust manager factory\n\t * @return a new {@link SslManagerBundle} instance\n\t * @since 3.5.0\n\t */",
 "/**\n\t * 工厂方法：使用给定 {@link TrustManagerFactory} 与默认 {@link KeyManagerFactory}\n\t * 创建新的 {@link SslManagerBundle}。\n\t * @param trustManagerFactory the trust manager factory 信任管理器工厂\n\t * @return a new {@link SslManagerBundle} instance 新的 {@link SslManagerBundle} 实例\n\t * @since 3.5.0\n\t */"),
("/**\n\t * Factory method to create a new {@link SslManagerBundle} using the given\n\t * {@link TrustManager TrustManagers} and the default {@link KeyManagerFactory}.\n\t * @param trustManagers the trust managers to use\n\t * @return a new {@link SslManagerBundle} instance\n\t * @since 3.5.0\n\t */",
 "/**\n\t * 工厂方法：使用给定 {@link TrustManager 信任管理器} 与默认 {@link KeyManagerFactory}\n\t * 创建新的 {@link SslManagerBundle}。\n\t * @param trustManagers the trust managers to use 要使用的信任管理器\n\t * @return a new {@link SslManagerBundle} instance 新的 {@link SslManagerBundle} 实例\n\t * @since 3.5.0\n\t */"),
],
"SslOptions.java": [
("/**\n * Configuration options that should be applied when establishing an SSL connection.\n *\n * @author Scott Frederick\n * @since 3.1.0\n * @see SslBundle#getOptions()\n */",
 "/**\n * 建立 SSL 连接时应应用的配置选项。\n *\n * @author Scott Frederick\n * @since 3.1.0\n * @see SslBundle#getOptions()\n */"),
("/**\n\t * {@link SslOptions} that returns {@code null} results.\n\t */",
 "/**\n\t * 各方法均返回 {@code null} 的 {@link SslOptions} 实例。\n\t */"),
("/**\n\t * Return if any SSL options have been specified.\n\t * @return {@code true} if SSL options have been specified\n\t */",
 "/**\n\t * 判断是否已指定任何 SSL 选项。\n\t * @return {@code true} if SSL options have been specified 已指定 SSL 选项时返回 {@code true}\n\t */"),
("/**\n\t * Return the ciphers that can be used or {@code null}. The cipher names in this set\n\t * should be compatible with those supported by\n\t * {@link SSLEngine#getSupportedCipherSuites()}.\n\t * @return the ciphers that can be used or {@code null}\n\t */",
 "/**\n\t * 返回可用的密码套件，或 {@code null}。\n\t * 名称应与 {@link SSLEngine#getSupportedCipherSuites()} 支持的套件兼容。\n\t * @return the ciphers that can be used or {@code null} 可用密码套件或 {@code null}\n\t */"),
("/**\n\t * Return the protocols that should be enabled or {@code null}. The protocols names in\n\t * this set should be compatible with those supported by\n\t * {@link SSLEngine#getSupportedProtocols()}.\n\t * @return the protocols to enable or {@code null}\n\t */",
 "/**\n\t * 返回应启用的协议，或 {@code null}。\n\t * 名称应与 {@link SSLEngine#getSupportedProtocols()} 支持的协议兼容。\n\t * @return the protocols to enable or {@code null} 要启用的协议或 {@code null}\n\t */"),
("/**\n\t * Factory method to create a new {@link SslOptions} instance.\n\t * @param ciphers the ciphers\n\t * @param enabledProtocols the enabled protocols\n\t * @return a new {@link SslOptions} instance\n\t */",
 "/**\n\t * 工厂方法：创建新的 {@link SslOptions} 实例。\n\t * @param ciphers the ciphers 密码套件\n\t * @param enabledProtocols the enabled protocols 启用的协议\n\t * @return a new {@link SslOptions} instance 新的 {@link SslOptions} 实例\n\t */"),
("/**\n\t * Helper method that provides a null-safe way to convert a {@code String[]} to a\n\t * {@link Collection} for client libraries to use.\n\t * @param array the array to convert\n\t * @return a collection or {@code null}\n\t */",
 "/**\n\t * 辅助方法：以 null 安全方式将 {@code String[]} 转为 {@link Collection}，供客户端库使用。\n\t * @param array the array to convert 要转换的数组\n\t * @return a collection or {@code null} 集合或 {@code null}\n\t */"),
],
"SslStoreBundle.java": [
("/**\n * A bundle of key and trust stores that can be used to establish an SSL connection.\n *\n * @author Scott Frederick\n * @since 3.1.0\n * @see SslBundle#getStores()\n */",
 "/**\n * 可用于建立 SSL 连接的密钥库与信任库 bundle。\n *\n * @author Scott Frederick\n * @since 3.1.0\n * @see SslBundle#getStores()\n */"),
("/**\n\t * {@link SslStoreBundle} that returns {@code null} for each method.\n\t */",
 "/**\n\t * 各方法均返回 {@code null} 的 {@link SslStoreBundle} 实例。\n\t */"),
("/**\n\t * Return a key store generated from the trust material or {@code null}.\n\t * @return the key store\n\t */",
 "/**\n\t * 返回由密钥材料生成的密钥库，或 {@code null}。\n\t * @return the key store 密钥库\n\t */"),
("/**\n\t * Return the password for the key in the key store or {@code null}.\n\t * @return the key password\n\t */",
 "/**\n\t * 返回密钥库中密钥的密码，或 {@code null}。\n\t * @return the key password 密钥密码\n\t */"),
("/**\n\t * Return a trust store generated from the trust material or {@code null}.\n\t * @return the trust store\n\t */",
 "/**\n\t * 返回由信任材料生成的信任库，或 {@code null}。\n\t * @return the trust store 信任库\n\t */"),
("/**\n\t * Factory method to create a new {@link SslStoreBundle} instance.\n\t * @param keyStore the key store or {@code null}\n\t * @param keyStorePassword the key store password or {@code null}\n\t * @param trustStore the trust store or {@code null}\n\t * @return a new {@link SslStoreBundle} instance\n\t */",
 "/**\n\t * 工厂方法：创建新的 {@link SslStoreBundle} 实例。\n\t * @param keyStore the key store or {@code null} 密钥库或 {@code null}\n\t * @param keyStorePassword the key store password or {@code null} 密钥库密码或 {@code null}\n\t * @param trustStore the trust store or {@code null} 信任库或 {@code null}\n\t * @return a new {@link SslStoreBundle} instance 新的 {@link SslStoreBundle} 实例\n\t */"),
],
"JksSslStoreBundle.java": [
("/**\n * {@link SslStoreBundle} backed by a Java keystore.\n *\n * @author Scott Frederick\n * @author Phillip Webb\n * @author Moritz Halbritter\n * @since 3.1.0\n */",
 "/**\n * 由 Java 密钥库（keystore）支持的 {@link SslStoreBundle}。\n * <p>\n * 支持 JKS、PKCS12 及 PKCS11 硬件密钥库；通过 {@link ResourceLoader} 加载存储文件。\n *\n * @author Scott Frederick\n * @author Phillip Webb\n * @author Moritz Halbritter\n * @since 3.1.0\n */"),
("/**\n\t * Create a new {@link JksSslStoreBundle} instance.\n\t * @param keyStoreDetails the key store details\n\t * @param trustStoreDetails the trust store details\n\t */",
 "/**\n\t * 创建新的 {@link JksSslStoreBundle} 实例。\n\t * @param keyStoreDetails the key store details 密钥库详情\n\t * @param trustStoreDetails the trust store details 信任库详情\n\t */"),
("/**\n\t * Create a new {@link JksSslStoreBundle} instance.\n\t * @param keyStoreDetails the key store details\n\t * @param trustStoreDetails the trust store details\n\t * @param resourceLoader the resource loader used to load content\n\t * @since 3.3.5\n\t */",
 "/**\n\t * 创建新的 {@link JksSslStoreBundle} 实例。\n\t * @param keyStoreDetails the key store details 密钥库详情\n\t * @param trustStoreDetails the trust store details 信任库详情\n\t * @param resourceLoader the resource loader used to load content 用于加载内容的资源加载器\n\t * @since 3.3.5\n\t */"),
],
"JksSslStoreDetails.java": [
("/**\n * Details for an individual trust or key store in a {@link JksSslStoreBundle}.\n *\n * @param type the key store type, for example {@code JKS} or {@code PKCS11}. A\n * {@code null} value will use {@link KeyStore#getDefaultType()}).\n * @param provider the name of the key store provider\n * @param location the location of the key store file or {@code null} if using a\n * {@code PKCS11} hardware store\n * @param password the password used to unlock the store or {@code null}\n * @author Scott Frederick\n * @author Phillip Webb\n * @since 3.1.0\n */",
 "/**\n * {@link JksSslStoreBundle} 中单个信任库或密钥库的详情。\n *\n * @param type the key store type, for example {@code JKS} or {@code PKCS11}. A\n * {@code null} value will use {@link KeyStore#getDefaultType()} 密钥库类型，例如 {@code JKS} 或 {@code PKCS11}；{@code null} 时使用 {@link KeyStore#getDefaultType()}\n * @param provider the name of the key store provider 密钥库 Provider 名称\n * @param location the location of the key store file or {@code null} if using a\n * {@code PKCS11} hardware store 密钥库文件位置；使用 {@code PKCS11} 硬件库时为 {@code null}\n * @param password the password used to unlock the store or {@code null} 解锁存储的密码或 {@code null}\n * @author Scott Frederick\n * @author Phillip Webb\n * @since 3.1.0\n */"),
("/**\n\t * Return a new {@link JksSslStoreDetails} instance with a new password.\n\t * @param password the new password\n\t * @return a new {@link JksSslStoreDetails} instance\n\t */",
 "/**\n\t * 返回使用新密码的 {@link JksSslStoreDetails} 实例。\n\t * @param password the new password 新密码\n\t * @return a new {@link JksSslStoreDetails} instance 新的 {@link JksSslStoreDetails} 实例\n\t */"),
("/**\n\t * Factory method to create a new {@link JksSslStoreDetails} instance for the given\n\t * location.\n\t * @param location the location\n\t * @return a new {@link JksSslStoreDetails} instance.\n\t */",
 "/**\n\t * 工厂方法：为给定位置创建新的 {@link JksSslStoreDetails} 实例。\n\t * @param location the location 资源位置\n\t * @return a new {@link JksSslStoreDetails} instance 新的 {@link JksSslStoreDetails} 实例\n\t */"),
],
"LoadedPemSslStore.java": [
("/**\n * {@link PemSslStore} loaded from {@link PemSslStoreDetails}.\n *\n * @author Phillip Webb\n * @see PemSslStore#load(PemSslStoreDetails)\n */",
 "/**\n * 从 {@link PemSslStoreDetails} 加载的 {@link PemSslStore}。\n * <p>\n * 延迟加载证书与私钥，首次访问时通过 {@link PemContent} 解析 PEM 内容。\n *\n * @author Phillip Webb\n * @see PemSslStore#load(PemSslStoreDetails)\n */"),
],
"PemCertificateParser.java": [
("/**\n * Parser for X.509 certificates in PEM format.\n *\n * @author Scott Frederick\n * @author Phillip Webb\n */",
 "/**\n * PEM 格式 X.509 证书解析器。\n * <p>\n * 使用正则匹配 {@code BEGIN/END CERTIFICATE} 块并 Base64 解码。\n *\n * @author Scott Frederick\n * @author Phillip Webb\n */"),
("/**\n\t * Parse certificates from the specified string.\n\t * @param text the text to parse\n\t * @return the parsed certificates\n\t */",
 "/**\n\t * 从指定字符串解析证书。\n\t * @param text the text to parse 待解析文本\n\t * @return the parsed certificates 解析得到的证书列表\n\t */"),
],
"PemContent.java": [
("/**\n * PEM encoded content that can provide {@link X509Certificate certificates} and\n * {@link PrivateKey private keys}.\n *\n * @author Scott Frederick\n * @author Phillip Webb\n * @since 3.2.0\n */",
 "/**\n * 可提供 {@link X509Certificate 证书} 与 {@link PrivateKey 私钥} 的 PEM 编码内容。\n *\n * @author Scott Frederick\n * @author Phillip Webb\n * @since 3.2.0\n */"),
("/**\n\t * Parse and return all {@link X509Certificate certificates} from the PEM content.\n\t * Most PEM files either contain a single certificate or a certificate chain.\n\t * @return the certificates\n\t * @throws IllegalStateException if no certificates could be loaded\n\t */",
 "/**\n\t * 解析并返回 PEM 内容中的全部 {@link X509Certificate 证书}。\n\t * 多数 PEM 文件包含单个证书或证书链。\n\t * @return the certificates 证书列表\n\t * @throws IllegalStateException if no certificates could be loaded 无法加载证书时抛出\n\t */"),
("/**\n\t * Parse and return the {@link PrivateKey private keys} from the PEM content.\n\t * @return the private keys\n\t * @throws IllegalStateException if no private key could be loaded\n\t */",
 "/**\n\t * 解析并返回 PEM 内容中的 {@link PrivateKey 私钥}。\n\t * @return the private keys 私钥\n\t * @throws IllegalStateException if no private key could be loaded 无法加载私钥时抛出\n\t */"),
("/**\n\t * Parse and return the {@link PrivateKey private keys} from the PEM content or\n\t * {@code null} if there is no private key.\n\t * @param password the password to decrypt the private keys or {@code null}\n\t * @return the private keys\n\t */",
 "/**\n\t * 解析并返回 PEM 内容中的 {@link PrivateKey 私钥}；无私钥时返回 {@code null}。\n\t * @param password the password to decrypt the private keys or {@code null} 解密私钥的密码或 {@code null}\n\t * @return the private keys 私钥\n\t */"),
("/**\n\t * Load {@link PemContent} from the given content (either the PEM content itself or a\n\t * reference to the resource to load).\n\t * @param content the content to load\n\t * @param resourceLoader the resource loader used to load content\n\t * @return a new {@link PemContent} instance or {@code null}\n\t * @throws IOException on IO error\n\t */",
 "/**\n\t * 从给定内容加载 {@link PemContent}（可为 PEM 文本本身或资源引用）。\n\t * @param content the content to load 要加载的内容\n\t * @param resourceLoader the resource loader used to load content 用于加载内容的资源加载器\n\t * @return a new {@link PemContent} instance or {@code null} 新的 {@link PemContent} 实例或 {@code null}\n\t * @throws IOException on IO error I/O 错误时抛出\n\t */"),
("/**\n\t * Load {@link PemContent} from the given {@link Path}.\n\t * @param path a path to load the content from\n\t * @return the loaded PEM content\n\t * @throws IOException on IO error\n\t */",
 "/**\n\t * 从给定 {@link Path} 加载 {@link PemContent}。\n\t * @param path a path to load the content from 内容路径\n\t * @return the loaded PEM content 加载的 PEM 内容\n\t * @throws IOException on IO error I/O 错误时抛出\n\t */"),
("/**\n\t * Load {@link PemContent} from the given {@link InputStream}.\n\t * @param in an input stream to load the content from\n\t * @return the loaded PEM content\n\t * @throws IOException on IO error\n\t */",
 "/**\n\t * 从给定 {@link InputStream} 加载 {@link PemContent}。\n\t * @param in an input stream to load the content from 输入流\n\t * @return the loaded PEM content 加载的 PEM 内容\n\t * @throws IOException on IO error I/O 错误时抛出\n\t */"),
("/**\n\t * Return a new {@link PemContent} instance containing the given text.\n\t * @param text the text containing PEM encoded content\n\t * @return a new {@link PemContent} instance\n\t */",
 "/**\n\t * 返回包含给定文本的新 {@link PemContent} 实例。\n\t * @param text the text containing PEM encoded content 含 PEM 编码内容的文本\n\t * @return a new {@link PemContent} instance 新的 {@link PemContent} 实例\n\t */"),
("/**\n\t * Return if PEM content is present in the given text.\n\t * @param text the text to check\n\t * @return if the text includes PEM encoded content.\n\t */",
 "/**\n\t * 判断给定文本是否包含 PEM 内容。\n\t * @param text the text to check 待检查文本\n\t * @return if the text includes PEM encoded content 包含 PEM 编码内容时返回 {@code true}\n\t */"),
],
"PemSslStore.java": [
("/**\n * An individual trust or key store that has been loaded from PEM content.\n *\n * @author Phillip Webb\n * @since 3.2.0\n * @see PemSslStoreDetails\n * @see PemContent\n */",
 "/**\n * 从 PEM 内容加载的单个信任库或密钥库。\n *\n * @author Phillip Webb\n * @since 3.2.0\n * @see PemSslStoreDetails\n * @see PemContent\n */"),
("/**\n\t * The key store type, for example {@code JKS} or {@code PKCS11}. A {@code null} value\n\t * will use {@link KeyStore#getDefaultType()}).\n\t * @return the key store type\n\t */",
 "/**\n\t * 密钥库类型，例如 {@code JKS} 或 {@code PKCS11}；{@code null} 时使用 {@link KeyStore#getDefaultType()}。\n\t * @return the key store type 密钥库类型\n\t */"),
("/**\n\t * The alias used when setting entries in the {@link KeyStore}.\n\t * @return the alias\n\t */",
 "/**\n\t * 在 {@link KeyStore} 中设置条目时使用的别名。\n\t * @return the alias 别名\n\t */"),
("/**\n\t * The password used when\n\t * {@link KeyStore#setKeyEntry(String, java.security.Key, char[], java.security.cert.Certificate[])\n\t * setting key entries} in the {@link KeyStore}.\n\t * @return the password\n\t */",
 "/**\n\t * 在 {@link KeyStore} 中\n\t * {@link KeyStore#setKeyEntry(String, java.security.Key, char[], java.security.cert.Certificate[])\n\t * 设置密钥条目} 时使用的密码。\n\t * @return the password 密码\n\t */"),
("/**\n\t * The certificates for this store. When a {@link #privateKey() private key} is\n\t * present the returned value is treated as a certificate chain, otherwise it is\n\t * treated a list of certificates that should all be registered.\n\t * @return the X509 certificates\n\t */",
 "/**\n\t * 此存储的证书。存在 {@link #privateKey() 私钥} 时视为证书链；\n\t * 否则视为应全部注册的证书列表。\n\t * @return the X509 certificates X509 证书列表\n\t */"),
("/**\n\t * The private key for this store or {@code null}.\n\t * @return the private key\n\t */",
 "/**\n\t * 此存储的私钥，或 {@code null}。\n\t * @return the private key 私钥\n\t */"),
("/**\n\t * Return a new {@link PemSslStore} instance with a new alias.\n\t * @param alias the new alias\n\t * @return a new {@link PemSslStore} instance\n\t */",
 "/**\n\t * 返回使用新别名的新 {@link PemSslStore} 实例。\n\t * @param alias the new alias 新别名\n\t * @return a new {@link PemSslStore} instance 新的 {@link PemSslStore} 实例\n\t */"),
("/**\n\t * Return a new {@link PemSslStore} instance with a new password.\n\t * @param password the new password\n\t * @return a new {@link PemSslStore} instance\n\t */",
 "/**\n\t * 返回使用新密码的新 {@link PemSslStore} 实例。\n\t * @param password the new password 新密码\n\t * @return a new {@link PemSslStore} instance 新的 {@link PemSslStore} 实例\n\t */"),
("/**\n\t * Return a {@link PemSslStore} instance loaded using the given\n\t * {@link PemSslStoreDetails}.\n\t * @param details the PEM store details\n\t * @return a loaded {@link PemSslStore} or {@code null}.\n\t */",
 "/**\n\t * 使用给定 {@link PemSslStoreDetails} 加载并返回 {@link PemSslStore} 实例。\n\t * @param details the PEM store details PEM 存储详情\n\t * @return a loaded {@link PemSslStore} or {@code null} 已加载的 {@link PemSslStore} 或 {@code null}\n\t */"),
("/**\n\t * Return a {@link PemSslStore} instance loaded using the given\n\t * {@link PemSslStoreDetails}.\n\t * @param details the PEM store details\n\t * @param resourceLoader the resource loader used to load content\n\t * @return a loaded {@link PemSslStore} or {@code null}.\n\t * @since 3.3.5\n\t */",
 "/**\n\t * 使用给定 {@link PemSslStoreDetails} 与资源加载器加载 {@link PemSslStore}。\n\t * @param details the PEM store details PEM 存储详情\n\t * @param resourceLoader the resource loader used to load content 用于加载内容的资源加载器\n\t * @return a loaded {@link PemSslStore} or {@code null} 已加载的 {@link PemSslStore} 或 {@code null}\n\t * @since 3.3.5\n\t */"),
("/**\n\t * Factory method that can be used to create a new {@link PemSslStore} with the given\n\t * values.\n\t * @param type the key store type\n\t * @param certificates the certificates for this store\n\t * @param privateKey the private key\n\t * @return a new {@link PemSslStore} instance\n\t */",
 "/**\n\t * 工厂方法：使用给定值创建新的 {@link PemSslStore}。\n\t * @param type the key store type 密钥库类型\n\t * @param certificates the certificates for this store 此存储的证书\n\t * @param privateKey the private key 私钥\n\t * @return a new {@link PemSslStore} instance 新的 {@link PemSslStore} 实例\n\t */"),
("/**\n\t * Factory method that can be used to create a new {@link PemSslStore} with the given\n\t * values.\n\t * @param certificates the certificates for this store\n\t * @param privateKey the private key\n\t * @return a new {@link PemSslStore} instance\n\t */",
 "/**\n\t * 工厂方法：使用给定证书与私钥创建新的 {@link PemSslStore}。\n\t * @param certificates the certificates for this store 此存储的证书\n\t * @param privateKey the private key 私钥\n\t * @return a new {@link PemSslStore} instance 新的 {@link PemSslStore} 实例\n\t */"),
("/**\n\t * Factory method that can be used to create a new {@link PemSslStore} with the given\n\t * values.\n\t * @param type the key store type\n\t * @param alias the alias used when setting entries in the {@link KeyStore}\n\t * @param password the password used\n\t * {@link KeyStore#setKeyEntry(String, java.security.Key, char[], java.security.cert.Certificate[])\n\t * setting key entries} in the {@link KeyStore}\n\t * @param certificates the certificates for this store\n\t * @param privateKey the private key\n\t * @return a new {@link PemSslStore} instance\n\t */",
 "/**\n\t * 工厂方法：使用给定值创建新的 {@link PemSslStore}。\n\t * @param type the key store type 密钥库类型\n\t * @param alias the alias used when setting entries in the {@link KeyStore} 在 {@link KeyStore} 中设置条目时使用的别名\n\t * @param password the password used\n\t * {@link KeyStore#setKeyEntry(String, java.security.Key, char[], java.security.cert.Certificate[])\n\t * setting key entries} in the {@link KeyStore} 设置密钥条目时使用的密码\n\t * @param certificates the certificates for this store 此存储的证书\n\t * @param privateKey the private key 私钥\n\t * @return a new {@link PemSslStore} instance 新的 {@link PemSslStore} 实例\n\t */"),
],
"PemSslStoreBundle.java": [
("/**\n * {@link SslStoreBundle} backed by PEM-encoded certificates and private keys.\n *\n * @author Scott Frederick\n * @author Phillip Webb\n * @author Moritz Halbritter\n * @since 3.1.0\n */",
 "/**\n * 由 PEM 编码证书与私钥支持的 {@link SslStoreBundle}。\n * <p>\n * 将 {@link PemSslStore} 转换为内存 {@link KeyStore}，支持私钥条目或纯证书信任库。\n *\n * @author Scott Frederick\n * @author Phillip Webb\n * @author Moritz Halbritter\n * @since 3.1.0\n */"),
("/**\n\t * Create a new {@link PemSslStoreBundle} instance.\n\t * @param keyStoreDetails the key store details\n\t * @param trustStoreDetails the trust store details\n\t */",
 "/**\n\t * 创建新的 {@link PemSslStoreBundle} 实例。\n\t * @param keyStoreDetails the key store details 密钥库详情\n\t * @param trustStoreDetails the trust store details 信任库详情\n\t */"),
("/**\n\t * Create a new {@link PemSslStoreBundle} instance.\n\t * @param pemKeyStore the PEM key store\n\t * @param pemTrustStore the PEM trust store\n\t * @since 3.2.0\n\t */",
 "/**\n\t * 创建新的 {@link PemSslStoreBundle} 实例。\n\t * @param pemKeyStore the PEM key store PEM 密钥库\n\t * @param pemTrustStore the PEM trust store PEM 信任库\n\t * @since 3.2.0\n\t */"),
],
"PemSslStoreDetails.java": [
("/**\n * Details for an individual trust or key store in a {@link PemSslStoreBundle}.\n *\n * @param type the key store type, for example {@code JKS} or {@code PKCS11}. A\n * {@code null} value will use {@link KeyStore#getDefaultType()}).\n * @param alias the alias used when setting entries in the {@link KeyStore}\n * @param password the password used\n * {@link KeyStore#setKeyEntry(String, java.security.Key, char[], java.security.cert.Certificate[])\n * setting key entries} in the {@link KeyStore}\n * @param certificates the certificates content (either the PEM content itself or a\n * reference to the resource to load). When a {@link #privateKey() private key} is present\n * this value is treated as a certificate chain, otherwise it is treated a list of\n * certificates that should all be registered.\n * @param privateKey the private key content (either the PEM content itself or a reference\n * to the resource to load)\n * @param privateKeyPassword a password used to decrypt an encrypted private key\n * @author Scott Frederick\n * @author Phillip Webb\n * @since 3.1.0\n * @see PemSslStore#load(PemSslStoreDetails)\n */",
 "/**\n * {@link PemSslStoreBundle} 中单个信任库或密钥库的详情。\n *\n * @param type the key store type, for example {@code JKS} or {@code PKCS11}. A\n * {@code null} value will use {@link KeyStore#getDefaultType()} 密钥库类型；{@code null} 时使用 {@link KeyStore#getDefaultType()}\n * @param alias the alias used when setting entries in the {@link KeyStore} 在 {@link KeyStore} 中设置条目时使用的别名\n * @param password the password used\n * {@link KeyStore#setKeyEntry(String, java.security.Key, char[], java.security.cert.Certificate[])\n * setting key entries} in the {@link KeyStore} 设置密钥条目时使用的密码\n * @param certificates the certificates content (either the PEM content itself or a\n * reference to the resource to load). When a {@link #privateKey() private key} is present\n * this value is treated as a certificate chain, otherwise it is treated a list of\n * certificates that should all be registered 证书内容（PEM 文本或资源引用）；有私钥时视为证书链，否则视为待注册证书列表\n * @param privateKey the private key content (either the PEM content itself or a reference\n * to the resource to load) 私钥内容（PEM 文本或资源引用）\n * @param privateKeyPassword a password used to decrypt an encrypted private key 解密加密私钥的密码\n * @author Scott Frederick\n * @author Phillip Webb\n * @since 3.1.0\n * @see PemSslStore#load(PemSslStoreDetails)\n */"),
("/**\n\t * Create a new {@link PemSslStoreDetails} instance.\n\t * @param type the key store type, for example {@code JKS} or {@code PKCS11}. A\n\t * {@code null} value will use {@link KeyStore#getDefaultType()}).\n\t * @param alias the alias used when setting entries in the {@link KeyStore}\n\t * @param password the password used\n\t * {@link KeyStore#setKeyEntry(String, java.security.Key, char[], java.security.cert.Certificate[])\n\t * setting key entries} in the {@link KeyStore}\n\t * @param certificates the certificate content (either the PEM content itself or a\n\t * reference to the resource to load)\n\t * @param privateKey the private key content (either the PEM content itself or a\n\t * reference to the resource to load)\n\t * @param privateKeyPassword a password used to decrypt an encrypted private key\n\t * @since 3.2.0\n\t */",
 "/**\n\t * 创建新的 {@link PemSslStoreDetails} 实例。\n\t * @param type the key store type, for example {@code JKS} or {@code PKCS11}. A\n\t * {@code null} value will use {@link KeyStore#getDefaultType()} 密钥库类型\n\t * @param alias the alias used when setting entries in the {@link KeyStore} 别名\n\t * @param password the password used\n\t * {@link KeyStore#setKeyEntry(String, java.security.Key, char[], java.security.cert.Certificate[])\n\t * setting key entries} in the {@link KeyStore} 密钥条目密码\n\t * @param certificates the certificate content (either the PEM content itself or a\n\t * reference to the resource to load) 证书内容\n\t * @param privateKey the private key content (either the PEM content itself or a\n\t * reference to the resource to load) 私钥内容\n\t * @param privateKeyPassword a password used to decrypt an encrypted private key 私钥解密密码\n\t * @since 3.2.0\n\t */"),
("/**\n\t * Create a new {@link PemSslStoreDetails} instance.\n\t * @param type the key store type, for example {@code JKS} or {@code PKCS11}. A\n\t * {@code null} value will use {@link KeyStore#getDefaultType()}).\n\t * @param certificate the certificate content (either the PEM content itself or a\n\t * reference to the resource to load)\n\t * @param privateKey the private key content (either the PEM content itself or a\n\t * reference to the resource to load)\n\t * @param privateKeyPassword a password used to decrypt an encrypted private key\n\t */",
 "/**\n\t * 创建新的 {@link PemSslStoreDetails} 实例。\n\t * @param type the key store type, for example {@code JKS} or {@code PKCS11}. A\n\t * {@code null} value will use {@link KeyStore#getDefaultType()} 密钥库类型\n\t * @param certificate the certificate content (either the PEM content itself or a\n\t * reference to the resource to load) 证书内容\n\t * @param privateKey the private key content (either the PEM content itself or a\n\t * reference to the resource to load) 私钥内容\n\t * @param privateKeyPassword a password used to decrypt an encrypted private key 私钥解密密码\n\t */"),
("/**\n\t * Create a new {@link PemSslStoreDetails} instance.\n\t * @param type the key store type, for example {@code JKS} or {@code PKCS11}. A\n\t * {@code null} value will use {@link KeyStore#getDefaultType()}).\n\t * @param certificate the certificate content (either the PEM content itself or a\n\t * reference to the resource to load)\n\t * @param privateKey the private key content (either the PEM content itself or a\n\t * reference to the resource to load)\n\t */",
 "/**\n\t * 创建新的 {@link PemSslStoreDetails} 实例。\n\t * @param type the key store type, for example {@code JKS} or {@code PKCS11}. A\n\t * {@code null} value will use {@link KeyStore#getDefaultType()} 密钥库类型\n\t * @param certificate the certificate content (either the PEM content itself or a\n\t * reference to the resource to load) 证书内容\n\t * @param privateKey the private key content (either the PEM content itself or a\n\t * reference to the resource to load) 私钥内容\n\t */"),
("/**\n\t * Return a new {@link PemSslStoreDetails} instance with a new alias.\n\t * @param alias the new alias\n\t * @return a new {@link PemSslStoreDetails} instance\n\t * @since 3.2.0\n\t */",
 "/**\n\t * 返回使用新别名的新 {@link PemSslStoreDetails} 实例。\n\t * @param alias the new alias 新别名\n\t * @return a new {@link PemSslStoreDetails} instance 新的 {@link PemSslStoreDetails} 实例\n\t * @since 3.2.0\n\t */"),
("/**\n\t * Return a new {@link PemSslStoreDetails} instance with a new password.\n\t * @param password the new password\n\t * @return a new {@link PemSslStoreDetails} instance\n\t * @since 3.2.0\n\t */",
 "/**\n\t * 返回使用新密码的新 {@link PemSslStoreDetails} 实例。\n\t * @param password the new password 新密码\n\t * @return a new {@link PemSslStoreDetails} instance 新的 {@link PemSslStoreDetails} 实例\n\t * @since 3.2.0\n\t */"),
("/**\n\t * Return a new {@link PemSslStoreDetails} instance with a new private key.\n\t * @param privateKey the new private key\n\t * @return a new {@link PemSslStoreDetails} instance\n\t */",
 "/**\n\t * 返回使用新私钥的新 {@link PemSslStoreDetails} 实例。\n\t * @param privateKey the new private key 新私钥\n\t * @return a new {@link PemSslStoreDetails} instance 新的 {@link PemSslStoreDetails} 实例\n\t */"),
("/**\n\t * Return a new {@link PemSslStoreDetails} instance with a new private key password.\n\t * @param privateKeyPassword the new private key password\n\t * @return a new {@link PemSslStoreDetails} instance\n\t */",
 "/**\n\t * 返回使用新私钥密码的新 {@link PemSslStoreDetails} 实例。\n\t * @param privateKeyPassword the new private key password 新私钥密码\n\t * @return a new {@link PemSslStoreDetails} instance 新的 {@link PemSslStoreDetails} 实例\n\t */"),
("/**\n\t * Factory method to create a new {@link PemSslStoreDetails} instance for the given\n\t * certificate. <b>Note:</b> This method doesn't actually check if the provided value\n\t * only contains a single certificate. It is functionally equivalent to\n\t * {@link #forCertificates(String)}.\n\t * @param certificate the certificate content (either the PEM content itself or a\n\t * reference to the resource to load)\n\t * @return a new {@link PemSslStoreDetails} instance.\n\t */",
 "/**\n\t * 工厂方法：为给定证书创建新的 {@link PemSslStoreDetails} 实例。\n\t * <b>注意：</b> 此方法并不验证值是否仅含单个证书，\n\t * 功能上等价于 {@link #forCertificates(String)}。\n\t * @param certificate the certificate content (either the PEM content itself or a\n\t * reference to the resource to load) 证书内容\n\t * @return a new {@link PemSslStoreDetails} instance 新的 {@link PemSslStoreDetails} 实例\n\t */"),
("/**\n\t * Factory method to create a new {@link PemSslStoreDetails} instance for the given\n\t * certificates.\n\t * @param certificates the certificates content (either the PEM content itself or a\n\t * reference to the resource to load)\n\t * @return a new {@link PemSslStoreDetails} instance.\n\t * @since 3.2.0\n\t */",
 "/**\n\t * 工厂方法：为给定证书创建新的 {@link PemSslStoreDetails} 实例。\n\t * @param certificates the certificates content (either the PEM content itself or a\n\t * reference to the resource to load) 证书内容\n\t * @return a new {@link PemSslStoreDetails} instance 新的 {@link PemSslStoreDetails} 实例\n\t * @since 3.2.0\n\t */"),
],
"AnsiOutputApplicationListener.java": [
("/**\n * An {@link ApplicationListener} that configures {@link AnsiOutput} depending on the\n * value of the property {@code spring.output.ansi.enabled}. See {@link Enabled} for valid\n * values.\n *\n * @author Raphael von der Grün\n * @author Madhura Bhave\n * @since 4.0.0\n */",
 "/**\n * 根据 {@code spring.output.ansi.enabled} 属性配置 {@link AnsiOutput} 的\n * {@link ApplicationListener}。有效值参见 {@link Enabled}。\n *\n * @author Raphael von der Grün\n * @author Madhura Bhave\n * @since 4.0.0\n */"),
("\t\t// Apply after EnvironmentPostProcessorApplicationListener",
 "\t\t// 在 EnvironmentPostProcessorApplicationListener 之后应用"),
],
"EnvironmentPostProcessorApplicationListener.java": [
("/**\n * {@link SmartApplicationListener} used to trigger {@link EnvironmentPostProcessor\n * EnvironmentPostProcessors} registered in the {@code spring.factories} file.\n *\n * @author Phillip Webb\n * @author Stephane Nicoll\n * @since 4.0.0\n */",
 "/**\n * 用于触发 {@code spring.factories} 中注册的 {@link EnvironmentPostProcessor\n * EnvironmentPostProcessors} 的 {@link SmartApplicationListener}。\n * <p>\n * 在 {@link ApplicationEnvironmentPreparedEvent} 时执行后处理器；\n * AOT 模式下还会加载生成的 {@code __EnvironmentPostProcessor} 类。\n *\n * @author Phillip Webb\n * @author Stephane Nicoll\n * @since 4.0.0\n */"),
("/**\n\t * The default order for the processor.\n\t */",
 "/**\n\t * 处理器的默认顺序。\n\t */"),
("/**\n\t * Create a new {@link EnvironmentPostProcessorApplicationListener} with\n\t * {@link EnvironmentPostProcessor} classes loaded through {@code spring.factories}.\n\t */",
 "/**\n\t * 创建新的 {@link EnvironmentPostProcessorApplicationListener}，\n\t * 通过 {@code spring.factories} 加载 {@link EnvironmentPostProcessor} 类。\n\t */"),
("/**\n\t * Create a new {@link EnvironmentPostProcessorApplicationListener} with post\n\t * processors created by the given factory.\n\t * @param postProcessorsFactory the post processors factory\n\t */",
 "/**\n\t * 创建新的 {@link EnvironmentPostProcessorApplicationListener}，\n\t * 使用给定工厂创建后处理器。\n\t * @param postProcessorsFactory the post processors factory 后处理器工厂\n\t */"),
("/**\n\t * Factory method that creates an {@link EnvironmentPostProcessorApplicationListener}\n\t * with a specific {@link EnvironmentPostProcessorsFactory}.\n\t * @param postProcessorsFactory the environment post processor factory\n\t * @return an {@link EnvironmentPostProcessorApplicationListener} instance\n\t */",
 "/**\n\t * 工厂方法：使用指定 {@link EnvironmentPostProcessorsFactory}\n\t * 创建 {@link EnvironmentPostProcessorApplicationListener}。\n\t * @param postProcessorsFactory the environment post processor factory 环境后处理器工厂\n\t * @return an {@link EnvironmentPostProcessorApplicationListener} instance 实例\n\t */"),
("/**\n\t * Contribute a {@code <Application>__EnvironmentPostProcessor} class that stores AOT\n\t * optimizations.\n\t */",
 "/**\n\t * 贡献存储 AOT 优化的 {@code <Application>__EnvironmentPostProcessor} 类。\n\t */"),
("type.addJavadoc(\"Configure the environment with AOT optimizations.\");",
 "type.addJavadoc(\"使用 AOT 优化配置环境。\");"),
],
"EnvironmentPostProcessorsFactory.java": [
("/**\n * Factory interface used by the {@link EnvironmentPostProcessorApplicationListener} to\n * create the {@link EnvironmentPostProcessor} instances.\n *\n * @author Phillip Webb\n * @since 4.0.0\n */",
 "/**\n * {@link EnvironmentPostProcessorApplicationListener} 用于创建\n * {@link EnvironmentPostProcessor} 实例的工厂接口。\n *\n * @author Phillip Webb\n * @since 4.0.0\n */"),
("/**\n\t * Create all requested {@link EnvironmentPostProcessor} instances.\n\t * @param logFactory a deferred log factory\n\t * @param bootstrapContext a bootstrap context\n\t * @return the post processor instances\n\t */",
 "/**\n\t * 创建所有请求的 {@link EnvironmentPostProcessor} 实例。\n\t * @param logFactory a deferred log factory 延迟日志工厂\n\t * @param bootstrapContext a bootstrap context 引导上下文\n\t * @return the post processor instances 后处理器实例列表\n\t */"),
("/**\n\t * Return a {@link EnvironmentPostProcessorsFactory} backed by\n\t * {@code spring.factories}.\n\t * @param classLoader the source class loader\n\t * @return an {@link EnvironmentPostProcessorsFactory} instance\n\t */",
 "/**\n\t * 返回由 {@code spring.factories} 支持的 {@link EnvironmentPostProcessorsFactory}。\n\t * @param classLoader the source class loader 源类加载器\n\t * @return an {@link EnvironmentPostProcessorsFactory} instance 实例\n\t */"),
("/**\n\t * Return a {@link EnvironmentPostProcessorsFactory} that reflectively creates post\n\t * processors from the given classes.\n\t * @param classes the post processor classes\n\t * @return an {@link EnvironmentPostProcessorsFactory} instance\n\t */",
 "/**\n\t * 返回通过反射从给定类创建后处理器的 {@link EnvironmentPostProcessorsFactory}。\n\t * @param classes the post processor classes 后处理器类\n\t * @return an {@link EnvironmentPostProcessorsFactory} instance 实例\n\t */"),
("/**\n\t * Return a {@link EnvironmentPostProcessorsFactory} that reflectively creates post\n\t * processors from the given class names.\n\t * @param classNames the post processor class names\n\t * @return an {@link EnvironmentPostProcessorsFactory} instance\n\t */",
 "/**\n\t * 返回通过反射从给定类名创建后处理器的 {@link EnvironmentPostProcessorsFactory}。\n\t * @param classNames the post processor class names 后处理器类名\n\t * @return an {@link EnvironmentPostProcessorsFactory} instance 实例\n\t */"),
("/**\n\t * Return a {@link EnvironmentPostProcessorsFactory} that reflectively creates post\n\t * processors from the given class names.\n\t * @param classLoader the source class loader\n\t * @param classNames the post processor class names\n\t * @return an {@link EnvironmentPostProcessorsFactory} instance\n\t */",
 "/**\n\t * 返回通过反射从给定类名创建后处理器的 {@link EnvironmentPostProcessorsFactory}。\n\t * @param classLoader the source class loader 源类加载器\n\t * @param classNames the post processor class names 后处理器类名\n\t * @return an {@link EnvironmentPostProcessorsFactory} instance 实例\n\t */"),
],
"RandomValuePropertySourceEnvironmentPostProcessor.java": [
("/**\n * {@link EnvironmentPostProcessor} to add the {@link RandomValuePropertySource}.\n *\n * @author Phillip Webb\n * @since 4.0.0\n */",
 "/**\n * 添加 {@link RandomValuePropertySource} 的 {@link EnvironmentPostProcessor}。\n * <p>\n * 支持 {@code ${random.*}} 占位符解析随机值。\n *\n * @author Phillip Webb\n * @since 4.0.0\n */"),
("/**\n\t * The default order of this post-processor.\n\t */",
 "/**\n\t * 此后处理器的默认顺序。\n\t */"),
("/**\n\t * Create a new {@link RandomValuePropertySourceEnvironmentPostProcessor} instance.\n\t * @param logFactory the log factory to use\n\t * @since 3.0.0\n\t */",
 "/**\n\t * 创建新的 {@link RandomValuePropertySourceEnvironmentPostProcessor} 实例。\n\t * @param logFactory the log factory to use 要使用的日志工厂\n\t * @since 3.0.0\n\t */"),
],
"ReflectionEnvironmentPostProcessorsFactory.java": [
("/**\n * {@link EnvironmentPostProcessorsFactory} implementation that uses reflection to create\n * instances.\n *\n * @author Phillip Webb\n */",
 "/**\n * 使用反射创建实例的 {@link EnvironmentPostProcessorsFactory} 实现。\n * <p>\n * 通过 {@link Instantiator} 注入 {@link DeferredLogFactory}、\n * {@link ConfigurableBootstrapContext} 等构造函数参数。\n *\n * @author Phillip Webb\n */"),
],
"SpringApplicationJsonEnvironmentPostProcessor.java": [
("/**\n * An {@link EnvironmentPostProcessor} that parses JSON from\n * {@code spring.application.json} or equivalently {@code SPRING_APPLICATION_JSON} and\n * adds it as a map property source to the {@link Environment}. The new properties are\n * added with higher priority than the system properties.\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @author Madhura Bhave\n * @author Artsiom Yudovin\n * @since 4.0.0\n */",
 "/**\n * 解析 {@code spring.application.json} 或等价的 {@code SPRING_APPLICATION_JSON} 中 JSON，\n * 并将其作为 map 属性源添加到 {@link Environment} 的 {@link EnvironmentPostProcessor}。\n * 新属性优先级高于系统属性。\n *\n * @author Dave Syer\n * @author Phillip Webb\n * @author Madhura Bhave\n * @author Artsiom Yudovin\n * @since 4.0.0\n */"),
("/**\n\t * Name of the {@code spring.application.json} property.\n\t */",
 "/**\n\t * {@code spring.application.json} 属性名。\n\t */"),
("/**\n\t * Name of the {@code SPRING_APPLICATION_JSON} environment variable.\n\t */",
 "/**\n\t * {@code SPRING_APPLICATION_JSON} 环境变量名。\n\t */"),
("/**\n\t * The default order for the processor.\n\t */",
 "/**\n\t * 处理器的默认顺序。\n\t */"),
("/**\n\t * Flatten the map keys using period separator.\n\t * @param map the map that should be flattened\n\t * @return the flattened map\n\t */",
 "/**\n\t * 使用点号分隔符扁平化 map 键。\n\t * @param map the map that should be flattened 待扁平化的 map\n\t * @return the flattened map 扁平化后的 map\n\t */"),
],
"SpringFactoriesEnvironmentPostProcessorsFactory.java": [
("/**\n * An {@link EnvironmentPostProcessorsFactory} that uses {@link SpringFactoriesLoader}.\n *\n * @author Andy Wilkinson\n */",
 "/**\n * 使用 {@link SpringFactoriesLoader} 的 {@link EnvironmentPostProcessorsFactory}。\n * <p>\n * 加载 {@link EnvironmentPostProcessor} 及已弃用的\n * {@code org.springframework.boot.env.EnvironmentPostProcessor}，并按 {@link Ordered} 排序。\n *\n * @author Andy Wilkinson\n */"),
],
"SystemEnvironmentPropertySourceEnvironmentPostProcessor.java": [
("/**\n * An {@link EnvironmentPostProcessor} that replaces the systemEnvironment\n * {@link SystemEnvironmentPropertySource} with an\n * {@link OriginAwareSystemEnvironmentPropertySource} that can track the\n * {@link SystemEnvironmentOrigin} for every system environment property.\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @since 4.0.0\n */",
 "/**\n * 将 systemEnvironment {@link SystemEnvironmentPropertySource} 替换为可跟踪\n * 每个系统环境属性 {@link SystemEnvironmentOrigin} 的\n * {@link OriginAwareSystemEnvironmentPropertySource} 的 {@link EnvironmentPostProcessor}。\n *\n * @author Madhura Bhave\n * @author Phillip Webb\n * @since 4.0.0\n */"),
("/**\n\t * The default order for the processor.\n\t */",
 "/**\n\t * 处理器的默认顺序。\n\t */"),
("/**\n\t * Post-process the given {@link ConfigurableEnvironment} by copying appropriate\n\t * settings from a parent {@link ConfigurableEnvironment}.\n\t * @param environment the environment to post-process\n\t * @param parentEnvironment the parent environment\n\t * @since 3.4.12\n\t */",
 "/**\n\t * 从父 {@link ConfigurableEnvironment} 复制适当设置以后处理给定环境。\n\t * @param environment the environment to post-process 待后处理的环境\n\t * @param parentEnvironment the parent environment 父环境\n\t * @since 3.4.12\n\t */"),
("/**\n\t * {@link SystemEnvironmentPropertySource} that also tracks {@link Origin}.\n\t */",
 "/**\n\t * 同时跟踪 {@link Origin} 的 {@link SystemEnvironmentPropertySource}。\n\t */"),
],
"ApplicationHome.java": [
("/**\n * Provides access to the application home directory. Attempts to pick a sensible home for\n * both Jar Files, Exploded Archives and directly running applications.\n *\n * @author Phillip Webb\n * @author Raja Kolli\n * @since 2.0.0\n */",
 "/**\n * 提供应用主目录（home directory）访问。\n * <p>\n * 尝试为 JAR 文件、解压归档及直接运行的应用选择合理的主目录。\n *\n * @author Phillip Webb\n * @author Raja Kolli\n * @since 2.0.0\n */"),
("/**\n\t * Create a new {@link ApplicationHome} instance.\n\t */",
 "/**\n\t * 创建新的 {@link ApplicationHome} 实例。\n\t */"),
("/**\n\t * Create a new {@link ApplicationHome} instance for the specified source class.\n\t * @param sourceClass the source class or {@code null}\n\t */",
 "/**\n\t * 为指定源类创建新的 {@link ApplicationHome} 实例。\n\t * @param sourceClass the source class or {@code null} 源类或 {@code null}\n\t */"),
("\t\t\t// Ignore",
 "\t\t\t// 忽略"),
("/**\n\t * Returns the underlying source used to find the home directory. This is usually the\n\t * jar file or a directory. Can return {@code null} if the source cannot be\n\t * determined.\n\t * @return the underlying source or {@code null}\n\t */",
 "/**\n\t * 返回用于定位主目录的底层源，通常为 JAR 文件或目录。\n\t * 无法确定源时返回 {@code null}。\n\t * @return the underlying source or {@code null} 底层源或 {@code null}\n\t */"),
("/**\n\t * Returns the application home directory.\n\t * @return the home directory (never {@code null})\n\t */",
 "/**\n\t * 返回应用主目录。\n\t * @return the home directory (never {@code null}) 主目录（永不为 {@code null}）\n\t */"),
],
}

def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found: {old[:120]}")
        text = text.replace(old, new, 1)
    return text

def main() -> int:
    failures, ok = [], 0
    for rel in BATCH_FILES:
        name = Path(rel).name
        src, dst = ORIGINAL / rel, ANALYZED / rel
        if not src.exists():
            failures.append(f"MISSING original: {rel}")
            continue
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)
        reps = FILE_REPLACEMENTS.get(name, [])
        if not reps:
            failures.append(f"NO_REPLACEMENTS: {rel}")
            continue
        try:
            text = apply_replacements(dst.read_text(encoding="utf-8"), reps)
            cn = len(re.findall(r"[\u4e00-\u9fff]", text))
            if cn < 10 or "Licensed under the Apache License" not in text:
                failures.append(f"VALIDATION cn={cn}: {rel}")
                continue
            dst.write_text(text, encoding="utf-8")
            ok += 1
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
    if ok == len(BATCH_FILES) and not failures:
        subprocess.run(
            [sys.executable, str(ROOT / "scripts/mark_batch_done.py"),
             "--project", "springboot", "--version", "4.1.0",
             "--note", "wave13b ssl-pem/env-postprocessors [20:40]", *BATCH_FILES],
            check=True,
        )
        batch = json.loads((QUEUE / "batch.json").read_text(encoding="utf-8"))
        batch["done"] = len([ln for ln in (QUEUE / "done.txt").read_text(encoding="utf-8").splitlines() if ln.strip()])
        batch["remaining_pending"] = len([ln for ln in (QUEUE / "pending.txt").read_text(encoding="utf-8").splitlines() if ln.strip()])
        (QUEUE / "batch.json").write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"ok": ok, "failures": failures}))
    return 1 if failures else 0

if __name__ == "__main__":
    raise SystemExit(main())
