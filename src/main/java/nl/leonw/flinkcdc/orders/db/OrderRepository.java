package nl.leonw.flinkcdc.orders.db;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends CrudRepository<Order, UUID> {
    List<Order> findAllByOrderByIdAsc(); // to guarantee the same order over reloads
}
