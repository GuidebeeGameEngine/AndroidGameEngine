/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
//--------------------------------- PACKAGE ------------------------------------
package com.guidebee.game;

//--------------------------------- IMPORTS ------------------------------------

import com.guidebee.game.net.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//[------------------------------ MAIN CLASS ----------------------------------]

/**
 * Provides methods to perform networking operations, such as simple HTTP get
 * and post requests, and TCP server/client socket
 * communication.</p>
 * <p>
 * To perform an HTTP request create a {@link HttpRequest} with the HTTP method
 * (see {@link HttpMethods} for common methods) and
 * invoke {@link #sendHttpRequest(HttpRequest, HttpResponseListener)} with it
 * and a {@link HttpResponseListener}. After the HTTP
 * request was processed, the {@link HttpResponseListener} is called with a
 * {@link HttpResponse} with the HTTP response values and
 * an status code to determine if the request was successful or not.</p>
 * <p>
 * To create a TCP client socket to communicate with a remote TCP server,
 * invoke the {@link #newClientSocket(Protocol, String, int, com.guidebee.game.net.SocketHints)}
 * method. The returned {@link com.guidebee.game.net.Socket} offers an {@link InputStream}
 * and {@link OutputStream} to communicate with the end point.</p>
 * <p/>
 * To create a TCP server socket that waits for incoming connections, invoke the
 * {@link #newServerSocket(Protocol, int, com.guidebee.game.net.ServerSocketHints)} method.
 * The returned {@link com.guidebee.game.net.ServerSocket} offers an
 * {@link ServerSocket#accept(com.guidebee.game.net.SocketHints options)} method that waits for
 * an incoming connection.
 *
 * @author mzechner
 * @author noblemaster
 * @author arielsan
 */
public interface Net {

    /**
     * Process the specified {@link HttpRequest} and reports the
     * {@link HttpResponse} to the specified {@link HttpResponseListener}
     * .
     *
     * @param httpRequest          The {@link HttpRequest} to be performed.
     * @param httpResponseListener The {@link HttpResponseListener}
     *                             to call once the HTTP response is ready
     *                             to be processed. Could
     *                             be null, in that case no listener is called.
     */
    public void sendHttpRequest(HttpRequest httpRequest,
                                HttpResponseListener httpResponseListener);

    /**
     * Cancel a http request.
     * @param httpRequest
     */
    public void cancelHttpRequest(HttpRequest httpRequest);

    /**
     * Creates a new server socket on the given port, using the
     * given {@link Protocol}, waiting for incoming connections.
     *
     * @param port  the port to listen on
     * @param hints additional {@link ServerSocketHints} used to create
     *              the socket. IInput null to use the default setting provided
     *              by the system.
     * @return the {@link com.guidebee.game.net.ServerSocket}
     * @throws GameEngineRuntimeException in case the socket couldn't be opened
     */
    public ServerSocket newServerSocket(Protocol protocol, int port,
                                        ServerSocketHints hints);

    /**
     * Creates a new TCP client socket that connects to the given host and port.
     *
     * @param host  the host address
     * @param port  the port
     * @param hints additional {@link com.guidebee.game.net.SocketHints} used to create the socket.
     *              IInput null to use the default setting provided by the
     *              system.
     * @return GameEngineRuntimeException in case the socket couldn't be opened
     */
    public Socket newClientSocket(Protocol protocol, String host,
                                  int port, SocketHints hints);

    /**
     * Launches the default browser to display a URI. If the default browser
     * is not able to handle the specified URI, the
     * application registered for handling URIs of the specified type is invoked.
     * The application is determined from the protocol
     * and path of the URI.
     *
     * @param URI the URI to be opened.
     */
    public void openURI(String URI);

    /**
     * Protocol used by {@link Net#newServerSocket(Protocol,
     * int, com.guidebee.game.net.ServerSocketHints)} and
     * {@link Net#newClientSocket(Protocol, String, int, com.guidebee.game.net.SocketHints)}.
     *
     * @author mzechner
     */
    public enum Protocol {
        TCP
    }

