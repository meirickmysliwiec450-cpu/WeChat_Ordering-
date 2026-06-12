package com.wechat.ordering.mapper;

import com.wechat.ordering.entity.Admin;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface AdminMapper {
    List<Admin> selectAll();
    Admin selectById(Long id);
    Admin selectByUsername(@Param("username") String username);
    int insert(Admin admin);
    int update(Admin admin);
    int deleteById(Long id);
}
