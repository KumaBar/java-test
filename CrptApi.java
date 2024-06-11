package testapi;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.Semaphore;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

public class CrptApi {
    private final Semaphore semaphore;
    private final HttpClient httpClient;

    // конструктор
    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.semaphore = new Semaphore(requestLimit);
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }
    // создание документа
    public String createDocument(String documentJson, String signature) throws InterruptedException {

        semaphore.acquire();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(documentJson))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                return "Ошибка: " + response.statusCode();
            }
        } catch (Exception e) {
            return "исключение при отправке запроса: " + e.getMessage();
        } finally {

            semaphore.release();
        }
    }

}
 class Main {
    public static void main(String[] args) throws InterruptedException {
        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 5);
        String documentJson = "{\"description\": {\"participantInn\": \"string\"}, \"doc_id\": \"string\"}";
        String signature = "подпись";

        String response = crptApi.createDocument(documentJson, signature);
        System.out.println(response);
    }
}