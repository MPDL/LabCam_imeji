package de.mpg.mpdl.labcam.Retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.util.Base64;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

import de.mpg.mpdl.labcam.code.utils.DeviceStatus;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;

/**
 * Created by allen on 27/08/15.
 */
public class ServiceGenerator {


    // No need to instantiate this class.
    private ServiceGenerator() {
    }

    public static <S> S createService(Class<S> serviceClass, String baseUrl) {
        // call basic auth generator method without user and pass
        return createService(serviceClass, baseUrl, null, null);
    }

    public static <S> S createService(Class<S> serviceClass, String baseUrl,
                                      String username, String password) {
        return createService(serviceClass, baseUrl, username, password, null);
    }
    // Almost every webservice and API evaluates the Authorization header of the HTTP request.
    // That's why we set the encoded credentials value to that header field.
    public static <S> S createService(Class<S> serviceClass, String baseUrl,
                                      String username, String password,
                                      Converter converter) {
        RestAdapter.Builder builder = new RestAdapter.Builder();

        Gson gson = new GsonBuilder()
                .serializeNulls()
                .excludeFieldsWithoutExposeAnnotation()
                .create();

        // set endpoint url and use OkHTTP as HTTP client
        builder.setEndpoint(baseUrl)
                .setConverter(new GsonConverter(gson))
                .setClient(new OkClient(new OkHttpClient()));


        // execute only when user provide the username and password
        if (username != null && password != null) {
            // concatenate username and password with colon for authentication
            final String credentials = username + ":" + password;

            builder.setRequestInterceptor(new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    // create Base64 encodet string
                    String string = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                    request.addHeader("Authorization", string);
                    request.addHeader("Accept", "application/json");
                }
            });
            // set log level
            builder.setLogLevel(RestAdapter.LogLevel.FULL).setLog(new RestAdapter.Log() {
                public void log(String msg) {
                    Log.i("retrofit", msg);
                }
            });
        }


        RestAdapter adapter = builder.build();

        return adapter.create(serviceClass);
    }

    public static <S> S createService(Class<S> serviceClass, String baseUrl,
                                      String APIkey) {
        RestAdapter.Builder builder = new RestAdapter.Builder();

        Gson gson = new GsonBuilder()
                .serializeNulls()
                .excludeFieldsWithoutExposeAnnotation()
                .create();

        // set endpoint url and use OkHTTP as HTTP client
        //TODO: sometimes there is a base url null error
        /** add a default url here **/
        if(baseUrl ==null){
            baseUrl = DeviceStatus.BASE_URL;
        }

        builder.setEndpoint(baseUrl)
                .setConverter(new GsonConverter(gson))
                .setClient(new OkClient(new OkHttpClient()));


        final String key =  APIkey;
        // execute only when user provide the username and password
        if (key!=null) {
            // concatenate username and password with colon for authentication

            builder.setRequestInterceptor(new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    // create Base64 encodet string
                    String string = "Bearer " + key;

                    request.addHeader("Authorization", string);
                    request.addHeader("Accept", "application/json");
                }
            });
            // set log level
            builder.setLogLevel(RestAdapter.LogLevel.FULL).setLog(new RestAdapter.Log() {
                public void log(String msg) {
                    Log.i("retrofit", msg);
                }
            });
        }


        RestAdapter adapter = builder.build();

        return adapter.create(serviceClass);
    }
}