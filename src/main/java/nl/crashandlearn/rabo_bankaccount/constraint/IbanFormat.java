package nl.crashandlearn.rabo_bankaccount.constraint;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.FIELD;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Constraint(validatedBy = IbanFormatValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
@Documented
public @interface IbanFormat {
    String IBAN_EXAMPLE = "NL91 ABNA 0417 1643 00";

    String message() default
            "IBAN number must be formatted correctly. Example: " + IBAN_EXAMPLE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
