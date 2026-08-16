package org.example.aisprinboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.aisprinboot.entity.AlertKeyword;

/**
 * 预警关键词数据访问层
 *
 * @author PANJU
 */
@Mapper
public interface AlertKeywordMapper extends BaseMapper<AlertKeyword> {
}
