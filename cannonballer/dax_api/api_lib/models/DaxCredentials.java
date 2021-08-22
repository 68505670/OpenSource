package scripts.dax_api.api_lib.models;

public class DaxCredentials {

    private final String apiKey;
    private final String secretKey;

    public DaxCredentials(String apiKey, String secretKey) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getSecretKey() {
        return secretKey;
    }
}
