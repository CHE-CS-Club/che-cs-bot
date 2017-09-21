package club.checs.csbot.bot;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidParameterException;

import static club.checs.csbot.bot.HttpsRequest.reqType.POST;

public class HttpsRequest {

    public enum reqType {GET, POST}
    private URL url;
    private reqType requestType;

    public HttpsRequest(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.requestType = reqType.GET;
    }

    public HttpsRequest(String url, reqType type) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.requestType = type;
    }

    public HttpsRequest(reqType type, String url, String... args) {
        // Make sure we have valid args
        if ((args.length & 1) != 0) // If number is odd
            throw new InvalidParameterException("Need even number of arguments");
        // Go through the keys/values and build the arg string with them
        StringBuilder argString = new StringBuilder();
        for (int i = 0; i < args.length; i += 2) {
            try {
                argString.append((i == 0) ? "?" : "&");
                argString.append(URLEncoder.encode(args[i], "UTF-8")).append("=");
                argString.append(URLEncoder.encode(args[i + 1], "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        System.out.println("URL: " + url + argString.toString());
        // Build the request and set it
        try {
            this.url = new URL(url + argString.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.requestType = type;
    }

    public String getRawResponse() {
        switch (requestType) {
            case GET : {
                try {
                    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                    return readBufferedInput(conn.getInputStream());

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
            case POST : {
                try {
                    // Setup connection
                    URL baseUrl = new URL(url.getProtocol() + "://" + url.getAuthority() + url.getPath()); // Remove args for initial connection
                    HttpsURLConnection conn = (HttpsURLConnection) baseUrl.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    // Setup and send the query portion of the url
                    byte[] query = url.getQuery().getBytes();
                    conn.setFixedLengthStreamingMode(query.length);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                    conn.connect();
                    try(OutputStream os = conn.getOutputStream()) {
                        os.write(query);
                    }

                    // Read the buffered input stream
                    return readBufferedInput(conn.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    String readBufferedInput (InputStream stream) throws IOException {
        BufferedReader br =
                new BufferedReader(
                        new InputStreamReader(stream));

        String input, output = "";

        while ((input = br.readLine()) != null)
            output += input;

        br.close();
        stream.close();
        return output;
    }


}
