//
//  Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

package utility

// token.go 提供访问令牌签名/验签与随机令牌生成。

import (
	"crypto/rand"
	"crypto/sha1"
	"encoding/base64"
	"encoding/hex"
	"errors"
	"fmt"
	"strings"

	"github.com/google/uuid"
	"github.com/iromli/go-itsdangerous"
)

// ExtractAccessToken 从 Authorization 头解析 access_token，对齐 Python jwt.loads。
func ExtractAccessToken(authorization, secretKey string) (string, error) {
	if authorization == "" {
		return "", errors.New("empty authorization")
	}

	// 去除 Bearer 前缀
	token := strings.TrimPrefix(authorization, "Bearer ")

	// 创建 itsdangerous 兼容签名器（salt/key_derivation/digest 对齐 Python）
	algo := &itsdangerous.HMACAlgorithm{DigestMethod: sha1.New}
	signer := itsdangerous.NewTimestampSignature(
		secretKey,
		"itsdangerous",
		".",
		"django-concat",
		sha1.New,
		algo,
	)

	// 验签并提取 payload
	encodedValue, err := signer.Unsign(token, 0)
	if err != nil {
		return "", fmt.Errorf("failed to decode token: %w", err)
	}

	// Base64 解码 payload
	jsonValue, err := urlSafeB64Decode(encodedValue)
	if err != nil {
		return "", fmt.Errorf("failed to decode payload: %w", err)
	}

	// 解析 JSON 字符串（去除引号）
	value := string(jsonValue)
	if strings.HasPrefix(value, "\"") && strings.HasSuffix(value, "\"") {
		value = value[1 : len(value)-1]
	}

	return value, nil
}

// DumpAccessToken 将 access_token 签名打包为 Authorization 令牌。
func DumpAccessToken(accessToken, secretKey string) (string, error) {
	if accessToken == "" {
		return "", errors.New("empty access token")
	}

	// Create URLSafeTimedSerializer with correct configuration
	// Matching Python itsdangerous configuration:
	// - salt: "itsdangerous"
	// - key_derivation: "django-concat"
	// - digest_method: sha1
	algo := &itsdangerous.HMACAlgorithm{DigestMethod: sha1.New}
	signer := itsdangerous.NewTimestampSignature(
		secretKey,
		"itsdangerous",
		".",
		"django-concat",
		sha1.New,
		algo,
	)

	// 将 access_token 编码为 JSON 字符串
	jsonValue := fmt.Sprintf("\"%s\"", accessToken)
	encodedValue := urlSafeB64Encode([]byte(jsonValue))

	// 签名生成最终令牌
	token, err := signer.Sign(encodedValue)
	if err != nil {
		return "", fmt.Errorf("failed to sign token: %w", err)
	}

	return token, nil
}

// urlSafeB64Decode URL 安全 Base64 解码（自动补 padding）。
func urlSafeB64Decode(s string) ([]byte, error) {
	// 不足 4 倍数时补 = padding
	padding := 4 - len(s)%4
	if padding != 4 {
		s += strings.Repeat("=", padding)
	}
	return base64.URLEncoding.DecodeString(s)
}

// urlSafeB64Encode URL 安全 Base64 编码（无 padding）。
func urlSafeB64Encode(data []byte) string {
	encoded := base64.URLEncoding.EncodeToString(data)
	// 去除末尾 = padding
	return strings.TrimRight(encoded, "=")
}

// GenerateSecretKey 生成 32 字节 hex 密钥（256 位）。
func GenerateSecretKey() (string, error) {
	bytes := make([]byte, 32) // 32 bytes = 256 bits
	if _, err := rand.Read(bytes); err != nil {
		return "", fmt.Errorf("failed to generate random key: %v", err)
	}
	return hex.EncodeToString(bytes), nil
}

func GenerateToken() string {
	return strings.ReplaceAll(uuid.New().String(), "-", "")
}

// GenerateUUID 生成无连字符 UUID（最多 32 字符）。
func GenerateUUID() string {
	newID := strings.ReplaceAll(uuid.New().String(), "-", "")
	if len(newID) > 32 {
		newID = newID[:32]
	}
	return newID
}

// GenerateAPIToken 生成 ragflow- 前缀的安全 API 密钥。
func GenerateAPIToken() string {
	// 生成 32 字节随机数
	bytes := make([]byte, 32)
	if _, err := rand.Read(bytes); err != nil {
		// 随机失败时回退 UUID
		return "ragflow-" + strings.ReplaceAll(uuid.New().String(), "-", "")
	}
	// URL 安全 Base64 编码
	return "ragflow-" + base64.RawURLEncoding.EncodeToString(bytes)
}

// GenerateBetaAPIToken 生成 beta 访问密钥（等同 GenerateUUID）。
func GenerateBetaAPIToken() string {
	return GenerateUUID()
}
// token.go — 访问令牌签名/验签（itsdangerous 兼容）及各类随机令牌生成。
