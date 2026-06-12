package com.wechat.ordering.mapper;

import com.wechat.ordering.entity.Address;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface AddressMapper {
    List<Address> selectAll();
    List<Address> selectByUserId(@Param("userId") Long userId);
    Address selectById(Long id);
    int insert(Address address);
    int update(Address address);
    int deleteById(Long id);
}
