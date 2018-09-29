package org.intellij.ideaplugins.tabswitcherextreme;

import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.IconUtil;
import org.apache.http.util.TextUtils;
import org.intellij.ideaplugins.tabswitcherextreme.utils.IJCollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListManager {

	private static final int NO_INDEX = -1;

	private Project mProject;
	private List<VirtualFile> mRecentFiles;

	@NotNull
	private final List<ListDescription> mListDescriptions;
	private int mActiveListIndex = 0;
	public int mDesiredIndexInList;

	public ListManager(@NotNull final Project project, @NotNull final List<VirtualFile> recentFiles) {
		mListDescriptions = new ArrayList<>();
		mProject = project;
		mRecentFiles = recentFiles;
	}


	public void generateFileLists(List<VirtualFile> allFiles) {
		// copy the list, and if found one, remove from shadowlist. If any left, make a new list with leftovers
		allFiles.sort(Comparator.comparing(VirtualFile::getName));

		List<VirtualFile> controlList = new ArrayList<>(allFiles.size());
		controlList.addAll(allFiles);
		List<String> controlListStrings = new ArrayList<>(allFiles.size());
		for (VirtualFile f : controlList) {
			controlListStrings.add(f.getPath());
		}

		List<ListDescription> removeList = new ArrayList<ListDescription>();

		for (ListDescription desc : mListDescriptions) {
			List<VirtualFile> filtered = new ArrayList<VirtualFile>();
			for (VirtualFile f : allFiles) {
				String filename = f.getPath();
				// don't keep doubles
				if (controlListStrings.contains(filename)) {
					if (filename.matches(desc.myMatch)) {
						filtered.add(f);
						controlList.remove(f);
						controlListStrings.remove(filename);
					}
				}
			}
			if (filtered.isEmpty()) {
				removeList.add(desc);
			} else {
				desc.setGroupedFileList(filtered);
			}
		}

		for (ListDescription desc : removeList) {
			mListDescriptions.remove(desc);
		}

		// check if we have some lost souls
		if (!controlList.isEmpty()) {
			ListDescription leftovers = new ListDescription("Other", ".*", ".xml");
			leftovers.setGroupedFileList(controlList);
			mListDescriptions.add(leftovers);
		}
	}

	public JList getActiveList() {
		return getListFromIndex(mActiveListIndex);
	}

	public int getActiveListIndex() {
		return mActiveListIndex;
	}

	public void setActiveListIndex(int listIndex) {
		mActiveListIndex = Utils.modulo(listIndex, getListCount());
	}

	public int getListCount() {
		return mListDescriptions.size();
	}

	public JList getListFromIndex(int index) {
		int correctedIndex = Utils.modulo(index, getListCount());
		return mListDescriptions.get(correctedIndex).mList;
	}

	public void addListDescription(String title, String match, String exclude) {
		ListDescription desc = new ListDescription(title, match, exclude);
		mListDescriptions.add(desc);
	}

	@Nullable
	public VirtualFile getSelectedFile() {
		return (VirtualFile) getActiveList().getSelectedValue();
	}

	public void insertIntoPanel(JPanel panel, JLabel pathLabel) {


		int rows = 2; // header + list
		int cols = getListCount() * 2 - 1; // separator between filenamelist

		GridLayoutManager manager = new GridLayoutManager(rows, cols);
		panel.setLayout(manager);

		// add them in
		for (int i = 0; i < getListCount(); i++) {
			int rowNr = i;

			ListDescription desc = mListDescriptions.get(i);

			// label
			GridConstraints labelConstraints = new GridConstraints();
			labelConstraints.setRow(0);
			labelConstraints.setColumn(rowNr);

			panel.add(desc.getTitleLabel(), labelConstraints);

			// list
			GridConstraints listConstraints = new GridConstraints();
			listConstraints.setRow(1);
			listConstraints.setColumn(rowNr);
			listConstraints.setFill(GridConstraints.FILL_VERTICAL);
			panel.add(desc.getList(), listConstraints);

			setListData(desc, desc.getGroupedFileList(), pathLabel);
		}
	}

	private void setListData(ListManager.ListDescription desc, final List<VirtualFile> filtered, JLabel pathLabel) {
		desc.mList.setModel(new AbstractListModel() {
			public int getSize() {
				return filtered.size();
			}

			public Object getElementAt(int index) {
				return filtered.get(index);
			}
		});

		desc.mList.setCellRenderer(getRenderer(mProject));
		desc.mList.getSelectionModel().addListSelectionListener(getListener(desc.mList, pathLabel));
		desc.mList.setVisibleRowCount(filtered.size());
	}

	private static ListSelectionListener getListener(final JList list, final JLabel path) {
		return new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						updatePath(list, path);
					}
				});
			}
		};
	}
	private static void updatePath(JList list, JLabel path) {
		String text = " ";
		final Object[] values = list.getSelectedValues();
		if ((values != null) && (values.length == 1)) {
			final VirtualFile parent = ((VirtualFile) values[0]).getParent();
			if (parent != null) {
				text = parent.getPresentableUrl();
				final FontMetrics metrics = path.getFontMetrics(path.getFont());
				while ((metrics.stringWidth(text) > path.getWidth()) &&
					(text.indexOf(File.separatorChar, 4) > 0)) {
					text = "..." + text.substring(text.indexOf(File.separatorChar, 4));
				}
			}
		}
		path.setText(text);
	}

	private static ListCellRenderer getRenderer(final Project project) {
		return new ColoredListCellRenderer() {
			@Override
			protected void customizeCellRenderer(JList list, Object value, int index,
												 boolean selected, boolean hasFocus) {
				if (value instanceof VirtualFile) {
					final VirtualFile file = (VirtualFile) value;
					setIcon(IconUtil.getIcon(file, Iconable.ICON_FLAG_READ_STATUS, project));

					final FileStatus status = FileStatusManager.getInstance(project).getStatus(file);
					final TextAttributes attributes =
						new TextAttributes(status.getColor(), null, null,
							EffectType.LINE_UNDERSCORE, Font.PLAIN);

					String filename = file.getName();
					String remover = (( ListDescription.JMyList) list).exclude;
					if (null != remover) {
						filename = filename.replaceAll(remover, "");
					}

					append(filename, SimpleTextAttributes.fromTextAttributes(attributes));
				}
			}
		};
	}

	public static class ListDescription {

		private static class JMyList extends JList {
			String exclude;
		}

		private JLabel myTitleLabel;
		private String myMatch;

		private JMyList mList;
		@NotNull
		private final List<VirtualFile> myGroupedFileList = new ArrayList<>();

		public ListDescription(String title, String match, String exclude ) {
			myMatch = match;

			//noinspection UndesirableClassUsage
			mList = new JMyList();
			if (!TextUtils.isBlank(exclude)) {
				mList.exclude = exclude;
			}

			myTitleLabel = new JLabel("<html><b>" + title + "</b></html>", SwingConstants.CENTER);

		}

		public void setGroupedFileList(@Nullable final List<VirtualFile> groupedFileList) {
			if (!IJCollectionUtils.isNullOrEmpty(groupedFileList)) {
				myGroupedFileList.clear();
				myGroupedFileList.addAll(groupedFileList);
			}
		}

		@NotNull
		public List<VirtualFile> getGroupedFileList() {
			return myGroupedFileList;
		}

		public JList getList() {
			return mList;
		}

		public JLabel getTitleLabel() {
			return myTitleLabel;
		}
	}

	public void updateSelection(NavigateCommand nav) {
		int previousListIndex = getActiveListIndex();
		JList previousList = getActiveList();
		int previousIndexInList = previousList.getSelectedIndex();

		// logic is done in here
		FilePosition targetFilePosition = getTargetFilePosition(previousListIndex, previousIndexInList, nav);

		// no move possible? Just abort
		if (targetFilePosition == null) {
			return;
		}

		JList nextList = getListFromIndex(targetFilePosition.getListIndex());
		int nextIndexInList = targetFilePosition.getIndexInList();
		nextList.setSelectedIndex(nextIndexInList);
		nextList.ensureIndexIsVisible(nextIndexInList);

		if (targetFilePosition.getListIndex() != previousListIndex ) {
			setActiveListIndex(targetFilePosition.getListIndex());
			// clear the previous one
			previousList.clearSelection();
		}
	}

	@Nullable
	private VirtualFile findNextRecentFile(@NotNull final Project project,
										   @Nullable final VirtualFile curFile,
										   final boolean wantNewer) {

		List<VirtualFile> recentFilesList = new ArrayList<>(mRecentFiles);

		if (!wantNewer) {
			Utils.log("want older");
			Collections.reverse(recentFilesList);
		} else {
			Utils.log("want newer");
		}

		int curIndex = -1;
		int size = recentFilesList.size();
		for (int i = 0; i < size; i++) {
			VirtualFile file = recentFilesList.get(i);
			if (file.equals(curFile)) {
				curIndex = i;
				break;
			}
		}

		final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
		for (int i = curIndex + 1; i < size; i++) {
			VirtualFile file = recentFilesList.get(i);
			if (fileEditorManager.isFileOpen(file)) {
				return file;
			}
		}

		return null;
	}


	private FilePosition getNextInHistory(final boolean wantNewer) {
		VirtualFile cur = getSelectedFile();
		VirtualFile other = findNextRecentFile(mProject, cur, wantNewer);

		return getFilePosition(other);
	}

	private FilePosition getTargetFilePosition(int curListIndex, int curIndexInList, NavigateCommand navCmd) {
		// selection is in a certain list, press a key, figure out where the next selection will be.
		// loop around to this current list again = no action
		if (navCmd == NavigateCommand.TAB) {
			Utils.log("TAB");
			return getNextInHistory(false);
		}
		if (navCmd == NavigateCommand.SHIFT_TAB) {
			Utils.log("SHIFT_TAB");
			return getNextInHistory(true);
		}

		// if targetList is empty, try one beyond
		if (curIndexInList == -1) {
			return null;
		}

		// up or down
		if (navCmd == NavigateCommand.UP || navCmd == NavigateCommand.DOWN) {
			JList curList = getListFromIndex(curListIndex);
			int size = curList.getModel().getSize();

			int offset = navCmd == NavigateCommand.DOWN ? 1 : -1;
			Utils.log("Offset: " + offset);
			int newIndexInList = Utils.modulo(curIndexInList + offset, size);
			if (newIndexInList == curIndexInList) {
				return null;
			}

			mDesiredIndexInList = newIndexInList;

			return new FilePosition(curListIndex, newIndexInList);

		} else if (navCmd == NavigateCommand.LEFT || navCmd == NavigateCommand.RIGHT) {
			int direction = navCmd == NavigateCommand.LEFT ? -1 : 1;

			int targetListIndex = curListIndex;
			//Utils.log("we zittne op lijst " + curListIndex);

			// find the first list that is not empty, in specified direction
			int targetIndexInList;
			do {
				targetListIndex = Utils.modulo(targetListIndex + direction, getListCount());
				//Utils.log("Wat zou de index zijn op " + targetListIndex);
				//targetIndexInList = getActualTargetIndexInOtherList(targetListIndex, curIndexInList);
				targetIndexInList = getActualTargetIndexInOtherList(targetListIndex, mDesiredIndexInList);

				//Utils.log("  nou, " + targetIndexInList);
			} while (targetIndexInList == -1);

			if (targetListIndex != curListIndex) {
				return new FilePosition(targetListIndex, targetIndexInList);
			}

		} else if (navCmd == NavigateCommand.PAGE_UP || navCmd == NavigateCommand.PAGE_DOWN) {
			JList curList = getListFromIndex(curListIndex);
			int targetIndexInList;
			if (navCmd == NavigateCommand.PAGE_UP) {
				targetIndexInList = 0;
			} else {
				targetIndexInList = curList.getModel().getSize() - 1;
			}
			mDesiredIndexInList = targetIndexInList;
			if (targetIndexInList != curIndexInList) {
				return new FilePosition(curListIndex, targetIndexInList);
			}
		}

		return null;
	}

	private int getActualTargetIndexInOtherList(int listIndex, int requestedIndexInList) {
		// returns -1 if empty
		JList targetList = getListFromIndex(listIndex);
		int size = targetList.getModel().getSize();

		if (size == 0) {
			return -1;
		} else {
			return Math.min( size-1, Math.max(0, requestedIndexInList));
		}
	}

	public FilePosition getFilePosition(@Nullable final VirtualFile file) {
		int listIndex = 0;
		int indexInList = 0;

		if (file != null) {
			Utils.log("get position for: " + file.getName());
			int foundListIndex = NO_INDEX;
			int foundIndexInList = NO_INDEX;
			int size = mListDescriptions.size();
			for (int i = 0; i< size; i++) {
				List<VirtualFile> filteredFileList = mListDescriptions.get(i).getGroupedFileList();

				int index = filteredFileList.indexOf(file);
				if (index > 0) {
					foundListIndex = i;
					foundIndexInList = index;
					break;
				}
			}

			if (foundIndexInList != NO_INDEX) {
				listIndex = foundListIndex;
				indexInList = foundIndexInList;
			}
		}

		return new FilePosition(listIndex, indexInList);
	}

	static class FilePosition {
		
		private int myListIndex;
		private int myIndexInList;

		FilePosition(int listIndex, int indexInList) {
			myListIndex = listIndex;
			myIndexInList = indexInList;
		}

		int getListIndex() {
			return myListIndex;
		}

		int getIndexInList() {
			return myIndexInList;
		}
	}

	public enum NavigateCommand {
		LEFT,
		RIGHT,
		UP,
		DOWN,
		PAGE_UP,
		PAGE_DOWN,
		TAB,
		SHIFT_TAB
	}
}

