package nl.leonw.flinkcdc.orders.db;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name="orders")
public class Order {

    @Id
    @UuidGenerator
    private UUID id;

    private long totalCents;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<OrderItem> items;

}
