// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

// card 包扫描辅助函数，用于 cards 表行与结构体映射。
package card

import (
	"github.com/drone/drone/store/shared/db"
)

// toParams 将 card 结构体转为命名查询参数字典。
func toParams(card *card) (map[string]interface{}, error) {
	return map[string]interface{}{
		"card_id":   card.Id,
		"card_data": card.Data,
	}, nil
}

// scanRow 从 sql.Row 扫描列值写入 card 结构体。
func scanRow(scanner db.Scanner, dst *card) error {
	err := scanner.Scan(
		&dst.Id,
		&dst.Data,
	)
	if err != nil {
		return err
	}
	return nil
}
