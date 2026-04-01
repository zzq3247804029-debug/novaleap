package com.novaleap.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.novaleap.api.entity.Question;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题库 Mapper 接口
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
