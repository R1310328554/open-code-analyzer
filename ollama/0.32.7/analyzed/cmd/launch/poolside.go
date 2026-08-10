package launch

import (
	"fmt"
	"os"
	"os/exec"
	"runtime"

	"github.com/ollama/ollama/envconfig"
)

// Poolside 实现 Runner，将 Poolside CLI 指向本地 Ollama OpenAI 兼容端点。
type Poolside struct{}

var poolsideGOOS = runtime.GOOS

func (p *Poolside) String() string { return "Pool" }

// poolsideUnsupportedError 在 Windows 上返回不支持提示。
func poolsideUnsupportedError() error {
	return fmt.Errorf("Warning: Poolside is not currently supported on Windows")
}

// args 组装 pool 子进程的 -m 与透传参数。
func (p *Poolside) args(model string, extra []string) []string {
	var args []string
	if model != "" {
		args = append(args, "-m", model)
	}
	args = append(args, extra...)
	return args
}

// Run 在 PATH 中查找 pool 并以 Ollama 独立模式环境变量启动。
func (p *Poolside) Run(model string, _ []LaunchModel, args []string) error {
	if poolsideGOOS == "windows" {
		return poolsideUnsupportedError()
	}

	bin, err := exec.LookPath("pool")
	if err != nil {
		return fmt.Errorf("pool is not installed")
	}

	cmd := exec.Command(bin, p.args(model, args)...)
	cmd.Stdin = os.Stdin
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr
	cmd.Env = append(os.Environ(),
		"POOLSIDE_STANDALONE_BASE_URL="+envconfig.Host().String()+"/v1",
		"POOLSIDE_API_KEY=ollama",
	)
	return cmd.Run()
}