    /**
     * HTTP response interface with methods to get the response data as a byte[],
     * a {@link String} or an {@link InputStream}.
     */
    public static interface HttpResponse {
        /**
         * Returns the data of the HTTP response as a byte[].
         * <p><b>Note</b>: This method may only be called once per response.</p>
         *
         * @return the result as a byte[] or null in case of a timeout or
         * if the operation was canceled/terminated abnormally. The
         * timeout is specified when creating the HTTP request,
         * with {@link HttpRequest#setTimeOut(int)}
         */
        byte[] getResult();

        /**
         * Returns the data of the HTTP response as a {@link String}.
         * <p><b>Note</b>: This method may only be called once per response.</p>
         *
         * @return the result as a string or null in case of a timeout or
         * if the operation was canceled/terminated abnormally. The
         * timeout is specified when creating the HTTP request,
         * with {@link HttpRequest#setTimeOut(int)}
         */
        String getResultAsString();

        /**
         * Returns the data of the HTTP response as an {@link InputStream}. <b><br>
         * Warning:</b> Do not store a reference to this InputStream outside of
         * {@link HttpResponseListener#handleHttpResponse(HttpResponse)}.
         * The underlying HTTP connection will be closed after that
         * callback finishes executing. Reading from the InputStream after it's
         * connection has been closed will lead to exception.
         *
         * @return An {@link InputStream} with the {@link HttpResponse} data.
         */
        InputStream getResultAsStream();

        /**
         * Returns the {@link HttpStatus} containing the statusCode
         * of the HTTP response.
         */
        HttpStatus getStatus();

        /**
         * Returns the value of the header with the given name as a {@link String},
         * or null if the header is not set.
         */
        String getHeader(String name);

        /**
         * Returns a Map of the headers. The keys are Strings that represent the header
         * name. Each values is a List of Strings that
         * represent the corresponding header values.
         */
        Map<String, List<String>> getHeaders();
    }

    /**
     * Provides common HTTP methods to use when creating a {@link HttpRequest}.
     * <ul>
     * <li>GET</li>
     * <li>POST</li>
     * <li>PUT</li>
     * <li>DELETE</li>
     * </ul>
     */
    public static interface HttpMethods {

        public static final String GET = "GET";
        public static final String POST = "POST";
        public static final String PUT = "PUT";
        public static final String DELETE = "DELETE";

    }

    /**
     * Listener to be able to do custom logic once the {@link HttpResponse} is ready
     * to be processed, register it with
     * {@link Net#sendHttpRequest(HttpRequest, HttpResponseListener)}.
     */
    public static interface HttpResponseListener {

        /**
         * Called when the {@link HttpRequest} has been processed and there
         * is a {@link HttpResponse} ready. Passing data to the
         * rendering thread should be done using
         * {@link Application#postRunnable(java.lang.Runnable runnable)}
         * {@link HttpResponse}
         * contains the {@link HttpStatus} and should be used to determine if the
         * request was successful or not (see more info at
         * {@link HttpStatus#getStatusCode()}). For example:
         * <p/>
         * <pre>
         *  HttpResponseListener listener = new HttpResponseListener() {
         *  	public void handleHttpResponse (HttpResponse httpResponse) {
         *  		HttpStatus status = httpResponse.getStatus();
         *  		if (status.getStatusCode() >= 200 && status.getStatusCode() < 300) {
         *  			// it was successful
         *        } else {
         *  			// do something else
         *        }
         *    }
         *  }
         * </pre>
         *
         * @param httpResponse The {@link HttpResponse} with the HTTP response values.
         */
        void handleHttpResponse(HttpResponse httpResponse);

        /**
         * Called if the {@link HttpRequest} failed because an exception when
         * processing the HTTP request, could be a timeout any
         * other reason (not an HTTP error).
         *
         * @param t If the HTTP request failed because an Exception, t encapsulates
         *          it to give more information.
         */
        void failed(Throwable t);

        void cancelled();
    }

