package nl.leonw.flinkcdc.orders.db;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getPricePerItemExVatCents() {
        return pricePerItemExVatCents;
    }

    public void setPricePerItemExVatCents(long pricePerItemExVatCents) {
        this.pricePerItemExVatCents = pricePerItemExVatCents;
    }

    public long getVatPerItemCents() {
        return vatPerItemCents;
    }

    public void setVatPerItemCents(long vatPerItemCents) {
        this.vatPerItemCents = vatPerItemCents;
    }

    public AuditMetadata getAuditMetaData() {
        return auditMetaData;
    }

    public void setAuditMetaData(AuditMetadata auditMetaData) {
        this.auditMetaData = auditMetaData;
    }
}
