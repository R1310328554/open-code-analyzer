// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

package machine

import (
	"bytes"
	"encoding/json"
	"io"
	"io/ioutil"
	"strings"
)

// Config 表示 docker-machine 的配置结构（驱动、TLS 与主机选项）。
type Config struct {
	Name   string
	Driver struct {
		IPAddress   string
		MachineName string
	}
	HostOptions struct {
		EngineOptions struct {
			TLSVerify bool `json:"TlsVerify"`
		}
		AuthOptions struct {
			CertDir          string
			CaCertPath       string
			CaPrivateKeyPath string
			ServerCertPath   string
			ServerKeyPath    string
			ClientKeyPath    string
			ClientCertPath   string
			StorePath        string
		}
	}
}

// parseReader 从 io.Reader 读取并解析 docker-machine JSON 配置。
func parseReader(r io.Reader) (*Config, error) {
	out := new(Config)
	err := json.NewDecoder(r).Decode(out)
	return out, err
}

// parseString 从 JSON 字符串解析 docker-machine 配置。
func parseString(s string) (*Config, error) {
	r := strings.NewReader(s)
	return parseReader(r)
}

// parseFile 从本地 JSON 文件路径读取并解析 docker-machine 配置。
func parseFile(path string) (*Config, error) {
	d, err := ioutil.ReadFile(path)
	if err != nil {
		return nil, err
	}
	r := bytes.NewReader(d)
	return parseReader(r)
}
