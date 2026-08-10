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

// version.go 读取 RAGFlow 版本号。

import (
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"sync"
)

var (
	ragflowVersionInfo = "unknown"
	versionOnce        sync.Once
)

// GetRAGFlowVersion 获取 RAGFlow 版本（VERSION 文件或 git describe）。
func GetRAGFlowVersion() string {
	versionOnce.Do(func() {
		ragflowVersionInfo = getRAGFlowVersionInternal()
	})
	return ragflowVersionInfo
}

// getRAGFlowVersionInternal 内部版本读取逻辑。
func getRAGFlowVersionInternal() string {
	// 从可执行文件向上查找 VERSION 文件
	exePath, err := os.Executable()
	if err != nil {
		return getClosestTagAndCount()
	}

	// 最多向上 5 层目录查找 VERSION
	dir := filepath.Dir(exePath)
	for i := 0; i < 5; i++ { // Try up to 5 levels up
		versionPath := filepath.Join(dir, "VERSION")
		var data []byte
		if data, err = os.ReadFile(versionPath); err == nil {
			return strings.TrimSpace(string(data))
		}
		parent := filepath.Dir(dir)
		if parent == dir {
			break
		}
		dir = parent
	}

	// 回退到 git describe
	return getClosestTagAndCount()
}

// getClosestTagAndCount 通过 git describe 获取最近 tag。
func getClosestTagAndCount() string {
	cmd := exec.Command("git", "describe", "--tags", "--match=v*", "--first-parent", "--always")
	output, err := cmd.Output()
	if err != nil {
		return "unknown"
	}
	return strings.TrimSpace(string(output))
}
// version.go — RAGFlow 版本号读取（VERSION 文件或 git describe 回退）。
