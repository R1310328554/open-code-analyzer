package util //nolint:revive

// allowed_tenants 根据 enabled/disabled 列表判定租户是否允许访问：nil 表示不限制；enabled 非空时仅白名单租户通过。

// AllowedTenants 封装租户白名单与黑名单，disabled 优先于 enabled 判定。
// AllowedTenants that can answer whether tenant is allowed or not based on configuration.
// Default value (nil) allows all tenants.
type AllowedTenants struct {
// enabled 非空时仅映射内租户允许；为空则不启用白名单限制。
	// If empty, all tenants are enabled. If not empty, only tenants in the map are enabled.
	enabled map[string]struct{}

// disabled 非空时命中映射的租户一律拒绝，即使已在 enabled 白名单中。
	// If empty, no tenants are disabled. If not empty, tenants in the map are disabled.
	disabled map[string]struct{}
}

// NewAllowedTenants 将字符串切片转为 set 映射，空切片表示该维度不生效。
// NewAllowedTenants builds new allowed tenants based on enabled and disabled tenants.
// If there are any enabled tenants, then only those tenants are allowed.
// If there are any disabled tenants, then tenant from that list, that would normally be allowed, is disabled instead.
func NewAllowedTenants(enabled []string, disabled []string) *AllowedTenants {
	a := &AllowedTenants{}

	if len(enabled) > 0 {
		a.enabled = make(map[string]struct{}, len(enabled))
		for _, u := range enabled {
			a.enabled[u] = struct{}{}
		}
	}

	if len(disabled) > 0 {
		a.disabled = make(map[string]struct{}, len(disabled))
		for _, u := range disabled {
			a.disabled[u] = struct{}{}
		}
	}

	return a
}

func (a *AllowedTenants) IsAllowed(tenantID string) bool {
	if a == nil {
		return true
	}

	if len(a.enabled) > 0 {
		if _, ok := a.enabled[tenantID]; !ok {
			return false
		}
	}

	if len(a.disabled) > 0 {
		if _, ok := a.disabled[tenantID]; ok {
			return false
		}
	}

	return true
}
// IsAllowed 对 nil 接收者返回 true，便于配置缺省时跳过租户过滤逻辑。
