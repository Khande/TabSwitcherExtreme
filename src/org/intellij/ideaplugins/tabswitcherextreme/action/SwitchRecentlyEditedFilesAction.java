/*
 * Copyright (c) 2008-2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland & Bas Leijdekkers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.ideaplugins.tabswitcherextreme.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.ideaplugins.tabswitcherextreme.SwitcherDialog;
import org.intellij.ideaplugins.tabswitcherextreme.utils.IJFileEditorUtils;

import java.util.List;

public class SwitchRecentlyEditedFilesAction extends BaseSwitchTabAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = event.getProject();

        if (project == null) {
            return;
        }

        List<VirtualFile> recentlyEditedFileList = IJFileEditorUtils.getRecentlyEditedFileList(project);
        recentlyEditedFileList = ContainerUtil.filter(recentlyEditedFileList, file -> file != null && file.isValid());

        if (!recentlyEditedFileList.isEmpty()) {
            SwitcherDialog.show("Select Recently Edited File", project, recentlyEditedFileList);
        }
    }

}