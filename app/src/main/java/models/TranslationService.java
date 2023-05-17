package models;

import android.os.StrictMode;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TranslationService {
    private static final String LOCATION = "global";
    private static final String SUBSCRIPTION_KEY = "9ad62dcefa8a41049859a49a9a24322f";
    private static final String API_ENDPOINT = "https://api.cognitive.microsofttranslator.com/";

    private final OkHttpClient client = new OkHttpClient().newBuilder().build();
    Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();


    public TranslationService() {

    }

    public TranslationResult[] getTranslation(String inputLanguage, String textToTranslate, String targetLanguage) throws IOException {
        String route = String.format("/translate?api-version=3.0&from=%s&to=%s", inputLanguage, targetLanguage);
        String requestUri = API_ENDPOINT + route;

        DetectedLanguage[] detectedLanguages = detectLanguage(textToTranslate);
        inputLanguage = detectedLanguages[0].language;

        TranslationResult[] translationResult = translateText(requestUri, textToTranslate, inputLanguage);
        return translationResult;
    }

    public DetectedLanguage[] detectLanguage(String text) throws IOException {
        String route = "Detect?api-version=3.0";
        String requestUri = API_ENDPOINT + route;

        MediaType contentType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(gson.toJson(new Object[] { new Text(text) }), contentType);

        Request request = new Request.Builder()
                .url(requestUri)
                .post(requestBody)
                .addHeader("Ocp-Apim-Subscription-Key", SUBSCRIPTION_KEY)
                .addHeader("Ocp-Apim-Subscription-Region", LOCATION)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            Type type = new TypeToken<DetectedLanguage[]>() {}.getType();
            DetectedLanguage[] detectedLanguages = gson.fromJson(responseBody, type);
            return detectedLanguages;
        }
    }

    private TranslationResult[] translateText(String requestUri, String inputText, String inputLanguage) throws IOException {
        MediaType contentType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(gson.toJson(new Object[] { new Text(inputText) }), contentType);

        Request request = new Request.Builder()
                .url(requestUri)
                .post(requestBody)
                .addHeader("Ocp-Apim-Subscription-Key", SUBSCRIPTION_KEY)
                .addHeader("Ocp-Apim-Subscription-Region", LOCATION)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            Type type = new TypeToken<List<TranslationResult>>() {}.getType();
            List<TranslationResult> translationResults = gson.fromJson(responseBody, type);

            translationResults.get(0).detectedlanguage = new DetectedLanguage(inputLanguage);
            return translationResults.toArray(new TranslationResult[0]);
        }
    }


    public AvailableLanguage getAvailableLanguages() throws IOException {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String endpoint = "https://api.cognitive.microsofttranslator.com/languages?api-version=3.0&scope=translation";

        Request request = new Request.Builder()
                .url(endpoint)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();

            AvailableLanguage availableLanguage=gson.fromJson(responseBody,AvailableLanguage.class);
            return availableLanguage;
        }
        catch (Exception e){
            System.out.println(e.getCause());
        }
        return null;
    }

    private static class Text {
        public String Text;

        public Text(String text) {
            this.Text = text;
        }
    }
}