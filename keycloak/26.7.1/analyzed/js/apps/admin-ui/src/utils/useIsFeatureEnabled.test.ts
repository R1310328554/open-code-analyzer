/**
 * useIsFeatureEnabled 模块单元测试。
 * 重点验证 unversionedName 对特性名版本后缀（如 _V2、_V3）的剥离逻辑。
 */
import { describe, expect, it } from "vitest";
import { unversionedName } from "./useIsFeatureEnabled";

describe("unversionedName", () => {
  it("strips version suffix from feature names", () => {
    // 末尾 _V数字 应被去掉，便于跨版本比较特性是否启用
    expect(unversionedName("ACCOUNT_V3")).toBe("ACCOUNT");
    expect(unversionedName("ADMIN_FINE_GRAINED_AUTHZ_V2")).toBe(
      "ADMIN_FINE_GRAINED_AUTHZ",
    );
    expect(unversionedName("TOKEN_EXCHANGE_STANDARD_V2")).toBe(
      "TOKEN_EXCHANGE_STANDARD",
    );
  });

  it("returns name unchanged when there is no version suffix", () => {
    expect(unversionedName("ACCOUNT_API")).toBe("ACCOUNT_API");
    expect(unversionedName("ORGANIZATION")).toBe("ORGANIZATION");
    expect(unversionedName("DPOP")).toBe("DPOP");
    expect(unversionedName("CLIENT_POLICIES")).toBe("CLIENT_POLICIES");
  });

  it("only strips trailing version suffix", () => {
    // 仅剥离末尾后缀，名称中间的 V2 等片段保持不变
    expect(unversionedName("V2_SOMETHING")).toBe("V2_SOMETHING");
    expect(unversionedName("FEATURE_V2_EXTRA")).toBe("FEATURE_V2_EXTRA");
  });
});
