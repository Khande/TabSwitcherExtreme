package org.intellij.ideaplugins.tabswitcherextreme;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;
import org.intellij.ideaplugins.tabswitcherextreme.config.TabGroupConfig;
import org.intellij.ideaplugins.tabswitcherextreme.config.TabSwitchExtremeConfigService;
import org.intellij.ideaplugins.tabswitcherextreme.utils.IJFileEditorUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

public class SwitcherDialog extends DialogWrapper implements KeyEventDispatcher{

	private static final String CONFIG_DELIMITER = "\n";

	private JPanel centerPanel;
	private JLabel path;
	private JPanel myCenterPanel;
	private JPanel myBottomPanel;

	private Project myProject;

	private ListManager mListManager;
	public boolean proceed = true;

	@NotNull
	private ListManager getListManager() {
		return mListManager;
	}


	public SwitcherDialog(@NotNull final Project project) {
		super(project);

		myProject = project;

		final Color listBackground = UIUtil.getListBackground();
		centerPanel.setBackground(listBackground);
		myBottomPanel.setBackground(listBackground);
		myCenterPanel.setBackground(listBackground);

		TabGroupConfig config = TabSwitchExtremeConfigService.getInstance(project).getState();
		String titles = config.getTitles();
		String matches = config.getMatches();
		String excludes = config.getExcludes();

		String[] titleArr = titles.split(CONFIG_DELIMITER);
		String[] matchArr = matches.split(CONFIG_DELIMITER);
		String[] excludeArr = excludes.split(CONFIG_DELIMITER);

		int validLen = Math.min(titleArr.length, matchArr.length);

		final List<VirtualFile> openFileList = IJFileEditorUtils.getOpenFileList(project);
        // TODO: 18/9/29 这段逻辑放在外面
        if (openFileList.size() < 2) {
			proceed = false; // hack to not show the dialog

			return;
		}

        List<VirtualFile> recentFileList = IJFileEditorUtils.getHistoryFileList(project);
        mListManager = new ListManager(project, recentFileList);

		for (int i = 0; i < validLen; i++) {
			String exclude = null;
			if (i < excludeArr.length) {
				exclude = excludeArr[i];
			}

			getListManager().addListDescription(titleArr[i], matchArr[i], exclude);
		}

		getListManager().generateFileLists(openFileList);

		VirtualFile latestOpenFile = IJFileEditorUtils.getLatestOpenFile(project);

		ListManager.FilePosition initialPos = getListManager().getFilePosition(latestOpenFile);

		getListManager().mDesiredIndexInList = initialPos.getIndexInList();

		path.setHorizontalAlignment(SwingConstants.RIGHT);
		path.setFont(path.getFont().deriveFont((float) 10));

		getListManager().insertIntoPanel(myCenterPanel, path);

		getListManager().setActiveListIndex(initialPos.getListIndex());
		getListManager().getActiveList().setSelectedIndex(initialPos.getIndexInList());

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);

		init();
	}

    @Nullable
	@Override
	protected JComponent createCenterPanel() {
		return centerPanel;
	}

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getID() == KeyEvent.KEY_RELEASED) {
            return true;
        }

        final int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.VK_TAB:
                if (event.isShiftDown()) {
                    Utils.log("SHIFT IS DOWN");
                    getListManager().updateSelection(ListManager.NavigateCommand.SHIFT_TAB);
                } else {
                    Utils.log("SHIFT IS UP");
                    getListManager().updateSelection(ListManager.NavigateCommand.TAB);
                }
            case KeyEvent.VK_UP:
                getListManager().updateSelection(ListManager.NavigateCommand.UP);
                break;
            case KeyEvent.VK_DOWN:
                getListManager().updateSelection(ListManager.NavigateCommand.DOWN);
                break;
            case KeyEvent.VK_RIGHT:
                getListManager().updateSelection(ListManager.NavigateCommand.RIGHT);
                break;
            case KeyEvent.VK_LEFT:
                getListManager().updateSelection(ListManager.NavigateCommand.LEFT);
                break;
            case KeyEvent.VK_PAGE_UP:
                getListManager().updateSelection(ListManager.NavigateCommand.PAGE_UP);
                break;
            case KeyEvent.VK_PAGE_DOWN:
                getListManager().updateSelection(ListManager.NavigateCommand.PAGE_DOWN);
                break;
            case KeyEvent.VK_ENTER:
                openFile();
                break;
            default:
                break;
        }
        return true;
    }

    private void openFile() {

        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);

        close(0, true);

        final VirtualFile file = getListManager().getSelectedFile();
        if (file == null || !file.isValid()) {
            return;
        }

        ApplicationManager.getApplication().runReadAction(() -> {
            FileEditorManagerEx fileEditorManagerEx = FileEditorManagerEx.getInstanceEx(myProject);
            fileEditorManagerEx.openFile(file, true);
        });

    }

}
