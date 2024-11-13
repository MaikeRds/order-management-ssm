package br.com.maike.order_managment_ssm.controllers;

import br.com.maike.order_managment_ssm.enums.OrderEvents;
import br.com.maike.order_managment_ssm.services.OrderService;
import br.com.maike.order_managment_ssm.models.Order;
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

    @GetMapping(value = "/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Order> getOrder(@PathVariable Long orderId) {
        Order order = orderService.getOrder(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{orderId}/event/{event}")
    public ResponseEntity processOrder(@PathVariable Long orderId, @PathVariable OrderEvents event) {
        String response = orderService.processOrder(orderId, event);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}/event/{event}/async")
    public ResponseEntity processOrderAsync(@PathVariable Long orderId, @PathVariable OrderEvents event) {
        orderService.processOrderAsync(orderId, event);
        return ResponseEntity.ok().build();
    }

}
