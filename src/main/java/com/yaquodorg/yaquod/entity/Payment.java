package com.yaquodorg.yaquod.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column private String paymobOrderId;

    @Column private String paymobTransactionId;

    @Column(nullable = false)
    private Timestamp createdAt;

    @Column private Timestamp paidAt;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "trip_id", referencedColumnName = "id")
    private Trip trip;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "saved_card_id", referencedColumnName = "id")
    private SavedCard savedCard;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        createdAt = new Timestamp(System.currentTimeMillis());
    }
}
