package br.com.maike.order_managment_ssm.dtos;

import br.com.maike.order_managment_ssm.enums.OrderEvents;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventOrderDTO {
    private Long id;
    private OrderEvents event;
}
