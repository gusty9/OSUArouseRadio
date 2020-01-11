package com.gusty.arousemvvm.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.gusty.arousemvvm.model.RecentTracks;
import com.gusty.arousemvvm.model.repository.MusicRepository;

public class TrackViewModel extends AndroidViewModel {
    private final LiveData<RecentTracks> trackLiveData;

    public ObservableField<RecentTracks> tracks = new ObservableField<>();

    public TrackViewModel(@NonNull Application application) {
        super(application);
        trackLiveData = MusicRepository.getInstance().getTrack("");
    }

    public LiveData<RecentTracks> getObservableProject() {
        return trackLiveData;
    }

    public void setTrack(RecentTracks track) {
        this.tracks.set(track);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        @NonNull
        private final Application application;

        public Factory(@NonNull Application application) {
            this.application = application;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new TrackViewModel(application);
        }
    }
}
