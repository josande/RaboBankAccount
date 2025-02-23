package nl.crashandlearn.rabo_bankaccount.model;

public enum CardType {
    DEBIT_CART(1.00),
    CREDIT_CART(1.01);

    public final double fee;
    CardType(double fee) {
        this.fee = fee;
    }
    public double getFee() {return fee;}
}
