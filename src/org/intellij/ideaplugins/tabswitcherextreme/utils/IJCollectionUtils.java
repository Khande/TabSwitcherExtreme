package org.intellij.ideaplugins.tabswitcherextreme.utils;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author Khande
 *         Created on 18/7/17.
 **/
public final class IJCollectionUtils {
    private IJCollectionUtils() {
        //no instance
    }

    public static boolean isNullOrEmpty(@Nullable final Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }


}
