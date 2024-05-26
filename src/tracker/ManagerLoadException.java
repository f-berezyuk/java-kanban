package tracker;

import java.io.IOException;

public class ManagerLoadException extends RuntimeException {
    public ManagerLoadException(IOException e) {
        super("Unexpected exception during manager load operation. " + e.getMessage());
    }
}
