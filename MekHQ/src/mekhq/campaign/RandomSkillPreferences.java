/*
 * RandomSkillPreferences.java
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
package mekhq.campaign;

import megamek.Version;
import mekhq.utilities.MHQXMLUtility;
import mekhq.campaign.personnel.enums.PersonnelRole;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;

/**
 * @author Jay Lawson
 */
public class RandomSkillPreferences {
    private int overallRecruitBonus;
    private int[] recruitBonuses;
    private boolean randomizeSkill;
    private boolean useClanBonuses;
    private int antiMekProb;
    private int[] specialAbilBonus;
    private int combatSmallArmsBonus;
    private int supportSmallArmsBonus;
    private int[] tacticsMod;
    private int artilleryProb;
    private int artilleryBonus;
    private int secondSkillProb;
    private int secondSkillBonus;

    //special abilities

    public RandomSkillPreferences() {
        overallRecruitBonus = 0;
        recruitBonuses = new int[PersonnelRole.values().length];
        randomizeSkill = true;
        useClanBonuses = true;
        antiMekProb = 10;
        combatSmallArmsBonus = -3;
        supportSmallArmsBonus = -10;
        specialAbilBonus = new int[]{-10,-10,-2,0,1};
        tacticsMod = new int[]{-10,-10,-7,-4,-1};
        artilleryProb = 10;
        artilleryBonus = -2;
        secondSkillProb = 0;
        secondSkillBonus = -4;
    }

    public int getOverallRecruitBonus() {
        return overallRecruitBonus;
    }

    public void setOverallRecruitBonus(int b) {
        overallRecruitBonus = b;
    }

    public int[] getRecruitBonuses() {
        return recruitBonuses;
    }

    public int getRecruitBonus(PersonnelRole role) {
        return getRecruitBonuses()[role.ordinal()];
    }

    public void setRecruitBonus(int index, int bonus) {
        recruitBonuses[index] = bonus;
    }

    public int getSpecialAbilBonus(int type) {
        return (type < specialAbilBonus.length) ? specialAbilBonus[type] : 0;
    }

    public void setSpecialAbilBonus(int type, int bonus) {
        if (type < specialAbilBonus.length) {
            specialAbilBonus[type] = bonus;
        }
    }

    public void setRandomizeSkill(boolean b) {
        this.randomizeSkill = b;
    }

    public boolean randomizeSkill() {
        return randomizeSkill;
    }

    public void setUseClanBonuses(boolean b) {
        this.useClanBonuses = b;
    }

    public boolean useClanBonuses() {
        return useClanBonuses;
    }

    public int getAntiMekProb() {
        return antiMekProb;
    }

    public void setAntiMekProb(int b) {
        antiMekProb = b;
    }

    public int getCombatSmallArmsBonus() {
        return combatSmallArmsBonus;
    }

    public void setCombatSmallArmsBonus(int b) {
        combatSmallArmsBonus = b;
    }

    public int getSupportSmallArmsBonus() {
        return supportSmallArmsBonus;
    }

    public void setSupportSmallArmsBonus(int b) {
        supportSmallArmsBonus = b;
    }

    public int getTacticsMod(int lvl) {
        return tacticsMod[lvl];
    }

    public void setTacticsMod(int lvl, int bonus) {
        if (lvl < tacticsMod.length) {
            tacticsMod[lvl] = bonus;
        }
    }

    public void setArtilleryProb(int b) {
        this.artilleryProb = b;
    }

    public int getArtilleryProb() {
        return artilleryProb;
    }

    public void setArtilleryBonus(int b) {
        this.artilleryBonus = b;
    }

    public int getArtilleryBonus() {
        return artilleryBonus;
    }

    public void setSecondSkillProb(int b) {
        this.secondSkillProb = b;
    }

    public int getSecondSkillProb() {
        return secondSkillProb;
    }

    public void setSecondSkillBonus(int b) {
        this.secondSkillBonus = b;
    }

    public int getSecondSkillBonus() {
        return secondSkillBonus;
    }

