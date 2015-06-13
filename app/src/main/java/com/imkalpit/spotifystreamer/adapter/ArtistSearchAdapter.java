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
import com.imkalpit.spotifystreamer.view.ProgressViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by imkalpit on 6/5/15.
 */
public class ArtistSearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private List<Artist> artists = new ArrayList<>();
  private Context context;
  public static final int ITEM_VIEW = 100;
  public static final int FOOTER_VIEW = 101;
  private boolean progressBarIsShown = true;
  private ClickListener itemClickListener;
  public ArtistSearchAdapter(){
  }

  public void setArtists(List<Artist> artists){
    this.artists = artists;
    notifyDataSetChanged();
  }

  public void addArtists(List<Artist> artists){
    this.artists.addAll(artists);
    notifyDataSetChanged();
  }

  public List<Artist> getArtists(){
    return this.artists;
  }
  public void toogleProgressBarVisibilty(boolean progressBarIsShown){
    this.progressBarIsShown = progressBarIsShown;
    notifyDataSetChanged();
  }
  public void clear(){
    this.artists = new ArrayList<>();
    notifyDataSetChanged();
  }
  @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    context = parent.getContext();
    View view;
    switch (viewType){
      case ITEM_VIEW:
        view = LayoutInflater.from(context).inflate(R.layout.artist_search_item,parent,false);
        return new ArtistSearchViewHolder(view);
      case FOOTER_VIEW:
        view = LayoutInflater.from(context).inflate(R.layout.progress_bar,parent,false);
        return new ProgressViewHolder(view);
      default:
        return null;
    }
  }

  @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    if(holder instanceof ArtistSearchViewHolder){
      TextView artistName = ((ArtistSearchViewHolder) holder).artistName;
      ImageView artistImage = ((ArtistSearchViewHolder) holder).artistImage;
      artistName.setText(artists.get(position).name);
      List<Image> images = artists.get(position).images;
      if(images.size()>=2)
      Glide.with(context)
          .load(artists.get(position).images.get(1).url)
          .placeholder(R.drawable.placeholder)
          .error(R.drawable.placeholder)
          .into(artistImage);
    }else if(holder instanceof ProgressViewHolder){
      if (progressBarIsShown && artists.size()>0){ //Without data, don't show footer view
        ((ProgressViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
      }else{
        ((ProgressViewHolder) holder).progressBar.setVisibility(View.GONE);
      }
    }
  }

  @Override public int getItemCount() {
    return artists==null?0:artists.size()+1; //plus one for the footer view space
  }

  @Override public int getItemViewType(int position) {
    return position<artists.size()?ITEM_VIEW:FOOTER_VIEW; //Footer view should be given when there're no more artists
  }

  public void setOnItemClickListener(final ClickListener itemClickListener) {
    this.itemClickListener = itemClickListener;
  }

  public interface ClickListener {
    void onItemClick(View view, int position);
  }

  public class ArtistSearchViewHolder extends RecyclerView.ViewHolder implements
      View.OnClickListener {
    @InjectView(R.id.artist_search_image) ImageView artistImage;
    @InjectView(R.id.artist_search_name) TextView artistName;

    public ArtistSearchViewHolder(View itemView) {
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
