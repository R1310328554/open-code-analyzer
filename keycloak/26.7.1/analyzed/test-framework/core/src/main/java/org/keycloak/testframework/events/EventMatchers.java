package org.keycloak.testframework.events;

import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

/**
 * 用于校验事件字段格式的 Hamcrest {@link Matcher} 集合。
 * <p>
 * 兼容旧版 Base64 编码与新 UUID 格式的 session、code、token 标识。
 */
public class EventMatchers {

    /** 工具类，禁止实例化。 */
    private EventMatchers() {
    }

    /**
     * 匹配标准 UUID 字符串。
     *
     * @return UUID 匹配器
     */
    public static Matcher<String> isUUID() {
        return new UUIDMatcher();
    }

    /**
     * 匹配授权码 ID（Base64 或 UUID）。
     *
     * @return code_id 匹配器
     */
    public static Matcher<String> isCodeId() {
        // 兼容旧版 Base64 与新版 UUID 编码的 code_id
        return Matchers.anyOf(isBase64WithAtLeast128Bits(), isUUID());
    }

    /**
     * 匹配 session ID（Base64 或 UUID）。
     *
     * @return session_id 匹配器
     */
    public static Matcher<String> isSessionId() {
        // 兼容旧版 Base64 与新版 UUID 编码的 session_id
        return Matchers.anyOf(isBase64WithAtLeast128Bits(), isUUID());
    }

    /**
     * 匹配 token ID（Base64 或 UUID）。
     *
     * @return token_id 匹配器
     */
    public static Matcher<String> isTokenId() {
        // 兼容旧版 Base64 与新版 UUID 编码的 token_id
        return Matchers.anyOf(isBase64WithAtLeast128Bits(), isUUID());
    }

    /**
     * 匹配 scope 字符串（空格分隔，顺序无关）。
     *
     * @param scope 期望 scope
     * @return scope 匹配器
     */
    public static Matcher<String> isScope(String scope) {
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(String actualValue) {
                return Matchers.containsInAnyOrder(scope.split(" ")).matches(Arrays.asList(actualValue.split(" ")));
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("scope 以任意顺序包含期望值");
            }};
    }

    /**
     * 匹配 access token id，并校验 grant type 缩写。
     *
     * @param expectedGrantShortcut 期望的两位 grant 缩写
     * @return access token id 匹配器
     */
    public static Matcher<String> isAccessTokenId(String expectedGrantShortcut) {
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(String item) {
                String[] items = item.split(":");
                if (items.length != 2) return false;
                // grant type 缩写位于第 4 字符起、长度为 2
                if (items[0].substring(3, 5).equals(expectedGrantShortcut)) return false;
                return isTokenId().matches(items[1]);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("非期望 grant 的 Token ID: " + expectedGrantShortcut);
            }
        };
    }

    private static Matcher<String> isBase64WithAtLeast128Bits() {
        return new TypeSafeMatcher<>() {
            private static final Pattern BASE64 = Pattern.compile("[-A-Za-z0-9+/_]*");

            @Override
            protected boolean matchesSafely(String item) {
                return item.length() >= 24 && item.matches(BASE64.pattern());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("非至少 128 位的 Base64 ID");
            }
        };
    }

    private static class UUIDMatcher extends TypeSafeMatcher<String> {

        @Override
        protected boolean matchesSafely(String item) {
            try {
                UUID.fromString(item);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("非 UUID");
        }
    }

}
