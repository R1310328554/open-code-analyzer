package com.alibaba.csp.sentinel.demo.zuul2.gateway;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.netflix.zuul.filters.FilterRegistry;
import com.netflix.zuul.filters.ZuulFilter;

/**
 * Zuul 2.x 过滤器注册服务：启动时将 Spring 容器中的 {@link ZuulFilter} 注册到 {@link FilterRegistry}。
 */
public class FiltersRegisteringService {

    private final List<ZuulFilter> filters;
    private final FilterRegistry filterRegistry;

    @Inject
    public FiltersRegisteringService(FilterRegistry filterRegistry, Set<ZuulFilter> filters) {
        this.filters = new ArrayList<>(filters);
        this.filterRegistry = filterRegistry;
    }

    public List<ZuulFilter> getFilters() {
        return filters;
    }

    @PostConstruct
    /** {@link PostConstruct} 回调：将所有过滤器按名称注册。 */
    public void initialize() {
        for (ZuulFilter filter: filters) {
            this.filterRegistry.put(filter.filterName(), filter);
        }
    }
}
