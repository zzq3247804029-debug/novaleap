package com.novaleap.api.module.admin.note.assembler;

import com.novaleap.api.entity.Note;
import com.novaleap.api.module.admin.note.vo.AdminNoteDetailVO;
import com.novaleap.api.module.admin.note.vo.AdminNoteListItemVO;

public final class AdminNoteViewAssembler {

    private AdminNoteViewAssembler() {
    }

    public static AdminNoteListItemVO toListItemVO(Note note) {
        AdminNoteListItemVO vo = new AdminNoteListItemVO();
        fillCommon(note, vo);
        return vo;
    }

    public static AdminNoteDetailVO toDetailVO(Note note) {
        AdminNoteDetailVO vo = new AdminNoteDetailVO();
        fillCommon(note, vo);
        vo.setContent(note == null ? null : note.getContent());
        return vo;
    }

    private static void fillCommon(Note note, AdminNoteListItemVO vo) {
        if (note == null || vo == null) {
            return;
        }
        vo.setId(note.getId());
        vo.setTitle(note.getTitle());
        vo.setSummary(note.getSummary());
        vo.setCategory(note.getCategory());
        vo.setEmoji(note.getEmoji());
        vo.setAuthor(note.getAuthor());
        vo.setUserId(note.getUserId());
        vo.setViewCount(note.getViewCount());
        vo.setStatus(note.getStatus());
        vo.setRejectReason(note.getRejectReason());
        vo.setAuditSource(note.getAuditSource());
        vo.setAuditedAt(note.getAuditedAt());
        vo.setCreatedAt(note.getCreatedAt());
        vo.setUpdatedAt(note.getUpdatedAt());
    }

    private static void fillCommon(Note note, AdminNoteDetailVO vo) {
        if (note == null || vo == null) {
            return;
        }
        vo.setId(note.getId());
        vo.setTitle(note.getTitle());
        vo.setSummary(note.getSummary());
        vo.setCategory(note.getCategory());
        vo.setEmoji(note.getEmoji());
        vo.setAuthor(note.getAuthor());
        vo.setUserId(note.getUserId());
        vo.setViewCount(note.getViewCount());
        vo.setStatus(note.getStatus());
        vo.setRejectReason(note.getRejectReason());
        vo.setAuditSource(note.getAuditSource());
        vo.setAuditedAt(note.getAuditedAt());
        vo.setCreatedAt(note.getCreatedAt());
        vo.setUpdatedAt(note.getUpdatedAt());
    }
}
