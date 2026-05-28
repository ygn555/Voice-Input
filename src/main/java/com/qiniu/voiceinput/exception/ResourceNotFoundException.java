package com.qiniu.voiceinput.exception;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(404, message);
    }

    public static ResourceNotFoundException audioFile(Long id) {
        return new ResourceNotFoundException("Audio file not found with id: " + id);
    }
}
