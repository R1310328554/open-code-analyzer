// 多模态媒体数据构造与音频/图像类型探测。
package llm

import (
	"bytes"
	"net/http"
	"strings"
)

// NewMediaData 根据原始字节构造带 ID 与自动探测 Kind 的 MediaData。
func NewMediaData(id int, data []byte) MediaData {
	return MediaData{
		Data: data,
		ID:   id,
		Kind: DetectMediaKind(data),
	}
}

// DetectMediaKind 按魔数与 HTTP Content-Type 判断音频或图像。
func DetectMediaKind(data []byte) MediaKind {
	if _, ok := AudioFormat(data); ok {
		return MediaKindAudio
	}
	if strings.HasPrefix(http.DetectContentType(data), "image/") {
		return MediaKindImage
	}
	return MediaKindUnknown
}

// AudioFormat 识别 WAV/MP3 音频格式，返回格式名与是否匹配。
func AudioFormat(data []byte) (string, bool) {
	if len(data) >= 12 && bytes.Equal(data[:4], []byte("RIFF")) && bytes.Equal(data[8:12], []byte("WAVE")) {
		return "wav", true
	}
	if len(data) >= 3 && bytes.Equal(data[:3], []byte("ID3")) {
		return "mp3", true
	}
	if len(data) >= 2 && data[0] == 0xff && data[1]&0xe0 == 0xe0 {
		return "mp3", true
	}
	return "", false
}
