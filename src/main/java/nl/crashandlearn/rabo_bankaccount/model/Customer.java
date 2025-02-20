package nl.crashandlearn.rabo_bankaccount.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.util.Set;

@Entity
@Table(name="CUSTOMER")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1234")
    private Long id;

    @Schema(example = "Jane")
    String firstName;

    @Schema(example = "Doe")
    String lastName;

    @Schema(example = "jane.doe@email.com")
    @Email
    String email;

    @ManyToOne
    @JoinColumn(name="user_id", updatable = false)
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private User user;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "customer_id")
    private Set<Account> accounts;
}
