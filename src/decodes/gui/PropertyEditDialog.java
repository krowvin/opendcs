/*
 *  $Id$
 */
package decodes.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.LineBorder;

import decodes.dbeditor.TimeZoneSelector;
import decodes.util.PropertySpec;

import java.awt.event.*;
import java.io.File;
import java.util.ResourceBundle;

import ilex.util.AsciiUtil;
import ilex.util.EnvExpander;
import ilex.util.Logger;
import ilex.util.StringPair;
import ilex.util.TextUtil;

/**
 * Dialog for editing a single property name and value.
 */
@SuppressWarnings("serial")
public class PropertyEditDialog 
	extends GuiDialog
{
	private static ResourceBundle genericLabels = 
		PropertiesEditDialog.getGenericLabels();
	private JButton okButton = new JButton();
	private JButton cancelButton = new JButton();
	private String name, value;
	private JComponent valueField = new JTextField();
	private BorderLayout borderLayout1 = new BorderLayout();
	private JTextField nameField = new JTextField();
	private boolean changed;
	private PropertySpec propSpec = null;
	private static JFileChooser fileChooser = null;

	/**
	 * Construct dialog with frame owner.
	 * 
	 * @param owne the owner frame
	 * @param name the property name
	 * @param value the property value edited in a JTextField
	 */
	public PropertyEditDialog(JFrame owner, String name, String value)
	{
		this(owner, name, value, null);
	}
	
	/**
	 * Construct dialog with frame owner.
	 * 
	 * @param owne the owner frame
	 * @param name the property name
	 * @param value the property value edited in a JTextField
	 * @param propSpec the Specification for this property
	 */
	public PropertyEditDialog(JFrame owner, String name, String value,
		PropertySpec propSpec)
	{
		super(owner, "Edit Property", true);
		this.setTitle(genericLabels
			.getString("PropertyEditDialog.editProperty"));
		this.propSpec = propSpec;
		init(name, value);
	}

	/**
	 * Construct dialog with dialog owner.
	 * 
	 * @param owner the owner dialog
	 * @param name the property name
	 * @param value the property value edited in a JTextField
	 */
	public PropertyEditDialog(JDialog owner, String name, String value)
	{
		this(owner, name, value, null);
	}
	
	/**
	 * Construct dialog with dialog owner.
	 * 
	 * @param owner the owner dialog
	 * @param name the property name
	 * @param value the property value edited in a JTextField
	 * @param propSpec the Specification for this property
	 */
	public PropertyEditDialog(JDialog owner, String name, String value,
		PropertySpec propSpec)
	{
		super(owner, "Edit Property", true);
		this.setTitle(genericLabels
			.getString("PropertyEditDialog.editProperty"));
		this.propSpec = propSpec;
		init(name, value);
	}
	

	/**
	 * Post-Initialize dialog with name & value.
	 * 
	 * @param name the property name
	 * @param value the property value edited in a JTextField
	 */
	private void init(String name, String value)
	{
		this.name = name;
		this.value = value;
		changed = false;
		try
		{
			jbInit();
			getRootPane().setDefaultButton(okButton);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		nameField.setText(name);
		setValue(value);
		pack();

		addWindowListener(new WindowAdapter()
		{
			boolean started = false;

			public void windowActivated(WindowEvent e)
			{
				if (!started)
					nameField.requestFocus();
				started = true;
			}
		});
	}

	/** Initializes gui components. */
	void jbInit() throws Exception
	{
		setTitle(genericLabels.getString("PropertyEditDialog.editPropertyValue"));
		JPanel mainPanel = new JPanel(borderLayout1);
		getContentPane().add(mainPanel);
		JPanel southButtonPanel = 
			new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
		mainPanel.add(southButtonPanel, BorderLayout.SOUTH);

		okButton.setText(genericLabels.getString("PropertiesEditDialog.OK"));
		okButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				okPressed();
			}
		});
		cancelButton.setText(genericLabels.getString("cancel"));
		cancelButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				cancelPressed();
			}
		});
		southButtonPanel.add(okButton, null);
		southButtonPanel.add(cancelButton, null);

		JPanel centerPropPanel = new JPanel(new GridBagLayout());
		centerPropPanel.add(new JLabel(genericLabels.getString("name") + ":"), 
			new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, 
				new Insets(10, 10, 5, 2), 0, 0));
		centerPropPanel.add(nameField, 
			new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
				new Insets(10, 0, 5, 10), 0, 0));
		
		centerPropPanel.add(new JLabel(genericLabels.getString("PropertyEditDialog.value")), 
			new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, 
				new Insets(5, 10, 5, 2), 0, 0));

		if (name != null && name.toLowerCase().contains("password"))
			valueField = new JPasswordField(10);
		else if (propSpec != null)
		{
			if (propSpec.getType().equals(PropertySpec.BOOLEAN))
			{
				valueField = new JComboBox(new String[] { "", "True", "False" });
			}
			else if (propSpec.getType().equals(PropertySpec.TIMEZONE))
				valueField = new TimeZoneSelector();
			else if (propSpec.getType().startsWith(PropertySpec.DECODES_ENUM))
			{
Logger.instance().info("decodes enum combo '" + propSpec + "'");
				String enumName = propSpec.getType().substring(2);
				EnumComboBox ecb = new EnumComboBox(enumName, "");
				if (ecb.getModel().getSize() > 0)
					valueField = ecb;
			}
			else if (propSpec.getType().equals(PropertySpec.FILENAME)
				 || propSpec.getType().equals(PropertySpec.DIRECTORY))
			{
				JButton selectButton = new JButton(genericLabels.getString("select"));
				selectButton.addActionListener(new java.awt.event.ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						selectPressed();
					}
				});
				centerPropPanel.add(selectButton, 
					new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 10), 0, 0));
			}
			else if (propSpec.getType().startsWith(PropertySpec.JAVA_ENUM))
			{
				
			}
		}

		centerPropPanel.add(valueField, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
			new Insets(5, 0, 5, 10), 0, 0));
		mainPanel.add(centerPropPanel, BorderLayout.CENTER);

		if (propSpec != null && propSpec.getDescription() != null
		 && propSpec.getDescription().length() > 0)
		{
			nameField.setEditable(false);
			String descText = AsciiUtil.wrapString(propSpec.getDescription(), 50);
			int nRows=1;
			StringBuilder sb = new StringBuilder("<html>");
			for(int i=0; i<descText.length(); i++)
				if (descText.charAt(i) == '\n')
				{
					nRows++;
					sb.append("<br>");
				}
				else
					sb.append(descText.charAt(i));
			JLabel descArea = new JLabel(sb.toString());
//			JTextArea descArea = new JTextArea(descText, nRows, 50);
//			JTextArea descArea = new JTextArea(descText);
//			descArea.setRows(nRows);
			descArea.setBorder(new LineBorder(Color.gray));
//			descArea.setEditable(false);
//			descArea.setWrapStyleWord(true);
//			descArea.setLineWrap(true);
//			descArea.setEnabled(false);
			centerPropPanel.add(descArea, new GridBagConstraints(0, 2, 3, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(20, 10, 20, 10), 0, 0));
		}
	}

	protected void selectPressed()
	{
		if (fileChooser == null)
		{
			fileChooser = new JFileChooser(
				EnvExpander.expand("$DECODES_INSTALL_DIR"));
		}
		if (propSpec.getType().equals(PropertySpec.DIRECTORY))
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		else if (propSpec.getType().equals(PropertySpec.FILENAME))
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (fileChooser.showDialog(this, 
			genericLabels.getString("select")) == JFileChooser.APPROVE_OPTION)
		{
			File f = fileChooser.getSelectedFile();
			setValue(f.getPath());
		}
	}

	/**
	 * Called when 'OK' is pressed.
	 */
	void okPressed()
	{
		if (!name.equals(nameField.getText()))
		{
			name = nameField.getText();
			changed = true;
		}
		String nv = getValueText().trim();
		if (nv.length() > 0 && propSpec != null)
		{
			if (propSpec.getType().equals(PropertySpec.INT))
			{
				try { Long.parseLong(nv); }
				catch(Exception ex)
				{
					showError("Invalid integer value '" + nv + "'!");
					return;
				}
			}
			else if (propSpec.getType().equals(PropertySpec.NUMBER))
			{
				try { Double.parseDouble(nv); }
				catch(Exception ex)
				{
					showError("Invalid number value '" + nv + "'!");
					return;
				}
			}
			else if (propSpec.getType().equals(PropertySpec.HOSTNAME))
			{
				for(int i=0; i<nv.length(); i++)
				{
					char c = nv.charAt(i);
					if (!(Character.isLetterOrDigit(c)
						|| c == '.' || c == '-' ||c == '_' || c=='.'))
					{
						showError("Invalid character '" + c + "' in hostname!");
						return;

					}
				}
			}
		}
		if (!value.equals(nv))
		{
			value = nv;
			changed = true;
		}
		closeDlg();
	}

	/**
	 * @return the result as a string pair if changes were made.
	 */
	public StringPair getResult()
	{
		if (changed)
			return new StringPair(name, value);
		else
			return null;
	}

	/** Closes dialog. */
	void closeDlg()
	{
		setVisible(false);
		dispose();
	}

	/**
	 * Called when 'Cancel' is pressed.
	 */
	void cancelPressed()
	{
		changed = false;
		closeDlg();
	}
	
	private String getValueText()
	{
		if (valueField instanceof JPasswordField)
			return new String(((JPasswordField) valueField).getPassword());
		else if (valueField instanceof JTextField)
			return ((JTextField)valueField).getText();
		else if (valueField instanceof JComboBox)
			return ((JComboBox)valueField).getSelectedItem().toString();
		else return "";
	}
	
	private void setValue(String value)
	{
		if (valueField instanceof JTextField)
			((JTextField)valueField).setText(value);
		else if (propSpec != null && propSpec.getType() == PropertySpec.BOOLEAN)
		{
			JComboBox trueFalseCombo = (JComboBox)valueField;
			if (value == null || value.trim().length() == 0)
				trueFalseCombo.setSelectedIndex(0);
			else if (TextUtil.str2boolean(value))
				trueFalseCombo.setSelectedIndex(1);
			else
				trueFalseCombo.setSelectedIndex(2);
		}
		else if (valueField instanceof JComboBox)
			((JComboBox)valueField).setSelectedItem(value);
	}
}
