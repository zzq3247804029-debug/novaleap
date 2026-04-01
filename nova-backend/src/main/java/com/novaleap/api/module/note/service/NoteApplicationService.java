package com.novaleap.api.module.note.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaleap.api.common.exception.NotFoundException;
import com.novaleap.api.common.exception.UnauthorizedException;
import com.novaleap.api.entity.Note;
import com.novaleap.api.entity.NoteComment;
import com.novaleap.api.entity.NoteLike;
import com.novaleap.api.entity.User;
import com.novaleap.api.mapper.NoteMapper;
import com.novaleap.api.mapper.NoteLikeMapper;
import com.novaleap.api.mapper.NoteCommentMapper;
import com.novaleap.api.dto.NoteCommentRequest;
import com.novaleap.api.dto.NoteCommentVO;
import com.novaleap.api.dto.NoteLikeVO;
import com.novaleap.api.module.note.assembler.NoteViewAssembler;
import com.novaleap.api.module.note.dto.NoteCreateRequest;
import com.novaleap.api.module.note.dto.NoteUpdateRequest;
import com.novaleap.api.module.note.vo.NoteDetailVO;
import com.novaleap.api.module.note.vo.NoteListItemVO;
import com.novaleap.api.module.system.security.ActorIdentity;
import com.novaleap.api.module.system.security.CurrentUserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoteApplicationService {

    private final NoteMapper noteMapper;
    private final NoteLikeMapper noteLikeMapper;
    private final NoteCommentMapper noteCommentMapper;
    private final CurrentUserService currentUserService;

    public NoteApplicationService(
            NoteMapper noteMapper,
            NoteLikeMapper noteLikeMapper,
            NoteCommentMapper noteCommentMapper,
            CurrentUserService currentUserService
    ) {
        this.noteMapper = noteMapper;
        this.noteLikeMapper = noteLikeMapper;
        this.noteCommentMapper = noteCommentMapper;
        this.currentUserService = currentUserService;
    }

    public Page<NoteListItemVO> getNoteList(
            Integer page,
            Integer size,
            String keyword,
            String category,
            Authentication authentication
    ) {
        Page<Note> pageParam = new Page<>(page == null ? 1 : page, size == null ? 10 : size);
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getStatus, 1);
        
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Note::getTitle, keyword.trim())
                    .or()
                    .like(Note::getAuthor, keyword.trim()));
        }
        if (category != null && !category.isBlank()) {
            wrapper.eq(Note::getCategory, category);
        }
        wrapper.orderByDesc(Note::getCreatedAt);

        Page<Note> result = noteMapper.selectPage(pageParam, wrapper);
        ActorIdentity actor = currentUserService.resolveActor(authentication);
        
        List<NoteListItemVO> records = result.getRecords().stream()
                .map(note -> {
                    enrichNoteMetadata(note, actor);
                    return NoteViewAssembler.toListItemVO(note);
                })
                .collect(Collectors.toList());
                
        Page<NoteListItemVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(records);
        return voPage;
    }

    public Page<NoteListItemVO> getMine(Authentication authentication, Integer page, Integer size, Integer status) {
        User user = currentUserService.requireDatabaseUser(authentication, "请先登录后查看我的手记");
        Page<Note> pageParam = new Page<>(page == null ? 1 : page, size == null ? 20 : size);
        
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, user.getId());
        if (status != null) {
            wrapper.eq(Note::getStatus, status);
        }
        wrapper.orderByDesc(Note::getCreatedAt);

        Page<Note> result = noteMapper.selectPage(pageParam, wrapper);
        ActorIdentity actor = new ActorIdentity("user", user.getUsername());
        
        List<NoteListItemVO> records = result.getRecords().stream()
                .map(note -> {
                    enrichNoteMetadata(note, actor);
                    return NoteViewAssembler.toListItemVO(note);
                })
                .collect(Collectors.toList());

        Page<NoteListItemVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(records);
        return voPage;
    }

    public NoteDetailVO getNoteDetail(Long id, Authentication authentication) {
        Note note = noteMapper.selectById(id);
        if (note == null) {
            throw new NotFoundException("手记不存在");
        }
        ActorIdentity actor = currentUserService.resolveActor(authentication);
        enrichNoteMetadata(note, actor);
        return NoteViewAssembler.toDetailVO(note);
    }

    @Transactional
    public NoteDetailVO increaseView(Long id, Authentication authentication) {
        Note note = noteMapper.selectById(id);
        if (note == null) {
            throw new NotFoundException("手记不存在");
        }
        note.setViewCount((note.getViewCount() == null ? 0 : note.getViewCount()) + 1);
        noteMapper.updateById(note);
        
        enrichNoteMetadata(note, currentUserService.resolveActor(authentication));
        return NoteViewAssembler.toDetailVO(note);
    }

    @Transactional
    public NoteDetailVO createNote(NoteCreateRequest request, Authentication authentication) {
        User user = currentUserService.requireDatabaseUser(authentication, "游客账号不能提交手记");
        Note note = new Note();
        note.setUserId(user.getId());
        note.setAuthor(user.getNickname());
        note.setSummary(request.getSummary());
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setCategory(request.getCategory());
        note.setEmoji(request.getEmoji());
        note.setStatus(2); // 待审核
        note.setViewCount(0);
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());
        
        noteMapper.insert(note);
        return NoteViewAssembler.toDetailVO(note);
    }

    @Transactional
    public NoteDetailVO updateNote(Long id, NoteUpdateRequest request, Authentication authentication) {
        User user = currentUserService.requireDatabaseUser(authentication, "您无权操作该资源");
        Note note = noteMapper.selectById(id);
        if (note == null || !note.getUserId().equals(user.getId())) {
            throw new NotFoundException("手记不存在或无权操作");
        }
        
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setSummary(request.getSummary());
        note.setCategory(request.getCategory());
        note.setEmoji(request.getEmoji());
        note.setStatus(2); // 更新后重新进入审核
        note.setUpdatedAt(LocalDateTime.now());
        
        noteMapper.updateById(note);
        return NoteViewAssembler.toDetailVO(note);
    }

    @Transactional
    public NoteLikeVO toggleLike(Long id, Authentication authentication) {
        ActorIdentity actor = currentUserService.resolveActor(authentication);
        if (actor.isEmpty()) {
            throw new UnauthorizedException("请先登录或初始化身份");
        }
        
        Note note = noteMapper.selectById(id);
        if (note == null) {
            throw new NotFoundException("手记不存在");
        }
        
        LambdaQueryWrapper<NoteLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteLike::getNoteId, id)
                .eq(NoteLike::getActorType, actor.type())
                .eq(NoteLike::getActorId, actor.id());
        NoteLike existing = noteLikeMapper.selectOne(wrapper);
        
        boolean liked;
        if (existing != null) {
            noteLikeMapper.deleteById(existing.getId());
            liked = false;
        } else {
            NoteLike nl = new NoteLike();
            nl.setNoteId(id);
            nl.setActorType(actor.type());
            nl.setActorId(actor.id());
            nl.setCreatedAt(LocalDateTime.now());
            noteLikeMapper.insert(nl);
            liked = true;
        }
        
        Long count = countLikes(id);
        return new NoteLikeVO(id, liked, count);
    }

    public List<NoteCommentVO> getComments(Long id, Authentication authentication) {
        LambdaQueryWrapper<NoteComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteComment::getNoteId, id).orderByDesc(NoteComment::getCreatedAt);
        List<NoteComment> comments = noteCommentMapper.selectList(wrapper);
        return comments.stream().map(c -> {
            NoteCommentVO vo = new NoteCommentVO();
            vo.setId(c.getId());
            vo.setNoteId(c.getNoteId());
            vo.setContent(c.getContent());
            vo.setNickname(c.getNickname());
            vo.setUsername(c.getUsername());
            vo.setUserId(c.getUserId());
            vo.setCreatedAt(c.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
    }

    @Transactional
    public NoteCommentVO addComment(Long id, NoteCommentRequest request, Authentication authentication) {
        User user = currentUserService.requireDatabaseUser(authentication, "游客账号不能提交评论");
        NoteComment comment = new NoteComment();
        comment.setNoteId(id);
        comment.setUserId(user.getId());
        comment.setNickname(user.getNickname());
        comment.setUsername(user.getUsername());
        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        
        noteCommentMapper.insert(comment);
        
        NoteCommentVO vo = new NoteCommentVO();
        vo.setId(comment.getId());
        vo.setNoteId(comment.getNoteId());
        vo.setContent(comment.getContent());
        vo.setNickname(comment.getNickname());
        vo.setUsername(comment.getUsername());
        vo.setUserId(comment.getUserId());
        vo.setCreatedAt(comment.getCreatedAt());
        return vo;
    }

    @Transactional
    public void deleteNote(Long id, Authentication authentication) {
        User user = currentUserService.requireDatabaseUser(authentication, "您无权操作该资源");
        Note note = noteMapper.selectById(id);
        if (note == null || !note.getUserId().equals(user.getId())) {
            throw new NotFoundException("手记不存在或无权操作");
        }
        noteMapper.deleteById(id);
    }

    private void enrichNoteMetadata(Note note, ActorIdentity actor) {
        if (note == null) return;
        note.setLikeCount(countLikes(note.getId()));
        note.setCommentCount(countComments(note.getId()));
        if (actor != null && !actor.isEmpty()) {
            LambdaQueryWrapper<NoteLike> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(NoteLike::getNoteId, note.getId())
                    .eq(NoteLike::getActorType, actor.type())
                    .eq(NoteLike::getActorId, actor.id());
            note.setLikedByMe(noteLikeMapper.selectCount(wrapper) > 0);
        } else {
            note.setLikedByMe(false);
        }
    }

    private Long countLikes(Long noteId) {
        LambdaQueryWrapper<NoteLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteLike::getNoteId, noteId);
        return noteLikeMapper.selectCount(wrapper);
    }

    private Long countComments(Long noteId) {
        LambdaQueryWrapper<NoteComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteComment::getNoteId, noteId);
        return noteCommentMapper.selectCount(wrapper);
    }
}