    public void writeToXml(PrintWriter pw1, int indent) {
        MHQXMLUtility.writeSimpleXMLOpenIndentedLine(pw1, indent++, "randomSkillPreferences");
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "overallRecruitBonus", overallRecruitBonus);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "recruitBonuses", StringUtils.join(recruitBonuses, ','));
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "specialAbilBonus", StringUtils.join(specialAbilBonus, ','));
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "tacticsMod", StringUtils.join(tacticsMod, ','));
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "randomizeSkill", randomizeSkill);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "useClanBonuses", useClanBonuses);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "antiMekProb", antiMekProb);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "combatSmallArmsBonus", combatSmallArmsBonus);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "supportSmallArmsBonus", supportSmallArmsBonus);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "artilleryProb", artilleryProb);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "artilleryBonus", artilleryBonus);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "secondSkillProb", secondSkillProb);
        MHQXMLUtility.writeSimpleXmlTag(pw1, indent, "secondSkillBonus", secondSkillBonus);
        MHQXMLUtility.writeSimpleXMLCloseIndentedLine(pw1, --indent, "randomSkillPreferences");
    }

    public static RandomSkillPreferences generateRandomSkillPreferencesFromXml(Node wn, Version version) {
        LogManager.getLogger().debug("Loading Random Skill Preferences from XML...");

        wn.normalize();
        RandomSkillPreferences retVal = new RandomSkillPreferences();
        NodeList wList = wn.getChildNodes();

        // Okay, lets iterate through the children, eh?
        for (int x = 0; x < wList.getLength(); x++) {
            Node wn2 = wList.item(x);

            // If it's not an element node, we ignore it.
            if (wn2.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            LogManager.getLogger().debug(wn2.getNodeName() + ": " + wn2.getTextContent());
            try {
                if (wn2.getNodeName().equalsIgnoreCase("overallRecruitBonus")) {
                    retVal.overallRecruitBonus = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("randomizeSkill")) {
                    retVal.randomizeSkill = wn2.getTextContent().equalsIgnoreCase("true");
                } else if (wn2.getNodeName().equalsIgnoreCase("useClanBonuses")) {
                    retVal.useClanBonuses = wn2.getTextContent().equalsIgnoreCase("true");
                } else if (wn2.getNodeName().equalsIgnoreCase("antiMekProb")) {
                    retVal.antiMekProb = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("combatSmallArmsBonus")) {
                    retVal.combatSmallArmsBonus = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("supportSmallArmsBonus")) {
                    retVal.supportSmallArmsBonus = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("artilleryProb")) {
                    retVal.artilleryProb = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("artilleryBonus")) {
                    retVal.artilleryBonus = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("secondSkillProb")) {
                    retVal.secondSkillProb = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("secondSkillBonus")) {
                    retVal.secondSkillBonus = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("recruitBonuses")) {
                    String[] values = wn2.getTextContent().split(",");
                    if (version.isLowerThan("0.49.0")) {
                        for (int i = 0; i < values.length; i++) {
                            retVal.recruitBonuses[PersonnelRole.parseFromString(String.valueOf(i)).ordinal()] = Integer.parseInt(values[i]);
                        }
                    } else {
                        for (int i = 0; i < values.length; i++) {
                            retVal.recruitBonuses[i] = Integer.parseInt(values[i]);
                        }
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("tacticsMod")) {
                    String[] values = wn2.getTextContent().split(",");
                    for (int i = 0; i < values.length; i++) {
                        retVal.tacticsMod[i] = Integer.parseInt(values[i]);
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("specialAbilBonus")) {
                    String[] values = wn2.getTextContent().split(",");
                    for (int i = 0; i < values.length; i++) {
                        retVal.specialAbilBonus[i] = Integer.parseInt(values[i]);
                    }
                }
            } catch (Exception e) {
                LogManager.getLogger().error("", e);
            }
        }

        LogManager.getLogger().debug("Load Random Skill Preferences Complete!");

        return retVal;
    }
}
