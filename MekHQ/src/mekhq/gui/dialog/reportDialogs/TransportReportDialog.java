/*
 * Copyright (c) 2021 - The MegaMek Team. All Rights Reserved.
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
package mekhq.gui.dialog.reportDialogs;

import mekhq.campaign.report.TransportReport;

import javax.swing.*;
import java.awt.*;

public class TransportReportDialog extends AbstractReportDialog {
    //region Variable Declarations
    private final TransportReport transportReport;
    //endregion Variable Declarations

    //region Constructors
    public TransportReportDialog(final JFrame frame, final TransportReport transportReport) {
        super(frame, "TransportReportDialog", "TransportReportDialog.title");
        this.transportReport = transportReport;
        initialize();
    }
    //endregion Constructors

    //region Getters
    public TransportReport getTransportReport() {
        return transportReport;
    }

    @Override
    protected JTextPane createTxtReport() {
        final JTextPane txtReport = new JTextPane();
        txtReport.setText(getTransportReport().getTransportDetails());
        txtReport.setName("txtReport");
        txtReport.setFont(new Font("Courier New", Font.PLAIN, 12));
        txtReport.setEditable(false);
        txtReport.setCaretPosition(0);
        return txtReport;
    }
    //endregion Getters
}
