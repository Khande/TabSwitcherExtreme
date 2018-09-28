package org.intellij.ideaplugins.tabswitcherextreme.config;

import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Khande
 *         Created on 18/7/17.
 **/
public class TabGroupConfig {

    private static final String DEFAULT_TITLES = "Activity\n" +
            "Fragment\n" +
            "Service\n" +
            "Java\n" +
            "Layout\n" +
            "EditableDrawable\n" +
            "Bitmaps\n" +
            "Values\n" +
            "Xml\n" +
            "Gradle";

    private static final String DEFAULT_MATCHES = ".*Activity.*\\.java\n" +
            ".*Fragment.*\\.java\n" +
            ".*Service.*\\.java\n" +
            ".*\\.java\n" +
            ".*res/layout.*/.*\\.xml\n" +
            ".*res/drawable.*/.*\\.xml\n" +
            ".*\\.png|.*\\.jpg|.*\\.gif|.*\\.webp\n" +
            ".*res/values.*/.*\\.xml\n" +
            ".*\\.xml\n" +
            ".*\\.gradle";


    private String titles;

    private String matches;

    private String excludes;

    public String getTitles() {
        return !TextUtils.isBlank(matches) && !TextUtils.isBlank(titles) ? titles : DEFAULT_TITLES;
    }

    public void setTitles(@Nullable String titles) {
        this.titles = titles;
    }

    @NotNull
    public String getMatches() {
        return !TextUtils.isBlank(matches) && !TextUtils.isBlank(titles) ? matches : DEFAULT_MATCHES;
    }

    public void setMatches(@Nullable String matches) {
        this.matches = matches;
    }

    @NotNull
    public String getExcludes() {
        return !TextUtils.isBlank(excludes) ? excludes : "";
    }


    public void setExcludes(@Nullable String excludes) {
        this.excludes = excludes;
    }
}
