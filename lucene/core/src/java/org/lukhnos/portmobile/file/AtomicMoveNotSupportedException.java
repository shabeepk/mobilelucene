package org.lukhnos.portmobile.file;

import java.io.IOException;

public class AtomicMoveNotSupportedException extends IOException {
    public AtomicMoveNotSupportedException(String source, String target, String reason)
    {
        super(String.format("Atomic move not supported from %s to %s, reason: %s.", source, target, reason));
    }
}
