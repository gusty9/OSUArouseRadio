package com.gusty.arousemvvm;

public interface FragmentCallback {
    /**
     * callback for the service to communicate with the fragment.
     * This should not cause leaks as the service is connected to the fragment/activity
     * @param isPlaying
     *              the state of the musicplayer
     */
    void musicStateChanged(boolean isPlaying);
    void finishApp();
}
