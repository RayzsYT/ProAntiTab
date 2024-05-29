package de.rayzs.pat.utils.permission;

import java.util.*;

public class PermissionMap {

    private final UUID uuid;
    private final HashMap<String, Boolean> permissionMap = new HashMap<>();

    public PermissionMap(UUID uuid) {
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

    public UUID getUUID() {
        return uuid;
    }

    enum PermissionState { PERMITTED, DENIED, EMPTY }
}
