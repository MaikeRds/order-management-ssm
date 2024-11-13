package br.com.maike.order_managment_ssm.dtos;

import br.com.maike.order_managment_ssm.enums.OrderStates;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private Long id;
    private OrderStates orderStates;
    private String mensagem;
}
