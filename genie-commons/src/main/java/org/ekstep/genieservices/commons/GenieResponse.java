package org.ekstep.genieservices.commons;

import android.util.Log;

import java.util.Arrays;
import java.util.List;

/**
 * Created on 14/4/17.
 *
 * @author shriharsh
 */
public class GenieResponse<T> {

    private String message;
    private T result;
    private boolean status;
    private List<String> errorMessages;
    private String error;


    public static GenieResponse getErrorResponse(String error, String errorMessage, String logMessage, String logTag) {
        GenieResponse response = new GenieResponse();
        response.setStatus(false);
        Log.e(logTag, logMessage);
        response.setErrorMessages(Arrays.asList(new String[]{errorMessage}));
        response.setError(error);
        return response;
    }

    public static GenieResponse getSuccessResponse(String message, String logMessage, String logTag) {
        GenieResponse response = new GenieResponse();
        response.setStatus(true);
        Log.i(logTag, logMessage);
        response.setMessage(message);
        return response;
    }

    public T getResult() {
        return this.result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean getStatus() {
        return this.status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<String> getErrorMessages() {
        return this.errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}