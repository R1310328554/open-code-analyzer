package filedata

import (
	"errors"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"os"
	"path/filepath"
	"regexp"
	"slices"
	"strings"

	"github.com/ollama/ollama/api"
)

// filedata 从用户输入中提取多模态文件路径、规范化 URL 并读取二进制内容。
// File 表示已从输入中解析并成功读取的一个附件。
type File struct {
	Path string
	Data api.ImageData
}

// NormalizePath 去除引号、还原转义字符，并将 file:// URL 转为本地路径。
func NormalizePath(fp string) string {
	fp = strings.Trim(fp, "\"")
	fp = strings.NewReplacer(
		"\\ ", " ",
		"\\(", "(",
		"\\)", ")",
		"\\[", "[",
		"\\]", "]",
		"\\{", "{",
		"\\}", "}",
		"\\$", "$",
		"\\&", "&",
		"\\;", ";",
		"\\'", "'",
		"\\\\", "\\",
		"\\*", "*",
		"\\?", "?",
		"\\~", "~",
	).Replace(fp)

	if u, err := url.Parse(fp); err == nil && strings.EqualFold(u.Scheme, "file") {
		return normalizeFileURL(u)
	} else if normalized, ok := normalizeMalformedFileURL(fp); ok {
		return normalized
	}

	return fp
}

// fileExtractRe 匹配 file:// 或本地路径中的图片/音频扩展名；包级编译一次供每次按键补全复用。
// fileExtractRe matches file:// URLs and filesystem paths ending in image/audio
// extensions. Hoisted to package scope so the per-keystroke slash-completion
// path (chat.slashInputIsMultimodalFile -> ExtractNames) doesn't recompile it
// on every call.
var fileExtractRe = regexp.MustCompile(`(?:file://\S+?\.(?i:jpg|jpeg|png|webp|wav)\b)|(?:(?:[a-zA-Z]:)?(?:\./|\.\\|/|\\)[\S\\ ]+?\.(?i:jpg|jpeg|png|webp|wav)\b)`)

// ExtractNames 返回输入中出现的候选文件路径/URL 字符串。
func ExtractNames(input string) []string {
	return fileExtractRe.FindAllString(input, -1)
}

// Extract 解析附件并仅返回 api.ImageData 切片（不含路径元数据）。
func Extract(input string) (string, []api.ImageData, error) {
	cleaned, files, err := ExtractWithFiles(input)
	if err != nil {
		return "", nil, err
	}
	data := make([]api.ImageData, 0, len(files))
	for _, file := range files {
		data = append(data, file.Data)
	}
	return cleaned, data, nil
}

// ExtractWithFiles 解析附件并保留规范化后的路径与数据。
func ExtractWithFiles(input string) (string, []File, error) {
	filePaths := ExtractNames(input)
	var files []File

	for _, fp := range filePaths {
		nfp := NormalizePath(fp)
		data, err := GetData(nfp)
		if errors.Is(err, os.ErrNotExist) {
			continue
		} else if err != nil {
			return "", nil, fmt.Errorf("couldn't process file %q: %w", nfp, err)
		}
		input = strings.ReplaceAll(input, "'"+nfp+"'", "")
		input = strings.ReplaceAll(input, "'"+fp+"'", "")
		input = strings.ReplaceAll(input, `"`+nfp+`"`, "")
		input = strings.ReplaceAll(input, `"`+fp+`"`, "")
		input = strings.ReplaceAll(input, fp, "")
		files = append(files, File{Path: nfp, Data: data})
	}
	return strings.TrimSpace(input), files, nil
}

// GetData 打开本地文件，校验允许的内容类型与大小后读入内存。
func GetData(filePath string) ([]byte, error) {
	file, err := os.Open(filePath)
	if err != nil {
		return nil, err
	}
	defer file.Close()

	buf := make([]byte, 512)
	_, err = file.Read(buf)
	if err != nil {
		return nil, err
	}

	contentType := http.DetectContentType(buf)
	allowedTypes := []string{"image/jpeg", "image/jpg", "image/png", "image/webp", "audio/wave"}
	if !slices.Contains(allowedTypes, contentType) {
		return nil, fmt.Errorf("invalid file type: %s", contentType)
	}

	info, err := file.Stat()
	if err != nil {
		return nil, err
	}

	var maxSize int64 = 100 * 1024 * 1024
	if info.Size() > maxSize {
		return nil, errors.New("file size exceeds maximum limit (100MB)")
	}

	buf = make([]byte, info.Size())
	_, err = file.Seek(0, 0)
	if err != nil {
		return nil, err
	}

	_, err = io.ReadFull(file, buf)
	if err != nil {
		return nil, err
	}

	return buf, nil
}

// Kind 根据扩展名返回 "audio" 或 "image"。
func Kind(path string) string {
	if strings.EqualFold(filepath.Ext(path), ".wav") {
		return "audio"
	}
	return "image"
}

// normalizeFileURL 将标准 file:// URL 转为当前 OS 的文件路径。
func normalizeFileURL(u *url.URL) string {
	path := u.Path
	if unescaped, err := url.PathUnescape(path); err == nil {
		path = unescaped
	}
	host := u.Host
	if unescaped, err := url.PathUnescape(host); err == nil {
		host = unescaped
	}
	if len(host) >= 2 && host[1] == ':' && isASCIIAlpha(host[0]) {
		return filepath.Clean(filepath.FromSlash(host + path))
	}
	if len(path) >= 4 && path[0] == '/' && path[2] == ':' && isASCIIAlpha(path[1]) {
		path = path[1:]
	}
	if u.Host != "" && !strings.EqualFold(u.Host, "localhost") {
		return `\\` + u.Host + filepath.FromSlash(path)
	}
	return filepath.FromSlash(path)
}

// normalizeMalformedFileURL 尝试修复缺少主机等常见畸形 file:// 字符串。
func normalizeMalformedFileURL(raw string) (string, bool) {
	const prefix = "file://"
	if !strings.HasPrefix(strings.ToLower(raw), prefix) {
		return "", false
	}

	path := raw[len(prefix):]
	if unescaped, err := url.PathUnescape(path); err == nil {
		path = unescaped
	}
	path = strings.TrimPrefix(path, "localhost")
	if len(path) >= 3 && path[0] == '/' && path[2] == ':' && isASCIIAlpha(path[1]) {
		path = path[1:]
	}
	if len(path) >= 2 && path[1] == ':' && isASCIIAlpha(path[0]) {
		return filepath.Clean(filepath.FromSlash(path)), true
	}
	return "", false
}

// isASCIIAlpha 判断字节是否为 ASCII 字母（用于 Windows 盘符检测）。
func isASCIIAlpha(b byte) bool {
	return (b >= 'a' && b <= 'z') || (b >= 'A' && b <= 'Z')
}
