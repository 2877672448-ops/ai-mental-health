package org.example.aisprinboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.aisprinboot.entity.ConsultationMessage;

/**
 * 咨询消息数据访问层
 *
 * @author PANJU
 */
@Mapper
public interface ConsultationMessageMapper extends BaseMapper<ConsultationMessage> {
}
