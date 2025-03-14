/*
 * NewKillDialog.java
 *
 * Copyright (c) 2009 - Jay Lawson (jaylawson39 at yahoo.com). All rights reserved.
 * Copyright (c) 2020 - The MegaMek Team. All Rights Reserved.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ. If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.gui.dialog;

import megamek.client.ui.preferences.JWindowPreference;
import megamek.client.ui.preferences.PreferencesNode;
import megamek.common.util.EncodeControl;
import mekhq.MekHQ;
import mekhq.campaign.Kill;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * @author Taharqa
 */
public class AddOrEditKillEntryDialog extends JDialog {
    private static final int ADD_OPERATION = 1;
    private static final int EDIT_OPERATION = 2;

    private JFrame frame;
    private int operationType;
    private Kill kill;
    private LocalDate date;

    private JButton btnClose;
    private JButton btnOK;
    private JLabel lblKill;
    private JTextField txtKill;
    private JLabel lblKiller;
    private JTextField txtKiller;
    private JButton btnDate;

    public AddOrEditKillEntryDialog(JFrame parent, boolean modal, UUID killerPerson, String killerUnit, LocalDate entryDate) {
        this(parent, modal, ADD_OPERATION, new Kill(killerPerson, "?", killerUnit, entryDate));
    }

    public AddOrEditKillEntryDialog(JFrame parent, boolean modal, Kill kill) {
        this(parent, modal, EDIT_OPERATION, kill);
    }

    private AddOrEditKillEntryDialog(JFrame parent, boolean modal, int operationType, Kill kill) {
        super(parent, modal);

        this.frame = parent;
        this.kill = Objects.requireNonNull(kill);
        this.date = this.kill.getDate();
        this.operationType = operationType;
        initComponents();
        setLocationRelativeTo(parent);
        setUserPreferences();
    }

    public Optional<Kill> getKill() {
        return Optional.ofNullable(kill);
    }

    private void initComponents() {
         GridBagConstraints gridBagConstraints;

        txtKill = new JTextField();
        lblKill = new JLabel();
        txtKiller = new JTextField();
        lblKiller = new JLabel();
        btnOK = new JButton();
        btnClose = new JButton();
        btnDate = new JButton();

        final ResourceBundle resourceMap = ResourceBundle.getBundle("mekhq.resources.AddOrEditKillEntryDialog",
                MekHQ.getMHQOptions().getLocale(), new EncodeControl());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form");
        if (this.operationType == ADD_OPERATION) {
            setTitle(resourceMap.getString("dialogAdd.title"));
        } else {
            setTitle(resourceMap.getString("dialogEdit.title"));
        }
        getContentPane().setLayout(new GridBagLayout());

        lblKill.setText(resourceMap.getString("lblKill.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(lblKill, gridBagConstraints);

        txtKill.setText(kill.getWhatKilled());
        txtKill.setMinimumSize(new Dimension(150, 28));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(txtKill, gridBagConstraints);

        lblKiller.setText(resourceMap.getString("lblKiller.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(lblKiller, gridBagConstraints);

        txtKiller.setText(kill.getKilledByWhat());
        txtKiller.setMinimumSize(new Dimension(150, 28));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(txtKiller, gridBagConstraints);

        btnDate.setText(MekHQ.getMHQOptions().getDisplayFormattedDate(date));
        btnDate.setName("btnDate");
        btnDate.addActionListener(evt -> changeDate());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        getContentPane().add(btnDate, gridBagConstraints);

        btnOK.setText(resourceMap.getString("btnOK.text"));
        btnOK.setName("btnOK");
        btnOK.addActionListener(this::btnOKActionPerformed);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(btnOK, gridBagConstraints);

        btnClose.setText(resourceMap.getString("btnClose.text"));
        btnClose.setName("btnClose");
        btnClose.addActionListener(this::btnCloseActionPerformed);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(btnClose, gridBagConstraints);

        pack();
    }

    @Deprecated // These need to be migrated to the Suite Constants / Suite Options Setup
    private void setUserPreferences() {
        try {
            PreferencesNode preferences = MekHQ.getMHQPreferences().forClass(AddOrEditKillEntryDialog.class);
            this.setName("dialog");
            preferences.manage(new JWindowPreference(this));
        } catch (Exception ex) {
            LogManager.getLogger().error("Failed to set user preferences", ex);
        }
    }

    private void btnOKActionPerformed(ActionEvent evt) {
        kill.setWhatKilled(txtKill.getText());
        kill.setKilledByWhat(txtKiller.getText());
        kill.setDate(date);
        this.setVisible(false);
    }

    private void btnCloseActionPerformed(ActionEvent evt) {
        kill = null;
        this.setVisible(false);
    }

    private void changeDate() {
        DateChooser dc = new DateChooser(frame, date);
        if (dc.showDateChooser() == DateChooser.OK_OPTION) {
            date = dc.getDate();
            btnDate.setText(MekHQ.getMHQOptions().getDisplayFormattedDate(date));
        }
    }
}
