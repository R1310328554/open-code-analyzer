package com.taobao.arthas.core.shell.term.impl;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.core.shell.ShellServerOptions;
import com.taobao.arthas.core.shell.term.TermServer;
import io.termd.core.readline.Keymap;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * 终端 readline 键位映射（keymap）加载工具。
 * <p>
 * 按优先级依次尝试：用户目录 {@code ~/.arthas/conf/inputrc}、Arthas 内置
 * {@link ShellServerOptions#DEFAULT_INPUTRC}、termd 默认 {@code inputrc} 资源。
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Helper {

    private static final Logger logger = LoggerFactory.getLogger(Helper.class);

    /** 加载 inputrc 并构造 termd {@link Keymap} 实例 */
    public static Keymap loadKeymap() {
        return new Keymap(loadInputRcFile());
    }

    /**
     * 按三级回退策略加载 inputrc 输入流。
     *
     * @return inputrc 文件流，三级均失败时抛出 {@link IllegalStateException}
     */
    public static InputStream loadInputRcFile() {
        InputStream inputrc;
        // 步骤 1：尝试加载用户自定义 keymap
        try {
            String customInputrc = System.getProperty("user.home") + "/.arthas/conf/inputrc";
            inputrc = new FileInputStream(customInputrc);
            logger.info("Loaded custom keymap file from " + customInputrc);
            return inputrc;
        } catch (Throwable e) {
            // ignore
        }

        // 步骤 2：加载 Arthas 内置默认 keymap
        inputrc = TermServer.class.getClassLoader().getResourceAsStream(ShellServerOptions.DEFAULT_INPUTRC);
        if (inputrc != null) {
            logger.info("Loaded arthas keymap file from " + ShellServerOptions.DEFAULT_INPUTRC);
            return inputrc;
        }

        // 步骤 3：回退到 termd 自带 inputrc
        inputrc = Keymap.class.getResourceAsStream("inputrc");
        if (inputrc != null) {
            return inputrc;
        }

        throw new IllegalStateException("Could not load inputrc file.");
    }


//    public static Buffer loadResource(FileSystem fs, String path) {
//        try {
//            return fs.readFileBlocking(path);
//        } catch (Exception e) {
//            return loadResource(path);
//        }
//    }

//    public static Buffer loadResource(String path) {
//        URL resource = HttpTermServer.class.getResource(path);
//        if (resource != null) {
//            try {
//                byte[] tmp = new byte[512];
//                InputStream in = resource.openStream();
//                Buffer buffer = Buffer.buffer();
//                while (true) {
//                    int l = in.read(tmp);
//                    if (l == -1) {
//                        break;
//                    }
//                    buffer.appendBytes(tmp, 0, l);
//                }
//                return buffer;
//            } catch (IOException ignore) {
//            }
//        }
//        return null;
//    }
}
