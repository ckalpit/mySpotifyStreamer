package com.imkalpit.spotifystreamer.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.imkalpit.spotifystreamer.R;

/**
 * Created by imkalpit on 6/8/15.
 */
public class ProgressViewHolder extends RecyclerView.ViewHolder {
  public ProgressBar progressBar;

  public ProgressViewHolder(View itemView) {
    super(itemView);
    progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
  }

}
