package com.darkbladedev.exceptions;

public class FeatureDisabledException extends RuntimeException {

    public FeatureDisabledException() {
        super("The feature is disabled");
    }

    public FeatureDisabledException(String feature) {
        super("The feature " + feature + " is disabled");
    }
}
