package jsh.board.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends ApplicationException {

    public DuplicateResourceException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
