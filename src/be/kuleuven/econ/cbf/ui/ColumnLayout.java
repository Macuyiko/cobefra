package be.kuleuven.econ.cbf.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;

public class ColumnLayout implements LayoutManager2 {

	private int hgap;
	private Component[] components;

	public ColumnLayout(int nbColumns, int hgap) {
		this.hgap = hgap;
		components = new Component[nbColumns];
	}

	@Override
	public void addLayoutComponent(String arg0, Component arg1) {
		for (int i = 0; i < components.length; i++)
			if (components[i] == null) {
				components[1] = arg1;
				break;
			}
	}

	@Override
	public void layoutContainer(Container arg0) {
		Component[] components;
		synchronized (arg0.getTreeLock()) {
			components = arg0.getComponents();
		}
		Insets in = arg0.getInsets();
		int width = arg0.getWidth();
		width -= in.left + in.right;
		width -= hgap * (components.length - 1);
		width /= components.length;
		int height = 0;
		for (int i = 0; i < components.length; i++) {
			if (components[i] == null)
				continue;
			Dimension d = components[i].getPreferredSize();
			if (d.height > height)
				height = d.height;
		}
		for (int i = 0; i < components.length; i++) {
			int x = i * (width + hgap) + in.left;
			int y = in.top;
			if (components[i] != null)
				components[i].setBounds(x, y, width, height);
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container arg0) {
		Component[] components;
		synchronized (arg0.getTreeLock()) {
			components = arg0.getComponents();
		}
		Insets in = arg0.getInsets();
		int maxWidth = 0;
		int maxHeight = 0;
		for (int i = 0; i < components.length; i++) {
			if (components[i] == null)
				continue;
			Dimension d = components[i].getMinimumSize();
			if (d.width > maxWidth)
				maxWidth = d.width;
			if (d.height > maxHeight)
				maxHeight = d.height;
		}
		int width = maxWidth * components.length + (components.length - 1)
				* hgap + in.right + in.left;
		return new Dimension(width, maxHeight + in.top + in.bottom);
	}

	@Override
	public Dimension preferredLayoutSize(Container arg0) {
		Component[] components;
		synchronized (arg0.getTreeLock()) {
			components = arg0.getComponents();
		}
		Insets in = arg0.getInsets();
		int maxWidth = 0;
		int maxHeight = 0;
		for (int i = 0; i < components.length; i++) {
			if (components[i] == null)
				continue;
			Dimension d = components[i].getPreferredSize();
			if (d.width > maxWidth)
				maxWidth = d.width;
			if (d.height > maxHeight)
				maxHeight = d.height;
		}
		int width = maxWidth * components.length + (components.length - 1)
				* hgap + in.right + in.left;
		return new Dimension(width, maxHeight + in.top + in.bottom);
	}

	@Override
	public void removeLayoutComponent(Component arg0) {
		for (int i = 0; i < components.length; i++)
			if (components[i] == arg0)
				components[i] = null;
	}

	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
		addLayoutComponent((String) constraints, comp);
	}

	@Override
	public float getLayoutAlignmentX(Container target) {
		return 0.5f;
	}

	@Override
	public float getLayoutAlignmentY(Container target) {
		return 1.0f;
	}

	@Override
	public void invalidateLayout(Container target) {
	}

	@Override
	public Dimension maximumLayoutSize(Container target) {
		Component[] components;
		synchronized (target.getTreeLock()) {
			components = target.getComponents();
		}
		int height = 0;
		for (int i = 0; i < components.length; i++) {
			if (components[i] == null)
				continue;
			Dimension d = components[i].getPreferredSize();
			if (d.height > height)
				height = d.height;
		}
		Insets in = target.getInsets();
		height += in.top + in.bottom;
		return new Dimension(Integer.MAX_VALUE, height);
	}
}
