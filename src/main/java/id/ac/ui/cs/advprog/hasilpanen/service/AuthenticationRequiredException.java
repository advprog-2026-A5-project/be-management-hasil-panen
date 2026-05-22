package id.ac.ui.cs.advprog.hasilpanen.service;

public class AuthenticationRequiredException extends RuntimeException {

    public AuthenticationRequiredException(String message) {
        super(message);
    }
}
