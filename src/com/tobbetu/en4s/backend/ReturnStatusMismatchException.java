package com.tobbetu.en4s.backend;

public class ReturnStatusMismatchException extends Exception {

    public ReturnStatusMismatchException(String reasonPhrase) {
        super(reasonPhrase);
    }

    private static final long serialVersionUID = -4085792277718626786L;

}
