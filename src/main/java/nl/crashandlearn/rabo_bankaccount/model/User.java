package nl.crashandlearn.rabo_bankaccount.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(hidden = true)
@Data
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1")
    private Long id;

    private String username;

    @JsonIgnore
    private String password;

    private Role role;

    @Schema(example = "Jane")
    String firstName;

    @Schema(example = "Doe")
    String lastName;

    @Schema(example = "jane.doe@email.com")
    @Email
    String email;

    @OneToMany(
            mappedBy = "user",
            fetch = FetchType.EAGER,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<Account> accounts = new HashSet<>();

    @Override
    public int hashCode() {
        return 42 +
                id.intValue() * firstName.hashCode() +
                lastName.hashCode() * email.hashCode();
    }
}