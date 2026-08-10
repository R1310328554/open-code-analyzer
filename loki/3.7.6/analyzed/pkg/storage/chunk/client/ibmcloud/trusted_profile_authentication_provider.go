package ibmcloud

// 本文件实现 IBM IAM Trusted Profile 认证提供者，供 COS SDK 在容器/计算资源场景下通过 CR token 文件换取 Bearer 访问令牌。

import (
	"os"

	"github.com/IBM/go-sdk-core/v5/core"
	"github.com/IBM/ibm-cos-sdk-go/aws/awserr"
	"github.com/IBM/ibm-cos-sdk-go/aws/credentials"
	"github.com/IBM/ibm-cos-sdk-go/aws/credentials/ibmiam/token"
	"github.com/go-kit/log/level"
	"github.com/pkg/errors"

	"github.com/grafana/loki/v3/pkg/util/log"
)

const (
	trustedProfileProviderName = "TrustedProfileProviderNameIBM"
	tokenType                  = "Bearer"
)

// TrustedProfileProvider implements Provider interface from https://github.com/IBM/ibm-cos-sdk-go
// TrustedProfileProvider 封装 core.ContainerAuthenticator，实现 credentials.Provider 接口。
type TrustedProfileProvider struct {
	// Name of Provider
	providerName string

	// Type of Provider - SharedCred, SharedConfig, etc.
	providerType string

	// Authenticator implements an IAM-based authentication schema
	authenticator *core.ContainerAuthenticator

	// Error
	ErrorStatus error
}

// NewTrustedProfileProvider creates custom IBM IAM Provider for Trusted Profile authentication
// NewTrustedProfileProvider 校验 profile 名/ID 与 CR token 文件存在性后构建 authenticator。
func NewTrustedProfileProvider(providerName string, trustedProfileName,
	trustedProfileID, crTokenFilePath, authEndpoint string) *TrustedProfileProvider {
	provider := new(TrustedProfileProvider)

	provider.providerName = providerName
	provider.providerType = "oauth"

	if trustedProfileName == "" && trustedProfileID == "" {
		provider.ErrorStatus = awserr.New("trustedProfileNotFound", "either Trusted profile name or id must be provided", nil)
		level.Debug(log.Logger).Log("msg", provider.ErrorStatus)

		return provider
	}

	if crTokenFilePath == "" {
		provider.ErrorStatus = awserr.New("crTokenFilePathEmpty", "must supply cr token file path", nil)
		level.Debug(log.Logger).Log("msg", provider.ErrorStatus)

		return provider
	}

	if _, err := os.Stat(crTokenFilePath); errors.Is(err, os.ErrNotExist) {
		provider.ErrorStatus = awserr.New("crTokenFileNotFound", "no such file", err)
		level.Debug(log.Logger).Log("msg", "no such file", "err", err)

		return provider
	}

	if authEndpoint == "" {
		authEndpoint = defaultCOSAuthEndpoint
		level.Debug(log.Logger).Log("msg", "using default auth endpoint", "endpoint", authEndpoint)
	}

	// We can either pass trusted profile name or trusted profile ID along with
	// the compute resource token file. If we pass both trusted profile name and
	// trusted profile ID it should be of same trusted profile.
	authenticator, err := core.NewContainerAuthenticatorBuilder().
		SetIAMProfileName(trustedProfileName).
		SetIAMProfileID(trustedProfileID).
		SetCRTokenFilename(crTokenFilePath).
		SetURL(authEndpoint).
		Build()

	if err != nil {
		provider.ErrorStatus = awserr.New("errCreatingAuthenticatorClient", "failed to setup new Trusted Profile Authenticator client", err)
		level.Debug(log.Logger).Log("msg", provider.ErrorStatus)

		return provider
	}

	provider.authenticator = authenticator

	return provider
}

// IsValid validates the trusted profile provider
func (p *TrustedProfileProvider) IsValid() bool {
	return nil == p.ErrorStatus
}

// Retrieve returns the creadential values
// Retrieve 调用 GetToken 获取或刷新 Bearer token，失败时返回 awserr 包装错误。
func (p *TrustedProfileProvider) Retrieve() (credentials.Value, error) {
	if p.ErrorStatus != nil {
		level.Debug(log.Logger).Log("msg", p.ErrorStatus)

		return credentials.Value{ProviderName: p.providerName}, p.ErrorStatus
	}

	// GetToken function gets the token or generate a new token if the token is expired.
	tokenValue, err := p.authenticator.GetToken()
	if err != nil {
		level.Debug(log.Logger).Log("msg", "failed to get token", "err", err)
		returnErr := awserr.New("TokenGetError", "failed to get token", err)

		return credentials.Value{}, returnErr
	}

	return credentials.Value{
		Token: token.Token{
			AccessToken: tokenValue,
			TokenType:   tokenType,
		},
		ProviderName: p.providerName,
		ProviderType: p.providerType,
	}, nil
}

// IsExpired should ideally check the token expiry
// but here we are skipping the expiry check since the token variable in authenticator is not an exported variable.
// The GetToken function in Retrieve method is checking whether the token is expired
// or not before making the call to the server.
// IsExpired 恒返回 true，实际过期检测委托给 authenticator.GetToken 内部逻辑。
func (p *TrustedProfileProvider) IsExpired() bool {
	return true
}

// NewTrustedProfileCredentials a constructor for IBM IAM that uses IAM Trusted Profile credentials passed in
// NewTrustedProfileCredentials 构造可被 COS SDK 使用的静态 Credentials 包装器。
func NewTrustedProfileCredentials(authEndpoint, trustedProfileName,
	trustedProfileID, crTokenFilePath string) *credentials.Credentials {
	return credentials.NewCredentials(
		NewTrustedProfileProvider(
			trustedProfileProviderName, trustedProfileName,
			trustedProfileID, crTokenFilePath, authEndpoint,
		),
	)
}
// profile 名称与 ID 可二选一传入，若同时提供须指向同一 trusted profile 实体。
