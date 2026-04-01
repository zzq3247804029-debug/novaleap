package com.novaleap.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.novaleap.api.dto.WishCommentVO;
import com.novaleap.api.dto.WishLikeVO;
import com.novaleap.api.dto.WishRequest;
import com.novaleap.api.dto.WishWallItemVO;
import com.novaleap.api.entity.Wish;

import java.util.List;

public interface WishService extends IService<Wish> {
    void submit(WishRequest request, String username);

    void enqueuePendingWish(Long wishId);

    Wish getPendingWish(Long wishId);

    boolean completeAutoReview(Long wishId, String emotion, String color, double speed, int posX, int posY);

    List<Long> listPendingWishIds(int limit);

    List<WishWallItemVO> listApproved(String actorType, String actorId);

    WishLikeVO toggleLike(Long wishId, String actorType, String actorId);

    List<WishCommentVO> listComments(Long wishId, String currentUsername);

    WishCommentVO addComment(Long wishId, Long userId, String username, String nickname, String content);
}
