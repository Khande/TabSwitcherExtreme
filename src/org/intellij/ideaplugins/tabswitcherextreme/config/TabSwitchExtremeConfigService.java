package org.intellij.ideaplugins.tabswitcherextreme.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Khande
 *         Created on 18/7/17.
 **/
@State(name = "TabSwitchExtreme.Config", storages = @Storage("tab.switch.extreme.config.xml"))
public class TabSwitchExtremeConfigService implements PersistentStateComponent<TabGroupConfig> {

    private final TabGroupConfig myTabGroupConfig = new TabGroupConfig();

    @NotNull
    @Override
    public TabGroupConfig getState() {
        return myTabGroupConfig;
    }

    @Override
    public void loadState(TabGroupConfig tabGroupConfig) {
        XmlSerializerUtil.copyBean(tabGroupConfig, myTabGroupConfig);
    }


    public static TabSwitchExtremeConfigService getInstance() {
        return ServiceManager.getService(TabSwitchExtremeConfigService.class);
    }

}
