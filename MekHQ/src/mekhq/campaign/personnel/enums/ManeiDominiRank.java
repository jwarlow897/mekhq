/*
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
package mekhq.campaign.personnel.enums;

import megamek.common.util.EncodeControl;
import mekhq.MekHQ;
import org.apache.logging.log4j.LogManager;

import java.util.ResourceBundle;

public enum ManeiDominiRank {
    //region Enum Declarations
    NONE("ManeiDominiRank.NONE.text"),
    ALPHA("ManeiDominiRank.ALPHA.text"),
    BETA("ManeiDominiRank.BETA.text"),
    OMEGA("ManeiDominiRank.OMEGA.text"),
    TAU("ManeiDominiRank.TAU.text"),
    DELTA("ManeiDominiRank.DELTA.text"),
    SIGMA("ManeiDominiRank.SIGMA.text"),
    OMICRON("ManeiDominiRank.OMICRON.text");
    //endregion Enum Declarations

    //region Variable Declarations
    private final String name;
    //endregion Variable Declarations

    //region Constructors
    ManeiDominiRank(final String name) {
        final ResourceBundle resources = ResourceBundle.getBundle("mekhq.resources.Personnel",
                MekHQ.getMHQOptions().getLocale(), new EncodeControl());
        this.name = resources.getString(name);
    }
    //endregion Constructors

    //region Boolean Comparison Methods
    public boolean isNone() {
        return this == NONE;
    }

    public boolean isAlpha() {
        return this == ALPHA;
    }

    public boolean isBeta() {
        return this == BETA;
    }

    public boolean isOmega() {
        return this == OMEGA;
    }

    public boolean isTau() {
        return this == TAU;
    }

    public boolean isDelta() {
        return this == DELTA;
    }

    public boolean isSigma() {
        return this == SIGMA;
    }

    public boolean isOmicron() {
        return this == OMICRON;
    }
    //endregion Boolean Comparison Methods

    //region File I/O
    public static ManeiDominiRank parseFromString(final String text) {
        // Parse based on the enum name
        try {
            return valueOf(text);
        } catch (Exception ignored) {

        }

        // Parse from Ordinal Int - Legacy save method
        final ManeiDominiRank[] values = values();
        try {
            int mdRank = Integer.parseInt(text) + 1;
            if (values.length > mdRank) {
                return values[mdRank];
            }
        } catch (Exception ignored) {

        }

        LogManager.getLogger().error("Unable to parse " + text + "into a ManeiDominiRank. Returning NONE.");
        return NONE;
    }
    //endregion File I/O

    @Override
    public String toString() {
        return name;
    }
}
