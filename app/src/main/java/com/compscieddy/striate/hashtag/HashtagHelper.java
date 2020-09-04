package com.compscieddy.striate.hashtag;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HashtagHelper {

  public static List<String> parseHashtags(String photoCaptionText) {
    List<String> hashtags = new ArrayList<>();
    if (TextUtils.isEmpty(photoCaptionText)) {
      return hashtags;
    }

    Matcher matcher = Pattern.compile("(#[^#\\s]*)").matcher(photoCaptionText);

    while (matcher.find()) {
      hashtags.add(matcher.group());
    }

    return hashtags;
  }
}
