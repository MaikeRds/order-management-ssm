package br.com.maike.order_managment_ssm.ssm;

import br.com.maike.order_managment_ssm.actions.PayOrderAction;
import br.com.maike.order_managment_ssm.actions.ShipOrderAction;
import br.com.maike.order_managment_ssm.actions.ValidarOrderAction;
import br.com.maike.order_managment_ssm.enums.OrderEvents;
import br.com.maike.order_managment_ssm.enums.OrderStates;
import br.com.maike.order_managment_ssm.guards.ExampleGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.transition.Transition;

import java.util.EnumSet;
import java.util.Objects;

@Configuration
@EnableStateMachineFactory
public class OrderStateMachineConfig extends StateMachineConfigurerAdapter<OrderStates, OrderEvents> {

    @Autowired
    private ValidarOrderAction validarOrderAction;

    @Autowired
    private PayOrderAction payOrderAction;

    @Autowired
    private ShipOrderAction shipOrderAction;

    @Autowired
    private ExampleGuard exampleGuard;

    @Override
    public void configure(StateMachineStateConfigurer<OrderStates, OrderEvents> states) throws Exception {
        states.withStates()
                .initial(OrderStates.NEW)
                .states(EnumSet.allOf(OrderStates.class))
                .end(OrderStates.COMPLETED)
                .end(OrderStates.CANCELLED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderStates, OrderEvents> transitions) throws Exception {
        transitions
                .withExternal()
                .source(OrderStates.NEW)
                .target(OrderStates.VALIDATED)
                .event(OrderEvents.VALIDATE)
                .action(validarOrderAction)
//                .guard(exampleGuard)

                .and()

                .withExternal()
                .source(OrderStates.VALIDATED)
                .target(OrderStates.PAID)
                .event(OrderEvents.PAY)
                .action(payOrderAction)

                .and()

                .withExternal()
                .source(OrderStates.PAID)
                .target(OrderStates.SHIPPED)
                .event(OrderEvents.SHIP)
                .action(shipOrderAction)

                .and()

                .withExternal()
                .source(OrderStates.SHIPPED)
                .target(OrderStates.COMPLETED)
                .event(OrderEvents.COMPLETE)

                .and()

                .withExternal()
                .source(OrderStates.VALIDATED)
                .target(OrderStates.CANCELLED)
                .event(OrderEvents.CANCEL)

                .and()

                .withExternal()
                .source(OrderStates.PAID)
                .target(OrderStates.CANCELLED)
                .event(OrderEvents.CANCEL);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<OrderStates, OrderEvents> config) throws Exception {
        config.withConfiguration()
                .autoStartup(false)
                .listener(stateMachineListener());
    }

    @Bean
    StateMachineListener<OrderStates, OrderEvents> stateMachineListener() {
        return new StateMachineListenerAdapter<>() {
            @Override
            public void transition(Transition<OrderStates, OrderEvents> transition) {
                if (Objects.nonNull(transition.getSource()))
                    System.out.println("Trasition from " + transition.getSource().getId() + " to " + transition.getTarget().getId());
            }

            @Override
            public void eventNotAccepted(Message<OrderEvents> event) {
                System.out.println("event not accepted: " + event);
            }
        };
    }

}
