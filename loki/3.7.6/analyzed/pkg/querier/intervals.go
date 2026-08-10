package querier

// querier 包 intervals 根据 IngesterQueryStoreMaxLookback 与 QueryIngestersWithin 将查询时间轴拆分为 ingester 与 store 两段区间。

import "time"

type QueryInterval struct {
	start, end time.Time
}

func BuildQueryIntervalsWithLookback(cfg Config, queryStart, queryEnd time.Time, queryIngestersWithin time.Duration) (*QueryInterval, *QueryInterval) {
	// limitQueryInterval is a flag for whether store queries should be limited to start time of ingester queries.
	limitQueryInterval := cfg.IngesterQueryStoreMaxLookback != 0

	// ingesterMLB having -1 means query ingester for whole duration.
	ingesterMLB := calculateIngesterMaxLookbackPeriod(cfg, queryIngestersWithin)

	// query ingester for whole duration.
	if ingesterMLB == -1 {
		i := &QueryInterval{
			start: queryStart,
			end:   queryEnd,
		}

		if limitQueryInterval {
			// query only ingesters.
			return i, nil
		}

		// query both stores and ingesters without limiting the query interval.
		return i, i
	}

	ingesterQueryWithinRange := isWithinIngesterMaxLookbackPeriod(ingesterMLB, queryEnd)

	// see if there is an overlap between ingester query interval and actual query interval, if not just do the store query.
	if !ingesterQueryWithinRange {
		return nil, &QueryInterval{
			start: queryStart,
			end:   queryEnd,
		}
	}

	ingesterOldestStartTime := time.Now().Add(-ingesterMLB)

	// if there is an overlap and we are not limiting the query interval then do both store and ingester query for whole query interval.
	if !limitQueryInterval {
		i := &QueryInterval{
			start: queryStart,
			end:   queryEnd,
		}
		return i, i
	}

	// since we are limiting the query interval, check if the query touches just the ingesters, if yes then query just the ingesters.
	if ingesterOldestStartTime.Before(queryStart) {
		return &QueryInterval{
			start: queryStart,
			end:   queryEnd,
		}, nil
	}

	// limit the start of ingester query interval to ingesterOldestStartTime.
	ingesterQueryInterval := &QueryInterval{
		start: ingesterOldestStartTime,
		end:   queryEnd,
	}

	// limit the end of ingester query interval to ingesterOldestStartTime.
	storeQueryInterval := &QueryInterval{
		start: queryStart,
		end:   ingesterOldestStartTime,
	}

	// query touches only ingester query interval so do not do store query.
	if storeQueryInterval.start.After(storeQueryInterval.end) {
		storeQueryInterval = nil
	}

	return ingesterQueryInterval, storeQueryInterval
}

// calculateIngesterMaxLookbackPeriod 优先 IngesterQueryStoreMaxLookback，否则 queryIngestersWithin；-1 表示全量查 ingester。
func calculateIngesterMaxLookbackPeriod(cfg Config, queryIngestersWithin time.Duration) time.Duration {
	mlb := time.Duration(-1)
	if cfg.IngesterQueryStoreMaxLookback != 0 {
		// IngesterQueryStoreMaxLookback takes the precedence over QueryIngestersWithin while also limiting the store query range.
		mlb = cfg.IngesterQueryStoreMaxLookback
	} else if queryIngestersWithin != 0 {
		mlb = queryIngestersWithin
	}

	return mlb
}

// isWithinIngesterMaxLookbackPeriod 判断 queryEnd 是否落在 ingester 可查回溯窗口内。
func isWithinIngesterMaxLookbackPeriod(maxLookback time.Duration, queryEnd time.Time) bool {
	// if no lookback limits are configured, always consider this within the range of the lookback period
	if maxLookback <= 0 {
		return true
	}

	// find the first instance that we would want to query the ingester from...
	ingesterOldestStartTime := time.Now().Add(-maxLookback)

	// ...and if the query range ends before that, don't query the ingester
	return queryEnd.After(ingesterOldestStartTime)
}
// limitQueryInterval 为 true 时 store 查询止于 ingester 最老可查时间点，避免重复扫描。
