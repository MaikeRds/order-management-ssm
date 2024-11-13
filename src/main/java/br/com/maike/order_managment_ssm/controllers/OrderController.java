package br.com.maike.order_managment_ssm.controllers;

import br.com.maike.order_managment_ssm.dtos.EventOrderDTO;
import br.com.maike.order_managment_ssm.dtos.OrderDTO;
import br.com.maike.order_managment_ssm.models.Order;
import br.com.maike.order_managment_ssm.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("add")
    public String addOrder() {
        return orderService.add();
    }

    @GetMapping("/listAll")
    List<Order> all() {
        return orderService.findAll();
    }

    @GetMapping(value = "/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Order> getOrder(@PathVariable Long orderId) {
        Order order = orderService.getOrder(orderId);
        return ResponseEntity.ok(order);
    }

    @PostMapping("state")
    public ResponseEntity<OrderDTO> processOrder(@RequestBody EventOrderDTO eventOrderDTO) {
        OrderDTO response = orderService.alterState(eventOrderDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/state/async")
    public ResponseEntity<Object> processOrderAsync(@RequestBody EventOrderDTO eventOrderDTO) {
        orderService.alterStateAsync(eventOrderDTO);
        return ResponseEntity.ok().build();
    }

}
