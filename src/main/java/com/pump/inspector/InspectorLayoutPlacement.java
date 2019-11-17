package com.pump.inspector;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;

import com.pump.inspector.Inspector.Position;

/**
 * This is the blueprint describing exactly how wide the columns/rows in an
 * Inspector should be.
 * <p>
 * This may be used either to calculate the minimum/preferred size, or to
 * actually position all the inspector components.
 */
class InspectorLayoutPlacement {
	static class RowInfo {
		int minimumRowHeight;
		int leadVerticalPadding;
		int mainVerticalPadding;
		int sharedBaseline;
		Insets borderInsets;
	}

	protected final JPanel parent;
	protected final InspectorRowPanel[] rows;
	protected final RowInfo[] rowInfos;

	/**
	 * The minimum column width for all leading components (the left-most
	 * column)
	 */
	protected int leadWidth = 0;

	/**
	 * The minimum column width for all other components (the right-most column)
	 */
	protected int mainWidth = 0;

	protected int totalWidth;

	protected int totalHeight;

	protected float totalVerticalWeight = 0;
	protected Map<JComponent, Insets> insets = new HashMap<>();
	protected Insets borderInsets;

	public InspectorLayoutPlacement(Inspector inspector, JPanel parent) {
		this.parent = parent;
		rows = getRows(parent);
		rowInfos = new RowInfo[rows.length];
		boolean ignoreHiddenComponents = inspector.isIgnoreHiddenComponents();
		Border border = parent.getBorder();
		borderInsets = border == null ? new Insets(0, 0, 0, 0) : border
				.getBorderInsets(parent);
		totalHeight += borderInsets.top + borderInsets.bottom;
		for (int a = 0; a < rows.length; a++) {
			InspectorRowPanel row = rows[a];
			rowInfos[a] = new RowInfo();

			Border rowBorder = row.getBorder();
			rowInfos[a].borderInsets = rowBorder == null ? new Insets(0, 0, 0,
					0) : rowBorder.getBorderInsets(row);

			totalVerticalWeight += row.getInspectorRow().getRowVerticalWeight();
			JComponent lead = row.getInspectorRow().getLeadComponent();
			JComponent main = row.getInspectorRow().getMainComponent();

			if (ignoreHiddenComponents) {
				if (!row.isVisible()) {
					lead = null;
					main = null;
				} else {
					if (lead != null && !lead.isVisible())
						lead = null;
					if (main != null && !main.isVisible())
						main = null;
				}
			}

			Insets leadInsets = lead == null ? new Insets(0, 0, 0, 0)
					: inspector.getInsets(lead, Position.LEAD);
			Insets mainInsets = main == null ? new Insets(0, 0, 0, 0)
					: inspector.getInsets(main, Position.MAIN_ONLY);

			if (lead != null)
				insets.put(lead, leadInsets);
			if (main != null)
				insets.put(main, mainInsets);

			int leadBaseline = getBaseline(lead);
			int mainBaseline = getBaseline(main);

			rowInfos[a].leadVerticalPadding = 0;
			rowInfos[a].mainVerticalPadding = 0;

			rowInfos[a].sharedBaseline = -1;
			if (leadBaseline > 0 && mainBaseline > 0) {
				rowInfos[a].sharedBaseline = Math.max(leadBaseline
						+ leadInsets.top, mainBaseline + mainInsets.top)
						+ rowInfos[a].borderInsets.top;
				rowInfos[a].leadVerticalPadding = rowInfos[a].sharedBaseline
						- leadBaseline;
				rowInfos[a].mainVerticalPadding = rowInfos[a].sharedBaseline
						- mainBaseline;
			}

			Dimension leadSize = lead == null ? null : lead.getPreferredSize();
			Dimension mainSize = main == null ? null : main.getPreferredSize();

			if (leadSize != null && mainSize != null) {
				rowInfos[a].minimumRowHeight = Math.max(leadSize.height
						+ rowInfos[a].leadVerticalPadding + leadInsets.top
						+ leadInsets.bottom, mainSize.height
						+ rowInfos[a].mainVerticalPadding + mainInsets.top
						+ mainInsets.bottom)
						+ rowInfos[a].borderInsets.top
						+ rowInfos[a].borderInsets.bottom;
				leadWidth = Math.max(leadWidth, leadSize.width
						+ leadInsets.left + leadInsets.right
						+ rowInfos[a].borderInsets.left);
				mainWidth = Math.max(mainWidth, mainSize.width
						+ mainInsets.left + mainInsets.right
						+ rowInfos[a].borderInsets.right);
			} else if (leadSize != null) {
				rowInfos[a].minimumRowHeight = leadSize.height
						+ rowInfos[a].leadVerticalPadding + leadInsets.top
						+ leadInsets.bottom + rowInfos[a].borderInsets.top
						+ rowInfos[a].borderInsets.bottom;
				leadWidth = Math.max(leadWidth, leadSize.width
						+ leadInsets.left + leadInsets.right
						+ rowInfos[a].borderInsets.left);
			} else if (mainSize != null) {
				rowInfos[a].minimumRowHeight = mainSize.height
						+ rowInfos[a].mainVerticalPadding + mainInsets.top
						+ mainInsets.bottom + rowInfos[a].borderInsets.top
						+ rowInfos[a].borderInsets.bottom;
				// if the lead is null the main component gets the full width:
				totalWidth = Math.max(totalWidth, mainSize.width
						+ mainInsets.left + mainInsets.right
						+ borderInsets.left + borderInsets.right
						+ rowInfos[a].borderInsets.left
						+ rowInfos[a].borderInsets.right);
			} else {
				// this might happen if the row is not visible
				rowInfos[a].minimumRowHeight = 0;
			}

			if (!row.isVisible()) {
				rowInfos[a].minimumRowHeight = 0;
			}

			totalWidth = Math.max(totalWidth, leadWidth + mainWidth
					+ borderInsets.left + borderInsets.right);
			totalHeight += rowInfos[a].minimumRowHeight;
		}
	}

