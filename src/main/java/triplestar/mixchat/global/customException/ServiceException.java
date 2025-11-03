package triplestar.mixchat.global.customException;

import lombok.Getter;

@Getter
public abstract class ServiceException extends RuntimeException {

    private final int statusCode;
    private final String message;

    protected ServiceException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.message = message;
    }
}
