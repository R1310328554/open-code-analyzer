// image_utils.go — 图像编解码辅助：PNG/JPEG 与 base64 互转，供裁剪与表格截图使用。

package util

import (
	"bytes"
	"encoding/base64"
	"image"
	"image/jpeg"
	"image/png"
)

// EncodePNG 将 image 编码为 PNG 字节。
func EncodePNG(img image.Image) ([]byte, error) {
	var buf bytes.Buffer
	if err := png.Encode(&buf, img); err != nil {
		return nil, err
	}
	return buf.Bytes(), nil
}

// EncodeImageToBase64PNG 编码 PNG 并 base64 字符串化。
func EncodeImageToBase64PNG(img image.Image) (string, error) {
	data, err := EncodePNG(img)
	if err != nil {
		return "", err
	}
	return base64.StdEncoding.EncodeToString(data), nil
}

// DecodeBase64PNG 解码 base64 PNG 为 image.Image。
func DecodeBase64PNG(b64 string) (image.Image, error) {
	data, err := base64.StdEncoding.DecodeString(b64)
	if err != nil {
		return nil, err
	}
	return png.Decode(bytes.NewReader(data))
}

// EncodeJPEG 以 quality=90 编码 JPEG。
func EncodeJPEG(img image.Image) ([]byte, error) {
	var buf bytes.Buffer
	if err := jpeg.Encode(&buf, img, &jpeg.Options{Quality: 90}); err != nil {
		return nil, err
	}
	return buf.Bytes(), nil
}
