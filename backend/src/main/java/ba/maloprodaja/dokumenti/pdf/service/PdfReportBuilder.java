package ba.maloprodaja.dokumenti.pdf.service;

import ba.maloprodaja.common.exception.BusinessException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignElement;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.ModeEnum;

import java.awt.Color;
import java.math.BigDecimal;

/**
 * Builds JasperReport objects entirely in memory — no .jrxml files needed.
 * Page width: A4 landscape = 842pt, margins 20pt each side => usable 802pt.
 */
final class PdfReportBuilder {

    private static final int PAGE_WIDTH = 842;
    private static final int PAGE_HEIGHT = 595;
    private static final int MARGIN = 20;
    private static final int CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;

    private static final int COL_HEADER_HEIGHT = 20;
    private static final int DETAIL_HEIGHT = 18;
    private static final int HEADER_HEIGHT = 80;

    private PdfReportBuilder() {}

    // ------------------------------------------------------------------ faktura

    static JasperReport buildFakturaReport() {
        try {
            JasperDesign design = baseDesign("faktura");

            // --- Parameters ---
            addParameter(design, "NASLOV", String.class);
            addParameter(design, "DATUM", String.class);
            addParameter(design, "DOBAVLJAC", String.class);
            addParameter(design, "UKUPNO_BEZ_PDV", BigDecimal.class);
            addParameter(design, "UKUPNO_PDV", BigDecimal.class);
            addParameter(design, "UKUPNO", BigDecimal.class);

            // --- Fields ---
            addField(design, "sifra", String.class);
            addField(design, "naziv", String.class);
            addField(design, "kolicina", BigDecimal.class);
            addField(design, "vpc", BigDecimal.class);
            addField(design, "pdvStopa", BigDecimal.class);
            addField(design, "iznosPdv", BigDecimal.class);
            addField(design, "ukupno", BigDecimal.class);

            // --- Title band ---
            JRDesignBand titleBand = new JRDesignBand();
            titleBand.setHeight(HEADER_HEIGHT);
            addParamText(titleBand, "NASLOV", MARGIN, 5, CONTENT_WIDTH, 22, 14, true);
            addLabeledParamText(titleBand, "Datum: ", "DATUM", MARGIN, 30, 200, 16, 10, false);
            addLabeledParamText(titleBand, "Dobavljač: ", "DOBAVLJAC", MARGIN, 48, 400, 16, 10, false);
            design.setTitle(titleBand);

            // --- Column header ---
            JRDesignBand colHeader = new JRDesignBand();
            colHeader.setHeight(COL_HEADER_HEIGHT);
            int[] wF = {60, 220, 60, 80, 55, 80, 90};
            String[] hdrsF = {"Šifra", "Naziv", "Kol.", "VPC", "PDV%", "Iznos PDV", "Ukupno"};
            buildColumnHeader(colHeader, wF, hdrsF);
            design.setColumnHeader(colHeader);

            // --- Detail band ---
            JRDesignBand detail = new JRDesignBand();
            detail.setHeight(DETAIL_HEIGHT);
            String[] fieldsF = {"sifra", "naziv", "kolicina", "vpc", "pdvStopa", "iznosPdv", "ukupno"};
            buildDetailBand(detail, wF, fieldsF);
            ((JRDesignSection) design.getDetailSection()).addBand(detail);

            // --- Summary band ---
            JRDesignBand summary = buildFakturaSummary();
            design.setSummary(summary);

            return JasperCompileManager.compileReport(design);

        } catch (JRException e) {
            throw new BusinessException("Greška pri inicijalizaciji faktura PDF predloška");
        }
    }

    // ---------------------------------------------------------------- nivelacija

