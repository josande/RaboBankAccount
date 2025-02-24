package nl.crashandlearn.rabo_bankaccount.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "AUDIT")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AuditPost implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1")
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id", updatable = false)
    @EqualsAndHashCode.Exclude
    User createdBy;

    String operation;

    String parameters;

    String result;
}
