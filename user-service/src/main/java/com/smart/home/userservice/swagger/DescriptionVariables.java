package com.smart.home.userservice.swagger;

public class DescriptionVariables {

    public static final String MODEL_ID_RANGE = "Id must be greater than 0 and less than 9,223,372,036,854,775,808";
    public static final String NOT_BLANK = "must not be blank";
    public static final String USERNAME_SIZE = "Username must be between 1 and 50 characters";
    public static final String PASSWORD_SIZE = "Password must be between 8 and 100 characters";
    public static final String EMAIL_REGEX = "Email local part allows: " +
            "- Numeric values from 0 to 9. " +
            "- Uppercase and lowercase letters from a to z. " +
            "- Underscore “_”, hyphen “-“, and dot “.” " +
            "- Dot isn’t allowed at the start and end of the local part. " +
            "- Consecutive dots aren’t allowed. " +
            "- A maximum of 64 characters are allowed. " +
            "Email domain part allows: " +
            "- Numeric values from 0 to 9. " +
            "- Uppercase and lowercase letters from a to z. " +
            "- Hyphen “-” and dot “.” aren’t allowed at the start and end of the domain part. " +
            "- Consecutive dots aren’t allowed.";

    public static final String USER = "User Controller";
    public static final String AUTH = "Authentication Controller";

}
