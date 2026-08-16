package org.example.aisprinboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.aisprinboot.entity.AlertRecord;

/**
 * 危机预警记录数据访问层
 *
 * @author PANJU
 */
@Mapper
public interface AlertRecordMapper extends BaseMapper<AlertRecord> {
}
