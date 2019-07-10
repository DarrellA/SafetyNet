package net.aucutt.mindfultaco;

import android.util.Base64;
import android.util.Log;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;
//import com.google.api.client.util.Base64;
import com.google.api.client.util.Key;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

public class OnlineVerify {

    // Please use the Google Developers Console (https://console.developers.google.com/)
    // to create a project, enable the Android Device Verification API, generate an API key
    // and add it here.
    private static final String API_KEY = "AIzaSyD7MpUx_JuO3GX_YzwAvvpvVcqrtFuRQG4";
    private static final String TAG = "SafetyNetInteractor";

    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final String URL =
            "https://www.googleapis.com/androidcheck/v1/attestations/verify?key="
                    + API_KEY;

    /**
     * Class for parsing JSON data.
     */
    public static class VerificationRequest {
        public VerificationRequest(String signedAttestation) {
            this.signedAttestation = signedAttestation;
        }

        @Key
        public String signedAttestation;
    }

    /**
     * Class for parsing JSON data.
     */
    public static class VerificationResponse {
        @Key
        public boolean isValidSignature;

        /**
         * Optional field that is only set when the server encountered an error processing the
         * request.
         */
        @Key
        public String error;
    }

    private static VerificationResponse onlineVerify(VerificationRequest request) {
        // Prepare a request to the Device Verification API and set a parser for JSON data.
        HttpRequestFactory requestFactory =
                HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request) {
                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                    }
                });
        GenericUrl url = new GenericUrl(URL);
        HttpRequest httpRequest;
        try {
            // Post the request with the verification statement to the API.
            httpRequest = requestFactory.buildPostRequest(url, new JsonHttpContent(JSON_FACTORY,
                    request));
            // Parse the returned data as a verification response.
            return httpRequest.execute().parseAs(VerificationResponse.class);
        } catch (IOException e) {
          Log.e(TAG,
                    "Failure: Network error while connecting to the Google Service " + URL + ".");
            Log.e(TAG, "Ensure that you added your API key and enabled the Android device "
                    + "verification API.");
            return null;
        }
    }

    /**
     * Extracts the data part from a JWS signature.
     */
    private static byte[] extractJwsData(String jws) {
        // The format of a JWS is:
        // <Base64url encoded header>.<Base64url encoded JSON data>.<Base64url encoded signature>
        // Split the JWS into the 3 parts and return the JSON data part.
        String[] parts = jws.split("[.]");
        if (parts.length != 3) {
            System.err.println("Failure: Illegal JWS signature format. The JWS consists of "
                    + parts.length + " parts instead of 3.");
            return null;
        }
        return Base64.decode(parts[1], Base64.DEFAULT);
    }

    private static JsonObject parseAndVerify(String signedAttestationStatment) {
        // Send the signed attestation statement to the API for verification.
        Log.d(TAG, "All the chiefs");
        VerificationRequest request = new VerificationRequest(signedAttestationStatment);
        Log.d(TAG, "get slowly stoned");
        VerificationResponse response = onlineVerify(request);
        Log.d(TAG, "giggity dink donk ");
        if (response == null) {
            return null;
        }

        if (response.error != null) {
           Log.e(TAG,
                    "Failure: The API encountered an error processing this request: "
                            + response.error);
            return null;
        }

        if (!response.isValidSignature) {
            Log.e(TAG,
                    "Failure: The cryptographic signature of the attestation statement couldn't be "
                            + "verified.");
            return null;
        }

       Log.d(TAG, "Sucessfully verified the signature of the attestation statement.");

        // The signature is valid, extract the data JSON from the JWS signature.
        byte[] data = extractJwsData(signedAttestationStatment);

        String justTheString =  new String(data);
        Log.d(TAG, "choke on this  "  +justTheString);
        // Parse and use the data JSON.
        try {
            Log.d(TAG, "chase the chinese jesus ");
            Gson gson = new Gson();
          //  JsonObject json = new JsonObject();

            JsonObject json = gson.fromJson(justTheString, JsonObject.class );
            Log.d(TAG, Boolean.toString(json.get("ctsProfileMatch").getAsBoolean()));
            return json;
        } catch(Exception fe) {
            Log.d(TAG, "chase with rocks "  + fe);
            return null;
        }

    }

    public static void process(String signedAttestationStatement) {
        Log.d(TAG,"Let's poop: " +signedAttestationStatement);
        JsonObject stmt = parseAndVerify(signedAttestationStatement);
        if (stmt == null) {
           Log.e(TAG, "Failure: Failed to parse and verify the attestation statement.");
            return;
        }

        Log.d(TAG, "The content of the attestation statement is:");

        // Nonce that was submitted as part of this request.
       // Log.d(TAG,"Nonce: " + Arrays.toString(stmt.getNonce()));
        // Timestamp of the request.
//        Log.d(TAG,"Timestamp: " + stmt.getTimestampMs() + " ms");
//
//        if (stmt.getApkPackageName() != null && stmt.getApkDigestSha256() != null) {
//            // Package name and digest of APK that submitted this request. Note that these details
//            // may be omitted if the API cannot reliably determine the package information.
//            Log.d(TAG,"APK package name: " + stmt.getApkPackageName());
//            Log.d(TAG,"APK digest SHA256: " + Arrays.toString(stmt.getApkCertificateDigestSha256()));
//        }
//        // Has the device a matching CTS profile?
//        Log.d(TAG,"CTS profile match: " + stmt.isCtsProfileMatch());
//        // Has the device passed CTS (but the profile could not be verified on the server)?
//        Log.d(TAG,"Basic integrity match: " + stmt.getBasicIntegrity());
//        Log.d(TAG, " you " +  stmt.getApkCertificateDigestSha256().length );

        Log.d(TAG, " the fuck now "  +  stmt.toString());
        Log.d(TAG, "profile match "  + stmt.get("ctsProfileMatch").getAsBoolean());
        Log.d(TAG, " cts match " +  stmt.get("basicIntegrity").getAsBoolean());

        Log.d(TAG,"\n** This sample only shows how to verify the authenticity of an "
                + "attestation response. Next, you must check that the server response matches the "
                + "request by comparing the nonce, package name, timestamp and digest.");
    }



}
