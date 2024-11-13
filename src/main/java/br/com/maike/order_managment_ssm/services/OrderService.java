package br.com.maike.order_managment_ssm.services;

import br.com.maike.order_managment_ssm.dtos.EventOrderDTO;
import br.com.maike.order_managment_ssm.dtos.OrderDTO;
import br.com.maike.order_managment_ssm.enums.OrderEvents;
import br.com.maike.order_managment_ssm.enums.OrderStates;
import br.com.maike.order_managment_ssm.exceptions.NotFoundException;
import br.com.maike.order_managment_ssm.models.Order;
import br.com.maike.order_managment_ssm.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class OrderService {

    public static String HEADER_ORDEM_ID = "ordemId";

    @Autowired
    private StateMachineFactory<OrderStates, OrderEvents> stateMachineFactory;

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public String add() {
        Order order = new Order();
        order.setState(OrderStates.PEDIDO_CRIADO);
        Order orderCreated = orderRepository.save(order);
        System.out.println("Create order");
        return "Create order: " + orderCreated.getId();
    }

    public Order getOrder(Long orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        return order.orElseThrow(() -> new NotFoundException(orderId));
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    /**
     * Essa rotina de alteração do estado da máquina é executada de forma asíncrona.
     *
     * @param eventOrderDTO
     * @return OrderDTO
     */
    @Transactional
    public void alterStateAsync(EventOrderDTO eventOrderDTO) {
        System.out.println("Process order: " + eventOrderDTO.getId());
        Order order = this.getOrder(eventOrderDTO.getId());
        OrderEvents event = eventOrderDTO.getEvent();
        StateMachine<OrderStates, OrderEvents> stateMachine = stateMachineFactory.getStateMachine(eventOrderDTO.getId().toString());
        stateMachine.stopReactively().subscribe();

        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(access -> {
                    access.resetStateMachineReactively(
                            new DefaultStateMachineContext<>(order.getState(), null, null, null)
                    ).subscribe();
                });

        stateMachine.startReactively().subscribe();

        stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(event).setHeader(HEADER_ORDEM_ID, order).build()))
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

    /**
     * Essa rotina de alteração do estado da máquina é executada de forma síncrona.
     *
     * @param eventOrderDTO
     * @return OrderDTO
     */
    @Transactional
    public OrderDTO alterState(EventOrderDTO eventOrderDTO) {
        System.out.println("Process order: " + eventOrderDTO.getId());
        Order order = this.getOrder(eventOrderDTO.getId());

        StateMachine<OrderStates, OrderEvents> stateMachine = stateMachineFactory.getStateMachine(eventOrderDTO.getId().toString());
        stateMachine.stopReactively().block();
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(access -> {
                    access.resetStateMachineReactively(
                            new DefaultStateMachineContext<>(order.getState(), null, null, null, null, stateMachine.getId())
                    ).block();
                });
        stateMachine.startReactively().block();

        var sendEvent = Mono.just(MessageBuilder.withPayload(eventOrderDTO.getEvent()).setHeader(HEADER_ORDEM_ID, order.getId()).build());
        StateMachineEventResult<OrderStates, OrderEvents> result = stateMachine.sendEvent(sendEvent).blockLast();
        var resultType = Objects.nonNull(result) ? result.getResultType() : StateMachineEventResult.ResultType.DEFERRED;

        if (resultType.equals(StateMachineEventResult.ResultType.ACCEPTED)) {
            order.setState(stateMachine.getState().getId());
            orderRepository.save(order);
            return OrderDTO.builder()
                    .id(order.getId())
                    .orderStates(stateMachine.getState().getId())
                    .mensagem("SUCESSO ao alterar o estado de maquina.")
                    .build();
        }

        return OrderDTO.builder()
                .id(order.getId())
                .orderStates(stateMachine.getState().getId())
                .mensagem("FALHA ao alterar o estado de maquina.")
                .build();
    }
}