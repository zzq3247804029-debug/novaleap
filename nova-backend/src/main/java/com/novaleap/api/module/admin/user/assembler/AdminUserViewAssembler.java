package com.novaleap.api.module.admin.user.assembler;

import com.novaleap.api.entity.User;
import com.novaleap.api.module.admin.user.vo.AdminUserVO;

public final class AdminUserViewAssembler {

    private AdminUserViewAssembler() {
    }

    public static AdminUserVO toVO(User user) {
        if (user == null) {
            return null;
        }
        AdminUserVO vo = new AdminUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRole(user.getRole());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
}
