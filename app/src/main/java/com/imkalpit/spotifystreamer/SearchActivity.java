package com.imkalpit.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.imkalpit.spotifystreamer.adapter.ArtistSearchAdapter;
import com.imkalpit.spotifystreamer.controller.ArtistListStateFragment;
import com.imkalpit.spotifystreamer.listener.EndlessRecyclerOnScrollListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by yemyatthu on 6/2/15.
 */
public class SearchActivity extends AppCompatActivity
    implements android.support.v7.widget.SearchView.OnQueryTextListener,
    ArtistSearchAdapter.ClickListener {

  @InjectView(R.id.artist_recycler_view)
  RecyclerView artistRecyclerView;
  @InjectView(R.id.toolbar)
  Toolbar toolbar;
  @InjectView(R.id.toolbar_search)
  SearchView searchView;
  @InjectView(R.id.search_progress_bar) ProgressBar searchProgressBar;
  @InjectView(R.id.empty_view) TextView emptyText;

  private static final String LIST_STATE_FRAGEMENT = "artist_list_state_fragment";
  private SpotifyApi api;
  private SpotifyService spotify;
  private ArtistsPager results;
  private ArtistSearchAdapter artistSearchAdapter;
  private RecyclerView.LayoutManager layoutManager;
  private int offset;
  private String queryStr;
  private List<Artist> totalArtists;
  private ArtistListStateFragment artistListStateFragment;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_search);
    ButterKnife.inject(this);
    setSupportActionBar(toolbar);
    setSupportActionBar(toolbar);
    artistListStateFragment =
        (ArtistListStateFragment) getSupportFragmentManager().findFragmentByTag(
            LIST_STATE_FRAGEMENT);
    if (artistListStateFragment == null) {
      artistListStateFragment = new ArtistListStateFragment();
      getSupportFragmentManager().beginTransaction()
          .add(artistListStateFragment, LIST_STATE_FRAGEMENT)
          .commit();
    }
    api = new SpotifyApi();
    spotify = api.getService();
    offset = 0;
    layoutManager = new LinearLayoutManager(this);
    artistSearchAdapter = new ArtistSearchAdapter();
    artistSearchAdapter.setOnItemClickListener(this);
    artistRecyclerView.setAdapter(artistSearchAdapter);
    artistRecyclerView.setLayoutManager(layoutManager);
    artistRecyclerView.setHasFixedSize(true);
    searchProgressBar.setVisibility(View.GONE); // Disable progress bar and show message
    emptyText.setText(getString(R.string.search_first_time));
    artistRecyclerView.setVisibility(View.GONE);
    searchView.setOnQueryTextListener(this);
    totalArtists = new ArrayList<>();
  }

  @Override public boolean onQueryTextSubmit(String s) {
    return false;
  }

  @Override public boolean onQueryTextChange(String s) {
    offset = 0; // offset = 0 means first page

    totalArtists.clear(); // clear total artists and adapter items so that they don't mix with the previous queries
    artistSearchAdapter.clear();

    artistSearchAdapter.toogleProgressBarVisibilty(
        true); //Show Loading Again, incase user scroll it to the end before the query change

    if (s.length() == 0) {
      artistRecyclerView.setVisibility(View.GONE);
      searchProgressBar.setVisibility(View.GONE);
      emptyText.setText(R.string.search_first_time);
    } else {
      artistRecyclerView.addOnScrollListener(
          getScrollListener()); //For some reason,Scroll Listener remove everytime the query changed
      queryStr = s;
      emptyText.setText("");
      if (artistListStateFragment.getArtists() != null
          && artistListStateFragment.getArtists().size() > 0) { // That means configuration changes

        artistRecyclerView.setVisibility(View.VISIBLE);
        searchProgressBar.setVisibility(View.GONE);
        totalArtists.addAll(artistListStateFragment.getArtists());
        artistSearchAdapter.setArtists(totalArtists);
        ((LinearLayoutManager) artistRecyclerView.getLayoutManager()).scrollToPositionWithOffset(
            artistListStateFragment.getLastScrollPosition(), artistRecyclerView.getChildCount());
        artistListStateFragment.clearArtists(); // clear data from headless fragments so that configuration changes and normal query changes don't mix
      } else {

        searchProgressBar.setVisibility(View.VISIBLE);
        spotify.searchArtists(s, new Callback<ArtistsPager>() {
          @Override public void success(final ArtistsPager artistsPager, final Response response) {
            runOnUiThread(new Runnable() {
              @Override public void run() {
                searchProgressBar.setVisibility(View.GONE);
                results = artistsPager;
                if (results.artists.items.size() == 0) {
                  artistRecyclerView.setVisibility(View.GONE);
                  emptyText.setText(getString(R.string.search_not_found));
                } else {
                  emptyText.setText("");
                  totalArtists.addAll(results.artists.items);
                  artistRecyclerView.setVisibility(View.VISIBLE);
                  artistSearchAdapter.setArtists(results.artists.items);
                }
              }
            });
          }

          @Override public void failure(RetrofitError error) {
            runOnUiThread(new Runnable() {
              @Override public void run() {
                searchProgressBar.setVisibility(View.GONE);
                emptyText.setText(getString(R.string.error_try_again));
              }
            });
          }
        });
      }
    }

    return true;
  }

  private EndlessRecyclerOnScrollListener getScrollListener() {
    return new EndlessRecyclerOnScrollListener((LinearLayoutManager) layoutManager) {
      @Override public void onLoadMore(int current_page) {
        offset += 20;
        Map<String, Object> offsetQuery = new HashMap<String, Object>();
        offsetQuery.put("offset", offset);
        spotify.searchArtists(queryStr, offsetQuery, new Callback<ArtistsPager>() {
          @Override public void success(final ArtistsPager artistsPager, Response response) {
            runOnUiThread(new Runnable() {
              @Override public void run() {
                results = artistsPager;
                if (results.artists.items.size() == 0) {
                  artistSearchAdapter.toogleProgressBarVisibilty(false);
                } else {
                  totalArtists.addAll(results.artists.items);
                  artistSearchAdapter.addArtists(results.artists.items);
                }
              }
            });
          }

          @Override public void failure(RetrofitError error) {
            runOnUiThread(new Runnable() {
              @Override public void run() {
                artistSearchAdapter.toogleProgressBarVisibilty(false);
              }
            });
          }
        });
      }
    };
  }

  @Override public void onItemClick(View view, int position) {
    Intent topTrackIntent = new Intent(SearchActivity.this, TopTracksActivity.class);
    topTrackIntent.putExtra(TopTracksActivity.ARTIST_ID, totalArtists.get(position).id);
    topTrackIntent.putExtra(TopTracksActivity.ARTIST_NAME, totalArtists.get(position).name);
    startActivity(topTrackIntent);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    artistListStateFragment.setArtists(artistSearchAdapter.getArtists());
    artistListStateFragment.setLastScrollPosition(
        ((LinearLayoutManager) artistRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition());
  }
}
