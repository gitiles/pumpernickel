package com.pump.swing.popover;

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ListSelectionVisibility<T extends JComponent> implements
		PopoverVisibility<T> {
	static class PopoverListSelectionListener implements ListSelectionListener {
		JPopover<?> popover;

		PopoverListSelectionListener(JPopover<?> popover) {
			this.popover = popover;
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			popover.refreshVisibility(false);
		}

	};

	static class PopoverHierarchyListener implements HierarchyListener {
		JPopover<?> popover;

		PopoverHierarchyListener(JPopover<?> popover) {
			this.popover = popover;
		}

		@Override
		public void hierarchyChanged(HierarchyEvent e) {
			popover.refreshVisibility(false);
		}

	}

	JList<?> list;
	Collection<Object> expectedSelection;
	Map<JPopover<T>, PopoverListSelectionListener> selectionListenerMap = new HashMap<>();
	Map<JPopover<T>, PopoverHierarchyListener> hierarchyListenerMap = new HashMap<>();

	public ListSelectionVisibility(JList<?> list, Object... expectedSelection) {
		this.list = list;
		this.expectedSelection = new HashSet<>(Arrays.asList(expectedSelection));
	}

	@Override
	public boolean isVisible(JPopover<T> popover) {
		Collection<Object> currentSelection2 = new HashSet<>(
				list.getSelectedValuesList());
		return currentSelection2.equals(expectedSelection)
				&& popover.getOwner().isShowing();
	}

	@Override
	public void install(JPopover<T> popover) {
		PopoverListSelectionListener l1 = selectionListenerMap.get(popover);
		if (l1 == null) {
			l1 = new PopoverListSelectionListener(popover);
			selectionListenerMap.put(popover, l1);
			list.getSelectionModel().addListSelectionListener(l1);
		}

		PopoverHierarchyListener l2 = hierarchyListenerMap.get(popover);
		if (l2 == null) {
			l2 = new PopoverHierarchyListener(popover);
			hierarchyListenerMap.put(popover, l2);
			list.addHierarchyListener(l2);
		}
	}

	@Override
	public void uninstall(JPopover<T> popover) {
		PopoverListSelectionListener l1 = selectionListenerMap.remove(popover);
		if (l1 == null) {
			list.getSelectionModel().removeListSelectionListener(l1);
		}

		PopoverHierarchyListener l2 = hierarchyListenerMap.remove(popover);
		if (l2 == null) {
			list.removeHierarchyListener(l2);
		}
	}

}
