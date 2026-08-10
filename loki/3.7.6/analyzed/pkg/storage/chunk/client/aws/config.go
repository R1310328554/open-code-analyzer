package aws

// config 提供 AWS URL 凭证解析辅助：从 URL Userinfo 提取静态密钥，未设置时返回空以走 SDK 默认凭证链（环境变量、IAM 角色等）。

import (
	"net/url"
)

const InvalidAWSRegion = "dummy"

func credentialsFromURL(awsURL *url.URL) (key, secret string) {
	if awsURL.User != nil {
		username := awsURL.User.Username()
		password, _ := awsURL.User.Password()

		// We request at least the username or password being set to enable the static credentials.
		if username != "" || password != "" {
			return username, password
		}
	}
	// Return empty credentials instead of error to allow AWS SDK to use default credential chain
	// (environment variables, IAM roles, etc.)
	return "", ""
}
// username 或 password 任一非空即启用静态凭证；两者皆空则委托 SDK 自动发现凭证。
