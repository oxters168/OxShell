package com.OxGames.OxShell;

public interface PermissionsListener {
    void onPermissionResponse(int requestCode, String[] permissions, int[] grantResults);
}
