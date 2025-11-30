package triplestar.mixchat.domain.admin.admin.exception;
import triplestar.mixchat.global.customException.ServiceException;

public class DuplicateSentenceGameException extends ServiceException {

    public DuplicateSentenceGameException() {
        super(400, "이미 등록된 문장입니다.");
    }
}