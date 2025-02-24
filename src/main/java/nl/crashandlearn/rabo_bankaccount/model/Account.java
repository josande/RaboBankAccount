package nl.crashandlearn.rabo_bankaccount.model;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name="ACCOUNT")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Account implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1")
    private Long id;

    @ManyToOne
    private User user;

    @PositiveOrZero
    @Schema(description = "Current account balance in €, must be at least 0. Default value is 0",
            example = "123.01")
    private double balance;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "card_id", referencedColumnName = "id")
    private Card card;

}
