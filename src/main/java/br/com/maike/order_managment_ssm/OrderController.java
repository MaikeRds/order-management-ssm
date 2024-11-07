package br.com.maike.order_managment_ssm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("new")
    public String newOrder() {
        orderService.newOrder();
        return "new order";
    }

    @GetMapping("pay")
    public String payOrder() {
        orderService.payOrder();
        return "pay order";
    }

    @GetMapping("ship")
    public String shipOrder() {
        orderService.shipOrder();
        return "ship order";
    }

    @GetMapping("complete")
    public String completeOrder() {
        orderService.completeOrder();
        return "complete order";
    }
}
