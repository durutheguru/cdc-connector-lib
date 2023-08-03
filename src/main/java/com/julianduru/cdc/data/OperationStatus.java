package com.julianduru.cdc.data;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;


@Data
public class OperationStatus {


    private Value status;


    private String statusMessage;


    public OperationStatus() { }


    public OperationStatus(Value status) {
        this(status, status.getDefaultMessage());
    }




    public OperationStatus(Value status, String statusMessage) {
        this.status = status;
        this.statusMessage = statusMessage;
    }


    public static OperationStatus success() {
        return new OperationStatus(Value.SUCCESSFUL);
    }


    public static OperationStatus success(String message) {
        return new OperationStatus(Value.SUCCESSFUL, message);
    }


    public static OperationStatus failure() {
        return new OperationStatus(Value.FAILED);
    }


    public static OperationStatus failure(String message) {
        return new OperationStatus(Value.FAILED, message);
    }


    public static OperationStatus pending() {
        return new OperationStatus(Value.PENDING);
    }


    public static OperationStatus pending(String message) {
        return new OperationStatus(Value.PENDING, message);
    }


    public static OperationStatus inProgress(String message) {
        return new OperationStatus(Value.IN_PROGRESS, message);
    }


    public boolean is(Value value) {
        return this.status == value;
    }


    @JsonIgnore
    public boolean isSuccessful() {
        return is(Value.SUCCESSFUL);
    }


    @JsonIgnore
    public boolean isFinal() {
        return isSuccessful() || isInvalid();
    }


    @JsonIgnore
    public boolean isFailed() {
        return is(Value.FAILED);
    }


    @JsonIgnore
    public boolean isInvalid() {
        return is(Value.INVALID);
    }


    @Getter
    public enum Value {

        PENDING("Pending"), // message processing has not been attempted

        IN_PROGRESS("In Progress"), // message processing is currently ongoing

        SUCCESSFUL("Successful"), // message processing was successful

        FAILED("Failed"), // message processing failed

        INVALID("Invalid"); // message is invalid and cannot be processed


        private final String defaultMessage;


        Value(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }


        public boolean isRetryable() {
            return this == PENDING || this == FAILED;
        }


    }


}
