package com.darkbladedev.exceptions;

public class CustomTypeNotFoundException extends RuntimeException {

    public CustomTypeNotFoundException() {
        super("The custom type was not found in the server Registries");
    }
}
