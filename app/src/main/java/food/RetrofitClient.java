package food;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://api.spoonacular.com/";
    private static RetrofitClient instance;
    private final SpoonacularService spoonacularService;

    private RetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        spoonacularService = retrofit.create(SpoonacularService.class);
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public SpoonacularService getSpoonacularService() {
        return spoonacularService;
    }
}
