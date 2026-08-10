/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.kerberos.KerberosTicket;

import org.ietf.jgss.GSSCredential;

/**
 * 提供 Kerberos {@link org.ietf.jgss.GSSCredential} 的序列化/反序列化，以便从认证服务器传输到应用，
 * 并用于后续调用 Kerberos 保护的服务。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class KerberosSerializationUtils {

    /** 当前 JVM 与操作系统信息，用于异常诊断。 */
    public static final String JAVA_INFO;

    static {
        // 收集运行时环境信息，便于排查跨 JDK/平台序列化问题
        String javaVersion = System.getProperty("java.version");
        String javaRuntimeVersion = System.getProperty("java.runtime.version");
        String javaVendor = System.getProperty("java.vendor");
        String os = System.getProperty("os.version");
        JAVA_INFO = "Java version: " + javaVersion + ", runtime version: " + javaRuntimeVersion + ", vendor: " + javaVendor + ", os: " + os;
    }

    private KerberosSerializationUtils() {
    }

    /**
     * 将 {@link GSSCredential} 序列化为 Base64 字符串。
     *
     * @param kerberosTicket 可选的 Kerberos 票据，用于 JDK 特定转换
     * @param gssCredential 待序列化的 GSS 凭证
     * @return Base64 编码的序列化结果
     * @throws KerberosSerializationException 输入为空或序列化失败时
     */
    public static String serializeCredential(KerberosTicket kerberosTicket, GSSCredential gssCredential) throws KerberosSerializationException {
        try {
            if (gssCredential == null) {
                throw new KerberosSerializationException("Null credential given as input");
            }

            kerberosTicket = KerberosJdkProvider.getProvider().gssCredentialToKerberosTicket(kerberosTicket, gssCredential);

            return serialize(kerberosTicket);
        } catch (IOException e) {
            throw new KerberosSerializationException("Unexpected exception when serialize GSSCredential", e);
        }
    }


    /**
     * 从 Base64 字符串反序列化 {@link GSSCredential}。
     *
     * @param serializedCred 序列化凭证字符串
     * @return 反序列化后的 GSS 凭证
     * @throws KerberosSerializationException 输入为空、类型不匹配或反序列化失败时
     */
    public static GSSCredential deserializeCredential(String serializedCred) throws KerberosSerializationException {
        if (serializedCred == null) {
            throw new KerberosSerializationException("Null credential given as input. Did you enable kerberos credential delegation for your web browser and mapping of gss credential to access token?");
        }

        try {
            Object deserializedCred = deserialize(serializedCred);
            if (!(deserializedCred instanceof KerberosTicket)) {
                throw new KerberosSerializationException("Deserialized object is not KerberosTicket! Type is: " + deserializedCred);
            }

            KerberosTicket ticket = (KerberosTicket) deserializedCred;

            return KerberosJdkProvider.getProvider().kerberosTicketToGSSCredential(ticket);
        } catch (KerberosSerializationException ke) {
            throw ke;
        } catch (Exception ioe) {
            throw new KerberosSerializationException("Unexpected exception when deserialize GSSCredential", ioe);
        }
    }


    private static String serialize(Serializable obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(obj);
            byte[] objBytes = bos.toByteArray();
            return java.util.Base64.getEncoder().encodeToString(objBytes);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static Object deserialize(String serialized) throws ClassNotFoundException, IOException {
        byte[] bytes = java.util.Base64.getMimeDecoder().decode(serialized);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(bis);
            DelegatingSerializationFilter.builder()
                    .addAllowedClass(KerberosTicket.class)
                    .addAllowedClass(KerberosPrincipal.class)
                    .addAllowedClass(InetAddress.class)
                    .addAllowedPattern("javax.security.auth.kerberos.KeyImpl")
                    .setFilter(in);
            return in.readObject();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /** Kerberos 凭证序列化/反序列化失败时抛出的运行时异常。 */
    public static class KerberosSerializationException extends RuntimeException {

        public KerberosSerializationException(String message, Throwable cause) {
            super(message + ", " + JAVA_INFO, cause);
        }

        public KerberosSerializationException(String message) {
            super(message + ", " + JAVA_INFO);
        }
    }
}
