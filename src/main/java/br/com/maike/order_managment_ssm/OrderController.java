package br.com.maike.order_managment_ssm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("new")
    public String newOrder() {
        return orderService.newOrder();
    }

    @GetMapping("/list/all")
    List<Order> all() {
        return orderService.findAll();
    }

    @GetMapping(value = "/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE )
    public ResponseEntity<Order> getOrder(@PathVariable Long orderId) {
        Order order =  orderService.getOrder(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{orderId}/validate")
    public String validateOrder(@PathVariable Long orderId) {
        orderService.validateOrder(orderId);
        return "validate order";
    }

    @GetMapping("/{orderId}/pay")
    public String payOrder(@PathVariable Long orderId) {
        orderService.payOrder(orderId);
        return "pay order";
    }

    @GetMapping("/{orderId}/ship")
    public String shipOrder(@PathVariable Long orderId) {
        orderService.shipOrder(orderId);
        return "ship order";
    }

    @GetMapping("/{orderId}/complete")
    public String completeOrder(@PathVariable Long orderId) {
        orderService.completeOrder(orderId);
        return "complete order";
    }
}
