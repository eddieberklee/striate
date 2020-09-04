package com.compscieddy.striate.god;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.compscieddy.striate.databinding.InfinoteGodFragmentBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class InfinoteGodFragment extends Fragment {

  private InfinoteGodFragmentBinding binding;
  private Resources res;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = InfinoteGodFragmentBinding.inflate(inflater, container, false);
    Context c = binding.getRoot().getContext();
    res = c.getResources();
    return binding.getRoot();
  }
}
