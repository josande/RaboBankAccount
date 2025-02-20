package nl.crashandlearn.rabo_bankaccount.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import nl.crashandlearn.rabo_bankaccount.constraint.IbanFormat;

@Entity
@Table(name="ACCOUNT")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1234")
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id", updatable = false)
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private User user;

    @IbanFormat
    @Schema(example = IbanFormat.IBAN_EXAMPLE)
    private String iban;

    @PositiveOrZero
    @Schema(description = "Current account balance in €, must be at least 0. Default value is 0",
            example = "123.01")
    private double balance;

}
