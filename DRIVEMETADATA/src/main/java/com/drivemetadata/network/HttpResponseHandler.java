package com.drivemetadata.network;


import java.util.HashMap;

public interface HttpResponseHandler {
    void handleResponse(String var1, Object var2);

    void handleHTTPError(int var1, HashMap<String, String> var2, Object var3);

    void handleError(Exception var1, String var2, Object var3);
}

