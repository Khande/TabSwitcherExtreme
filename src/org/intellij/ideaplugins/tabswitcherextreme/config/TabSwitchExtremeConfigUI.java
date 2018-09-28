package org.intellij.ideaplugins.tabswitcherextreme.config;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.intellij.ideaplugins.tabswitcherextreme.ConfigurationForm;
import org.intellij.ideaplugins.tabswitcherextreme.Utils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TabSwitchExtremeConfigUI implements Configurable {
    private ConfigurationForm myForm;
    @NotNull
    private final TabGroupConfig myTabGroupConfig;

    /**
     * 当为 projectConfigurable 时构造器带上 project 参数
     */
    public TabSwitchExtremeConfigUI(@NotNull Project project) {
        myTabGroupConfig = TabSwitchExtremeConfigService.getInstance(project).getState();
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "TabSwitcherExtreme";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        myForm = new ConfigurationForm();

        return myForm.holder;
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        Utils.log("Apply");
        myTabGroupConfig.setTitles(myForm.textArea1.getText());
        myTabGroupConfig.setMatches(myForm.textArea2.getText());
        myTabGroupConfig.setExcludes(myForm.textArea3.getText());
    }

    @Override
    public void reset() {
        Utils.log("Reset");
        myForm.textArea1.setText(myTabGroupConfig.getTitles());
        myForm.textArea2.setText(myTabGroupConfig.getMatches());
        myForm.textArea3.setText(myTabGroupConfig.getExcludes());
    }

    @Override
    public void disposeUIResources() {
        Utils.log("disposeUIResources");
        myForm = null;
    }

}
