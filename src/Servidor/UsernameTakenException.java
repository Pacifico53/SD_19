package Servidor;

public class UsernameTakenException extends Exception{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public UsernameTakenException(String str) {
        super(str);
    }
}
