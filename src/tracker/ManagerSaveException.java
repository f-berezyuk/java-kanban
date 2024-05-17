package tracker;

import java.io.IOException;

public class ManagerSaveException extends RuntimeException {
    public ManagerSaveException(IOException e) {
        super("Unexpected exception during manager save operation. " + e.getMessage());
    }
}
