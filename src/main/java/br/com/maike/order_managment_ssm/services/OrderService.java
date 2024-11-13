package br.com.maike.order_managment_ssm.services;

import br.com.maike.order_managment_ssm.enums.OrderEvents;
import br.com.maike.order_managment_ssm.enums.OrderStates;
import br.com.maike.order_managment_ssm.models.Order;
import br.com.maike.order_managment_ssm.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class OrderService {

    public static String HEADER_KEY_ORDEM = "order";


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

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Transactional
    public void processOrderAsync(Long orderId, OrderEvents event) {
        System.out.println("Process order: " + orderId);
        Order order = getOrder(orderId);

        stateMachine.stopReactively().subscribe();

        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(access -> {
                    access.resetStateMachineReactively(
                            new DefaultStateMachineContext<>(order.getState(), null, null, null)
                    ).subscribe();
                });

        stateMachine.startReactively().subscribe();

        stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(event).setHeader(HEADER_KEY_ORDEM, order).build()))
                .subscribe(result -> {
                    if (result.getResultType().equals(StateMachineEventResult.ResultType.ACCEPTED)) {
                        order.setState(stateMachine.getState().getId());
                        orderRepository.save(order);
                    }
                    System.out.println(result.getResultType());
                });

        System.out.println("Final state " + stateMachine.getState().getId());
        stateMachine.stopReactively().subscribe();
    }

    @Transactional
    public String processOrder(Long orderId, OrderEvents event) {
        System.out.println("Process order: " + orderId);
        Order order = getOrder(orderId);

        stateMachine.stopReactively().block();

        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(access -> {
                    access.resetStateMachineReactively(
                            new DefaultStateMachineContext<>(order.getState(), null, null, null)
                    ).block();
                });

        stateMachine.startReactively().block();

        var sendEvent = Mono.just(MessageBuilder.withPayload(event).setHeader(HEADER_KEY_ORDEM, order).build());

        StateMachineEventResult<OrderStates, OrderEvents> result = stateMachine.sendEvent(sendEvent).blockFirst();

        StateMachineEventResult.ResultType resultType = Objects.nonNull(result) ? result.getResultType() : StateMachineEventResult.ResultType.DEFERRED;

        if (resultType.equals(StateMachineEventResult.ResultType.ACCEPTED)) {
            order.setState(stateMachine.getState().getId());
            orderRepository.save(order);
        }

        System.out.println("ResultType " + resultType);
        System.out.println("Final state " + stateMachine.getState().getId());
        stateMachine.stopReactively().block();

        return resultType.toString();
    }
}
