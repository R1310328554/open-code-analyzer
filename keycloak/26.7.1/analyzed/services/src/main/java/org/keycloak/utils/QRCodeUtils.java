package org.keycloak.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * QR 码生成工具。
 * <p>基于 ZXing 将字符串编码为 PNG 格式二维码，支持字节数组或 Base64 字符串输出。</p>
 */
public class QRCodeUtils {

    /**
     * 将字符串编码为 PNG 格式 QR 码字节数组。
     *
     * @param contentToEncode 待编码内容
     * @param width 二维码宽度（像素）
     * @param height 二维码高度（像素）
     * @return PNG 格式的 QR 码字节数组
     */
    public static byte[] encodeAsQRBytes(String contentToEncode, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(contentToEncode, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "png", bos);
        bos.close();
        return bos.toByteArray();
    }

    /**
     * 将字符串编码为 Base64 格式的 PNG QR 码。
     *
     * @param contentToEncode 待编码内容
     * @param width 二维码宽度（像素）
     * @param height 二维码高度（像素）
     * @return Base64 编码的 PNG QR 码字符串
     */
    public static String encodeAsQRString(String contentToEncode, int width, int height) throws WriterException, IOException {
        byte[] bos = encodeAsQRBytes(contentToEncode, width, height);
        return Base64.getEncoder().encodeToString(bos);
    }

}
