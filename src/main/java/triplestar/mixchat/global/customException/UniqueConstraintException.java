package triplestar.mixchat.global.customException;

public class UniqueConstraintException extends ServiceException {
    private static final int STATUS_CODE = 400;

    public UniqueConstraintException(String message) {
        super(STATUS_CODE, message);
    }
}
