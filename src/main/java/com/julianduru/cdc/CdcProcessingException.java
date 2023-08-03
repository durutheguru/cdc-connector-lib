package com.julianduru.cdc;

import lombok.Getter;

/**
 * created by Julian Duru on 23/05/2023
 */
@Getter
public class CdcProcessingException extends RuntimeException {

    private CdcMessage cdcMessage;

    public CdcProcessingException(CdcMessage cdcMessage) {
        this.cdcMessage = cdcMessage;
    }

    public CdcProcessingException(String message, CdcMessage cdcMessage) {
        super(message);
        this.cdcMessage = cdcMessage;
    }

    public CdcProcessingException(String message, Throwable cause, CdcMessage cdcMessage) {
        super(message, cause);
        this.cdcMessage = cdcMessage;
    }

    public CdcProcessingException(Throwable cause, CdcMessage cdcMessage) {
        super(cause);
        this.cdcMessage = cdcMessage;
    }

    public CdcProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, CdcMessage cdcMessage) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.cdcMessage = cdcMessage;
    }


}
