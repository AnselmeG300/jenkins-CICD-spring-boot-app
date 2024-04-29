package com.paymybuddy.paymybuddy.constants;

public class EmailValidator {
    /**
     * Strict regular expression for email validation.
     *
     * The following restrictions are imposed in the email address' local part by using this regex:
     * - It allows numeric values from 0 to 9.
     * - Both uppercase and lowercase letters from a to z are allowed.
     * - Allowed are underscore “_”, hyphen “-“, and dot “.”
     * - Dot isn't allowed at the start and end of the local part.
     * - Consecutive dots aren't allowed.
     * - For the local part, a maximum of 64 characters are allowed.
     *
     * Restrictions for the domain part in this regular expression include:
     * - It allows numeric values from 0 to 9.
     * - We allow both uppercase and lowercase letters from a to z.
     * - Hyphen “-” and dot “.” aren't allowed at the start and end of the domain part.
     * - No consecutive dots.
     *
     * Source: <a href="https://www.baeldung.com/java-email-validation-regex">Baeldung</a>.
     */
    public static String REGEX_PATTERN = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                                         + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
}
