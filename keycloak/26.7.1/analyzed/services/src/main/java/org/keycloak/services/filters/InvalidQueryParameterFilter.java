package org.keycloak.services.filters;

import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Priority;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

/**
 * 无效查询参数过滤器。
 * <p>在预匹配阶段拦截含 ASCII 控制字符（除 TAB/LF/CR）的查询参数值，防止编码破坏与终端异常行为。</p>
 */
@Provider
@PreMatching
@Priority(10)
public class InvalidQueryParameterFilter implements ContainerRequestFilter {

    /** 日志记录器 */
    private static final Logger LOGGER = Logger.getLogger(InvalidQueryParameterFilter.class);

    /** {@inheritDoc} 校验所有查询参数值是否含非法控制字符 */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        final Map<String, List<String>> queryParams = requestContext.getUriInfo().getQueryParameters();

        for (final List<String> queryParamValues : queryParams.values()) {
            for (final String queryParamValue : queryParamValues) {
                if (containsAnyASCIIControlCharacter(queryParamValue)) {
                    LOGGER.debugf("Request with invalid query parameter value is blocked");
                    throw new BadRequestException("Blocking invalid query parameter value");
                }
            }
        }
    }

    /**
     * 判定输入是否含不安全 ASCII 控制字符。
     * <p>不安全字符包括 NUL（破坏 UTF-8 编码）、ESC（ANSI 终端异常）及除 TAB/LF/CR 外所有低于 0x20 的字符。</p>
     * Unsafe character values we can safely assume is a bad request:
     * NUL	U+0000	Breaks encoding (esp. UTF-8)
     * ESC  U+001B  Can lead to strange behavior in ANSI terminals
     * ... or any other character below 0x20 excluding TAB (09) or LF/CR (0A/0D).
     *
     * @param input the value to check if contains unsafe characters
     * @return true if the input contains at least one of the unsafe characters
     */
    private boolean containsAnyASCIIControlCharacter(String input) {
        if (input == null) {
            return false;
        }
        CharacterIterator it = new StringCharacterIterator(input);
        while (true) {
            char c = it.current();
            if (c == CharacterIterator.DONE) {
                break;
            } else {
                if (c < 32) {
                    switch (c) {
                        case 0x09: // 制表符 TAB
                        case 0x0A: // 换行 LF
                        case 0x0D: // 回车 CR
                            break;
                        default:
                            return true;
                    }
                }
                it.next();
            }
        }
        return false;
    }
}
