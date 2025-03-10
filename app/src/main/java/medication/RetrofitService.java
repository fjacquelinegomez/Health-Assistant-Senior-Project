package medication;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Setting up Retrofit instance to work with RxNormAPI
// https://square.github.io/retrofit/ <- Helpful documentation ^-^
public class RetrofitService {
    private static Retrofit retrofit = null;
    public static Retrofit getClient() {
        return new Retrofit.Builder()
                .baseUrl("https://rxnav.nlm.nih.gov/REST/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

    }
}
