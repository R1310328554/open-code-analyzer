// Copyright The Prometheus Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// DigitalOcean 托管数据库服务发现：分页列出 DB 集群，优先使用私有连接主机作为抓取地址。

// DigitalOcean 托管数据库服务发现：分页列出 DB 集群，优先使用私有连接主机作为抓取地址。

package digitalocean

import (
	"context"
	"fmt"
	"net"
	"strconv"

	"github.com/digitalocean/godo"
	"github.com/prometheus/common/model"

	"github.com/prometheus/prometheus/discovery/targetgroup"
)

const (
	dbLabelID          = metaLabelPrefix + "db_id"
	dbLabelName        = metaLabelPrefix + "db_name"
	dbLabelEngine      = metaLabelPrefix + "db_engine"
	dbLabelVersion     = metaLabelPrefix + "db_version"
	dbLabelStatus      = metaLabelPrefix + "db_status"
	dbLabelRegion      = metaLabelPrefix + "db_region"
	dbLabelSize        = metaLabelPrefix + "db_size"
	dbLabelNumNodes    = metaLabelPrefix + "db_num_nodes"
	dbLabelHost        = metaLabelPrefix + "db_host"
	dbLabelPrivateHost = metaLabelPrefix + "db_private_host"
	dbLabelTagPrefix   = metaLabelPrefix + "db_tag_"
)

// 托管数据库发现器：通过 godo Databases API 列出集群。
type databasesDiscovery struct {
	client *godo.Client
	port   int
}

func (d *databasesDiscovery) refresh(ctx context.Context) ([]*targetgroup.Group, error) {
	tg := &targetgroup.Group{
		Source: "DigitalOcean Databases",
	}

	clusters, err := d.listClusters(ctx)
	if err != nil {
		return nil, err
	}
	for _, cluster := range clusters {
		labels := model.LabelSet{
			dbLabelID:       model.LabelValue(cluster.ID),
			dbLabelName:     model.LabelValue(cluster.Name),
			dbLabelEngine:   model.LabelValue(cluster.EngineSlug),
			dbLabelVersion:  model.LabelValue(cluster.VersionSlug),
			dbLabelStatus:   model.LabelValue(cluster.Status),
			dbLabelRegion:   model.LabelValue(cluster.RegionSlug),
			dbLabelSize:     model.LabelValue(cluster.SizeSlug),
			dbLabelNumNodes: model.LabelValue(strconv.Itoa(cluster.NumNodes)),
		}

		host := ""
		if cluster.PrivateConnection != nil {
			host = cluster.PrivateConnection.Host
			labels[dbLabelPrivateHost] = model.LabelValue(host)
		}

		if cluster.Connection != nil {
			labels[dbLabelHost] = model.LabelValue(cluster.Connection.Host)
			if host == "" {
				host = cluster.Connection.Host
			}
		}

		if host != "" {
			addr := net.JoinHostPort(host, strconv.FormatUint(uint64(d.port), 10))
			labels[model.AddressLabel] = model.LabelValue(addr)
		}

		for _, tag := range cluster.Tags {
			labels[dbLabelTagPrefix+model.LabelName(tag)] = "true"
		}

		tg.Targets = append(tg.Targets, labels)
	}
	return []*targetgroup.Group{tg}, nil
}

// 分页拉取全部数据库集群列表。
func (d *databasesDiscovery) listClusters(ctx context.Context) ([]godo.Database, error) {
	var (
		clusters []godo.Database
		opts     = &godo.ListOptions{
			Page:    1,
			PerPage: 100,
		}
	)
	for {
		paginatedClusters, resp, err := d.client.Databases.List(ctx, opts)
		if err != nil {
			return nil, fmt.Errorf("error while listing database clusters page %d: %w", opts.Page, err)
		}
		if len(paginatedClusters) == 0 {
			break
		}
		clusters = append(clusters, paginatedClusters...)

		if resp.Links != nil && !resp.Links.IsLastPage() {
			page, err := resp.Links.CurrentPage()
			if err == nil {
				opts.Page = page + 1
				continue
			}
		}

		opts.Page++
	}
	return clusters, nil
}
