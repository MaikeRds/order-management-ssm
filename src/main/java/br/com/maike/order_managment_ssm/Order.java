package br.com.maike.order_managment_ssm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity
@Table(name = "tb_order")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_order")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", columnDefinition = "VARCHAR(30)")
    private OrderStates state;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrderStates getState() {
        return state;
    }

    public void setState(OrderStates state) {
        this.state = state;
    }
}
