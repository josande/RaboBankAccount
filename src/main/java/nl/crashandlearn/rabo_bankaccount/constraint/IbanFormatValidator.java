package nl.crashandlearn.rabo_bankaccount.constraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IbanFormatValidator implements ConstraintValidator<IbanFormat, String> {

    // This is not a proper validation of an IBAN; just some example code.
    @Override
    public boolean isValid(String iban, ConstraintValidatorContext context) {
        return iban == null || iban.matches("\\b[A-Z]{2}[0-9]{2}(?:[ -]?[0-9A-Z]{4}){3}(?!(?:[ -]?[0-9]){3})(?:[ -]?[0-9]{1,2})?\\b");
    }
}
