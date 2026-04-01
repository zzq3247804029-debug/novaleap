package com.novaleap.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaleap.api.common.Result;
import com.novaleap.api.module.admin.note.dto.AdminNoteSaveRequest;
import com.novaleap.api.module.admin.note.dto.AdminNoteStatusRequest;
import com.novaleap.api.module.admin.note.service.AdminNoteApplicationService;
import com.novaleap.api.module.admin.note.vo.AdminNoteDetailVO;
import com.novaleap.api.module.admin.note.vo.AdminNoteListItemVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/notes")
public class AdminNoteController {

    private final AdminNoteApplicationService adminNoteApplicationService;

    public AdminNoteController(AdminNoteApplicationService adminNoteApplicationService) {
        this.adminNoteApplicationService = adminNoteApplicationService;
    }

    @GetMapping
    public Result<Page<AdminNoteListItemVO>> getNoteList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) Integer status
    ) {
        return Result.success(adminNoteApplicationService.getNoteList(page, size, keyword, category, status));
    }

    @GetMapping("/{id}")
    public Result<AdminNoteDetailVO> getNoteDetail(@PathVariable("id") Long id) {
        return Result.success(adminNoteApplicationService.getNoteDetail(id));
    }

    @PostMapping
    public Result<AdminNoteDetailVO> createNote(@RequestBody @Valid AdminNoteSaveRequest request) {
        return Result.success(adminNoteApplicationService.createNote(request));
    }

    @PutMapping("/{id}")
    public Result<AdminNoteDetailVO> updateNote(
            @PathVariable("id") Long id,
            @RequestBody @Valid AdminNoteSaveRequest request
    ) {
        return Result.success(adminNoteApplicationService.updateNote(id, request));
    }

    @PutMapping("/{id}/status")
    public Result<AdminNoteDetailVO> updateNoteStatus(
            @PathVariable("id") Long id,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "reason", required = false) String reason,
            @RequestParam(value = "rejectReason", required = false) String rejectReason,
            @RequestBody(required = false) @Valid AdminNoteStatusRequest body
    ) {
        return Result.success(adminNoteApplicationService.updateNoteStatus(id, status, reason, rejectReason, body));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteNote(@PathVariable("id") Long id) {
        adminNoteApplicationService.deleteNote(id);
        return Result.success();
    }
}
