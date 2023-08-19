package org.georgemalandrakis.archion.exception;

public class AuthException extends Exception{

    public AuthException(){
        super("You have no permission to access the requested resource.");
    }
}
