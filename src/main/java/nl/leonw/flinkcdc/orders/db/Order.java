package nl.leonw.flinkcdc.orders.db;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Set;
import java.util.UUID;

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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getDeliveryAddressId() {
        return deliveryAddressId;
    }

    public void setDeliveryAddressId(String deliveryAddressId) {
        this.deliveryAddressId = deliveryAddressId;
    }

    public long getTotalPriceExVatCents() {
        return totalPriceExVatCents;
    }

    public void setTotalPriceExVatCents(long totalPriceExVatCents) {
        this.totalPriceExVatCents = totalPriceExVatCents;
    }

    public long getTotalVatCents() {
        return totalVatCents;
    }

    public void setTotalVatCents(long totalVatCents) {
        this.totalVatCents = totalVatCents;
    }

    public Set<OrderItem> getItems() {
        return items;
    }

    public void setItems(Set<OrderItem> items) {
        this.items = items;
    }

    public AuditMetadata getAuditMetaData() {
        return auditMetaData;
    }

    public void setAuditMetaData(AuditMetadata auditMetaData) {
        this.auditMetaData = auditMetaData;
    }
}
