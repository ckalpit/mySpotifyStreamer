package com.imkalpit.spotifystreamer.util;

import java.util.Locale;

/**
 * Created by yemyatthu on 6/8/15.
 */
public class GeneralUtils {
  public static String getCountry() {
    String country = Locale.getDefault().getCountry();
    if (country.equals(""))
    {
      country = "US";
    }
    return country;
  }

}
