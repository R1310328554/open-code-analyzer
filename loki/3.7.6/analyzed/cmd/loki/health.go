package main

// Loki 轻量健康检查：在完整配置解析前拦截 -health 标志，
// 对 /ready 端点发起 HTTP GET 并返回 0/1 退出码供容器探针使用。

import (
	"fmt"
	"net/http"
	"os"
	"regexp"
	"time"
)

const (
	healthFlag       = "health"
	defaultHealthURL = "http://localhost:3100/ready"
	healthTimeout    = 5 * time.Second
)

// 扫描 argv 是否含 -health，供 main 在加载配置前短路退出。
// CheckHealth checks if args contain the -health flag
func CheckHealth(args []string) bool {
	pattern := regexp.MustCompile(`^-+` + healthFlag + `$`)
	for _, a := range args {
		if pattern.MatchString(a) {
			return true
		}
	}
	return false
}

// 带超时请求 ready URL，状态 200 打印 healthy 否则返回错误码 1。
// RunHealthCheck performs a health check against the /ready endpoint
// Returns exit code 0 if healthy, 1 if unhealthy
func RunHealthCheck(args []string) int {

	url := getHealthURL(args)

	client := &http.Client{
		Timeout: healthTimeout,
	}

	resp, err := client.Get(url)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Health check failed: %v\n", err)
		return 1
	}
	defer resp.Body.Close()

	if resp.StatusCode == http.StatusOK {
		fmt.Println("Loki is healthy")
		return 0
	}

	fmt.Fprintf(os.Stderr, "Loki is unhealthy: status code %d\n", resp.StatusCode)
	return 1
}

// 支持 -health.url= 或 -health.url <url> 两种形式，默认 localhost:3100。
// getHealthURL extracts the URL from args or returns default
// Looks for -health.url=<url> or -health.url <url>
func getHealthURL(args []string) string {
	urlPattern := regexp.MustCompile(`^-+health\.url[=:]?(.*)$`)
	healthArgPattern := regexp.MustCompile(`^-`)

	for i, a := range args {
		if matches := urlPattern.FindStringSubmatch(a); matches != nil {
			if matches[1] != "" {
				return matches[1]
			}
			// Check next argument for the URL value
			if i+1 < len(args) && !healthArgPattern.MatchString(args[i+1]) {
				return args[i+1]
			}
		}
	}
	return defaultHealthURL
}
