package nl.crashandlearn.rabo_bankaccount.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

@Entity
@Table(name="CARD")
@Data
public class Card implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1")
    private Long id;

    @OneToOne(mappedBy = "card")
    private Account account;

    private CardType cartType;

}
