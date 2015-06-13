package com.imkalpit.spotifystreamer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.imkalpit.spotifystreamer.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by imkalpit on 6/8/15.
 */
public class TopTracksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private TopTracksAdapter.ClickListener itemClickListener;
  private List<Track> tracks = new ArrayList<>();
  private Context context;

  public void setTracks(List<Track> tracks){
    this.tracks = tracks;
    notifyDataSetChanged();
  }

  public List<Track> getTracks() {
    return this.tracks;
  }

  @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    context = parent.getContext();
    View view = LayoutInflater.from(context).inflate(R.layout.top_tracks_item,parent,false);
    return new TopTracksViewHolder(view);
  }

  @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    if(holder instanceof TopTracksViewHolder){
      ImageView trackImage = ((TopTracksViewHolder) holder).topTracksImage;
      TextView trackName = ((TopTracksViewHolder) holder).topTracksName;
      TextView trackAlbumn = ((TopTracksViewHolder) holder).topTracksAlbum;
      List<Image> albumImages = tracks.get(position).album.images;
      if(albumImages.size()>1){
      Glide.with(context)
          .load(tracks.get(position).album.images.get(1).url)
          .placeholder(R.drawable.placeholder)
          .error(R.drawable.placeholder)
          .into(trackImage);
      }
      trackName.setText(tracks.get(position).name);
      trackAlbumn.setText(tracks.get(position).album.name);
    }
  }

  @Override public int getItemCount() {
    return tracks!=null?tracks.size():0;
  }

  public void setOnItemClickListener(final ClickListener itemClickListener) {
    this.itemClickListener = itemClickListener;
  }

  public interface ClickListener {
    void onItemClick(View view, int position);
  }


  public class TopTracksViewHolder extends RecyclerView.ViewHolder implements
      View.OnClickListener {
    @InjectView(R.id.top_tracks_image) ImageView topTracksImage;
    @InjectView(R.id.top_tracks_name) TextView topTracksName;
    @InjectView(R.id.top_tracks_albumn) TextView topTracksAlbum;

    public TopTracksViewHolder(View itemView) {
      super(itemView);
      ButterKnife.inject(this,itemView);
      itemView.setOnClickListener(this);
    }

    @Override public void onClick(View view) {
      if (itemClickListener != null) {
        itemClickListener.onItemClick(view, getAdapterPosition());
      }
    }
  }
}
