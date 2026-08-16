package org.example.aisprinboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.aisprinboot.entity.ConsultationSession;

/**
 * 咨询会话数据访问层
 *
 * @author PANJU
 */
@Mapper
public interface ConsultationSessionMapper extends BaseMapper<ConsultationSession> {
}
