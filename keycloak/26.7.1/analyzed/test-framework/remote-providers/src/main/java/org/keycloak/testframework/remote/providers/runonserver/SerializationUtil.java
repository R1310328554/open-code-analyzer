package org.keycloak.testframework.remote.providers.runonserver;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.Base64;

/**
 * {@link RunOnServer} / {@link FetchOnServer} 与异常在测试进程与服务器之间的 Java 序列化工具。
 * <p>
 * 使用 Base64 编码字节流，便于通过 HTTP 纯文本传输。
 */
public class SerializationUtil {

    /**
     * 将可序列化对象编码为 Base64 字符串。
     *
     * @param function 待序列化的对象（通常为 {@link RunOnServer} 或 {@link FetchOnServer}）
     * @return Base64 编码的 Java 序列化字节
     */
    public static String encode(Object function) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(function);
            oos.close();

            return Base64.getEncoder().encodeToString(os.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 使用指定类加载器反序列化 Base64 编码对象。
     *
     * @param encoded Base64 编码的序列化数据
     * @param classLoader 解析类名时使用的类加载器
     * @return 反序列化后的对象
     */
    public static Object decode(String encoded, ClassLoader classLoader) {
        try {
            byte[] bytes = Base64.getMimeDecoder().decode(encoded);
            ByteArrayInputStream is = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(is) {
                @Override
                protected Class<?> resolveClass(ObjectStreamClass c) throws ClassNotFoundException {
                    return Class.forName(c.getName(), false, classLoader);
                }
            };

            return ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将异常序列化为带 {@code EXCEPTION:} 前缀的 Base64 字符串。
     *
     * @param t 服务器端捕获的异常
     * @return 可传回客户端的编码异常表示
     */
    public static String encodeException(Throwable t) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(t);
            oos.close();

            return "EXCEPTION:" + Base64.getEncoder().encodeToString(os.toByteArray());
        } catch (NotSerializableException e) {
            // 异常无法序列化时仍保留原始异常信息，便于排查
            throw new RuntimeException("Unable to serialize exception due to not serializable class " + e.getMessage(), t);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解码 {@link #encodeException(Throwable)} 生成的字符串。
     *
     * @param result 以 {@code EXCEPTION:} 开头的编码结果
     * @return 还原的 {@link Throwable}
     */
    public static Throwable decodeException(String result) {
        try {
            result = result.substring("EXCEPTION:".length());
            byte[] bytes = Base64.getMimeDecoder().decode(result);
            ByteArrayInputStream is = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(is);
            return (Throwable) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
