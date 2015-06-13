package com.imkalpit.spotifystreamer.controller;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by yemyatthu on 6/9/15.
 */
public class TrackListStateFragment extends Fragment {
  private List<Track> tracks;
  private int lastScrollPosition;

  public TrackListStateFragment(){

  }
  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    tracks = new ArrayList<>();
    lastScrollPosition = 0;

  }

  public List<Track> getTracks() {
    return this.tracks;
  }

  public void setTracks(List<Track> tracks) {
    this.tracks = tracks;
  }

  public void clearTracks(){
    this.tracks.clear();
  }

  public int getLastScrollPosition() {
    return this.lastScrollPosition;
  }

  public void setLastScrollPosition(int lastScrollPosition) {
    this.lastScrollPosition = lastScrollPosition;
  }

}
