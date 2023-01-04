package com.drivemetadata.network;

import com.drivemetadata.utils.DriveMetaDataLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;

public class HttpClient {
    private static DriveMetaDataLogger driveMetaDataLogger = null;
    private final int CONN_TIMEOUT = 10000; // 10 sec
    private final int READ_TIMEOUT = 60000;
    private HttpClient.DataDispatcher dispatcher;
    private String BASE_URL = "https://sdk.drivemetadata.com/";

    public HttpClient() {

    }

    /**
     * Process the http request asynchronously.
     * @param url                   the url
     * @param jsonInputString       Input string in JSON
     * @param proxyEnabled          To enable proxy
     * @param handler               the handler
     *
     */
    public void process(String url, String jsonInputString, boolean proxyEnabled, HttpResponseHandler handler, Object context) {
        driveMetaDataLogger = new DriveMetaDataLogger("com.drivemetadata.network.HttpClient");
        driveMetaDataLogger.info("Url = " + url, new Object[0]);
        driveMetaDataLogger.info("ReqJson = " + jsonInputString, new Object[0]);
        this.dispatcher = new HttpClient.DataDispatcher(url, jsonInputString,  proxyEnabled, handler, context);
        (new Thread(this.dispatcher)).start();
    }

    /**
     * Process the http request asynchronously.
     * @param url                   the url
     * @param jsonInputString       Input string in JSON
     * @param proxyEnabled          To enable proxy
     * @param handler               the handler
     *
     */
    public void processInSameThread(String url, String jsonInputString,  boolean proxyEnabled, HttpResponseHandler handler, Object context) throws UnsupportedEncodingException, IOException, JSONException {
        HttpClient.DataDispatcher dispatcher = new HttpClient.DataDispatcher(url, jsonInputString, proxyEnabled, handler, context);
        dispatcher.dispatch();
    }

    private class DataDispatcher implements Runnable {
        protected String url;
        protected String jsonInputString;
        protected HttpResponseHandler handler;
        protected Object context;
        boolean proxyEnabled;

        public DataDispatcher() {
        }

        public DataDispatcher(String url, String jsonInputString,  boolean proxyEnabled, HttpResponseHandler handler, Object context) {
            this.url = url;
            this.jsonInputString = jsonInputString;
            this.handler = handler;
            this.context = context;
            this.proxyEnabled = proxyEnabled;

        }

        private void dispatch() {
            HttpURLConnection urlConnection = null;
            try {
                String completeURL = BASE_URL + this.url;
                URL url = new URL(completeURL);
                driveMetaDataLogger.debug("completeURL = " + completeURL, new Object[0]);

                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setConnectTimeout(CONN_TIMEOUT);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                //urlConnection.setRequestProperty("Accept", "application/json");

                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                outputStreamWriter.write(jsonInputString);
                outputStreamWriter.flush();

                int responseCode = urlConnection.getResponseCode();
                driveMetaDataLogger.debug("responseCode = " + responseCode, new Object[0]);
                if(responseCode==urlConnection.HTTP_OK){
                    String strResponse = readStream(urlConnection.getInputStream());
                    driveMetaDataLogger.debug("RespJson = " + strResponse, new Object[0]);
                    this.handler.handleResponse(strResponse, this.context);
                } else {
                    driveMetaDataLogger.debug("HTTPURLCONNECTION_ERROR : "+responseCode,
                            new Object[0]);
                }

            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
                driveMetaDataLogger.debug("Req failed = " + ex.getMessage(), new Object[0]);
                driveMetaDataLogger.debug("Req failed = " + ex.getStackTrace(), new Object[0]);
                driveMetaDataLogger.debug("Req failed = " + ex.toString(), new Object[0]);
            } catch (IOException ex) {
                ex.printStackTrace();
                driveMetaDataLogger.debug("Req failed = " + ex.getMessage(), new Object[0]);
                driveMetaDataLogger.debug("Req failed = " + ex.getStackTrace(), new Object[0]);
                driveMetaDataLogger.debug("Req failed = " + ex.toString(), new Object[0]);
            } finally {
                urlConnection.disconnect();
            }

        }

        public void run() {
            this.dispatch();
        }

        private String readStream(InputStream inputStream) {
            StringBuilder response = new StringBuilder();

            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                try {
                    String responseLine = null;

                    while(true) {
                        if ((responseLine = br.readLine()) == null) {
                            driveMetaDataLogger.debug("Response = " + response.toString(), new Object[0]);
                            System.out.println(response.toString());
                            break;
                        }

                        response.append(responseLine.trim());
                    }
                } catch (Throwable ex) {
                    try {
                        br.close();
                    } catch (Throwable e) {
                        ex.addSuppressed(e);
                    }

                    throw ex;
                }

                br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            return response.toString();
        }
    }
}

