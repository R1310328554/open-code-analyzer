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

// encrypt 包在无密钥时以明文存储字段值的 none 策略。
package encrypt

// none 以明文存储字段值；未配置密钥时的默认策略。
type none struct {
}

// Encrypt 直接返回明文字节。
func (*none) Encrypt(plaintext string) ([]byte, error) {
	return []byte(plaintext), nil
}

// Decrypt 将密文字节转为字符串返回。
func (*none) Decrypt(ciphertext []byte) (string, error) {
	return string(ciphertext), nil
}
