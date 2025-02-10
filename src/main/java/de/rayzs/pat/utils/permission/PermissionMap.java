package de.rayzs.pat.utils.permission;

import java.util.*;
import java.util.stream.Collectors;

public class PermissionMap {

    private final UUID uuid;
    private final HashMap<String, Boolean> permissionMap = new HashMap<>();

    public PermissionMap(UUID uuid) {
        this.uuid = uuid;
    }

    public void remove(String permission) {
        permissionMap.remove(permission);
    }

    public Map<String, Boolean> getPermissionMap() {
        return permissionMap;
    }

    public Set<String> getHashedPermissions() {
        return permissionMap.keySet().stream().filter(permissionMap::get).collect(Collectors.toSet());
    }

    public void clear() {
        permissionMap.clear();
    }

    public void setState(String permission, boolean permitted) {
        permissionMap.put(permission, permitted);
    }

    public void setStateIfEmpty(String permission, boolean permitted) {
        permissionMap.putIfAbsent(permission, permitted);
    }

    public boolean isPermitted(String permission) {
        return getPermissionState(permission) == PermissionState.PERMITTED;
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
