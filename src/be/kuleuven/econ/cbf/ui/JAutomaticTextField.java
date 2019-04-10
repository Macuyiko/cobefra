package be.kuleuven.econ.cbf.ui;

import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_ERROR_DARK;

import java.awt.Color;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * This class allows you to easily provide error checking and updates from your
 * text field. Any changes in the contents of the textfield are automatically
 * verified and the result of this verification is visually show to the user. If
 * the contents have been successfully verified, the component proceeds to
 * perform the user's {@link #valueChanged()} method.
 */
public abstract class JAutomaticTextField extends JTextField {

	private static final long serialVersionUID = 4894701676477202420L;

	public JAutomaticTextField() {
		this.setInputVerifier(new InputVerifier() {

			@Override
			public boolean verify(JComponent arg0) {
				return JAutomaticTextField.this.verify();
			}
		});
		this.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				update();
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				update();
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				update();
			}

			void update() {
				if (JAutomaticTextField.this.verify()) {
					JAutomaticTextField.this.setForeground(Color.BLACK);
					valueChanged();
				} else
					JAutomaticTextField.this.setForeground(COLOUR_ERROR_DARK);
			}
		});
	}

	protected abstract void valueChanged();

	protected abstract boolean verify();
}
