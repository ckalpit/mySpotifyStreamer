package com.imkalpit.spotifystreamer;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.imkalpit.spotifystreamer.adapter.TopTracksAdapter;
import com.imkalpit.spotifystreamer.controller.TrackListStateFragment;
import com.imkalpit.spotifystreamer.util.FileUtils;
import com.imkalpit.spotifystreamer.util.GeneralUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by imkalpit on 6/8/15.
 */
public class TopTracksActivity extends AppCompatActivity {

  public static final String ARTIST_ID =
      "com.imkalpit.spotifystreamer.TopTracksActivity.ARTIST_ID";
  public static final String ARTIST_NAME =
      "com.imkalpit.spotifystreamer.TopTracksActivity.ARTIST_NAME";
  private static final String LIST_STATE_FRAGMENT = "TRACK_LIST_STATE_FRAGMENT";
  @InjectView(R.id.tracks_recycler_view)
  RecyclerView tracksRecyclerView;
  @InjectView(R.id.top_track_progress_bar) ProgressBar topTrackProgressBar;
  @InjectView(R.id.empty_view) TextView emptyText;
  @InjectView(R.id.toolbar)
  Toolbar toolbar;
  private SpotifyApi api;
  private SpotifyService spotify;
  private RecyclerView.LayoutManager layoutManager;
  private TopTracksAdapter topTracksAdapter;
  private List<Track> localTracks;
  private TrackListStateFragment trackListStateFragment;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_top_tracks);
    ButterKnife.inject(this);
    setSupportActionBar(toolbar);
    String title = String.format(getString(R.string.top_tracks_title),
        getIntent().getStringExtra(ARTIST_NAME));
    ActionBar actionBar = getSupportActionBar();
    actionBar.setTitle(title);
    actionBar.setDisplayHomeAsUpEnabled(true);
    trackListStateFragment =
        (TrackListStateFragment) getSupportFragmentManager().findFragmentByTag(LIST_STATE_FRAGMENT);
    if (trackListStateFragment == null) {
      trackListStateFragment = new TrackListStateFragment();
      getSupportFragmentManager().beginTransaction()
          .add(trackListStateFragment, LIST_STATE_FRAGMENT)
          .commit();
    }

    api = new SpotifyApi();
    spotify = api.getService();
    layoutManager = new LinearLayoutManager(this);
    topTracksAdapter = new TopTracksAdapter();
    tracksRecyclerView.setLayoutManager(layoutManager);
    tracksRecyclerView.setHasFixedSize(true);
    tracksRecyclerView.setAdapter(topTracksAdapter);
    final String artistId = getIntent().getStringExtra(ARTIST_ID);
    localTracks = FileUtils.loadJsonStringAsList(TopTracksActivity.this, artistId + ".dat");
    Map<String, Object> countryParam = new HashMap<>();
    countryParam.put("country", GeneralUtils.getCountry());
    if (trackListStateFragment.getTracks() != null
        && trackListStateFragment.getTracks().size() > 0) {
      topTrackProgressBar.setVisibility(View.GONE);
      topTracksAdapter.setTracks(trackListStateFragment.getTracks());
      tracksRecyclerView.setAdapter(topTracksAdapter);
      ((LinearLayoutManager) tracksRecyclerView.getLayoutManager()).scrollToPositionWithOffset(
          trackListStateFragment.getLastScrollPosition(), tracksRecyclerView.getChildCount());
    } else {
      spotify.getArtistTopTrack(artistId, countryParam, new Callback<Tracks>() {
        @Override public void success(final Tracks tracks, Response response) {
          if (tracks.tracks != null && tracks.tracks.size() > 0) {
            runOnUiThread(new Runnable() {
              @Override public void run() {
                topTrackProgressBar.setVisibility(View.GONE);
                topTracksAdapter.setTracks(tracks.tracks);
              }
            });
            FileUtils.saveListAsJsonString(TopTracksActivity.this, artistId + ".dat",
                tracks.tracks);
          } else {
            runOnUiThread(new Runnable() {
              @Override public void run() {
                if (localTracks != null && localTracks.size() > 0) {
                  topTrackProgressBar.setVisibility(View.GONE);
                  topTracksAdapter.setTracks(localTracks);
                } else {
                  tracksRecyclerView.setVisibility(View.GONE);
                  topTrackProgressBar.setVisibility(View.GONE);
                  emptyText.setText(getString(R.string.top_tracks_empty));
                }
              }
            });
          }
        }

        @Override public void failure(RetrofitError error) {
          runOnUiThread(new Runnable() {
            @Override public void run() {
              if (localTracks != null && localTracks.size() > 0) {
                topTrackProgressBar.setVisibility(View.GONE);
                topTracksAdapter.setTracks(localTracks);
              } else {
                tracksRecyclerView.setVisibility(View.GONE);
                topTrackProgressBar.setVisibility(View.GONE);
                emptyText.setText(getString(R.string.error_try_again));
              }
            }
          });
        }
      });
    }
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        NavUtils.navigateUpFromSameTask(this);
        return true;
    }
    return false;
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    trackListStateFragment.setTracks(topTracksAdapter.getTracks());
    trackListStateFragment.setLastScrollPosition(
        ((LinearLayoutManager) tracksRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition());
  }
}
