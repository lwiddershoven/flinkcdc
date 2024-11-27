package nl.leonw.flinkcdc.orders.db;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Data
@Entity
@Table(name = "items")
@EntityListeners(AuditingEntityListener.class)
public class OrderItem {

    @Id
    @UuidGenerator
    private UUID id;

    private String productId;

    private int quantity;
    private long pricePerItemExVatCents;
    private long vatPerItemCents;

    private AuditMetadata auditMetaData = new AuditMetadata();
}
