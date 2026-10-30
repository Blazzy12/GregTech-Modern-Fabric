package com.gregtechceu.gtceu.compat.common.util;

import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;

/** The old numeric permission-level checks ({@code hasPermission(2)}) on top of 26.x permission sets. */
public final class Perms {

    private Perms() {}

    public static boolean hasLevel(PermissionSet permissions, int level) {
        return permissions.hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(level)));
    }
}