    static JasperReport buildNivelacijaReport() {
        try {
            JasperDesign design = baseDesign("nivelacija");

            addParameter(design, "NASLOV", String.class);
            addParameter(design, "DATUM", String.class);
            addParameter(design, "VRSTA", String.class);
            addParameter(design, "POSLOVNICA", String.class);
            addParameter(design, "UKUPNO_NIVELACIJE", BigDecimal.class);

            addField(design, "sifra", String.class);
            addField(design, "naziv", String.class);
            addField(design, "kolicina", BigDecimal.class);
            addField(design, "vpc", BigDecimal.class);
            addField(design, "mpcStara", BigDecimal.class);
            addField(design, "mpcNova", BigDecimal.class);
            addField(design, "iznosNivelacije", BigDecimal.class);

            JRDesignBand titleBand = new JRDesignBand();
            titleBand.setHeight(HEADER_HEIGHT);
            addParamText(titleBand, "NASLOV", MARGIN, 5, CONTENT_WIDTH, 22, 14, true);
            addLabeledParamText(titleBand, "Datum: ", "DATUM", MARGIN, 30, 200, 16, 10, false);
            addLabeledParamText(titleBand, "Vrsta: ", "VRSTA", 230, 30, 200, 16, 10, false);
            addLabeledParamText(titleBand, "Poslovnica: ", "POSLOVNICA", MARGIN, 48, 400, 16, 10, false);
            design.setTitle(titleBand);

            JRDesignBand colHeader = new JRDesignBand();
            colHeader.setHeight(COL_HEADER_HEIGHT);
            int[] wN = {60, 200, 60, 80, 90, 90, 100};
            String[] hdrsN = {"Šifra", "Naziv", "Kol.", "VPC", "MPC stara", "MPC nova", "Iznos niv."};
            buildColumnHeader(colHeader, wN, hdrsN);
            design.setColumnHeader(colHeader);

            JRDesignBand detail = new JRDesignBand();
            detail.setHeight(DETAIL_HEIGHT);
            String[] fieldsN = {"sifra", "naziv", "kolicina", "vpc", "mpcStara", "mpcNova", "iznosNivelacije"};
            buildDetailBand(detail, wN, fieldsN);
            ((JRDesignSection) design.getDetailSection()).addBand(detail);

            JRDesignBand summary = buildNivelacijaSummary();
            design.setSummary(summary);

            return JasperCompileManager.compileReport(design);

        } catch (JRException e) {
            throw new BusinessException("Greška pri inicijalizaciji nivelacija PDF predloška");
        }
    }

    // --------------------------------------------------------------- otpremnica

    static JasperReport buildOtpremnicaReport() {
        try {
            JasperDesign design = baseDesign("otpremnica");

            addParameter(design, "NASLOV", String.class);
            addParameter(design, "DATUM", String.class);
            addParameter(design, "STATUS", String.class);
            addParameter(design, "POSILJALAC", String.class);
            addParameter(design, "PRIMALAC", String.class);

            addField(design, "sifra", String.class);
            addField(design, "naziv", String.class);
            addField(design, "kolicina", BigDecimal.class);
            addField(design, "vpc", BigDecimal.class);
            addField(design, "mpc", BigDecimal.class);

            JRDesignBand titleBand = new JRDesignBand();
            titleBand.setHeight(HEADER_HEIGHT);
            addParamText(titleBand, "NASLOV", MARGIN, 5, CONTENT_WIDTH, 22, 14, true);
            addLabeledParamText(titleBand, "Datum: ", "DATUM", MARGIN, 30, 200, 16, 10, false);
            addLabeledParamText(titleBand, "Status: ", "STATUS", 230, 30, 200, 16, 10, false);
            addLabeledParamText(titleBand, "Pošiljalac: ", "POSILJALAC", MARGIN, 48, 380, 16, 10, false);
            addLabeledParamText(titleBand, "Primalac: ", "PRIMALAC", 420, 48, 380, 16, 10, false);
            design.setTitle(titleBand);

            JRDesignBand colHeader = new JRDesignBand();
            colHeader.setHeight(COL_HEADER_HEIGHT);
            int[] wO = {70, 320, 80, 100, 100};
            String[] hdrsO = {"Šifra", "Naziv", "Količina", "VPC", "MPC"};
            buildColumnHeader(colHeader, wO, hdrsO);
            design.setColumnHeader(colHeader);

            JRDesignBand detail = new JRDesignBand();
            detail.setHeight(DETAIL_HEIGHT);
            String[] fieldsO = {"sifra", "naziv", "kolicina", "vpc", "mpc"};
            buildDetailBand(detail, wO, fieldsO);
            ((JRDesignSection) design.getDetailSection()).addBand(detail);

            return JasperCompileManager.compileReport(design);

        } catch (JRException e) {
            throw new BusinessException("Greška pri inicijalizaciji otpremnica PDF predloška");
        }
    }

    // ------------------------------------------------------------------ helpers

    private static JasperDesign baseDesign(String name) throws JRException {
        JasperDesign design = new JasperDesign();
        design.setName(name);
        design.setPageWidth(PAGE_WIDTH);
        design.setPageHeight(PAGE_HEIGHT);
        design.setOrientation(net.sf.jasperreports.engine.type.OrientationEnum.LANDSCAPE);
        design.setLeftMargin(MARGIN);
        design.setRightMargin(MARGIN);
        design.setTopMargin(MARGIN);
        design.setBottomMargin(MARGIN);
        design.setColumnWidth(CONTENT_WIDTH);
        return design;
    }

    private static void addParameter(JasperDesign design, String name, Class<?> type) throws JRException {
        JRDesignParameter param = new JRDesignParameter();
        param.setName(name);
        param.setValueClass(type);
        design.addParameter(param);
    }

