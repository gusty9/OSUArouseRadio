package com.gusty.arousemvvm.model.repository;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gusty.arousemvvm.BuildConfig;
import com.gusty.arousemvvm.model.RecentTracks;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MusicRepository {
    //paths interface
    private MusicService musicService;
    //the identifier used to check for api changes
    private String total = null;
    //repository of the project
    private static MusicRepository projectRepository;
    private final MutableLiveData<RecentTracks> data = new MutableLiveData<>();

    //create the retrofit http request object
    private MusicRepository() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                HttpUrl originalHttpUrl = original.url();
                HttpUrl url = originalHttpUrl.newBuilder()
                        .addQueryParameter("method", "user.getrecenttracks")
                        .addQueryParameter("user", "arouseosu")
                        .addQueryParameter("api_key", BuildConfig.ApiKey)
                        .addQueryParameter("format", "json")
                        .addQueryParameter("limit", "1")
                        .build();
                Request request = original.newBuilder().url(url).build();
                return chain.proceed(request);
            }
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MusicService.URL)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        musicService = retrofit.create(MusicService.class);
    }

    //return the one static instance of this class
    public synchronized static MusicRepository getInstance() {
        if (projectRepository == null) {
            projectRepository = new MusicRepository();
        }
        return projectRepository;
    }

    //return the live data representation of the data
    public LiveData<RecentTracks> getTrack(String source) {
        //create the callback object from the http request
        Callback<RecentTracks> rtCallback = new Callback<RecentTracks>() {
            @Override
            public void onResponse(Call<RecentTracks> call, retrofit2.Response<RecentTracks> response) {
                createHttpRequest(source);
                RecentTracks rt = response.body();
                if (rt != null) {
                    //check to see if the api changed
                    boolean downloadNewImage = (total == null) || (!rt.getRecentTracksInfo().getAttr().getTotal().equals(total));
                    if (downloadNewImage) {
                        (new DownloadImageTask(rt)).execute();
                    }
                }

            }

            @Override
            public void onFailure(Call<RecentTracks> call, Throwable t) {
                t.printStackTrace();
                createHttpRequest(source);
            }
        };
        musicService.getTrack(source).enqueue(rtCallback);
        return data;
    }

    /**
     * create another http request to the api to  check for music changes
     * @param source
     */
    private void createHttpRequest(String source) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //recursively call the http get method, checking for new data
                getTrack(source);
            }
        }, 10000L);
    }

    /**
     * async task to download the image at the given url
     * **I don't think this needs to be static because it isn't tied to a lifecycle** ?
     */
    public class DownloadImageTask extends AsyncTask<Void, Void, Bitmap> {

        RecentTracks recentTracks;
        public DownloadImageTask(RecentTracks recentTracks) {
            this.recentTracks = recentTracks;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Void... responses) {
            try {
                URL url = new URL(recentTracks.getRecentTracksInfo().getTracks().get(0).getImage().get(3).getText());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            recentTracks.getRecentTracksInfo().getTracks().get(0).setAlbumArt(bitmap);
            data.setValue(recentTracks);
            total = recentTracks.getRecentTracksInfo().getAttr().getTotal();
        }
    }
}
