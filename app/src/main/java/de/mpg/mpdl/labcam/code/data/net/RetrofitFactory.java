package de.mpg.mpdl.labcam.code.data.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import de.mpg.mpdl.labcam.LabCam;
import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.utils.DeviceStatus;
import de.mpg.mpdl.labcam.code.utils.PreferenceUtil;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitFactory {

    public static String BASE_URL = DeviceStatus.BASE_URL;

    private static volatile RetrofitFactory sInstance;

    private Retrofit retrofit;

    private RetrofitFactory() {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
//                    .addConverterFactory(GsonConverterFactory.create(buildGson()))
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(genericClient())
                    .build();

    }

    public void changeServer(String address) {
        retrofit = new Retrofit.Builder()
                .baseUrl(address)
//                .addConverterFactory(GsonConverterFactory.create(buildGson()))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(genericClient())
                .build();
    }

    public static RetrofitFactory getInstance() {
        if (null == sInstance) {
            synchronized (RetrofitFactory.class) {
                if (null == sInstance) {
                    sInstance = new RetrofitFactory();
                }
            }
        }
        return sInstance;
    }

    public <T> T create(final Class<T> service) {
        return retrofit.create(service);
    }

    private static Interceptor interceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            String credential =  PreferenceUtil.getString(LabCam.getContext(), Constants.SHARED_PREFERENCES, Constants.API_KEY, "");
            if(credential.contains("Basic")) {
                Request request = chain.request()
                        .newBuilder()
                        .addHeader("Accept", "application/json")
                        .addHeader("Content-Type", "application/json")
                        .addHeader("charset", "UTF-8")
                        .addHeader("Authorization", credential)
                        .build();
                return chain.proceed(request);
            }else {
                Request request = chain.request()
                        .newBuilder()
                        .addHeader("Accept", "application/json")
                        .addHeader("Content-Type", "application/json")
                        .addHeader("charset", "UTF-8")
                        .addHeader("Authorization", "Bearer " +credential)
                        .build();
                return chain.proceed(request);
            }
        }
    };

    private static HttpLoggingInterceptor initLoggingInterceptor() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return httpLoggingInterceptor;
    }

    private static OkHttpClient genericClient() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(initLoggingInterceptor())
                .addInterceptor(interceptor)
                .connectTimeout(300, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .addInterceptor(chain -> onOnIntercept(chain))
                .build();
        return httpClient;
    }

    private static Response onOnIntercept(Interceptor.Chain chain) throws IOException {
        Response response = null;
        try {
            response = chain.proceed(chain.request());
        }
        catch (SocketTimeoutException exception) {
            exception.printStackTrace();
        }

        return response;
    }

    private Gson buildGson() {
        GsonBuilder gsonbuilder = new GsonBuilder();
        // Register an adapter to manage the date types as long values
        gsonbuilder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });
        Gson gson = gsonbuilder.create();
        return gson;
    }
}
