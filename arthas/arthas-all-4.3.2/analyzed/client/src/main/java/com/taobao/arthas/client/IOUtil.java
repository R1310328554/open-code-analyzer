package com.taobao.arthas.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/***
 * Telnet 交互 IO 桥接工具：启动读写双线程，在本地终端与远程 Telnet 流之间双向转发字节。
 * <p>
 * 读线程：本地输入（stdin）→ 远程输出；写线程：远程输入 → 本地 Writer。远程关闭时写线程结束并唤醒主线程。
 ***/

public final class IOUtil {

    /**
     * 阻塞直到远程输入关闭：本地 keystroke 发往 remoteOutput，remoteInput 字符写入 localOutput。
     */
    public static final void readWrite(final InputStream remoteInput, final OutputStream remoteOutput,
                    final InputStream localInput, final Writer localOutput) {
        Thread reader, writer;

        // 本地 → 远程：逐字节转发 stdin 到 Telnet 输出流
        reader = new Thread() {
            @Override
            public void run() {
                int ch;

                try {
                    while (!interrupted() && (ch = localInput.read()) != -1) {
                        remoteOutput.write(ch);
                        remoteOutput.flush();
                    }
                } catch (IOException e) {
                    // e.printStackTrace();
                }
            }
        };

        // 远程 → 本地：UTF-8 解码 Telnet 响应并写入控制台
        writer = new Thread() {
            @Override
            public void run() {
                try {
                    InputStreamReader reader = new InputStreamReader(remoteInput, StandardCharsets.UTF_8);
                    while (true) {
                        int singleChar = reader.read();
                        if (singleChar == -1) {
                            break;
                        }
                        localOutput.write(singleChar);
                        localOutput.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        writer.setPriority(Thread.currentThread().getPriority() + 1);

        writer.start();
        reader.setDaemon(true);
        reader.start();

        try {
            writer.join();
            reader.interrupt();
        } catch (InterruptedException e) {
            // Ignored
        }
    }

}
