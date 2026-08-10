//go:build windows || darwin

// auth 包（app）负责构造 Ollama 账户连接 URL。
package auth

import (
	"encoding/base64"
	"fmt"
	"net/url"
	"os"

	"github.com/ollama/ollama/auth"
)

// BuildConnectURL 生成含公钥与设备名的 ollama.com 连接 URL。
// BuildConnectURL generates the connect URL with the public key and device name
func BuildConnectURL(baseURL string) (string, error) {
	pubKey, err := auth.GetPublicKey()
	if err != nil {
		return "", fmt.Errorf("failed to get public key: %w", err)
	}

	encodedKey := base64.RawURLEncoding.EncodeToString([]byte(pubKey))
	hostname, _ := os.Hostname()
	encodedDevice := url.QueryEscape(hostname)

	return fmt.Sprintf("%s/connect?name=%s&key=%s&launch=true", baseURL, encodedDevice, encodedKey), nil
}
