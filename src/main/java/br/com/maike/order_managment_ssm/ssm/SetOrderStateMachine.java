package br.com.maike.order_managment_ssm.ssm;

import br.com.maike.order_managment_ssm.enums.OrderEvents;
import br.com.maike.order_managment_ssm.enums.OrderStates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Component;

@Component
public class SetOrderStateMachine {

    @Autowired
    private StateMachine<OrderStates, OrderEvents> stateMachine;

    public boolean setState(OrderStates orderStates) {
        if (orderStates.equals(OrderStates.NEW)) {
            return true;
        }

        stateMachine.stopReactively().block();
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(access -> {
                    access.resetStateMachineReactively(
                            new DefaultStateMachineContext<>(orderStates, null, null, null)
                    ).block();
                });
        stateMachine.startReactively().block();
        return stateMachine.getState().getId().equals(orderStates);
    }
}
