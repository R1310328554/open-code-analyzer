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

// encrypt 包提供将明文 Secret 加密为 YAML 可用密文的 API 端点。
package encrypt

import (
	"crypto/aes"
	"crypto/cipher"
	"crypto/rand"
	"encoding/base64"
	"encoding/json"
	"io"
	"net/http"

	"github.com/drone/drone-go/drone"
	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/render"
	"github.com/go-chi/chi"
)

// respEncrypted 为加密接口的 JSON 响应体，data 字段为 Base64 编码的密文。
type respEncrypted struct {
	Data string `json:"data"`
}

// Handler 返回 HTTP 处理器，使用仓库级 256 位密钥加密请求中的 Secret 明文。
func Handler(repos core.RepositoryStore) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		namespace := chi.URLParam(r, "owner")
		name := chi.URLParam(r, "name")
		repo, err := repos.FindName(r.Context(), namespace, name)
		if err != nil {
			render.NotFound(w, err)
			return
		}

		in := new(drone.Secret)
		err = json.NewDecoder(r.Body).Decode(in)
		if err != nil {
			render.BadRequest(w, err)
			return
		}

		// 使用仓库专属 Secret 作为 AES 密钥；缺失或格式错误时向客户端返回错误。
		encrypted, err := encrypt([]byte(in.Data), []byte(repo.Secret))
		if err != nil {
			render.InternalError(w, err)
			return
		}

		// 密文经 Base64 编码后嵌入 .drone.yml 的 !binary 属性。
		encoded := base64.StdEncoding.EncodeToString(encrypted)

		render.JSON(w, &respEncrypted{Data: encoded}, 200)
	}
}

// encrypt 使用 AES-GCM 以给定密钥加密明文，返回 nonce 前缀的密文字节。
func encrypt(plaintext, key []byte) (ciphertext []byte, err error) {
	block, err := aes.NewCipher(key)
	if err != nil {
		return nil, err
	}

	gcm, err := cipher.NewGCM(block)
	if err != nil {
		return nil, err
	}

	nonce := make([]byte, gcm.NonceSize())
	_, err = io.ReadFull(rand.Reader, nonce)
	if err != nil {
		return nil, err
	}

	return gcm.Seal(nonce, nonce, plaintext, nil), nil
}
