/*
 * MissingKFHeliumTank.java
 *
 * Copyright (c) 2019 MegaMek Team
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

package mekhq.campaign.parts;

import java.io.PrintWriter;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import megamek.common.Jumpship;
import megamek.common.TechAdvancement;
import mekhq.utilities.MHQXMLUtility;
import mekhq.campaign.Campaign;

/**
 * @author MKerensky
 */
public class MissingKFHeliumTank extends MissingPart {
    // Standard, primitive, compact, subcompact...
    private int coreType;

    public int getCoreType() {
        return coreType;
    }

    // How many docking collars does this drive support?
    private int docks;

    public int getDocks() {
        return docks;
    }

    public MissingKFHeliumTank() {
        this(0, Jumpship.DRIVE_CORE_STANDARD, 0, null);
    }

    public MissingKFHeliumTank(int tonnage, int coreType, int docks, Campaign c) {
        super(0, c);
        this.coreType = coreType;
        this.docks = docks;
        this.name = "K-F Helium Tank";
    }

    @Override
    public int getBaseTime() {
        // BattleSpace, p28, *10
        return 1800;
    }

    @Override
    public int getDifficulty() {
        // BattleSpace, p28
        return 4;
    }

    @Override
    public String checkFixable() {
        return null;
    }

    @Override
    public Part getNewPart() {
        return new KFHeliumTank(getUnitTonnage(), coreType, docks, campaign);
    }

    @Override
    public void fix() {
        Part replacement = findReplacement(false);
        if (null != replacement) {
            Part actualReplacement = replacement.clone();
            unit.addPart(actualReplacement);
            if (null != unit && unit.getEntity() instanceof Jumpship) {
                Jumpship js = ((Jumpship) unit.getEntity());
                //Also repair your KF Drive integrity - up to 2/3 of the total if you have other components to fix
                //Otherwise, fix it all.
                if (js.isKFDriveDamaged()) {
                    js.setKFIntegrity(Math.min((js.getKFIntegrity() + js.getKFHeliumTankIntegrity()), js.getOKFIntegrity()));
                } else {
                    js.setKFIntegrity(js.getOKFIntegrity());
                }
            }
            campaign.getQuartermaster().addPart(actualReplacement, 0);
            replacement.decrementQuantity();
            remove(false);
            // assign the replacement part to the unit
            actualReplacement.updateConditionFromPart();
        }
    }

    @Override
    public boolean isAcceptableReplacement(Part part, boolean refit) {
        return part instanceof KFHeliumTank
                && coreType == ((KFHeliumTank) part).getCoreType()
                && docks == ((KFHeliumTank) part).getDocks();
    }

    @Override
    public double getTonnage() {
        return 0;
    }

    @Override
    public void updateConditionFromPart() {
        if (null != unit && unit.getEntity() instanceof Jumpship) {
            ((Jumpship) unit.getEntity()).setKFHeliumTankHit(true);
        }
    }

    @Override
    public void writeToXML(PrintWriter pw1, int indent) {
        writeToXmlBegin(pw1, indent);
        pw1.println(MHQXMLUtility.indentStr(indent+1)
                +"<coreType>"
                +coreType
                +"</coreType>");
        pw1.println(MHQXMLUtility.indentStr(indent+1)
                +"<docks>"
                +docks
                +"</docks>");
        writeToXmlEnd(pw1, indent);
    }

    @Override
    protected void loadFieldsFromXmlNode(Node wn) {
        NodeList nl = wn.getChildNodes();
        for (int x = 0; x < nl.getLength(); x++) {
            Node wn2 = nl.item(x);

            if (wn2.getNodeName().equalsIgnoreCase("coreType")) {
                coreType = Integer.parseInt(wn2.getTextContent());
            } else if (wn2.getNodeName().equalsIgnoreCase("docks")) {
                docks = Integer.parseInt(wn2.getTextContent());
            }
        }
    }

    @Override
    public String getLocationName() {
        return null;
    }

    @Override
    public int getLocation() {
        return Jumpship.LOC_HULL;
    }

    @Override
    public TechAdvancement getTechAdvancement() {
        return KFHeliumTank.TA_HELIUM_TANK;
    }
}
