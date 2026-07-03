package com.wechat.ordering.controller;

import com.wechat.ordering.service.WxOrderService;
import com.wechat.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/wx/orders")
public class WxOrderController {

    @Autowired
    private WxOrderService wxOrderService;

    /** 提交订单 */
    @PostMapping
    public Result<Map<String, Object>> submit(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        String diningType = (String) params.get("diningType");
        Long addressId = null;
        Object aidObj = params.get("addressId");

        // 仅外送强制校验地址ID
        if ("takeout".equals(diningType)) {
            if (aidObj == null) {
                return Result.error("请选择收货地址");
            }
            addressId = ((Number) aidObj).longValue();
            if (addressId <= 0) {
                return Result.error("请选择收货地址");
            }
        }
        // 堂食场景：无论前端传0还是不传，addressId保持null，不需要额外赋值

        String remark = (String) params.get("remark");
        try {
            return Result.success(wxOrderService.submit(userId, addressId, params));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 我的订单列表 */
    @GetMapping
    public Result<Map<String, Object>> list(HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(wxOrderService.list(userId, page, pageSize, status));
    }

    /** 订单详情 */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            return Result.success(wxOrderService.detail(userId, id));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 取消订单 */
    @PutMapping("/{id}/cancel")
    public Result<?> cancel(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            wxOrderService.cancel(userId, id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 修改订单 */
    @PutMapping("/{id}")
    public Result<?> updateOrder(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody Map<String, Object> params
    ) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            wxOrderService.updateOrder(userId, id, params);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

}
