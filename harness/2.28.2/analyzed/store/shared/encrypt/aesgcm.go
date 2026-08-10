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

// encrypt 包实现数据库字段加解密；Aesgcm 使用 AES-GCM 算法。
package encrypt

import (
	"crypto/cipher"
	"crypto/rand"
	"errors"
	"io"
)

// Aesgcm provides an encrypter that uses the aesgcm encryption
// algorithm.
type Aesgcm struct {
	block  cipher.Block
	Compat bool
}

// Encrypt 使用 AES-GCM 加密明文字符串，密文含随机 nonce 前缀。
func (e *Aesgcm) Encrypt(plaintext string) ([]byte, error) {
	gcm, err := cipher.NewGCM(e.block)
	if err != nil {
		return nil, err
	}

	nonce := make([]byte, gcm.NonceSize())
	_, err = io.ReadFull(rand.Reader, nonce)
	if err != nil {
		return nil, err
	}

	return gcm.Seal(nonce, nonce, []byte(plaintext), nil), nil
}

// Decrypt 使用 AES-GCM 解密密文；Compat 模式下失败时返回原文。
func (e *Aesgcm) Decrypt(ciphertext []byte) (string, error) {
	gcm, err := cipher.NewGCM(e.block)
	if err != nil {
		return "", err
	}

	if len(ciphertext) < gcm.NonceSize() {
		// 兼容模式下解密失败时直接返回密文字符串，
		// 适用于数据库中混存加密与明文内容的过渡期。
		if e.Compat {
			return string(ciphertext), nil
		}
		return "", errors.New("malformed ciphertext")
	}

	plaintext, err := gcm.Open(nil,
		ciphertext[:gcm.NonceSize()],
		ciphertext[gcm.NonceSize():],
		nil,
	)
	// 兼容模式下解密失败时直接返回密文字符串，
	// 适用于数据库中混存加密与明文内容的过渡期。
	if err != nil && e.Compat {
		return string(ciphertext), nil
	}
	return string(plaintext), err
}
