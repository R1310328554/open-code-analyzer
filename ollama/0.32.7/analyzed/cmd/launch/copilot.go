package launch

import (
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"runtime"

	"github.com/ollama/ollama/envconfig"
)

// Copilot 实现 Runner，将 GitHub Copilot CLI 指向 Ollama OpenAI 兼容 API。
// Copilot 集成 GitHub Copilot 命令行工具。
type Copilot struct{}

func (c *Copilot) String() string { return "Copilot CLI" }

func (c *Copilot) args(model string, extra []string) []string {
	var args []string
	if model != "" {
		args = append(args, "--model", model)
	}
	args = append(args, extra...)
	return args
}

// findPath 在 PATH 与 ~/.local/bin 中查找 copilot 可执行文件。
func (c *Copilot) findPath() (string, error) {
	if p, err := exec.LookPath("copilot"); err == nil {
		return p, nil
	}
	home, err := os.UserHomeDir()
	if err != nil {
		return "", err
	}
	name := "copilot"
	if runtime.GOOS == "windows" {
		name = "copilot.exe"
	}
	fallback := filepath.Join(home, ".local", "bin", name)
	if _, err := os.Stat(fallback); err != nil {
		return "", err
	}
	return fallback, nil
}

// Run 以 Ollama provider 环境变量启动 copilot 子进程。
func (c *Copilot) Run(model string, _ []LaunchModel, args []string) error {
	copilotPath, err := c.findPath()
	if err != nil {
		return fmt.Errorf("copilot is not installed, install from https://docs.github.com/en/copilot/how-tos/set-up/install-copilot-cli")
	}

	cmd := exec.Command(copilotPath, c.args(model, args)...)
	cmd.Stdin = os.Stdin
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	cmd.Env = append(os.Environ(), c.envVars(model)...)

	return cmd.Run()
}

// envVars 返回 COPILOT_PROVIDER_* 变量，使 Copilot CLI 使用 Ollama 作为模型后端。
// to use Ollama as its model provider.
func (c *Copilot) envVars(model string) []string {
	env := []string{
		"COPILOT_PROVIDER_BASE_URL=" + envconfig.Host().String() + "/v1",
		"COPILOT_PROVIDER_API_KEY=",
		"COPILOT_PROVIDER_WIRE_API=responses",
	}

	if model != "" {
		env = append(env, "COPILOT_MODEL="+model)
	}

	return env
}
