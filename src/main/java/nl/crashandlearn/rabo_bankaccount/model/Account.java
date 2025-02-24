package nl.crashandlearn.rabo_bankaccount.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

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
    @JsonIgnore
    private User user;

    @PositiveOrZero
    @Schema(description = "Current account balance in €, must be at least 0. Default value is 0",
            example = "123.01")
    private double balance;

    @OneToMany(
            mappedBy = "account",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true)
    private Set<Card> cards = new HashSet<>();

    @Override
    public int hashCode() {
        return 711 + id.intValue() * (int) balance;
    }
}
