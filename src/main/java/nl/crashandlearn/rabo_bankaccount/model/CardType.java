package nl.crashandlearn.rabo_bankaccount.model;

import lombok.Getter;

@Getter
public enum CardType {
    DEBIT_CARD(1.00),
    CREDIT_CARD(1.01);

    public final double fee;
    CardType(double fee) {
        this.fee = fee;
    }
}
