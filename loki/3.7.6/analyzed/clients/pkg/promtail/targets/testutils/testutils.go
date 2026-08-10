package testutils

// Promtail targets 测试辅助：随机名称生成与 relabel 配置校验封装。

import (
	"math/rand"
	"testing"
	"time"

	"github.com/prometheus/common/model"
	"github.com/prometheus/prometheus/model/relabel"
	"github.com/stretchr/testify/require"
)

var randomGenerator *rand.Rand

// 初始化全局 rand.Rand，供 RandName 生成唯一测试标识符。
func InitRandom() {
	randomGenerator = rand.New(rand.NewSource(time.Now().UnixNano()))
}

var letters = []rune("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ")

func RandName() string {
	b := make([]rune, 10)
	for i := range b {
		b[i] = letters[randomGenerator.Intn(len(letters))] //#nosec G404 -- Generating random test data, fine. -- nosemgrep: math-random-used
	}
	return string(b)
}

// 对每个 relabel.Config 调用 Validate(UTF8Validation) 并 require 无错误。
func ValidateRelabelConfig(t *testing.T, configs []*relabel.Config) []*relabel.Config {
	t.Helper()

	for _, c := range configs {
		require.NoError(t, c.Validate(model.UTF8Validation))
	}

	return configs
}
