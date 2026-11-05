package com.library.digital_library.service.service3;

public final class DownloadLimitExceededException extends SecurityException {

    private static final long serialVersionUID = 1L;

    public DownloadLimitExceededException(String message) {
        super(message);
    }
}
