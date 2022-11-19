package com.OxGames.OxShell.Interfaces;

public interface PermissionsListener {
    void onPermissionResponse(int requestCode, String[] permissions, int[] grantResults);
}
