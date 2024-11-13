# Resumo sobre o Spring State Machine (SSM)

**Spring State Machine (SSM)** é um framework da Spring que facilita a implementação de máquinas de estados no desenvolvimento de sistemas complexos. Ele é especialmente útil para modelar processos que envolvem transições entre estados e verificações condicionais baseadas em eventos. Abaixo estão os principais conceitos, benefícios e componentes fundamentais para entender e utilizar o SSM.

---

## Por que usar o Spring State Machine?

- **Gerenciamento de Fluxos Complexos**: Permite definir fluxos de trabalho (workflows) complexos em sistemas que exigem controle de estados e transições, como processos de pedidos, gestão de tarefas, automação de fluxos e outras operações com lógica baseada em eventos.
- **Reusabilidade e Manutenção**: Com o SSM, a lógica de transição de estados fica centralizada e modular, facilitando a manutenção e promovendo a reutilização de componentes.
- **Integração com o Ecossistema Spring**: O SSM se integra naturalmente com o Spring, permitindo injeção de dependências, uso de anotações como `@Component`, `@Service`, `@Configuration`, e suporte a AOP, além de fácil integração com persistência de dados, monitoramento e mensagens.

---

## Componentes Principais do SSM

1. **Estados (`State`)**:
    - Representam os possíveis **estágios ou fases** de um processo. Em um sistema de pedidos, os estados podem ser `PEDIDO_CRIADO`, `PAGAMENTO_APROVADO`, `PEDIDO_ENVIADO`, `PEDIDO_ENTREGUE`, `PEDIDO_CANCELADO`,  etc.

2. **Eventos (`Event`)**:
    - São **ações ou gatilhos** que impulsionam a transição de um estado para outro. Exemplo: o evento `APROVAR_PAGAMENTO` poderia mudar o estado de `PEDIDO_CRIADO` para `PAGAMENTO_APROVADO`.

3. **Transições (`Transition`)**:
   - Definem a mudança de um estado para outro, vinculadas a um evento específico. São configuradas com métodos como `source`, `target` e `event`.
   
   | Estado Atual          | Evento               | Próximo Estado       |
   |-----------------------|----------------------|-----------------------|
   | `PEDIDO_CRIADO`       | `APROVAR_PAGAMENTO`  | `PAGAMENTO_APROVADO` |
   | `PEDIDO_CRIADO`       | `CANCELAR_PEDIDO`    | `PEDIDO_CANCELADO`   |
   | `PAGAMENTO_APROVADO`  | `ENVIAR_PEDIDO`      | `PEDIDO_ENVIADO`     |
   | `PAGAMENTO_APROVADO`  | `CANCELAR_PEDIDO`    | `PEDIDO_CANCELADO`   |
   | `PEDIDO_ENVIADO`      | `CONFIRMAR_ENTREGA`  | `PEDIDO_ENTREGUE`    |


4. **Guards**:
    - Condicionam a execução de uma transição. Um guard é uma função de verificação que retorna `true` ou `false`. Se `false`, a transição é bloqueada. Exemplo: `APROVAR_PAGAMENTO` só ocorre se o valor do pagamento for positivo.

5. **Ações (`Action`)**:
    - Executam lógica específica ao longo de uma transição. São úteis para executar tarefas como enviar notificações, registrar logs ou alterar variáveis. Cada ação é implementada pela interface `Action<S, E>`, onde `S` e `E` representam estados e eventos.

6. **Extended State**:
    - Um repositório de dados temporários que pode ser compartilhado entre estados e transições. Usado para armazenar variáveis de controle ou valores temporários que não pertencem diretamente aos estados ou eventos, mas que influenciam no fluxo de trabalho.

---

## Exemplo Básico de Configuração

Para configurar uma máquina de estados, usamos `@EnableStateMachine` em uma classe de configuração e definimos as transições entre os estados:

```java
@Configuration
@EnableStateMachineFactory
public class OrderStateMachineConfig extends StateMachineConfigurerAdapter<OrderStates, OrderEvents> {

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

              .and()

              .withExternal()
              .source(OrderStates.PAGAMENTO_APROVADO)
              .event(OrderEvents.ENVIAR_PEDIDO)
              .target(OrderStates.PEDIDO_ENVIADO)

              .and()

              .withExternal()
              .source(OrderStates.PEDIDO_ENVIADO)
              .event(OrderEvents.CONFIRMAR_ENTREGA)
              .target(OrderStates.PEDIDO_ENTREGUE)

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
}
```
---

## Como Usar o Spring State Machine na Prática

1. **Iniciar e Enviar Eventos**:
   - Use o método `startReactively()` para iniciar a máquina de estados e `sendEvent()` para enviar um evento. Cada evento pode disparar uma transição condicional ao estado alvo.

    ```java
      @Service
      public class OrderService {
      
          public static String HEADER_ORDEM_ID = "ordemId";
      
          @Autowired
          private StateMachineFactory<OrderStates, OrderEvents> stateMachineFactory;
      
          @Autowired
          private OrderRepository orderRepository;
   
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
    ```

2. **Definir Lógica Condicional com Guards**:
   - Guards são úteis para assegurar que apenas certos critérios permitam a transição entre estados. Eles são especialmente úteis para verificar pré-condições.
   

3. **Executar Ações em Transições**:
   - Associe ações às transições para realizar tarefas específicas, como salvar logs ou atualizar um banco de dados, durante a mudança de estados.


4. **Usar Extended State para Dados Temporários**:
   - O ExtendedState é uma área de armazenamento temporário para variáveis que influenciam no fluxo, mas não são parte do estado fixo da máquina.

---

## Benefícios para a Equipe
- **Organização do Código**: O SSM permite estruturar fluxos de estados de forma modular e configurável, mantendo o código organizado e desacoplado.
- **Facilidade de Testes**: Cada transição e estado pode ser testado individualmente, facilitando a identificação e a resolução de problemas.
- **Escalabilidade e Flexibilidade**: À medida que o sistema cresce, novos estados e eventos podem ser adicionados facilmente ao modelo, sem reescrever o fluxo.

---

## Conclusão
O Spring State Machine é um framework poderoso para gerenciar fluxos de trabalho baseados em estados. Ele ajuda a equipe a modelar sistemas complexos com maior controle sobre os processos e transições, facilitando a escalabilidade e a manutenção do sistema.