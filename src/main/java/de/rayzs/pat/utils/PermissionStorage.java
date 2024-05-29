package de.rayzs.pat.utils;

import java.util.*;

public class PermissionStorage {

    private final UUID uuid;
    private final HashMap<String, Boolean> permissionMap = new HashMap<>();

    public PermissionStorage(UUID uuid) {
        this.uuid = uuid;
    }

    public void remove(String permission) {
        permissionMap.remove(permission);
    }

    public void clear() {
        permissionMap.clear();
    }

    public PermissionState getPermissionState(String permission) {
        if(!hasPermissionState(permission)) return PermissionState.EMPTY;
        return permissionMap.get(permission) ? PermissionState.PERMITTED : PermissionState.DENIED;
    }

    public boolean hasPermissionState(String permission) {
        return permissionMap.containsKey(permission);
    }

    enum PermissionState { PERMITTED, DENIED, EMPTY }
}
