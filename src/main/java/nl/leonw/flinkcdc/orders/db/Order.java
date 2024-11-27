package nl.leonw.flinkcdc.orders.db;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @UuidGenerator
    private UUID id;

    private String customerId; // Keys to other systems should be UUID or String, not numeric.
    private String deliveryAddressId; // They don't mean anything, you don't calculate with them, be generic.

    private long totalPriceExVatCents; // Money is never ever a float or double.
    private long totalVatCents;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<OrderItem> items;

    private AuditMetadata auditMetaData = new AuditMetadata();
}
