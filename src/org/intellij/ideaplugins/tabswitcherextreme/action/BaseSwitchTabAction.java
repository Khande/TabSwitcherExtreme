package org.intellij.ideaplugins.tabswitcherextreme.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;

/**
 * @author Khande
 *         Created on 18/7/17.
 **/
public abstract class BaseSwitchTabAction extends AnAction implements DumbAware {


    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        Project project = e.getProject();
        e.getPresentation().setEnabled(project != null);
    }
}
