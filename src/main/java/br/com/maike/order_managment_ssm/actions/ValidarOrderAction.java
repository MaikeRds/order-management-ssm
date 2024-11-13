package br.com.maike.order_managment_ssm.actions;

import br.com.maike.order_managment_ssm.enums.OrderEvents;
import br.com.maike.order_managment_ssm.enums.OrderStates;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import static br.com.maike.order_managment_ssm.services.OrderService.HEADER_ORDEM_ID;

@Component
public class ValidarOrderAction implements Action<OrderStates, OrderEvents> {

    @Override
    public void execute(StateContext<OrderStates, OrderEvents> context) {
        Long orderId = (Long) context.getMessageHeader(HEADER_ORDEM_ID);
        if (orderId == 1L) throw new RuntimeException("erro");
        System.out.println("Validating order " + orderId);
    }
}
