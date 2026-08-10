package main

// querycomparator storage 封装 ThanOS objstore 客户端：GCS/S3 桶均加 dataobj/ 前缀，S3 需显式 AK/SK/session（占位待填）。

import (
	"context"
	"log"

	glog "github.com/go-kit/log"
	"github.com/grafana/dskit/flagext"
	"github.com/grafana/loki/v3/pkg/storage/bucket/gcs"
	"github.com/grafana/loki/v3/pkg/storage/bucket/s3"
	"github.com/thanos-io/objstore"
)

// MustS3DataobjBucket 凭证为空时 log.Fatal，endpoint 形如 s3.<region>.amazonaws.com。
// MustS3DataobjBucket creates a S3 bucket client for dataobj storage
// The access key id, secret access key, and session token are required for S3 dataobj bucket and must be provided.
// The region endpoint follows the format "s3.<aws region name>.amazonaws.com" e.g. "s3.eu-south-2.amazonaws.com".
func MustS3DataobjBucket(bucketName string, regionEndpoint string) objstore.Bucket {
	accessKeyID := ""
	secretAccessKey := ""
	sessionToken := ""

	if accessKeyID == "" || secretAccessKey == "" || sessionToken == "" {
		log.Fatal("access key id, secret access key, and session token are required for S3 dataobj bucket")
	}

	bkt, err := s3.NewBucketClient(s3.Config{
		Endpoint:        regionEndpoint,
		BucketName:      bucketName,
		AccessKeyID:     accessKeyID,
		SecretAccessKey: flagext.SecretWithValue(secretAccessKey),
		SessionToken:    flagext.SecretWithValue(sessionToken),
	}, "querycomparator", glog.NewNopLogger(), nil)
	if err != nil {
		log.Fatal(err)
	}

	prefixedBkt := objstore.NewPrefixedBucket(bkt, "dataobj")
	return prefixedBkt
}

// MustGCSDataobjBucket 使用应用默认凭证连接 GCS，objstore 前缀限定 dataobj 对象键。
// MustGCSDataobjBucket creates a GCS bucket client for dataobj storage
func MustGCSDataobjBucket(bucketName string) objstore.Bucket {
	bkt, err := gcs.NewBucketClient(context.Background(), gcs.Config{
		BucketName: bucketName,
	}, "querycomparator", glog.NewNopLogger(), nil)
	if err != nil {
		log.Fatal(err)
	}
	objBucket := objstore.NewPrefixedBucket(bkt, "dataobj")
	return objBucket
}

// MustRawGCSBucket 不加前缀，供需要直接访问桶根路径的实验性命令使用。
// MustRawGCSBucket creates a GCS bucket client for raw storage
func MustRawGCSBucket(bucketName string) objstore.Bucket {
	bkt, err := gcs.NewBucketClient(context.Background(), gcs.Config{
		BucketName: bucketName,
	}, "querycomparator", glog.NewNopLogger(), nil)
	if err != nil {
		log.Fatal(err)
	}
	return bkt
}
// NewPrefixedBucket 透明拼接键前缀，使 metastore 与 engine 共享同一 dataobj 布局约定。
