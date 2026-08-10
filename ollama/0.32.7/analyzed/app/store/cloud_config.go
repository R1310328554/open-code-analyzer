//go:build windows || darwin

// Package store 云端配置读写：server.json 与 OLLAMA_NO_CLOUD 环境变量。
package store

import (
	"encoding/json"
	"errors"
	"fmt"
	"os"
	"path/filepath"

	"github.com/ollama/ollama/envconfig"
)

// serverConfigFilename 为用户主目录 ~/.ollama 下服务器配置文件名。
const serverConfigFilename = "server.json"

// serverConfig 对应 server.json 中与云端相关的字段。
type serverConfig struct {
	DisableOllamaCloud bool `json:"disable_ollama_cloud,omitempty"`
}

// CloudDisabled 返回是否应禁用云端功能；来源为环境变量或 server.json。
// CloudDisabled returns whether cloud features should be disabled.
// The source of truth is: OLLAMA_NO_CLOUD OR ~/.ollama/server.json:disable_ollama_cloud.
func (s *Store) CloudDisabled() (bool, error) {
	disabled, _, err := s.CloudStatus()
	return disabled, err
}

// CloudStatus 返回云端禁用状态及决策来源（none/env/config/both）。
// CloudStatus returns whether cloud is disabled and the source of that decision.
// Source is one of: "none", "env", "config", "both".
func (s *Store) CloudStatus() (bool, string, error) {
	if err := s.ensureDB(); err != nil {
		return false, "", err
	}

	configDisabled, err := readServerConfigCloudDisabled()
	if err != nil {
		return false, "", err
	}

	envDisabled := envconfig.NoCloudEnv()
	return envDisabled || configDisabled, cloudStatusSource(envDisabled, configDisabled), nil
}

// SetCloudEnabled 将云端开关写入 ~/.ollama/server.json。
// SetCloudEnabled writes the cloud setting to ~/.ollama/server.json.
func (s *Store) SetCloudEnabled(enabled bool) error {
	if err := s.ensureDB(); err != nil {
		return err
	}
	return setCloudEnabled(enabled)
}

// setCloudEnabled 更新 server.json 中的 disable_ollama_cloud 字段。
func setCloudEnabled(enabled bool) error {
	configPath, err := serverConfigPath()
	if err != nil {
		return err
	}

	if err := os.MkdirAll(filepath.Dir(configPath), 0o755); err != nil {
		return fmt.Errorf("create server config directory: %w", err)
	}

	configMap := map[string]any{}
	if data, err := os.ReadFile(configPath); err == nil {
		if err := json.Unmarshal(data, &configMap); err != nil {
			// If the existing file is invalid JSON, overwrite with a fresh object.
			configMap = map[string]any{}
		}
	} else if !errors.Is(err, os.ErrNotExist) {
		return fmt.Errorf("read server config: %w", err)
	}

	configMap["disable_ollama_cloud"] = !enabled

	data, err := json.MarshalIndent(configMap, "", "  ")
	if err != nil {
		return fmt.Errorf("marshal server config: %w", err)
	}
	data = append(data, '\n')

	if err := os.WriteFile(configPath, data, 0o644); err != nil {
		return fmt.Errorf("write server config: %w", err)
	}

	return nil
}

// readServerConfigCloudDisabled 读取配置文件；无效 JSON 时视为未禁用。
func readServerConfigCloudDisabled() (bool, error) {
	configPath, err := serverConfigPath()
	if err != nil {
		return false, err
	}

	data, err := os.ReadFile(configPath)
	if err != nil {
		if errors.Is(err, os.ErrNotExist) {
			return false, nil
		}
		return false, fmt.Errorf("read server config: %w", err)
	}

	var cfg serverConfig
	// Invalid or unexpected JSON should not block startup; treat as default.
	if json.Unmarshal(data, &cfg) == nil {
		return cfg.DisableOllamaCloud, nil
	}
	return false, nil
}

// serverConfigPath 返回 ~/.ollama/server.json 的绝对路径。
func serverConfigPath() (string, error) {
	home, err := os.UserHomeDir()
	if err != nil {
		return "", fmt.Errorf("resolve home directory: %w", err)
	}
	return filepath.Join(home, ".ollama", serverConfigFilename), nil
}

// cloudStatusSource 根据环境与配置两个开关组合返回来源标识。
func cloudStatusSource(envDisabled bool, configDisabled bool) string {
	switch {
	case envDisabled && configDisabled:
		return "both"
	case envDisabled:
		return "env"
	case configDisabled:
		return "config"
	default:
		return "none"
	}
}