	private int getBaseline(JComponent c) {
		if (c != null) {
			Dimension d = c.getPreferredSize();
			int b = c.getBaseline(d.width, d.height);
			if (b > 0) {
				return b;
			}
		}
		return -1;
	}

	private InspectorRowPanel[] getRows(JPanel parent) {
		List<InspectorRowPanel> rows = new ArrayList<>(
				parent.getComponentCount());
		for (Component child : parent.getComponents()) {
			if (child instanceof InspectorRowPanel)
				rows.add((InspectorRowPanel) child);
		}
		return rows.toArray(new InspectorRowPanel[rows.size()]);
	}

	public Dimension getPreferredSize() {
		return new Dimension(totalWidth, totalHeight);
	}

	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	public void install() {
		Dimension parentSize = parent.getSize();
		int rowWidth = parentSize.width - borderInsets.left
				- borderInsets.right;
		int remainingHeight = Math.max(0, parentSize.height - totalHeight);
		float remainingWeight = totalVerticalWeight;

		int y = borderInsets.top;
		for (int a = 0; a < rows.length; a++) {
			InspectorRowPanel row = rows[a];
			RowInfo i = rowInfos[a];

			float vertWeight = row.getInspectorRow().getRowVerticalWeight();
			int myExtraHeight = 0;
			if (vertWeight > 0 && remainingHeight > 0) {
				if (i.minimumRowHeight > 0) {
					myExtraHeight = (int) (remainingHeight * vertWeight / remainingWeight);
				}
				remainingHeight -= myExtraHeight;
				remainingWeight -= vertWeight;
			}

			int height = i.minimumRowHeight + myExtraHeight;
			row.setBounds(borderInsets.left, y, rowWidth, height);
			y += height;
		}
		for (int a = 0; a < rows.length; a++) {
			installRow(a);
		}
	}

	private void installRow(int rowIndex) {
		InspectorRowPanel row = rows[rowIndex];
		JComponent lead = row.getInspectorRow().getLeadComponent();
		JComponent main = row.getInspectorRow().getMainComponent();
		Insets mainInsets = insets.get(main);
		Insets leadInsets = insets.get(lead);

		// this can happen for non-visible components
		if (leadInsets == null)
			leadInsets = new Insets(0, 0, 0, 0);
		if (mainInsets == null)
			mainInsets = new Insets(0, 0, 0, 0);

		RowInfo i = rowInfos[rowIndex];

		if (lead != null) {
			Dimension leadSize = lead.getPreferredSize();
			int x = leadWidth - leadSize.width - leadInsets.right;
			int y;
			if (i.sharedBaseline > 0) {
				y = i.leadVerticalPadding > 0 ? i.leadVerticalPadding : 0;
			} else {
				y = (row.getHeight() - i.borderInsets.top - i.borderInsets.bottom)
						/ 2 - leadSize.height / 2 + i.borderInsets.top;
			}
			lead.setBounds(x, y, leadSize.width, leadSize.height);

			if (main != null) {
				Dimension d = main.getPreferredSize();
				int width;
				int extraWidth = mainWidth - d.width - mainInsets.left
						- mainInsets.right - i.borderInsets.right;
				float horizWeight = row.getInspectorRow()
						.getMainComponentHorizontalWeight();
				if (extraWidth < 0 || horizWeight == 0) {
					width = d.width;
				} else {
					width = d.width + (int) (horizWeight * extraWidth);
				}
				if (i.sharedBaseline > 0) {
					y = i.mainVerticalPadding > 0 ? i.mainVerticalPadding : 0;
				} else {
					y = (row.getHeight() - i.borderInsets.top - i.borderInsets.bottom)
							/ 2 - d.height / 2 + i.borderInsets.top;
				}
				main.setBounds(leadWidth - 1 + mainInsets.left, y, width,
						d.height);

			}
		} else if (main != null) {
			Dimension d = main.getPreferredSize();
			int width;
			float horizWeight = row.getInspectorRow()
					.getMainComponentHorizontalWeight();
			int extraWidth = row.getWidth() - i.borderInsets.left
					- i.borderInsets.right - d.width - mainInsets.left
					- mainInsets.right;
			if (extraWidth < 0 || horizWeight == 0) {
				width = d.width;
			} else {
				width = d.width + (int) (horizWeight * extraWidth);
			}

			main.setBounds(mainInsets.left + i.borderInsets.left,
					mainInsets.top + i.borderInsets.top, width, d.height);
		}
	}
}
