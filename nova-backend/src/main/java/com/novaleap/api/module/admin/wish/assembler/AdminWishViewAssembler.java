package com.novaleap.api.module.admin.wish.assembler;

import com.novaleap.api.entity.Wish;
import com.novaleap.api.module.admin.wish.vo.AdminWishVO;

public final class AdminWishViewAssembler {

    private AdminWishViewAssembler() {
    }

    public static AdminWishVO toVO(Wish wish) {
        if (wish == null) {
            return null;
        }
        AdminWishVO vo = new AdminWishVO();
        vo.setId(wish.getId());
        vo.setContent(wish.getContent());
        vo.setEmotion(wish.getEmotion());
        vo.setColor(wish.getColor());
        vo.setCity(wish.getCity());
        vo.setPosX(wish.getPosX());
        vo.setPosY(wish.getPosY());
        vo.setFloatSpeed(wish.getFloatSpeed());
        vo.setStatus(wish.getStatus());
        vo.setCreatedAt(wish.getCreatedAt());
        return vo;
    }
}
