/*
* $Id$
*/
package lrgs.rtstat;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.border.Border;

import java.util.ResourceBundle;
import java.util.StringTokenizer;

import ilex.util.AsciiUtil;
import lrgs.ldds.DdsUser;

public class EditUserDialog
	extends JDialog
{
	private static ResourceBundle labels = 
		RtStat.getLabels();
	private static ResourceBundle genericLabels = 
		RtStat.getGenericLabels();
	private JPanel panel1 = new JPanel();
	private BorderLayout borderLayout1 = new BorderLayout();
	private JPanel jPanel1 = new JPanel();
	private JButton okButton = new JButton();
	private JButton cancelButton = new JButton();
	private FlowLayout flowLayout1 = new FlowLayout();
	private JPanel centerPanel = new JPanel();
	private JLabel ddsNameLabel = new JLabel();
	private JTextField ddsNameField = new JTextField();
	private JLabel ddsPasswordLabel = new JLabel();
	private JPasswordField ddsPasswordField = new JPasswordField();
	private JPanel permissionsPanel = new JPanel();
	private Border permissionsBorder = null;
	private Border restrictionsBorder = null;
	private Border propertiesBorder = null;

	private JPanel northPanel = new JPanel();
	private JLabel titleLabel = new JLabel();
	private JTextField hostField = new JTextField();
	private JCheckBox ddsPermCheck = new JCheckBox();
	private GridLayout gridLayout1 = new GridLayout();
	private JCheckBox adminCheck = new JCheckBox();
	private JLabel descLabel = new JLabel();
	private JTextField descField = new JTextField();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private GridBagLayout gridBagLayout2 = new GridBagLayout();

	private boolean wasOk = false;
	private DdsUser ddsUser;
	boolean started=false;
	private JPanel restrictionsPanel = null;
	private JPanel ipPanel = null;
	private JCheckBox ipAddrCheckbox = null;
	private JTextField ipAddrField = null;
	private JPanel dcpLimitPanel = null;
	private JCheckBox dcpLimitCheckbox = null;
	private JTextField dcpLimitField = null;
	private JCheckBox forceAscendingCheck = null;
	private JCheckBox isLocalCheck = new JCheckBox();

	public EditUserDialog(Dialog owner, String title, boolean modal)
	{
		super(owner, title, modal);
		permissionsBorder = 
			new TitledBorder(BorderFactory.createEtchedBorder(Color.white,
				new Color(30, 30, 30)), 
				labels.getString("EditUserDialog.permissions"));
		restrictionsBorder = 
			new TitledBorder(BorderFactory.createEtchedBorder(Color.white,
				new Color(30, 30, 30)), labels.getString(
						"EditUserDialog.restrictions"));
		propertiesBorder = 
			new TitledBorder(BorderFactory.createEtchedBorder(Color.white,
				new Color(30, 30, 30)), genericLabels.getString("properties"));
		forceAscendingCheck = new JCheckBox(
				labels.getString("EditUserDialog.forceAscending"));
		try
		{
			//setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			jbInit();
			pack();
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
		addWindowListener(
			new WindowAdapter()
			{
				public void windowActivated(WindowEvent e)
				{
					if (!started)
					{
						if (ddsNameField.isEnabled())
							ddsNameField.requestFocus();
						else
							ddsPasswordField.requestFocus();
					}
					started = true;
				}
			});
		getRootPane().setDefaultButton(okButton);
	}

	public EditUserDialog()
	{
		this(null, "EditUserDialog", false);
	}

	private void jbInit()
		throws Exception
	{
		panel1.setLayout(borderLayout1);
		this.setModal(true);
		this.setTitle(labels.getString("EditUserDialog.editDDSUserInfo"));
		okButton.setPreferredSize(new Dimension(100, 23));
		okButton.setText(genericLabels.getString("OK"));
		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				okButton_actionPerformed(e);
			}
		});
		cancelButton.setPreferredSize(new Dimension(100, 23));
		cancelButton.setText(genericLabels.getString("cancel"));
		cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				cancelButton_actionPerformed(e);
			}
		});
		jPanel1.setLayout(flowLayout1);
		flowLayout1.setHgap(25);
		centerPanel.setLayout(gridBagLayout2);
		ddsNameLabel.setText(labels.getString("EditUserDialog.DDSUserName"));
		ddsNameField.setPreferredSize(new Dimension(120, 20));
		ddsNameField.setToolTipText(labels.getString(
				"EditUserDialog.DDSUserNameTT"));
		ddsNameField.setText("");
		ddsPasswordLabel.setText(labels.getString(
				"EditUserDialog.DDSPassword"));
		ddsPasswordField.setPreferredSize(new Dimension(120, 22));
		ddsPasswordField.setToolTipText(labels.getString(
			"EditUserDialog.DDSPasswordTT"));
		ddsPasswordField.setText("");

		descLabel.setText(genericLabels.getString("description")+":");
		descField.setToolTipText(labels.getString(
				"EditUserDialog.descriptionTT"));
		descField.setText("");
		
		isLocalCheck.setText(labels.getString("UserListDialog.localUserColumn"));

		permissionsPanel.setBorder(permissionsBorder);
		permissionsPanel.setLayout(gridLayout1);
		titleLabel.setText(labels.getString("EditUserDialog.DDSUserHost"));
		hostField.setPreferredSize(new Dimension(150, 20));
		hostField.setEditable(false);
		hostField.setText("");
		ddsPermCheck.setToolTipText(
			labels.getString("EditUserDialog.DDSPermCheckTT"));
		ddsPermCheck.setSelected(true);
		ddsPermCheck.setText(labels.getString(
				"EditUserDialog.retrDCPMsgs"));
		gridLayout1.setColumns(1);
		gridLayout1.setRows(2);
		adminCheck.setToolTipText(labels.getString(
			"EditUserDialog.administratorTT"));
		adminCheck.setText(labels.getString("EditUserDialog.administrator"));
		northPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		jPanel1.setBorder(BorderFactory.createRaisedBevelBorder());
		getContentPane().add(panel1);
		panel1.add(jPanel1, java.awt.BorderLayout.SOUTH);
		jPanel1.add(okButton);
		jPanel1.add(cancelButton);
		panel1.add(centerPanel, java.awt.BorderLayout.CENTER);
		panel1.add(northPanel, java.awt.BorderLayout.NORTH);
		northPanel.add(titleLabel);
		northPanel.add(hostField);
		permissionsPanel.add(ddsPermCheck);
		permissionsPanel.add(adminCheck);

		centerPanel.add(ddsNameLabel,
			new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(10, 15, 3, 2), 0, 0));
		centerPanel.add(ddsNameField,
			new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, 
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(10, 0, 3, 15), 0, 0));
		centerPanel.add(ddsPasswordLabel,
			new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(3, 15, 3, 2), 0, 0));
		centerPanel.add(ddsPasswordField,
			new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, 
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(3, 0, 3, 15), 0, 0));
		centerPanel.add(descLabel,
			new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(3, 15, 3, 2), 0, 0));
		centerPanel.add(descField,
			new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, 
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(3, 0, 3, 15), 0, 0));
		centerPanel.add(isLocalCheck,
			new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, 
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(3, 0, 10, 15), 0, 0));

		centerPanel.add(permissionsPanel, 
			new GridBagConstraints(0, 4, 2, 1, 1.0, 1.0, 
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 5, 5, 5), 0, 0));
		centerPanel.add(getRestrictionsPanel(), 
			new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 5, 5, 5), 0, 0));

		ipAddrCheckbox.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					ipAddrCheckbox_actionPerformed();
				}
			});
		dcpLimitCheckbox.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					dcpLimitCheckbox_actionPerformed();
				}
			});
	}

	public void okButton_actionPerformed(ActionEvent e)
	{
		ddsUser.userName = ddsNameField.getText().trim();
		if (ddsUser.userName.length() == 0)
		{
			JOptionPane.showMessageDialog(this,
           		AsciiUtil.wrapString(labels.getString(
           				"EditUserDialog.noUserNameErr"), 60),
				"Error!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		String pw = ddsPasswordField.getText().trim();
		if (pw.length() > 0)
			ddsUser.hasPassword = true;
		ddsUser.perms = null;
		if (ddsPermCheck.isSelected())
			ddsUser.addPerm("dds");
		if (adminCheck.isSelected())
			ddsUser.addPerm("admin");

		if (!ipAddrCheckbox.isSelected())
			ddsUser.setIpAddr(null);
		else
			ddsUser.setIpAddr(ipAddrField.getText().trim());

		String limstr = dcpLimitField.getText().trim();
		if (!dcpLimitCheckbox.isSelected() || limstr.length() == 0)
			ddsUser.dcpLimit = -1;
		else
		{
			try { ddsUser.dcpLimit = Integer.parseInt(limstr); }
			catch(NumberFormatException ex)
			{
				JOptionPane.showMessageDialog(this,
           			AsciiUtil.wrapString(labels.getString(
						"EditUserDialog.DCPLimitNumErr"), 60),
						"Error!", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		ddsUser.desc = descField.getText().trim();
		if (ddsUser.desc.length() == 0)
			ddsUser.desc = null;

		ddsUser.forceAscending = forceAscendingCheck.isSelected();
		
		ddsUser.isLocal = isLocalCheck.isSelected();

		setVisible(false);
		wasOk = true;
	}

	public void cancelButton_actionPerformed(ActionEvent e)
	{
		setVisible(false);
	}

	public void set(String host, DdsUser ddsUser, boolean pwOnly, 
		boolean modUserName)
	{
		this.ddsUser = ddsUser;
		ddsNameField.setText(ddsUser.userName);
		ddsNameField.setEnabled(modUserName);
		ddsPasswordField.setText("");
		hostField.setText(host);
		ddsPermCheck.setSelected(ddsUser.hasPerm("dds"));
		ddsPermCheck.setEnabled(!pwOnly);
		adminCheck.setSelected(ddsUser.hasPerm("admin"));
		ddsPermCheck.setEnabled(!pwOnly);
		forceAscendingCheck.setSelected(ddsUser.forceAscending);
		forceAscendingCheck.setEnabled(!pwOnly);
		isLocalCheck.setSelected(ddsUser.isLocal);
		isLocalCheck.setEnabled(modUserName);

		if (ddsUser.desc == null)
			descField.setText("");
		else
			descField.setText(ddsUser.desc);
		descField.setEnabled(!pwOnly);

		wasOk = false;
		started = false;

		String ipa = ddsUser.getIpAddr();
		if (ipa == null || ipa.trim().length() == 0)
		{
			ipAddrCheckbox.setSelected(false);
			ipAddrField.setText("");
			ipAddrField.setEnabled(false);
		}
		else
		{
			ipAddrCheckbox.setSelected(true);
			ipAddrField.setText(ipa);
			ipAddrField.setEnabled(true);
		}

		if (ddsUser.dcpLimit == -1)
		{
			dcpLimitCheckbox.setSelected(false);
			dcpLimitField.setText("");
			dcpLimitField.setEnabled(false);
		}
		else
		{
			dcpLimitCheckbox.setSelected(true);
			dcpLimitField.setText("" + ddsUser.dcpLimit);
			dcpLimitField.setEnabled(true);
		}
	}

	public boolean okPressed()
	{
		return wasOk;
	}

	public String getPassword()
	{
		String ret = ddsPasswordField.getText().trim();
		ddsPasswordField.setText("");
		return ret;
	}

	/**
	 * This method initializes restrictionsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getRestrictionsPanel() 
	{
		if (restrictionsPanel == null) 
		{
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(3);
			gridLayout.setColumns(1);
			restrictionsPanel = new JPanel();
			restrictionsPanel.setLayout(gridLayout);
			restrictionsPanel.setBorder(restrictionsBorder);
			restrictionsPanel.add(getIpPanel(), null);
			restrictionsPanel.add(getDcpLimitPanel(), null);
			forceAscendingCheck.setSelected(false);
			restrictionsPanel.add(forceAscendingCheck, null);
		}
		return restrictionsPanel;
	}

	/**
	 * This method initializes ipPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getIpPanel() {
		if (ipPanel == null) {
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints3.gridx = 1;
			gridBagConstraints3.gridy = 0;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.anchor = GridBagConstraints.EAST;
			gridBagConstraints3.insets = new Insets(5, 0, 5, 10);
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.insets = new Insets(5, 5, 5, 2);
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.anchor = GridBagConstraints.EAST;
			gridBagConstraints2.gridx = 0;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.gridx = 1;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			ipPanel = new JPanel();
			ipPanel.setLayout(new GridBagLayout());
			ipPanel.add(getIpAddrCheckbox(), gridBagConstraints2);
			ipPanel.add(getIpAddrField(), gridBagConstraints3);
		}
		return ipPanel;
	}

	/**
	 * This method initializes ipAddrCheckbox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getIpAddrCheckbox() {
		if (ipAddrCheckbox == null) {
			ipAddrCheckbox = new JCheckBox();
			ipAddrCheckbox.setText(labels.getString("EditUserDialog.iPAddr"));
			ipAddrCheckbox.setSelected(false);
		}
		return ipAddrCheckbox;
	}

	/**
	 * This method initializes ipAddrField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getIpAddrField() 
	{
		if (ipAddrField == null) 
		{
			ipAddrField = new JTextField();
			ipAddrField.setToolTipText(labels.getString(
					"EditUserDialog.iPAddrTT"));
			ipAddrField.setEnabled(false);
		}
		return ipAddrField;
	}

	/**
	 * This method initializes dcpLimitPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getDcpLimitPanel() {
		if (dcpLimitPanel == null) {
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints5.gridy = 0;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.anchor = GridBagConstraints.EAST;
			gridBagConstraints5.insets = new Insets(5, 0, 5, 10);
			gridBagConstraints5.gridx = 1;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.anchor = GridBagConstraints.EAST;
			gridBagConstraints4.insets = new Insets(5, 5, 5, 2);
			gridBagConstraints4.gridy = 0;
			dcpLimitPanel = new JPanel();
			dcpLimitPanel.setLayout(new GridBagLayout());
			dcpLimitCheckbox = new JCheckBox();
			dcpLimitCheckbox.setText(labels.getString(
					"EditUserDialog.DCPRTLimit"));
			dcpLimitCheckbox.setSelected(false);
			dcpLimitPanel.add(dcpLimitCheckbox, gridBagConstraints4);
			dcpLimitField = new JTextField();
			dcpLimitField.setEnabled(false);
			dcpLimitPanel.add(dcpLimitField, gridBagConstraints5);
		}
		return dcpLimitPanel;
	}

	private void ipAddrCheckbox_actionPerformed()
	{
		ipAddrField.setEnabled(ipAddrCheckbox.isSelected());
	}

	private void dcpLimitCheckbox_actionPerformed()
	{
		dcpLimitField.setEnabled(dcpLimitCheckbox.isSelected());
	}
}