    private static void addField(JasperDesign design, String name, Class<?> type) throws JRException {
        JRDesignField field = new JRDesignField();
        field.setName(name);
        field.setValueClass(type);
        design.addField(field);
    }

    private static void buildColumnHeader(JRDesignBand band, int[] widths, String[] labels) {
        int x = MARGIN;
        for (int i = 0; i < widths.length; i++) {
            JRDesignStaticText text = new JRDesignStaticText();
            text.setX(x);
            text.setY(0);
            text.setWidth(widths[i]);
            text.setHeight(COL_HEADER_HEIGHT);
            text.setText(labels[i]);
            text.setFontSize(9f);
            text.setBold(true);
            text.setMode(ModeEnum.OPAQUE);
            text.setBackcolor(new Color(200, 200, 200));
            text.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
            band.addElement(text);
            x += widths[i];
        }
    }

    private static void buildDetailBand(JRDesignBand band, int[] widths, String[] fieldNames) {
        int x = MARGIN;
        for (int i = 0; i < widths.length; i++) {
            JRDesignTextField tf = new JRDesignTextField();
            tf.setX(x);
            tf.setY(0);
            tf.setWidth(widths[i]);
            tf.setHeight(DETAIL_HEIGHT);
            tf.setFontSize(8f);
            tf.setBlankWhenNull(true);

            JRDesignExpression expr = new JRDesignExpression();
            expr.setText("$F{" + fieldNames[i] + "}");
            tf.setExpression(expr);
            band.addElement(tf);
            x += widths[i];
        }
    }

    private static void addParamText(JRDesignBand band, String paramName,
                                     int x, int y, int width, int height,
                                     float fontSize, boolean bold) {
        JRDesignTextField tf = new JRDesignTextField();
        tf.setX(x);
        tf.setY(y);
        tf.setWidth(width);
        tf.setHeight(height);
        tf.setFontSize(fontSize);
        tf.setBold(bold);
        tf.setBlankWhenNull(true);

        JRDesignExpression expr = new JRDesignExpression();
        expr.setText("$P{" + paramName + "}");
        tf.setExpression(expr);
        band.addElement(tf);
    }

    private static void addLabeledParamText(JRDesignBand band, String label, String paramName,
                                            int x, int y, int width, int height,
                                            float fontSize, boolean bold) {
        JRDesignTextField tf = new JRDesignTextField();
        tf.setX(x);
        tf.setY(y);
        tf.setWidth(width);
        tf.setHeight(height);
        tf.setFontSize(fontSize);
        tf.setBold(bold);
        tf.setBlankWhenNull(true);

        JRDesignExpression expr = new JRDesignExpression();
        expr.setText("\"" + label + "\" + ($P{" + paramName + "} != null ? $P{" + paramName + "} : \"\")");
        tf.setExpression(expr);
        band.addElement(tf);
    }

    private static JRDesignBand buildFakturaSummary() {
        JRDesignBand band = new JRDesignBand();
        band.setHeight(55);

        addSummaryRow(band, "Ukupno bez PDV:", "$P{UKUPNO_BEZ_PDV}", 0);
        addSummaryRow(band, "Ukupno PDV:", "$P{UKUPNO_PDV}", 18);
        addSummaryRow(band, "UKUPNO:", "$P{UKUPNO}", 36);

        return band;
    }

    private static JRDesignBand buildNivelacijaSummary() {
        JRDesignBand band = new JRDesignBand();
        band.setHeight(25);

        addSummaryRow(band, "Ukupno iznos nivelacije:", "$P{UKUPNO_NIVELACIJE}", 0);

        return band;
    }

    private static void addSummaryRow(JRDesignBand band, String label, String valueExpr, int y) {
        int labelWidth = 180;
        int valueWidth = 120;
        int labelX = CONTENT_WIDTH - labelWidth - valueWidth;

        JRDesignStaticText labelEl = new JRDesignStaticText();
        labelEl.setX(labelX);
        labelEl.setY(y);
        labelEl.setWidth(labelWidth);
        labelEl.setHeight(16);
        labelEl.setText(label);
        labelEl.setFontSize(9f);
        labelEl.setBold(true);
        labelEl.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        band.addElement(labelEl);

        JRDesignTextField valueEl = new JRDesignTextField();
        valueEl.setX(labelX + labelWidth);
        valueEl.setY(y);
        valueEl.setWidth(valueWidth);
        valueEl.setHeight(16);
        valueEl.setFontSize(9f);
        valueEl.setBold(true);
        valueEl.setBlankWhenNull(true);
        valueEl.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);

        JRDesignExpression expr = new JRDesignExpression();
        expr.setText(valueExpr);
        valueEl.setExpression(expr);
        band.addElement(valueEl);
    }
}
