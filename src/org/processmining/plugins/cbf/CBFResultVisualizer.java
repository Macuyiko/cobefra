package org.processmining.plugins.cbf;

import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_ERROR_LIGHT;
import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_ITEM_BACKGROUND;
import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_OK_LIGHT;
import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_WARNING_LIGHT;
import static be.kuleuven.econ.cbf.ui.UISettings.MARGIN;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.Iterator;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import be.kuleuven.econ.cbf.result.CBFResult;
import be.kuleuven.econ.cbf.result.CBFResult.InputID;
import be.kuleuven.econ.cbf.result.CBFResult.MetricID;
import be.kuleuven.econ.cbf.result.CBFResult.ResultValue;
import be.kuleuven.econ.cbf.result.CBFResult.Status;
import be.kuleuven.econ.cbf.ui.process.ErrorFrame;

@Plugin(
		name = "CBF ResultValue Visualizer",
		parameterLabels = { "CBF ResultValue" },
		returnLabels = "CBF ResultValue Visual",
		returnTypes = JComponent.class,
		userAccessible = true)
@Visualizer
public class CBFResultVisualizer {

	@PluginVariant(
			variantLabel = "CBF ResultValue Visualizer",
			requiredParameterLabels = { 0 })
	public JComponent visualize(UIPluginContext context, CBFResult result) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		
		return makeResultTable(result);
	}
	
	public JComponent makeResultTable(CBFResult result) {
		final JTable table = new JTable(new CBFModel(result));
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setDefaultRenderer(InputID.class, new InputIDCellRenderer());
		table.setDefaultRenderer(ResultValue.class, new ResultValueCell());
		table.setDefaultEditor(ResultValue.class, new ResultValueCell());
		TableColumnModel model = table.getColumnModel();
		for (int i = 0; i < model.getColumnCount(); i++)
			model.getColumn(i).setMinWidth(200);
		JScrollPane pane = new JScrollPane(table);
		return pane;
	}
}

class CBFModel implements TableModel {

	private String[] columns;
	private InputID[] rows;
	private ResultValue[][] content;

	public CBFModel(CBFResult result) {
		MetricID[] metrics = result.getMetricIDs();
		columns = new String[metrics.length];
		for (int i = 0; i < metrics.length; i++)
			columns[i] = metrics[i].metric;

		rows = new InputID[result.getNbInputIDs()];
		content = new ResultValue[result.getNbInputIDs()][metrics.length];
		Iterator<InputID> i = result.getInputIDIterator();
		int row = 0;
		while (i.hasNext()) {
			InputID input = i.next();
			rows[row] = input;
			int col = 0;
			for (MetricID metric : metrics) {
				content[row][col] = result.getValue(input, metric);
				col++;
			}
			row++;
		}
	}

	@Override
	public void addTableModelListener(TableModelListener arg0) {
		// throw new UnsupportedOperationException();
	}

	@Override
	public Class<?> getColumnClass(int row) {
		if (row == 0)
			return InputID.class;
		else
			return ResultValue.class;
	}

	@Override
	public int getColumnCount() {
		return columns.length + 1;
	}

	@Override
	public String getColumnName(int arg0) {
		if (arg0 == 0)
			return null;
		return columns[arg0 - 1];
	}

	@Override
	public int getRowCount() {
		return rows.length;
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (col == 0)
			return rows[row];
		else
			return content[row][col - 1];
	}

	@Override
	public boolean isCellEditable(int arg0, int arg1) {
		return true;
	}

	@Override
	public void removeTableModelListener(TableModelListener arg0) {
		// throw new UnsupportedOperationException();
	}

	@Override
	public void setValueAt(Object arg0, int arg1, int arg2) {
		// throw new UnsupportedOperationException();
	}
}

class InputIDCellRenderer implements TableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		InputID input = (InputID) value;
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(MARGIN / 2, MARGIN,
				MARGIN / 2, MARGIN));
		panel.setBackground(COLOUR_ITEM_BACKGROUND);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(Box.createVerticalStrut(0));

		String log = input.logfile;
		String net = input.netfile;
		int iLog = log.lastIndexOf(File.separatorChar);
		int iNet = net.lastIndexOf(File.separatorChar);
		String shortLog = log.substring(iLog + 1);
		String shortNet = net.substring(iNet + 1);

		panel.add(Box.createVerticalGlue());
		panel.add(new JLabel(shortLog));
		panel.add(Box.createVerticalGlue());
		panel.add(new JLabel(shortNet));
		panel.add(Box.createVerticalGlue());
		panel.setToolTipText("<html>" + log + "<br>" + net + "</html>");

		if (table.getRowHeight() < panel.getPreferredSize().height)
			table.setRowHeight(panel.getPreferredSize().height);

		return panel;
	}
}

class ResultValueCell extends AbstractCellEditor implements TableCellEditor,
		TableCellRenderer {

	public Component createComponent(JTable table, Object value) {
		final ResultValue result = (ResultValue) value;
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(MARGIN / 2, MARGIN,
				MARGIN / 2, MARGIN));
		panel.setLayout(new GridBagLayout());
		switch (result.status) {
		case OK:
			panel.setBackground(COLOUR_OK_LIGHT);
			break;
		case WARNING:
			panel.setBackground(COLOUR_WARNING_LIGHT);
			break;
		case ERROR:
			panel.setBackground(COLOUR_ERROR_LIGHT);
			break;
		case SUBMETRIC:
			panel.setBackground(COLOUR_ITEM_BACKGROUND);
		}
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 0.5;

		c.gridx = 0;
		c.anchor = GridBagConstraints.WEST;

		c.gridy = 0;
		panel.add(new JLabel("result:"), c);
		if (result.status != Status.SUBMETRIC) {
			c.gridy = 1;
			panel.add(new JLabel("runtime:"), c);
		}
		c.gridy = 2;
		panel.add(new JLabel("status:"), c);

		c.gridx = 1;
		c.anchor = GridBagConstraints.EAST;

		c.gridy = 0;
		panel.add(new JLabel(Double.toString(result.result)), c);
		if (result.status != Status.SUBMETRIC) {
			c.gridy = 1;
			panel.add(new JLabel(result.runtime + "s"), c);
		}
		c.gridy = 2;
		panel.add(new JLabel(result.status.name()), c);

		if (result.errors != null && result.errors.length() != 0) {
			c.gridy = 3;
			c.gridx = 0;
			c.gridwidth = 2;
			c.weightx = 1.0;
			c.anchor = GridBagConstraints.CENTER;
			JLabel lblErrors = new JLabel("click to show errors");
			lblErrors.setFont(lblErrors.getFont().deriveFont(8.0f));
			panel.add(lblErrors, c);
		}

		if (table.getRowHeight() < panel.getPreferredSize().height)
			table.setRowHeight(panel.getPreferredSize().height);

		return panel;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		return createComponent(table, value);
	}

	@Override
	public Object getCellEditorValue() {
		return null;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		cancelCellEditing();
		ResultValue result = (ResultValue) value;
		if (result.errors != null && result.errors.length() > 0)
			new ErrorFrame(result.errors);
		return null;
	}
}