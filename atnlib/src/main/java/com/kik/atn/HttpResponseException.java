package com.kik.atn;


import java.io.IOException;

class HttpResponseException extends IOException {

    HttpResponseException(int statusCode) {
        super("Error Response code " + statusCode);
    }

}