    /**
     * Contains getters and setters for the following parameters:
     * <ul>
     * <li><strong>httpMethod:</strong> GET or POST are most common,
     * can use {@link Net.HttpMethods HttpMethods} for static
     * references</li>
     * <li><strong>url:</strong> the url</li>
     * <li><strong>headers:</strong> a map of the headers, setter
     * can be called multiple times</li>
     * <li><strong>timeout:</strong> time spent trying to connect
     * before giving up</li>
     * <li><strong>content:</strong> A string containing the data
     * to be used when processing the HTTP request.</li>
     * </ul>
     * <p/>
     * Abstracts the concept of a HTTP Request:
     * <p/>
     * <pre>
     * Map<String, String> parameters = new HashMap<String, String>();
     * parameters.put("user", "myuser");
     *
     * HttpRequest httpGet = new HttpRequest(HttpMethods.Get);
     * httpGet.setUrl("http://somewhere.net");
     * httpGet.setContent(HttpParametersUtils.convertHttpParameters(parameters));
     * ...
     * GameEngine.net.sendHttpRequest (httpGet, new HttpResponseListener() {
     * 	public void handleHttpResponse(HttpResponse httpResponse) {
     * 		status = httpResponse.getResultAsString();
     * 		//do stuff here based on response
     *    }
     *
     * 	public void failed(Throwable t) {
     * 		status = "failed";
     * 		//do stuff here based on the failed attempt
     *    }
     * });
     * </pre>
     */
    public static class HttpRequest {

        private final String httpMethod;
        private String url;
        private Map<String, String> headers;
        private int timeOut = 0;

        private String content;
        private InputStream contentStream;
        private long contentLength;

        private boolean followRedirects = true;

        /**
         * Creates a new HTTP request with the specified HTTP method,
         * see {@link HttpMethods}.
         *
         * @param httpMethod This is the HTTP method for the request,
         *                   see {@link HttpMethods}
         */
        public HttpRequest(String httpMethod) {
            this.httpMethod = httpMethod;
            this.headers = new HashMap<String, String>();
        }

        /**
         * Sets a header to this HTTP request. Headers definition could be
         * found at <a
         * href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html">
         * HTTP/1.1: Header Field Definitions</a> document.
         *
         * @param name  the name of the header.
         * @param value the value of the header.
         */
        public void setHeader(String name, String value) {
            headers.put(name, value);
        }

        /**
         * Sets the content as a stream to be used for a POST for example,
         * to transmit custom data.
         *
         * @param contentStream The stream with the content data.
         */
        public void setContent(InputStream contentStream, long contentLength) {
            this.contentStream = contentStream;
            this.contentLength = contentLength;
        }

        /**
         * Returns the timeOut of the HTTP request.
         *
         * @return the timeOut.
         */
        public int getTimeOut() {
            return timeOut;
        }

        /**
         * Sets the time to wait for the HTTP request to be processed, use
         * 0 block until it is done. The timeout is used for both
         * the timeout when establishing TCP connection, and the timeout
         * until the first byte of data is received.
         *
         * @param timeOut the number of milliseconds to wait before giving up,
         *                0 or negative to block until the operation is done
         */
        public void setTimeOut(int timeOut) {
            this.timeOut = timeOut;
        }

        /**
         * Returns the HTTP method of the HttpRequest.
         */
        public String getMethod() {
            return httpMethod;
        }

        /**
         * Returns the URL of the HTTP request.
         */
        public String getUrl() {
            return url;
        }

        /**
         * Sets the URL of the HTTP request.
         *
         * @param url The URL to set.
         */
        public void setUrl(String url) {
            this.url = url;
        }

        /**
         * Returns the content string to be used for the HTTP request.
         */
        public String getContent() {
            return content;
        }

        /**
         * Sets the content to be used in the HTTP request.
         *
         * @param content A string encoded in the corresponding
         *                Content-Encoding set in the headers,
         *                with the data to send with the
         *                HTTP request. For example, in case of HTTP GET,
         *                the content is used as the query string of the GET while on a
         *                HTTP POST it is used to send the POST data.
         */
        public void setContent(String content) {
            this.content = content;
        }

        /**
         * Returns the content stream.
         */
        public InputStream getContentStream() {
            return contentStream;
        }

        /**
         * Returns the content length in case content is a stream.
         */
        public long getContentLength() {
            return contentLength;
        }

        /**
         * Returns a Map<String, String> with the headers of the HTTP request.
         */
        public Map<String, String> getHeaders() {
            return headers;
        }

        /**
         * Returns whether 301 and 302 redirects are followed. By default true.
         * Whether to follow redirects.
         */
        public boolean getFollowRedirects() {
            return followRedirects;
        }



    }
}
