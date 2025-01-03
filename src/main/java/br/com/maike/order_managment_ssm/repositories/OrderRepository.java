package br.com.maike.order_managment_ssm.repositories;

import br.com.maike.order_managment_ssm.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
