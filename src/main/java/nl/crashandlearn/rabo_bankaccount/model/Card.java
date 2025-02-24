package nl.crashandlearn.rabo_bankaccount.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @Schema(example = "1")
    private Long id;

    @OneToOne(mappedBy = "card", fetch = FetchType.EAGER)
    private Account account;

    private CardType cartType;

}
