package org.intellij.ideaplugins.tabswitcherextreme.utils;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.ex.IdeDocumentHistory;
import com.intellij.openapi.fileEditor.impl.EditorHistoryManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * @author Khande
 *         Created on 18/7/17.
 **/
public final class IJFileEditorUtils {

    private IJFileEditorUtils() {
        //no instance
    }


    /**
     * 或得当前 project 打开的文件列表
     *
     * @param project The project
     */
    @NotNull
    public static List<VirtualFile> getOpenFileList(@NotNull final Project project) {

        final FileEditorManager manager = FileEditorManager.getInstance(project);
        final VirtualFile[] openFiles = manager.getOpenFiles();

        List<VirtualFile> openFileList = new ArrayList<>(openFiles.length);
        Collections.addAll(openFileList, openFiles);

        return openFileList;
    }


    /**
     * 获取当前项目历史打开的文件列表，包含当前打开的文件。
     * 最早打开的文件在最前面；如果一个文件被打开多次，只算最后一次打开
     *
     * @param project The project
     */
    @NotNull
    public static List<VirtualFile> getHistoryFileList(@NotNull final Project project) {
        EditorHistoryManager editorHistoryManager = EditorHistoryManager.getInstance(project);
        // a set of valid files that are in the history, oldest first.
        // if the file occurs several times in the history, only its last occurrence counts
        LinkedHashSet<VirtualFile> fileSet = editorHistoryManager.getFileSet();
        return new ArrayList<>(fileSet);
    }


    @NotNull
    public static List<VirtualFile> getRecentlyEditedFileList(@NotNull final Project project) {
        VirtualFile[] changedFiles = IdeDocumentHistory.getInstance(project).getChangedFiles();
        return ContainerUtil.addAll(new ArrayList<>(changedFiles.length), changedFiles);
    }


    @Nullable
    public static VirtualFile getLatestOpenFile(@NotNull final Project project) {
        List<VirtualFile> historyFileList = getHistoryFileList(project);
        if (historyFileList.isEmpty()) {
            return null;
        }

        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        int size = historyFileList.size();
        for (int i = size - 1 ; i >= 0; i--) {
            VirtualFile file = historyFileList.get(i);
            if (fileEditorManager.isFileOpen(file)) {
                return file;
            }
        }

        return null;
    }

}
