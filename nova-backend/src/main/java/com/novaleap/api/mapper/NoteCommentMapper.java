package com.novaleap.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.novaleap.api.entity.NoteComment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoteCommentMapper extends BaseMapper<NoteComment> {
}

