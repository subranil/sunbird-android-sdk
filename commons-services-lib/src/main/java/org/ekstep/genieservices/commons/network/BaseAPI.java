package org.ekstep.genieservices.commons.network;

import org.ekstep.genieservices.ServiceConstants;
import org.ekstep.genieservices.commons.AppContext;
import org.ekstep.genieservices.commons.GenieResponseBuilder;
import org.ekstep.genieservices.commons.bean.GenieResponse;
import org.ekstep.genieservices.commons.bean.enums.JWTokenType;
import org.ekstep.genieservices.commons.utils.GsonUtil;
import org.ekstep.genieservices.commons.utils.JWTUtil;
import org.ekstep.genieservices.commons.utils.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 4/19/2017.
 *
 * @author anil
 */
public abstract class BaseAPI {

    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final int AUTHENTICATION_FAILURE = 401;

    private AppContext mAppContext;
    private IHttpClientFactory httpClientFactory;
    private Map<String, String> headers;
    private String url;
    private String TAG;

    public BaseAPI(AppContext appContext, String url, String TAG) {
        this.url = url;
        this.mAppContext = appContext;
        this.TAG = TAG;
        this.httpClientFactory = appContext.getHttpClientFactory();
        this.headers = new HashMap<>();
        this.headers.put("Accept-Encoding", "gzip, deflate");
    }

    public GenieResponse get() {
        return fetchFromServer(GET, true);
    }

    public GenieResponse post() {
        return fetchFromServer(POST, true);
    }

    private GenieResponse fetchFromServer(String requestType, boolean retryForAuthError) {
        if (!mAppContext.getConnectionInfo().isConnected()) {
            return getErrorResponse(NetworkConstants.CONNECTION_ERROR, NetworkConstants.CONNECTION_ERROR_MESSAGE);
        }
        try {
            ApiResponse apiResponse = invokeApi(requestType);
            if (apiResponse.isSuccessful()) {
                return getSuccessResponse(apiResponse.getResponseBody());
            } else if (apiResponse.getResponseCode() == AUTHENTICATION_FAILURE) {
                if (retryForAuthError) {
                    resetAuthToken();
                    return fetchFromServer(requestType, false);
                } else {
                    return getErrorResponse(NetworkConstants.SERVERAUTH_ERROR, NetworkConstants.SERVERAUTH_ERROR_MESSAGE);
                }
            } else {
                return getErrorResponse(NetworkConstants.SERVER_ERROR, NetworkConstants.SERVER_ERROR_MESSAGE);
            }
        } catch (IOException e) {
            Logger.e(TAG, e.getMessage());
            return getErrorResponse(NetworkConstants.NETWORK_ERROR, e.getMessage());
        }
    }

    private IHttpClient prepareClient() {
        IHttpClient httpClient = httpClientFactory.getClient();
        httpClient.createRequest(url);
        if (shouldAuthenticate()) {
            httpClient.setAuthHeaders();
        }
        httpClient.setHeaders(headers);
        httpClient.setHeaders(getRequestHeaders());
        return httpClient;
    }

    private GenieResponse<String> getSuccessResponse(String responseBody) {
        GenieResponse<String> response = GenieResponseBuilder.getSuccessResponse("", String.class);
        response.setResult(responseBody);
        return response;
    }

    private GenieResponse<String> getErrorResponse(String error, String errorMessage) {
        return GenieResponseBuilder.getErrorResponse(error, errorMessage, TAG, String.class);
    }

    private ApiResponse invokeApi(String requestType) throws IOException {
        IHttpClient httpClient = prepareClient();
        ApiResponse apiResponse = null;
        if (GET.equals(requestType)) {
            apiResponse = httpClient.doGet();
        } else if (POST.equals(requestType)) {
            apiResponse = httpClient.doPost(getRequestData());
        }
        return apiResponse;
    }

    private void resetAuthToken() {
        String key = mAppContext.getParams().getString(ServiceConstants.Params.MOBILE_APP_KEY);
        String secret = mAppContext.getParams().getString(ServiceConstants.Params.MOBILE_APP_SECRET);
        String deviceSecret = createDeviceSecret(JWTUtil.createJWToken(key, secret, JWTokenType.HS256));
        if (deviceSecret != null) {
            String deviceKey = mAppContext.getDeviceInfo().getDeviceID();
            mAppContext.getKeyValueStore().putString(NetworkConstants.API_BEARER_TOKEN, JWTUtil.createJWToken(deviceKey, deviceSecret, JWTokenType.HS256));
        }
    }

    private String createDeviceSecret(String bearerToken) {
        AuthAPI authAPI = new AuthAPI(mAppContext, bearerToken);
        GenieResponse<String> response = authAPI.post();
        String deviceSecret = null;
        if (response.getStatus()) {
            String body = response.getResult().toString();
            Map responseMap = GsonUtil.fromJson(body, Map.class);
            Map result = (Map) responseMap.get("result");
            if (result != null) {
                Object keyObj = result.get("secret");
                deviceSecret = keyObj == null ? "" : keyObj.toString();
            }
        }
        return deviceSecret;
    }

    protected boolean shouldAuthenticate() {
        return true;
    }

    protected abstract Map<String, String> getRequestHeaders();

    protected byte[] getRequestData() {
        return createRequestData().getBytes();
    }

    protected abstract String createRequestData();

}
