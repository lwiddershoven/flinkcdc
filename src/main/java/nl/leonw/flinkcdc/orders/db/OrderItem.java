package nl.leonw.flinkcdc.orders.db;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Data
@Entity
@Table(name="items")
public class OrderItem {

    @Id
    @UuidGenerator
    private UUID id;

    private String productId;
}
