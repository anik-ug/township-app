package com.anik.township.complaints;

public interface ClassifierHttpClient {
    String classify(String baseUrl, String model, String apiKey, String text);
}
