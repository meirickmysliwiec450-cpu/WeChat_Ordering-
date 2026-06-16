package com.wechat.ordering.service.impl;

import com.wechat.ordering.entity.Address;
import com.wechat.ordering.mapper.AddressMapper;
import com.wechat.ordering.service.WxAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WxAddressServiceImpl implements WxAddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public List<Address> list(Long userId) {
        return addressMapper.selectByUserId(userId);
    }

    @Override
    public Address getById(Long userId, Long id) {
        Address addr = addressMapper.selectById(id);
        if (addr == null || !addr.getUserId().equals(userId)) throw new RuntimeException("地址不存在");
        return addr;
    }

    @Override
    public void add(Long userId, Address address) {
        address.setUserId(userId);
        address.setCreateTime(LocalDateTime.now());
        if (address.getIsDefault() == null) address.setIsDefault(0);
        // 如果设为默认，先取消其他默认
        if (address.getIsDefault() == 1) clearOtherDefaults(userId);
        addressMapper.insert(address);
    }

    @Override
    public void update(Long userId, Long id, Address address) {
        Address exist = addressMapper.selectById(id);
        if (exist == null || !exist.getUserId().equals(userId)) throw new RuntimeException("地址不存在");
        if (address.getReceiver() != null) exist.setReceiver(address.getReceiver());
        if (address.getPhone() != null) exist.setPhone(address.getPhone());
        if (address.getAddressDetail() != null) exist.setAddressDetail(address.getAddressDetail());
        if (address.getIsDefault() != null) {
            if (address.getIsDefault() == 1) clearOtherDefaults(userId);
            exist.setIsDefault(address.getIsDefault());
        }
        addressMapper.update(exist);
    }

    @Override
    public void delete(Long userId, Long id) {
        Address addr = addressMapper.selectById(id);
        if (addr == null || !addr.getUserId().equals(userId)) throw new RuntimeException("地址不存在");
        addressMapper.deleteById(id);
    }

    @Override
    public void setDefault(Long userId, Long id) {
        Address addr = addressMapper.selectById(id);
        if (addr == null || !addr.getUserId().equals(userId)) throw new RuntimeException("地址不存在");
        clearOtherDefaults(userId);
        addr.setIsDefault(1);
        addressMapper.update(addr);
    }

    private void clearOtherDefaults(Long userId) {
        List<Address> list = addressMapper.selectByUserId(userId);
        for (Address a : list) {
            if (a.getIsDefault() != null && a.getIsDefault() == 1) {
                a.setIsDefault(0);
                addressMapper.update(a);
            }
        }
    }
}
