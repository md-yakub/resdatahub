package com.resdatahub.knowledgegraph.validation;

public class SparqlQueryTimeoutException extends RuntimeException {

    public SparqlQueryTimeoutException(String message) {
        super(message);
    }
}
