package br.com.maike.order_managment_ssm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OrderService {

    @Autowired
    private StateMachineFactory<OrderStates, OrderEvents> stateMachineFactory;
    private StateMachine<OrderStates, OrderEvents> stateMachine;

    public void newOrder(){
        initOrderSaga();
        validateOrder();
    }

    public void initOrderSaga() {
        System.out.println("Initializing order saga");
        stateMachine = stateMachineFactory.getStateMachine();
        stateMachine.startReactively().subscribe();
        System.out.println("Final state " + stateMachine.getState().getId());
    }

    public void validateOrder() {
        System.out.println("Validating order");
        stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(OrderEvents.VALIDATE).build()))
                .subscribe(result -> System.out.println(result.getResultType()));
        System.out.println("Final state " + stateMachine.getState().getId());
    }

    public void payOrder() {
        System.out.println("Pay order");
        stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(OrderEvents.PAY).build()))
                .subscribe(result -> System.out.println(result.getResultType()));
        System.out.println("Final state " + stateMachine.getState().getId());
    }

    public void shipOrder() {
        System.out.println("Shipping order");
        stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(OrderEvents.SHIP).build()))
                .subscribe(result -> System.out.println(result.getResultType()));
        System.out.println("Final state " + stateMachine.getState().getId());
    }

    public void completeOrder() {
        System.out.println("Completing order");
        stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(OrderEvents.COMPLETE).build()))
                .subscribe(result -> System.out.println(result.getResultType()));
        System.out.println("Final state " + stateMachine.getState().getId());
        stopOrderSaga();
    }

    private void stopOrderSaga() {
        System.out.println("Stopping saga..");
        stateMachine.stopReactively().subscribe();
    }


}
