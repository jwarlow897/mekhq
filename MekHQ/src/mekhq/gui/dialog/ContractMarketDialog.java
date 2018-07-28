/*
 * ContractMarketDialog.java
 *
 * Copyright (c) 2014 Carl Spain. All rights reserved.
 *
 * This file is part of MekHQ.
 *
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MekHQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ.  If not, see <http://www.gnu.org/licenses/>.
 */

package mekhq.gui.dialog;

/**
 * Presents contract offers generated by ContractMarket
 * 
 * Code borrowed heavily from PersonnelMarketDialog
 * 
 * @author Neoancient
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import megamek.common.util.EncodeControl;
import mekhq.campaign.Campaign;
import mekhq.campaign.finances.Transaction;
import mekhq.campaign.market.ContractMarket;
import mekhq.campaign.mission.AtBContract;
import mekhq.campaign.mission.Contract;
import mekhq.campaign.mission.Mission;
import mekhq.campaign.universe.Faction;
import mekhq.gui.FactionComboBox;
import mekhq.gui.view.ContractSummaryPanel;

public class ContractMarketDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2285074545510057268L;
	
	/* Save these settings between instantiations */
	private static boolean payMRBC = true;
	private static int advance = 25;
	private static int signingBonus = 0;
	private static int sharePct = 20;
	
	private Campaign campaign;
	private ContractMarket contractMarket;
	private Contract selectedContract = null;
	private ArrayList<String> possibleRetainerContracts;
	
	private JScrollPane scrollTableContracts;
	private JScrollPane scrollContractView;
	private JPanel panelTable;
	private JPanel panelFees;
	private JPanel panelRetainer;
	private JPanel panelOKBtns;
	private ContractSummaryPanel contractView;
	private JButton btnGenerate;
	private JButton btnRemove;
	private JButton btnAccept;
	private JButton btnClose;
	private JSplitPane splitMain;
    
	private JCheckBox chkMRBC;
	private JLabel lblSigningBonus;
	private JSpinner spnSigningBonus;
	private JLabel lblAdvance;
	private JSpinner spnAdvance;
	private JLabel lblSharePct;
	private JSpinner spnSharePct;
	private JTable tblContracts;
	private JLabel lblCurrentRetainer;
	private JLabel lblRetainerEmployer;
	private JButton btnEndRetainer;
	private JLabel lblRetainerAvailable;
	private FactionComboBox cbRetainerEmployer;
	private JButton btnStartRetainer;
	
	public ContractMarketDialog(Frame frame, Campaign c) {
		super(frame, true);
		campaign = c;
		contractMarket = c.getContractMarket();
		possibleRetainerContracts = new ArrayList<String>();
		if (c.getFactionCode().equals("MERC")) {
			countSuccessfulContracts();
		}
		
        initComponents();
        setLocationRelativeTo(frame);
	}

	/* A balance of six or more successful contracts with the same
	 * employer results in the offer of a retainer contract.
	 */
	private void countSuccessfulContracts() {
		HashMap<String, Integer> successfulContracts = new HashMap<String, Integer>();
		for (Mission m : campaign.getMissions()) {
			if (m.isActive() || !(m instanceof AtBContract) ||
					((AtBContract)m).getEmployerCode().equals(campaign.getRetainerEmployerCode())) {
				continue;
			}
			AtBContract contract = (AtBContract)m;
			int num;
			if (successfulContracts.keySet().contains(contract.getEmployerCode())) {
				num = successfulContracts.get(contract.getEmployerCode());
			} else {
				num = 0;
			}
			successfulContracts.put(contract.getEmployerCode(),
					num + ((contract.getStatus() == Mission.S_SUCCESS)?1:-1));
		}
		for (String key : successfulContracts.keySet()) {
			if (successfulContracts.get(key) >= 6) {
				possibleRetainerContracts.add(key);
			}
		}
	}
	
	private void initComponents() {
		scrollTableContracts = new JScrollPane();
		scrollContractView = new JScrollPane();
		panelTable = new JPanel();
		panelFees = new JPanel();
		panelRetainer = new JPanel();
		panelOKBtns = new JPanel();
		contractView = null;
		btnGenerate = new JButton();
		btnRemove = new JButton();
		btnAccept = new JButton();
		btnClose = new JButton();

		chkMRBC = new JCheckBox();
		chkMRBC.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				payMRBC = chkMRBC.isSelected();
				for (Contract c : contractMarket.getContracts()) {
					c.setMRBCFee(payMRBC);
					c.calculateContract(campaign);
				}
				if (contractView != null) {
					contractView.refreshAmounts();
				}
			}
		});
		lblAdvance = new JLabel();
		spnAdvance = new JSpinner(new SpinnerNumberModel(advance, 0, 25, 5));
		spnAdvance.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				advance = (Integer)spnAdvance.getValue();
				for (Contract c : contractMarket.getContracts()) {
					c.setAdvancePct(advance);
					c.calculateContract(campaign);
				}
				if (contractView != null) {
					contractView.refreshAmounts();
				}
			}
		});
		lblSigningBonus = new JLabel();
		spnSigningBonus = new JSpinner(new SpinnerNumberModel(signingBonus, 0, 10, 1));
		spnSigningBonus.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				signingBonus = (Integer)spnSigningBonus.getValue();
				for (Contract c : contractMarket.getContracts()) {
					c.setSigningBonusPct(signingBonus);
					c.calculateContract(campaign);
				}
				if (contractView != null) {
					contractView.refreshAmounts();
				}
			}
		});
		
		lblSharePct = new JLabel();
		spnSharePct = new JSpinner(new SpinnerNumberModel(sharePct, 20, 50, 10));		
		spnSharePct.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				sharePct = (Integer)spnSharePct.getValue();
				for (Contract c : contractMarket.getContracts()) {
			    	if (campaign.getCampaignOptions().getUseAtB() &&
			    			campaign.getCampaignOptions().getUseShareSystem() &&
			    			c instanceof AtBContract) {
			    		((AtBContract)c).setSharesPct(sharePct);
						c.calculateContract(campaign);
			    	}
				}
				if (contractView != null) {
					contractView.refreshAmounts();
				}
			}
		});
		
		tblContracts = new JTable();
		
		lblCurrentRetainer = new JLabel();
		lblRetainerEmployer = new JLabel();
		btnEndRetainer = new JButton();
		lblRetainerAvailable = new JLabel();
		cbRetainerEmployer = new FactionComboBox();
		btnStartRetainer = new JButton();

		ResourceBundle resourceMap = ResourceBundle.getBundle("mekhq.resources.ContractMarketDialog", new EncodeControl()); //$NON-NLS-1$
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle(resourceMap.getString("Form.title")); // NOI18N
		setName("Form"); // NOI18N
		getContentPane().setLayout(new BorderLayout());

		scrollTableContracts.setMinimumSize(new java.awt.Dimension(500, 400));
		scrollTableContracts.setName("scrollTableContracts"); // NOI18N
		scrollTableContracts.setPreferredSize(new java.awt.Dimension(500, 400));

		chkMRBC.setName("chkMRBC");
		chkMRBC.setText(resourceMap.getString("checkMRBC.text"));
		chkMRBC.setSelected(payMRBC);
		panelFees.add(chkMRBC);

		lblAdvance.setText(resourceMap.getString("lblAdvance.text"));
		panelFees.add(lblAdvance);
		panelFees.add(spnAdvance);
		lblSigningBonus.setText(resourceMap.getString("lblSigningBonus.text"));
		panelFees.add(lblSigningBonus);
		panelFees.add(spnSigningBonus);		
		lblSharePct.setText(resourceMap.getString("lblSharePct.text"));
		if (campaign.getCampaignOptions().getUseShareSystem()) {
			panelFees.add(lblSharePct);
			panelFees.add(spnSharePct);
		}

		Vector<Vector<String>> data = new Vector<Vector<String>>();
		Vector<String> colNames = new Vector<String>();
		for (Contract c : contractMarket.getContracts()) {
			/* Changes in rating or force size since creation can alter some
			 * details
			 */
			if (c instanceof AtBContract) {
				((AtBContract)c).initContractDetails(campaign);
				((AtBContract)c).calculatePaymentMultiplier(campaign);
				((AtBContract)c).calculatePartsAvailabilityLevel(campaign);
				((AtBContract)c).setSharesPct(campaign.getCampaignOptions().getUseShareSystem()?
						(Integer)spnSharePct.getValue():0);
			}
			c.setStartDate(null);
			c.setMRBCFee(payMRBC);
			c.setAdvancePct(advance);
			c.setSigningBonusPct(signingBonus);
			
			c.calculateContract(campaign);
			Vector<String> row = new Vector<String>();
			if (c instanceof AtBContract) {
				row.add(((AtBContract)c).getEmployerName(campaign.getGameYear()));
				row.add(((AtBContract)c).getEnemyName(campaign.getGameYear()));
				if (((AtBContract)c).isSubcontract()) {
					row.add(((AtBContract)c).getMissionTypeName() + " (Subcontract)");
				} else {
					row.add(((AtBContract)c).getMissionTypeName());
				}
			} else {
				row.add(c.getEmployer());
				row.add("");
				row.add(c.getType());
			}
			data.add(row);
		}
		colNames.add("Employer");
		colNames.add("Enemy");
		colNames.add("Mission Type");
		tblContracts.setModel(new DefaultTableModel(data, colNames));
		tblContracts.setName("tblContracts"); // NOI18N
		tblContracts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblContracts.createDefaultColumnsFromModel();
		tblContracts.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		tblContracts.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				if (!evt.getValueIsAdjusting()) {
					contractChanged();
				}
			}
		});

        tblContracts.setIntercellSpacing(new Dimension(0, 0));
        tblContracts.setShowGrid(false);
        scrollTableContracts.setViewportView(tblContracts);
        
        scrollContractView.setMinimumSize(new java.awt.Dimension(500, 600));
        scrollContractView.setPreferredSize(new java.awt.Dimension(500, 600));
        scrollContractView.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollContractView.setViewportView(null);
        
        panelTable.setLayout(new BorderLayout());
        panelTable.add(panelFees, BorderLayout.PAGE_START);
        panelTable.add(scrollTableContracts, BorderLayout.CENTER);
       	panelTable.add(panelRetainer, BorderLayout.PAGE_END);
        
        panelRetainer.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        lblCurrentRetainer.setText(resourceMap.getString("lblCurrentRetainer.text"));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        panelRetainer.add(lblCurrentRetainer, gbc);
        if (null != campaign.getRetainerEmployerCode()) {
        	lblRetainerEmployer.setText(Faction.getFaction(campaign.getRetainerEmployerCode()).getFullName(campaign.getGameYear()));
        }
        gbc.gridx = 1;
        gbc.gridy = 0;
        panelRetainer.add(lblRetainerEmployer, gbc);
        btnEndRetainer.setText(resourceMap.getString("btnEndRetainer.text"));
        gbc.gridx = 0;
        gbc.gridy = 1;
        panelRetainer.add(btnEndRetainer, gbc);
        lblCurrentRetainer.setVisible(null != campaign.getRetainerEmployerCode());
        lblRetainerEmployer.setVisible(null != campaign.getRetainerEmployerCode());
        btnEndRetainer.setVisible(null != campaign.getRetainerEmployerCode());
        btnEndRetainer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				campaign.setRetainerEmployerCode(null);
		        lblCurrentRetainer.setVisible(false);
		        lblRetainerEmployer.setVisible(false);
		        btnEndRetainer.setVisible(false);
		        //Add faction back to available ones
		        countSuccessfulContracts();
		        lblRetainerAvailable.setVisible(possibleRetainerContracts.size() > 0);
		        cbRetainerEmployer.setVisible(possibleRetainerContracts.size() > 0);
		        btnStartRetainer.setVisible(possibleRetainerContracts.size() > 0);
			}
        });
        
        lblRetainerAvailable.setText(resourceMap.getString("lblRetainerAvailable.text"));
        gbc.gridx = 0;
        gbc.gridy = 2;
        panelRetainer.add(lblRetainerAvailable, gbc);
        cbRetainerEmployer.addFactionEntries(possibleRetainerContracts, campaign.getGameYear());
        gbc.gridx = 1;
        gbc.gridy = 2;
        panelRetainer.add(cbRetainerEmployer, gbc);
        btnStartRetainer.setText(resourceMap.getString("btnStartRetainer.text"));
        gbc.gridx = 0;
        gbc.gridy = 3;
        panelRetainer.add(btnStartRetainer, gbc);
        lblRetainerAvailable.setVisible(possibleRetainerContracts.size() > 0);
        cbRetainerEmployer.setVisible(possibleRetainerContracts.size() > 0);
        btnStartRetainer.setVisible(possibleRetainerContracts.size() > 0);
        btnStartRetainer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				campaign.setRetainerEmployerCode(cbRetainerEmployer.getSelectedItemKey());
		        lblCurrentRetainer.setVisible(true);
		        lblRetainerEmployer.setVisible(true);
		        btnEndRetainer.setVisible(true);
	        	lblRetainerEmployer.setText(Faction.getFaction(campaign.getRetainerEmployerCode()).getFullName(campaign.getGameYear()));
		        //Remove the selected faction and add the previous one, if any
		        countSuccessfulContracts();
		        lblRetainerAvailable.setVisible(possibleRetainerContracts.size() > 0);
		        cbRetainerEmployer.setVisible(possibleRetainerContracts.size() > 0);
		        btnStartRetainer.setVisible(possibleRetainerContracts.size() > 0);
			}        	
        });
                
        splitMain = new javax.swing.JSplitPane(JSplitPane.HORIZONTAL_SPLIT,panelTable, scrollContractView);
        splitMain.setOneTouchExpandable(true);
        splitMain.setResizeWeight(0.0);
        getContentPane().add(splitMain, BorderLayout.CENTER);
          
        panelOKBtns.setLayout(new java.awt.GridBagLayout());
        
        btnGenerate.setText(resourceMap.getString("btnGenerate.text"));
        btnGenerate.setName("btnGenerate"); // NOI18N
        btnGenerate.addActionListener(new java.awt.event.ActionListener() {
        	public void actionPerformed(java.awt.event.ActionEvent evt) {
        		AtBContract c = contractMarket.addAtBContract(campaign);
        		
        		if(c == null) {
        		    campaign.addReport(resourceMap.getString("report.UnabletoGMContract"));
        		    return;
        		}
        		
        		c.initContractDetails(campaign);
        		c.calculatePartsAvailabilityLevel(campaign);
        		c.setSharesPct(campaign.getCampaignOptions().getUseShareSystem()?
        				(Integer)spnSharePct.getValue():0);
        		c.setStartDate(null);
        		c.setMRBCFee(payMRBC);
        		c.setAdvancePct(advance);
        		c.setSigningBonusPct(signingBonus);

        		c.calculateContract(campaign);
        		Vector<String> row = new Vector<String>();
        		row.add(((AtBContract)c).getEmployerName(campaign.getGameYear()));
        		row.add(((AtBContract)c).getEnemyName(campaign.getGameYear()));
        		row.add(((AtBContract)c).getMissionTypeName());

        		((DefaultTableModel)tblContracts.getModel()).addRow(row);
        	}
        });
        btnGenerate.setEnabled(campaign.isGM());
        panelOKBtns.add(btnGenerate, new java.awt.GridBagConstraints());
        
        btnRemove.setText(resourceMap.getString("btnRemove.text"));
        btnRemove.setName("btnRemove"); // NOI18N
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
        	public void actionPerformed(java.awt.event.ActionEvent evt) {
    	    	contractMarket.removeContract(selectedContract);
    	    	((DefaultTableModel)tblContracts.getModel()).removeRow(tblContracts.convertRowIndexToModel(tblContracts.getSelectedRow()));
        	}
        });
        panelOKBtns.add(btnRemove, new java.awt.GridBagConstraints());
        
        btnAccept.setText(resourceMap.getString("btnAccept.text"));
        btnAccept.setName("btnAccept"); // NOI18N
        btnAccept.addActionListener(new java.awt.event.ActionListener() {
        	public void actionPerformed(java.awt.event.ActionEvent evt) {
        		acceptContract(evt);
        	}
        });
        panelOKBtns.add(btnAccept, new java.awt.GridBagConstraints());
        
        btnClose.setText(resourceMap.getString("btnClose.text")); // NOI18N
        btnClose.setName("btnClose"); // NOI18N
        btnClose.addActionListener(new java.awt.event.ActionListener() {
        	public void actionPerformed(java.awt.event.ActionEvent evt) {
        		btnCloseActionPerformed(evt);
        	}
        });
        panelOKBtns.add(btnClose, new java.awt.GridBagConstraints());

        getContentPane().add(panelOKBtns, BorderLayout.PAGE_END);

        pack();
	}

	public Contract getContract() {
	    return selectedContract;
	}
	
	private void acceptContract(ActionEvent evt) {
	    if(selectedContract != null) {
	    	selectedContract.setName(contractView.getContractName());
	    	campaign.getFinances().credit(selectedContract.getTotalAdvanceMonies(), Transaction.C_CONTRACT, "Advance monies for " + selectedContract.getName(), campaign.getCalendar().getTime());
	    	campaign.addMission(selectedContract);
	    	contractMarket.removeContract(selectedContract);
	    	((DefaultTableModel)tblContracts.getModel()).removeRow(tblContracts.convertRowIndexToModel(tblContracts.getSelectedRow()));
	    	refreshContractView();
	    }
	}
	
	private void btnCloseActionPerformed(ActionEvent evt) {
	    selectedContract = null;
	    setVisible(false);
	}
	
    private void contractChanged() {
        int view = tblContracts.getSelectedRow();
        if(view < 0) {
            //selection got filtered away
            selectedContract = null;
            refreshContractView();
            return;
        }
	   	 /* preserve the name given to the previous contract (if any) */
	   	 if (selectedContract != null && contractView != null) {
	   		 selectedContract.setName(contractView.getContractName());
	   	 }
   	 
        selectedContract = contractMarket.getContracts().get(tblContracts.convertRowIndexToModel(view));
        refreshContractView();
    }

     void refreshContractView() {
    	 int row = tblContracts.getSelectedRow();
         if(row < 0) {
        	 contractView = null;
             scrollContractView.setViewportView(null);
             return;
         }
         contractView = new ContractSummaryPanel(selectedContract, campaign,
        		 campaign.getCampaignOptions().getUseAtB() &&
        		 selectedContract instanceof AtBContract &&
        		 !((AtBContract)selectedContract).isSubcontract());
    	 scrollContractView.setViewportView(contractView);
 		//This odd code is to make sure that the scrollbar stays at the top
 		//I cant just call it here, because it ends up getting reset somewhere later
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
 			public void run() { 
 				scrollContractView.getVerticalScrollBar().setValue(0);
 			}
 		});
    }

}
