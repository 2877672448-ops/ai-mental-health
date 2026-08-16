package org.example.aisprinboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.aisprinboot.entity.User;

/**
 * 用户数据访问层
 *
 * @author PANJU
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
