package com.novaleap.api.controller;

import com.novaleap.api.common.Result;
import com.novaleap.api.common.exception.ForbiddenException;
import com.novaleap.api.dto.WishCommentRequest;
import com.novaleap.api.dto.WishCommentVO;
import com.novaleap.api.dto.WishLikeRequest;
import com.novaleap.api.dto.WishLikeVO;
import com.novaleap.api.dto.WishRequest;
import com.novaleap.api.dto.WishWallItemVO;
import com.novaleap.api.entity.User;
import com.novaleap.api.module.system.security.ActorIdentity;
import com.novaleap.api.module.system.security.CurrentUserService;
import com.novaleap.api.service.WishService;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wishes")
public class WishWallController {

    private final WishService wishService;
    private final CurrentUserService currentUserService;

    public WishWallController(WishService wishService, CurrentUserService currentUserService) {
        this.wishService = wishService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public Result<List<WishWallItemVO>> getApprovedWishes(
            @RequestParam(value = "visitorId", required = false) String visitorId,
            Authentication authentication
    ) {
        ActorIdentity actor = resolveActor(authentication, visitorId);
        return Result.success(wishService.listApproved(actor.type(), actor.id()));
    }

    @PostMapping
    public Result<Void> submitWish(
            @RequestBody @Validated WishRequest request,
            Authentication authentication
    ) {
        User currentUser = currentUserService.requireDatabaseUser(authentication, "游客账号不能发布愿望");
        wishService.submit(request, currentUser.getUsername());
        return Result.success();
    }

    @PostMapping("/{wishId}/like")
    public Result<WishLikeVO> toggleLike(
            @PathVariable(value = "wishId") Long wishId,
            @RequestBody(required = false) WishLikeRequest request,
        Authentication authentication
    ) {
        if (currentUserService.isGuest(authentication)) {
            throw new ForbiddenException("游客账号不能点赞");
        }
        String visitorId = request == null ? null : request.getVisitorId();
        ActorIdentity actor = resolveActor(authentication, visitorId);
        if (actor.isEmpty()) {
            throw new IllegalArgumentException("访客标识不能为空");
        }
        return Result.success(wishService.toggleLike(wishId, actor.type(), actor.id()));
    }

    @GetMapping("/{wishId}/comments")
    public Result<List<WishCommentVO>> getComments(@PathVariable(value = "wishId") Long wishId, Authentication authentication) {
        String username = currentUserService.isAnonymous(authentication)
                ? null
                : currentUserService.current(authentication).safeUsername();
        return Result.success(wishService.listComments(wishId, username));
    }

    @PostMapping("/{wishId}/comments")
    public Result<WishCommentVO> addComment(
            @PathVariable(value = "wishId") Long wishId,
            @RequestBody @Validated WishCommentRequest request,
            Authentication authentication
    ) {
        User user = currentUserService.requireDatabaseUser(authentication, "游客账号不能评论");
        return Result.success(
                wishService.addComment(
                        wishId,
                        user.getId(),
                        user.getUsername(),
                        user.getNickname(),
                        request.getContent()
                )
        );
    }

    private ActorIdentity resolveActor(Authentication authentication, String visitorId) {
        ActorIdentity actor = currentUserService.resolveActor(authentication);
        if (!actor.isEmpty()) {
            return actor;
        }
        if (visitorId == null || visitorId.isBlank()) {
            return new ActorIdentity("", "");
        }
        return new ActorIdentity("visitor", visitorId.trim());
    }
}
