package com.novaleap.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.novaleap.api.entity.Note;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {
}
