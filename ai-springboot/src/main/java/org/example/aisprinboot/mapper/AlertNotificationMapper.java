package org.example.aisprinboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.aisprinboot.entity.AlertNotification;

/**
 * 管理员预警通知数据访问层
 *
 * @author PANJU
 */
@Mapper
public interface AlertNotificationMapper extends BaseMapper<AlertNotification> {
}
