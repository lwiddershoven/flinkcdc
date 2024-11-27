package nl.leonw.flinkcdc.orders.db;


import jakarta.persistence.Embeddable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

/*
 See https://docs.spring.io/spring-data/jpa/reference/auditing.html
 */
@Embeddable
public class AuditMetadata {
    @CreatedDate
    private Instant createdDate;

    @LastModifiedDate
    private Instant lastModifiedDate;
}
