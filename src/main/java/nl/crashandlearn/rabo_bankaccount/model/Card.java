package nl.crashandlearn.rabo_bankaccount.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name="CARD")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Card implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private Account account;

    private CardType cartType;

    @Override
    public int hashCode() {
        return id.intValue() * 7510;
    }
}
