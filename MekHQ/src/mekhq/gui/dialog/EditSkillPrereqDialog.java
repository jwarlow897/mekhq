/*
 * NewSkillPrereqDialog.java
 *
 * Copyright (c) 2009 Jay Lawson (jaylawson39 at yahoo.com). All rights reserved.
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
import mekhq.MekHQ;
import mekhq.campaign.personnel.SkillPrereq;
import mekhq.campaign.personnel.SkillType;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;

/**
 * @author Taharqa
 */
public class EditSkillPrereqDialog extends JDialog {
    private SkillPrereq prereq;

    private JButton btnClose;
    private JButton btnOK;
    private boolean cancelled;

    private Hashtable<String, JComboBox<String>> skillLevels = new Hashtable<>();
    private Hashtable<String, JCheckBox> skillChks = new Hashtable<>();

    public EditSkillPrereqDialog(Frame parent, SkillPrereq pre) {
        super(parent, true);
        cancelled = false;
        prereq = pre;
        initComponents();
        setLocationRelativeTo(parent);
        setUserPreferences();
    }

    private void initComponents() {

        btnOK = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();

        JPanel panMain = new JPanel(new GridLayout(SkillType.skillList.length,2));

        JCheckBox chkSkill;
        JComboBox<String> choiceLvl;
        DefaultComboBoxModel<String> skillLvlModel;
        for (int i = 0; i < SkillType.getSkillList().length; i++) {
            final String type = SkillType.getSkillList()[i];
            chkSkill = new JCheckBox(type);
            chkSkill.setSelected(prereq.getSkillLevel(type) > -1);
            skillChks.put(type, chkSkill);
            chkSkill.addItemListener(e -> changeLevelEnabled(type));

            skillLvlModel = new DefaultComboBoxModel<>();
            skillLvlModel.addElement("None");
            skillLvlModel.addElement(SkillType.getExperienceLevelName(SkillType.EXP_GREEN));
            skillLvlModel.addElement(SkillType.getExperienceLevelName(SkillType.EXP_REGULAR));
            skillLvlModel.addElement(SkillType.getExperienceLevelName(SkillType.EXP_VETERAN));
            skillLvlModel.addElement(SkillType.getExperienceLevelName(SkillType.EXP_ELITE));
            choiceLvl = new JComboBox<>(skillLvlModel);
            choiceLvl.setEnabled(chkSkill.isSelected());
            int lvl = prereq.getSkillLevel(type);
            if (lvl < 0) {
                lvl = 0;
            }
            choiceLvl.setSelectedIndex(lvl);

            skillLevels.put(type, choiceLvl);
            panMain.add(chkSkill);
            panMain.add(choiceLvl);
        }

        JPanel panButtons = new JPanel(new GridLayout(0,2));
        btnOK.setText("Done");
        btnOK.addActionListener(evt -> done());

        btnClose.setText("Cancel");
        btnClose.addActionListener(evt -> cancel());

        panButtons.add(btnOK);
        panButtons.add(btnClose);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Select Abilities");
        getContentPane().setLayout(new BorderLayout());

        getContentPane().add(new JScrollPane(panMain), BorderLayout.CENTER);
        getContentPane().add(panButtons, BorderLayout.SOUTH);

        this.setPreferredSize(new Dimension(400,700));

        pack();
    }

    @Deprecated // These need to be migrated to the Suite Constants / Suite Options Setup
    private void setUserPreferences() {
        try {
            PreferencesNode preferences = MekHQ.getMHQPreferences().forClass(EditSkillPrereqDialog.class);
            this.setName("dialog");
            preferences.manage(new JWindowPreference(this));
        } catch (Exception ex) {
            LogManager.getLogger().error("Failed to set user preferences", ex);
        }
    }

    private void done() {
        prereq = new SkillPrereq();
        for (String type : SkillType.skillList) {
            if (skillChks.get(type).isSelected()) {
                prereq.addPrereq(type, skillLevels.get(type).getSelectedIndex());
            }
        }
        this.setVisible(false);
    }

    public SkillPrereq getPrereq() {
        return prereq;
    }

    private void cancel() {
        this.setVisible(false);
        cancelled = true;
    }

    public boolean wasCancelled() {
        return cancelled;
    }

    private void changeLevelEnabled(String type) {
        skillLevels.get(type).setEnabled(skillChks.get(type).isSelected());
    }
}
