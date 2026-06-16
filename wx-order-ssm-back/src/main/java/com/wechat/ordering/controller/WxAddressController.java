package com.wechat.ordering.controller;

import com.wechat.ordering.entity.Address;
import com.wechat.ordering.service.WxAddressService;
import com.wechat.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/wx/address")
public class WxAddressController {

    @Autowired
    private WxAddressService wxAddressService;

    @GetMapping
    public Result<List<Address>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(wxAddressService.list(userId));
    }

    @PostMapping
    public Result<?> add(HttpServletRequest request, @RequestBody Address address) {
        Long userId = (Long) request.getAttribute("userId");
        wxAddressService.add(userId, address);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<?> update(HttpServletRequest request, @PathVariable Long id, @RequestBody Address address) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            wxAddressService.update(userId, id, address);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            wxAddressService.delete(userId, id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/default")
    public Result<?> setDefault(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            wxAddressService.setDefault(userId, id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
