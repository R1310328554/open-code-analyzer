// Copyright 2019 Drone IO, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// encrypt 包提供数据库字符串字段的加解密工厂与接口。
package encrypt

import (
	"crypto/aes"
	"errors"
)

// errKeySize 表示加密密钥长度不足（须为 32 字节）。
var errKeySize = errors.New("encryption key must be 32 bytes")

// Encrypter 提供数据库字段加解密；当前仅支持字符串类型。
type Encrypter interface {
	Encrypt(plaintext string) ([]byte, error)
	Decrypt(ciphertext []byte) (string, error)
}

// New 根据密钥创建 Encrypter；空密钥返回明文 none 实现。
func New(key string) (Encrypter, error) {
	if key == "" {
		return &none{}, nil
	}
	if len(key) != 32 {
		return nil, errKeySize
	}
	b := []byte(key)
	block, err := aes.NewCipher(b)
	if err != nil {
		return nil, err
	}
	return &Aesgcm{block: block}, nil
}
