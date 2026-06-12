package com.wechat.ordering.mapper;

import com.wechat.ordering.entity.Banner;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface BannerMapper {
    List<Banner> selectAll();
    Banner selectById(Long id);
    List<Banner> selectByStatus(@Param("status") Integer status);
    int insert(Banner banner);
    int update(Banner banner);
    int deleteById(Long id);
}
