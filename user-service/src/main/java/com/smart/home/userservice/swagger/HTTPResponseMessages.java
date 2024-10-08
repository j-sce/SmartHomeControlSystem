package com.smart.home.userservice.swagger;

public class HTTPResponseMessages {

    public static final String HTTP_200 = "Request was successful. The response includes the requested data. " +
            "See the example below for reference.";
    public static final String HTTP_201 = "Resource created successfully. The response includes the created data. " +
            "See the example below for details.";
    public static final String HTTP_204 = "Request was successful, and the resource was deleted. No content is returned.";
    public static final String HTTP_400 = "Bad Request. The input data contains validation errors, " +
            "such as missing or invalid fields.";
    public static final String HTTP_403 = "Forbidden. The client does not have permission to access the requested resource.";
    public static final String HTTP_404 = "Not Found. The requested resource could not be located.";
    public static final String HTTP_500 = "Internal Server Error. An unexpected error occurred on the server. " +
            "Check the response headers for additional details.";

}
