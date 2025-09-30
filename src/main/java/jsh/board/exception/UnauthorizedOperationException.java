package jsh.board.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedOperationException extends ApplicationException {

    public UnauthorizedOperationException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
