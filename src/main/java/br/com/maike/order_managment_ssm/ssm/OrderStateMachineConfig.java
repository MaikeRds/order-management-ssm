package br.com.maike.order_managment_ssm.ssm;

import br.com.maike.order_managment_ssm.actions.EnviarPedidoAction;
import br.com.maike.order_managment_ssm.actions.ConfirmarEntregaAction;
import br.com.maike.order_managment_ssm.actions.AprovarPagamentoAction;
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
    private AprovarPagamentoAction aprovarPagamentoAction;

    @Autowired
    private EnviarPedidoAction enviarPedidoAction;

    @Autowired
    private ConfirmarEntregaAction confirmarEntregaAction;

    @Autowired
    private ExampleGuard exampleGuard;

    @Override
    public void configure(StateMachineStateConfigurer<OrderStates, OrderEvents> states) throws Exception {
        states.withStates()
                .initial(OrderStates.PEDIDO_CRIADO)
                .states(EnumSet.allOf(OrderStates.class))
                .end(OrderStates.PEDIDO_CRIADO)
                .end(OrderStates.PEDIDO_CANCELADO);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderStates, OrderEvents> transitions) throws Exception {
        transitions
                .withExternal()
                .source(OrderStates.PEDIDO_CRIADO)
                .event(OrderEvents.APROVAR_PAGAMENTO)
                .target(OrderStates.PAGAMENTO_APROVADO)
                .action(aprovarPagamentoAction)
//                .guard(exampleGuard)

                .and()

                .withExternal()
                .source(OrderStates.PAGAMENTO_APROVADO)
                .event(OrderEvents.ENVIAR_PEDIDO)
                .target(OrderStates.PEDIDO_ENVIADO)
                .action(enviarPedidoAction)

                .and()

                .withExternal()
                .source(OrderStates.PEDIDO_ENVIADO)
                .event(OrderEvents.CONFIRMAR_ENTREGA)
                .target(OrderStates.PEDIDO_ENTREGUE)
                .action(confirmarEntregaAction)

                .and()

                .withExternal()
                .source(OrderStates.PAGAMENTO_APROVADO)
                .event(OrderEvents.CANCELAR_PEDIDO)
                .target(OrderStates.PEDIDO_CANCELADO)

                .and()

                .withExternal()
                .source(OrderStates.PEDIDO_CRIADO)
                .event(OrderEvents.CANCELAR_PEDIDO)
                .target(OrderStates.PEDIDO_CANCELADO);
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
