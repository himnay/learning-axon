package com.learning.axon.chequebook.exception;

/** Thrown to trigger saga rollback during cheque-book issuance (set {@code failure=true} in aggregate). */
public class CancelIssuedChequeBookException extends RuntimeException {

    public CancelIssuedChequeBookException(String message) {
        super(message);
    }
}
