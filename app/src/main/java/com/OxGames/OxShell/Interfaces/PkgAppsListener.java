package com.OxGames.OxShell.Interfaces;

import android.content.pm.ResolveInfo;

import java.util.List;

public interface PkgAppsListener {
    void onQueryApps(List<ResolveInfo> apps);
}
