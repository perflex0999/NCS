package com.ncs.order.controller;

import com.ncs.common.api.Result;
import com.ncs.order.dto.OrderVO;
import com.ncs.order.service.OrderService;
import com.ncs.order.web.UserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/my")
    public Result<List<OrderVO>> my() {
        return Result.ok(orderService.myOrders(UserContext.get()));
    }
}
