package com.wechat.ordering.service;

import com.wechat.ordering.entity.Address;
import java.util.List;

public interface WxAddressService {
    List<Address> list(Long userId);
    Address getById(Long userId, Long id);
    void add(Long userId, Address address);
    void update(Long userId, Long id, Address address);
    void delete(Long userId, Long id);
    void setDefault(Long userId, Long id);
}
