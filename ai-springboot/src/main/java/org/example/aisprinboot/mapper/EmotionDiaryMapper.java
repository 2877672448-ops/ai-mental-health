package org.example.aisprinboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.aisprinboot.entity.EmotionDiary;
import org.apache.ibatis.annotations.Mapper;

/**
 * 情绪日记 Mapper 接口
 */
@Mapper
public interface EmotionDiaryMapper extends BaseMapper<EmotionDiary> {
}
