package validation

// validation 包 limits 聚合多租户限额：在跨 tenant 查询时取最小正整数/时长或 feature flag 交集。

import (
	"slices"
	"time"
)

// SmallestPositiveIntPerTenant 遍历 tenantIDs 取 f 返回值最小者，空列表返回 0。
// SmallestPositiveIntPerTenant is returning the minimal positive value of the
// supplied limit function for all given tenants.
func SmallestPositiveIntPerTenant(tenantIDs []string, f func(string) int) int {
	var result *int
	for _, tenantID := range tenantIDs {
		v := f(tenantID)
		if result == nil || v < *result {
			result = &v
		}
	}
	if result == nil {
		return 0
	}
	return *result
}

// SmallestPositiveNonZeroIntPerTenant 忽略 0（表示 unlimited），仅比较正限额。
// SmallestPositiveNonZeroIntPerTenant is returning the minimal positive and
// non-zero value of the supplied limit function for all given tenants. In many
// limits a value of 0 means unlimited so the method will return 0 only if all
// inputs have a limit of 0 or an empty tenant list is given.
func SmallestPositiveNonZeroIntPerTenant(tenantIDs []string, f func(string) int) int {
	var result *int
	for _, tenantID := range tenantIDs {
		v := f(tenantID)
		if v > 0 && (result == nil || v < *result) {
			result = &v
		}
	}
	if result == nil {
		return 0
	}
	return *result
}

// SmallestPositiveNonZeroDurationPerTenant 逻辑同 int 版，用于时间类跨租户下限。
// SmallestPositiveNonZeroDurationPerTenant is returning the minimal positive
// and non-zero value of the supplied limit function for all given tenants. In
// many limits a value of 0 means unlimited so the method will return 0 only if
// all inputs have a limit of 0 or an empty tenant list is given.
func SmallestPositiveNonZeroDurationPerTenant(tenantIDs []string, f func(string) time.Duration) time.Duration {
	var result *time.Duration
	for _, tenantID := range tenantIDs {
		v := f(tenantID)
		if v > 0 && (result == nil || v < *result) {
			result = &v
		}
	}
	if result == nil {
		return 0
	}
	return *result
}

// MaxDurationPerTenant 取各 tenant duration 最大值，无 tenant 时返回 0。
// MaxDurationPerTenant is returning the maximum duration per tenant. Without
// tenants given it will return a time.Duration(0).
func MaxDurationPerTenant(tenantIDs []string, f func(string) time.Duration) time.Duration {
	result := time.Duration(0)
	for _, tenantID := range tenantIDs {
		v := f(tenantID)
		if v > result {
			result = v
		}
	}
	return result
}

// MaxDurationOrZeroPerTenant 任一 tenant 为 0（unlimited）则整体返回 0。
// MaxDurationOrZeroPerTenant is returning the maximum duration per tenant or zero if one tenant has time.Duration(0).
func MaxDurationOrZeroPerTenant(tenantIDs []string, f func(string) time.Duration) time.Duration {
	var result *time.Duration
	for _, tenantID := range tenantIDs {
		v := f(tenantID)
		if v == 0 {
			return v
		}

		if v > 0 && (result == nil || v > *result) {
			result = &v
		}
	}
	if result == nil {
		return 0
	}
	return *result
}

// IntersectionPerTenant 双指针求各 tenant 已排序 feature 列表交集，表示共同支持能力。
// IntersectionPerTenant is returning the intersection of feature flags. This is useful to determine the minimal
// feature set supported.
func IntersectionPerTenant(tenantIDs []string, f func(string) []string) []string {
	var result []string
	for _, tenantID := range tenantIDs {
		v := f(tenantID)
		slices.Sort(v)
		if result == nil {
			result = v
			continue
		}
		var updatedResult []string
		for i, j := 0, 0; i < len(result) && j < len(v); {
			if result[i] == v[j] {
				updatedResult = append(updatedResult, result[i])
				i++
				j++
			} else if result[i] < v[j] {
				i++
			} else {
				j++
			}
		}
		result = updatedResult
	}
	return result
}
// 多租户查询应使用这些 helper 而非单 tenant limit，确保最严格限额与最小 feature 集生效。
