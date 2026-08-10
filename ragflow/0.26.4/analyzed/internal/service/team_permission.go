package service

// team_permission.go 校验知识库团队共享访问权限。

import (
	"ragflow/internal/dao"
	"ragflow/internal/entity"
)

// hasKBTeamPermission 对齐 Python：owner 直接通过；team 共享需为成员租户。
// direct owner access is always allowed; otherwise the KB must be team-shared
// and the caller must be a joined normal member of the owner tenant.
func hasKBTeamPermission(kb *entity.Knowledgebase, userID string, tenantDAO *dao.TenantDAO) bool {
	if kb == nil {
		return false
	}
	if kb.TenantID == userID {
		return true
	}
	if kb.Permission != string(entity.TenantPermissionTeam) {
		return false
	}
	joinedTenants, err := tenantDAO.GetJoinedTenantsByUserID(userID)
	if err != nil {
		return false
	}
	for _, tenant := range joinedTenants {
		if tenant.TenantID == kb.TenantID {
			return true
		}
	}
	return false
}
// team_permission.go — 知识库团队共享权限校验（owner 或已加入成员租户）。
