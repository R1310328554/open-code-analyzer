package com.taobao.arthas.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 精简版文件工具：临时目录、字节数组读写及安全打开输出流。
 *
 * @see org.apache.commons.io.FileUtils
 * @author hengyunabc 2020-05-03
 */
public class FileUtils {

	/** 返回 JVM 系统属性 {@code java.io.tmpdir} 对应目录 */
	public static File getTempDirectory() {
		return new File(System.getProperty("java.io.tmpdir"));
	}

	/**
	 * 将字节数组写入文件；不存在则创建，必要时创建父目录。
	 *
	 * @param file 目标文件
	 * @param data 内容
	 * @throws IOException I/O 错误
	 * @since 1.1
	 */
	public static void writeByteArrayToFile(final File file, final byte[] data) throws IOException {
		writeByteArrayToFile(file, data, false);
	}

	/**
	 * Writes a byte array to a file creating the file if it does not exist.
	 *
	 * @param file   the file to write to
	 * @param data   the content to write to the file
	 * @param append if {@code true}, then bytes will be added to the end of the
	 *               file rather than overwriting
	 * @throws IOException in case of an I/O error
	 * @since 2.1
	 */
	public static void writeByteArrayToFile(final File file, final byte[] data, final boolean append)
			throws IOException {
		writeByteArrayToFile(file, data, 0, data.length, append);
	}

	/**
	 * Writes {@code len} bytes from the specified byte array starting at offset
	 * {@code off} to a file, creating the file if it does not exist.
	 *
	 * @param file the file to write to
	 * @param data the content to write to the file
	 * @param off  the start offset in the data
	 * @param len  the number of bytes to write
	 * @throws IOException in case of an I/O error
	 * @since 2.5
	 */
	public static void writeByteArrayToFile(final File file, final byte[] data, final int off, final int len)
			throws IOException {
		writeByteArrayToFile(file, data, off, len, false);
	}

	/**
	 * Writes {@code len} bytes from the specified byte array starting at offset
	 * {@code off} to a file, creating the file if it does not exist.
	 *
	 * @param file   the file to write to
	 * @param data   the content to write to the file
	 * @param off    the start offset in the data
	 * @param len    the number of bytes to write
	 * @param append if {@code true}, then bytes will be added to the end of the
	 *               file rather than overwriting
	 * @throws IOException in case of an I/O error
	 * @since 2.5
	 */
	public static void writeByteArrayToFile(final File file, final byte[] data, final int off, final int len,
			final boolean append) throws IOException {
		FileOutputStream out = null;
		try {
			out = openOutputStream(file, append);
			out.write(data, off, len);
		} finally {
			IOUtils.close(out);
		}
	}

	/**
	 * Opens a {@link FileOutputStream} for the specified file, checking and
	 * creating the parent directory if it does not exist.
	 * <p>
	 * At the end of the method either the stream will be successfully opened, or an
	 * exception will have been thrown.
	 * <p>
	 * The parent directory will be created if it does not exist. The file will be
	 * created if it does not exist. An exception is thrown if the file object
	 * exists but is a directory. An exception is thrown if the file exists but
	 * cannot be written to. An exception is thrown if the parent directory cannot
	 * be created.
	 *
	 * @param file   the file to open for output, must not be {@code null}
	 * @param append if {@code true}, then bytes will be added to the end of the
	 *               file rather than overwriting
	 * @return a new {@link FileOutputStream} for the specified file
	 * @throws IOException if the file object is a directory
	 * @throws IOException if the file cannot be written to
	 * @throws IOException if a parent directory needs creating but that fails
	 * @since 2.1
	 */
	/**
	 * 打开文件输出流；校验可写性并在缺失时创建父目录。
	 *
	 * @param file 目标文件
	 * @param append 是否追加
	 * @return 新 FileOutputStream
	 * @throws IOException 目录或权限错误
	 */
	public static FileOutputStream openOutputStream(final File file, final boolean append) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File '" + file + "' exists but is a directory");
			}
			if (!file.canWrite()) {
				throw new IOException("File '" + file + "' cannot be written to");
			}
		} else {
			final File parent = file.getParentFile();
			if (parent != null) {
				if (!parent.mkdirs() && !parent.isDirectory()) {
					throw new IOException("Directory '" + parent + "' could not be created");
				}
			}
		}
		return new FileOutputStream(file, append);
	}
	
    /**
     * 读取整个文件为字节数组，并在 finally 中关闭流。
     *
     * @param file 待读文件，非 null
     * @return 文件内容
     * @throws IOException I/O 错误
     * @since 1.1
     */
    public static byte[] readFileToByteArray(final File file) throws IOException {
    	InputStream in = null;
    	try {
    		in = new FileInputStream(file);
    		return IOUtils.getBytes(in);
		} finally {
			IOUtils.close(in);
		}
    }
}
