package com.wechat.ordering.controller;

import com.wechat.ordering.entity.Address;
import com.wechat.ordering.mapper.AddressMapper;
import com.wechat.ordering.service.WxOrderService;
import com.wechat.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wx/orders")
public class WxOrderController {

    @Autowired
    private WxOrderService wxOrderService;

    @Autowired
    private AddressMapper addressMapper;

    /** 提交订单 */
    @PostMapping
    public Result<Map<String, Object>> submit(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        System.out.println("========== 收到订单提交请求 ==========");
        System.out.println("请求参数：" + params);

        Long userId = (Long) request.getAttribute("userId");
        System.out.println("当前登录用户ID：" + userId);

        if (userId == null) {
            System.out.println("❌ 用户未登录！");
            return Result.error("请先登录");
        }

        String diningType = (String) params.get("diningType");
        Long addressId = null;
        Object aidObj = params.get("addressId");

        System.out.println("就餐类型：" + diningType);
        System.out.println("地址ID参数：" + aidObj);

        // 外送地址处理：优先用addressId查已有地址，没有则用文字自动创建
        if ("takeout".equals(diningType)) {
            if (aidObj != null && ((Number) aidObj).longValue() > 0) {
                addressId = ((Number) aidObj).longValue();
            } else {
                String addressText = (String) params.get("address");
                String receiver = (String) params.get("receiver");
                String phone = (String) params.get("phone");

                if (addressText != null && !addressText.trim().isEmpty()) {
                    Address newAddr = new Address();
                    newAddr.setUserId(userId);
                    newAddr.setReceiver(receiver != null && !receiver.trim().isEmpty() ? receiver.trim() : "用户");
                    newAddr.setPhone(phone != null ? phone.trim() : "");
                    newAddr.setAddressDetail(addressText.trim());
                    newAddr.setIsDefault(0);
                    newAddr.setCreateTime(LocalDateTime.now());
                    addressMapper.insert(newAddr);
                    addressId = newAddr.getId();
                    System.out.println("✓ 自动创建收货地址，ID：" + addressId + "，地址：" + addressText + "，收货人：" + newAddr.getReceiver() + "，电话：" + newAddr.getPhone());
                } else {
                    System.out.println("❌ 外送需要收货地址");
                    return Result.error("请填写收货地址");
                }
            }
        }
        // 堂食场景：无论前端传0还是不传，addressId保持null，不需要额外赋值

        String remark = (String) params.get("remark");
        try {
            Map<String, Object> result = wxOrderService.submit(userId, addressId, params);
            System.out.println("✅ 订单提交成功！返回：" + result);
            System.out.println("========== 订单处理完成 ==========");
            return Result.success(result);
        } catch (RuntimeException e) {
            System.out.println("❌ 订单提交失败：" + e.getMessage());
            e.printStackTrace();
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
        System.out.println("========== 收到订单列表请求 ==========");
        System.out.println("用户ID：" + userId);
        System.out.println("状态筛选：" + status);

        if (userId == null) {
            System.out.println("❌ 用户未登录！");
            return Result.error("请先登录");
        }

        Map<String, Object> result = wxOrderService.list(userId, page, pageSize, status);
        System.out.println("✅ 查询成功，订单数量：" + ((List<?>) result.get("list")).size());
        System.out.println("========== 订单列表查询完成 ==========");
        return Result.success(result);
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