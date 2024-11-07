package br.com.maike.order_managment_ssm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {


    @Autowired
    private StateMachine<OrderStates, OrderEvents> stateMachine;

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public String newOrder() {
        Order order = new Order();
        order.setState(OrderStates.NEW);
        Order orderCreated = orderRepository.save(order);
        System.out.println("Create order");
      return "Create order: " + orderCreated.getId();
    }

    public Order getOrder(Long orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        return order.get();
    }

    public void validateOrder(Long orderId) {
        System.out.println("Validating order");
        Order order = getOrder(orderId);
        stateMachine.stopReactively().subscribe();
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(access -> access.resetStateMachineReactively(new DefaultStateMachineContext<>(order.getState(), null, null, null)));
        stateMachine.startReactively().subscribe();
        stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(OrderEvents.VALIDATE).setHeader("order", order).build()))
                .subscribe(result -> System.out.println(result.getResultType()));
        System.out.println("Final state " + stateMachine.getState().getId());
    }

    public void payOrder(Long orderId) {
        System.out.println("Pay order");
        Order order = getOrder(orderId);
        stateMachine.stopReactively().subscribe();
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(access -> access.resetStateMachineReactively(new DefaultStateMachineContext<>(order.getState(), null, null, null)));
        stateMachine.startReactively().subscribe();
        stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(OrderEvents.PAY).setHeader("order", order).build()))
                .subscribe(result -> System.out.println(result.getResultType()));
        System.out.println("Final state " + stateMachine.getState().getId());
    }

    public void shipOrder(Long orderId) {
        System.out.println("Shipping order");
        Order order = getOrder(orderId);
        stateMachine.stopReactively().subscribe();
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(access -> access.resetStateMachineReactively(new DefaultStateMachineContext<>(order.getState(), null, null, null)));
        stateMachine.startReactively().subscribe();
        stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(OrderEvents.SHIP).setHeader("order", order).build()))
                .subscribe(result -> System.out.println(result.getResultType()));
        System.out.println("Final state " + stateMachine.getState().getId());
    }

    public void completeOrder(Long orderId) {
        System.out.println("Completing order");
        Order order = getOrder(orderId);
        stateMachine.stopReactively().subscribe();
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(access -> access.resetStateMachineReactively(new DefaultStateMachineContext<>(order.getState(), null, null, null)));
        stateMachine.startReactively().subscribe();
        stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(OrderEvents.COMPLETE).setHeader("order", order).build()))
                .subscribe(result -> System.out.println(result.getResultType()));
        System.out.println("Final state " + stateMachine.getState().getId());
        stopOrderSaga();
    }

    private void stopOrderSaga() {
        System.out.println("Stopping saga..");
        stateMachine.stopReactively().subscribe();
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }
}
