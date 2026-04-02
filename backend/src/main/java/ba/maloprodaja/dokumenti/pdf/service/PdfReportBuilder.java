package ba.maloprodaja.dokumenti.pdf.service;

import ba.maloprodaja.common.exception.BusinessException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignLine;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignStyle;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.type.OrientationEnum;

import java.awt.Color;
import java.math.BigDecimal;

/**
 * Builds JasperReport objects entirely in memory — no .jrxml files needed.
 * Faktura: A4 Portrait (595x842), margins 20pt, usable 555pt.
 * Nivelacija/Otpremnica: A4 Landscape (842x595), margins 20pt, usable 802pt.
 */
final class PdfReportBuilder {

    // Landscape (nivelacija, otpremnica)
    private static final int PAGE_WIDTH    = 842;
    private static final int PAGE_HEIGHT   = 595;
    private static final int MARGIN        = 20;
    private static final int CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN; // 802

    // Portrait (faktura)
    private static final int PORTRAIT_WIDTH         = 595;
    private static final int PORTRAIT_HEIGHT        = 842;
    private static final int PORTRAIT_CONTENT_WIDTH = PORTRAIT_WIDTH - 2 * MARGIN; // 555

    private static final int COL_HEADER_HEIGHT = 20;
    private static final int DETAIL_HEIGHT     = 18;
    private static final int HEADER_HEIGHT     = 80;

    private PdfReportBuilder() {}

    // ------------------------------------------------------------------ faktura

    static JasperReport buildFakturaReport() {
        try {
            JasperDesign design = portraitDesign("faktura");

            addParameter(design, "KOMPANIJA",      String.class);
            addParameter(design, "POSLOVNICA",     String.class);
            addParameter(design, "NASLOV",         String.class);
            addParameter(design, "DATUM",          String.class);
            addParameter(design, "DOBAVLJAC",      String.class);
            addParameter(design, "BROJ_DOKUMENTA", String.class);
            addParameter(design, "UKUPNO_BEZ_PDV", BigDecimal.class);
            addParameter(design, "UKUPNO_PDV",     BigDecimal.class);
            addParameter(design, "UKUPNO",         BigDecimal.class);

            addField(design, "sifra",    String.class);
            addField(design, "naziv",    String.class);
            addField(design, "kolicina", BigDecimal.class);
            addField(design, "vpc",      BigDecimal.class);
            addField(design, "pdvStopa", BigDecimal.class);
            addField(design, "iznosPdv", BigDecimal.class);
            addField(design, "ukupno",   BigDecimal.class);

            // --- Title band ---
            // All elements start at x=MARGIN to align with table columns
            final int TW = PORTRAIT_CONTENT_WIDTH - MARGIN; // 535 — same as table width
            JRDesignBand titleBand = new JRDesignBand();
            titleBand.setHeight(116);

            // Kompanija — bold, left, height=20 to avoid clipping at fontSize=13
            addParamTextAligned(titleBand, "KOMPANIJA",      MARGIN, 4,  TW, 20, 13, true,  HorizontalTextAlignEnum.LEFT);
            // Poslovnica — left
            addParamTextAligned(titleBand, "POSLOVNICA",     MARGIN, 26, TW, 14, 10, false, HorizontalTextAlignEnum.LEFT);
            // Br. dokumenta — left
            addLabeledParamText(titleBand, "Br. dokumenta: ", "BROJ_DOKUMENTA", MARGIN, 42, TW, 14, 10, false);
            // Black separator line — aligned with table
            addHorizontalLine(titleBand, MARGIN, 60, TW, Color.BLACK);
            // Naslov — bold, left
            addParamTextAligned(titleBand, "NASLOV",         MARGIN, 66, TW, 16, 12, true,  HorizontalTextAlignEnum.LEFT);
            // Datum — left
            addLabeledParamText(titleBand, "Datum: ",        "DATUM",     MARGIN, 84, TW, 14, 10, false);
            // Dobavljač — left
            addLabeledParamText(titleBand, "Dobavljač: ",    "DOBAVLJAC", MARGIN, 100, TW, 14, 10, false);

            design.setTitle(titleBand);

            // --- Column header ---
            JRDesignBand colHeader = new JRDesignBand();
            colHeader.setHeight(COL_HEADER_HEIGHT);
            int[]     wF      = {60, 180, 50, 65, 45, 65, 70};
            String[]  hdrsF   = {"Šifra", "Naziv artikla", "Kol.", "VPC", "PDV%", "Iznos PDV", "Ukupno"};
            boolean[] rightH  = {false, false, true, true, true, true, true};
            buildColumnHeaderAligned(colHeader, wF, hdrsF, rightH);
            design.setColumnHeader(colHeader);

            // --- Detail band ---
            JRDesignBand detail = new JRDesignBand();
            detail.setHeight(DETAIL_HEIGHT);
            String[]  fieldsF   = {"sifra", "naziv", "kolicina",   "vpc",      "pdvStopa", "iznosPdv", "ukupno"};
            String[]  patternsF = {null,    null,    "#,##0.####", "#,##0.00", "#,##0",    "#,##0.00", "#,##0.00"};
            boolean[] rightD    = {false, false, true, true, true, true, true};
            buildDetailBandFull(detail, wF, fieldsF, patternsF, rightD);
            ((JRDesignSection) design.getDetailSection()).addBand(detail);

            // --- Summary ---
            design.setSummary(buildFakturaSummary(PORTRAIT_CONTENT_WIDTH));

            return JasperCompileManager.compileReport(design);

        } catch (JRException e) {
            throw new BusinessException("Greška pri inicijalizaciji faktura PDF predloška");
        }
    }

    // ---------------------------------------------------------------- nivelacija

    static JasperReport buildNivelacijaReport() {
        try {
            JasperDesign design = baseDesign("nivelacija");

            addParameter(design, "NASLOV",            String.class);
            addParameter(design, "DATUM",             String.class);
            addParameter(design, "VRSTA",             String.class);
            addParameter(design, "POSLOVNICA",        String.class);
            addParameter(design, "UKUPNO_NIVELACIJE", BigDecimal.class);

            addField(design, "sifra",           String.class);
            addField(design, "naziv",           String.class);
            addField(design, "kolicina",        BigDecimal.class);
            addField(design, "vpc",             BigDecimal.class);
            addField(design, "mpcStara",        BigDecimal.class);
            addField(design, "mpcNova",         BigDecimal.class);
            addField(design, "iznosNivelacije", BigDecimal.class);

            JRDesignBand titleBand = new JRDesignBand();
            titleBand.setHeight(HEADER_HEIGHT);
            addParamText(titleBand, "NASLOV",     MARGIN, 5,  CONTENT_WIDTH, 22, 14, true);
            addLabeledParamText(titleBand, "Datum: ",      "DATUM",      MARGIN, 30, 200, 16, 10, false);
            addLabeledParamText(titleBand, "Vrsta: ",      "VRSTA",      230,    30, 200, 16, 10, false);
            addLabeledParamText(titleBand, "Poslovnica: ", "POSLOVNICA", MARGIN, 48, 400, 16, 10, false);
            design.setTitle(titleBand);

            JRDesignBand colHeader = new JRDesignBand();
            colHeader.setHeight(COL_HEADER_HEIGHT);
            int[]    wN    = {60, 200, 60, 80, 90, 90, 100};
            String[] hdrsN = {"Šifra", "Naziv", "Kol.", "VPC", "MPC stara", "MPC nova", "Iznos niv."};
            buildColumnHeader(colHeader, wN, hdrsN);
            design.setColumnHeader(colHeader);

            JRDesignBand detail = new JRDesignBand();
            detail.setHeight(DETAIL_HEIGHT);
            String[] fieldsN = {"sifra", "naziv", "kolicina", "vpc", "mpcStara", "mpcNova", "iznosNivelacije"};
            buildDetailBand(detail, wN, fieldsN);
            ((JRDesignSection) design.getDetailSection()).addBand(detail);

            design.setSummary(buildNivelacijaSummary(CONTENT_WIDTH));

            return JasperCompileManager.compileReport(design);

        } catch (JRException e) {
            throw new BusinessException("Greška pri inicijalizaciji nivelacija PDF predloška");
        }
    }

    // --------------------------------------------------------------- otpremnica

    static JasperReport buildOtpremnicaReport() {
        try {
            JasperDesign design = baseDesign("otpremnica");

            addParameter(design, "NASLOV",     String.class);
            addParameter(design, "DATUM",      String.class);
            addParameter(design, "STATUS",     String.class);
            addParameter(design, "POSILJALAC", String.class);
            addParameter(design, "PRIMALAC",   String.class);

            addField(design, "sifra",    String.class);
            addField(design, "naziv",    String.class);
            addField(design, "kolicina", BigDecimal.class);
            addField(design, "vpc",      BigDecimal.class);
            addField(design, "mpc",      BigDecimal.class);

            JRDesignBand titleBand = new JRDesignBand();
            titleBand.setHeight(HEADER_HEIGHT);
            addParamText(titleBand, "NASLOV",     MARGIN, 5,  CONTENT_WIDTH, 22, 14, true);
            addLabeledParamText(titleBand, "Datum: ",      "DATUM",      MARGIN, 30, 200, 16, 10, false);
            addLabeledParamText(titleBand, "Status: ",     "STATUS",     230,    30, 200, 16, 10, false);
            addLabeledParamText(titleBand, "Pošiljalac: ", "POSILJALAC", MARGIN, 48, 380, 16, 10, false);
            addLabeledParamText(titleBand, "Primalac: ",   "PRIMALAC",   420,    48, 380, 16, 10, false);
            design.setTitle(titleBand);

            JRDesignBand colHeader = new JRDesignBand();
            colHeader.setHeight(COL_HEADER_HEIGHT);
            int[]    wO    = {70, 320, 80, 100, 100};
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
        design.setOrientation(OrientationEnum.LANDSCAPE);
        design.setLeftMargin(MARGIN);
        design.setRightMargin(MARGIN);
        design.setTopMargin(MARGIN);
        design.setBottomMargin(MARGIN);
        design.setColumnWidth(CONTENT_WIDTH);
        applyDefaultFont(design);
        return design;
    }

    private static JasperDesign portraitDesign(String name) throws JRException {
        JasperDesign design = new JasperDesign();
        design.setName(name);
        design.setPageWidth(PORTRAIT_WIDTH);
        design.setPageHeight(PORTRAIT_HEIGHT);
        design.setOrientation(OrientationEnum.PORTRAIT);
        design.setLeftMargin(MARGIN);
        design.setRightMargin(MARGIN);
        design.setTopMargin(MARGIN);
        design.setBottomMargin(MARGIN);
        design.setColumnWidth(PORTRAIT_CONTENT_WIDTH);
        applyDefaultFont(design);
        return design;
    }

    private static void applyDefaultFont(JasperDesign design) throws JRException {
        JRDesignStyle style = new JRDesignStyle();
        style.setName("default");
        style.setDefault(true);
        style.setFontName("DejaVu Sans");
        style.setPdfEmbedded(true);
        design.addStyle(style);
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

    private static void addHorizontalLine(JRDesignBand band, int x, int y, int width) {
        addHorizontalLine(band, x, y, width, new Color(180, 180, 180));
    }

    private static void addHorizontalLine(JRDesignBand band, int x, int y, int width, Color color) {
        JRDesignLine line = new JRDesignLine();
        line.setX(x);
        line.setY(y);
        line.setWidth(width);
        line.setHeight(1);
        line.setForecolor(color);
        band.addElement(line);
    }

    // Landscape column header (left-aligned start, centered labels)
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

    // Portrait column header with per-column alignment
    private static void buildColumnHeaderAligned(JRDesignBand band, int[] widths, String[] labels, boolean[] rightAlign) {
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
            text.setHorizontalTextAlign(rightAlign[i] ? HorizontalTextAlignEnum.RIGHT : HorizontalTextAlignEnum.LEFT);
            band.addElement(text);
            x += widths[i];
        }
    }

    // Landscape detail band (no patterns, no explicit alignment)
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

    // Portrait detail band with numeric patterns and per-column alignment
    private static void buildDetailBandFull(JRDesignBand band, int[] widths, String[] fieldNames,
                                             String[] patterns, boolean[] rightAlign) {
        int x = MARGIN;
        for (int i = 0; i < widths.length; i++) {
            JRDesignTextField tf = new JRDesignTextField();
            tf.setX(x);
            tf.setY(0);
            tf.setWidth(widths[i]);
            tf.setHeight(DETAIL_HEIGHT);
            tf.setFontSize(8f);
            tf.setBlankWhenNull(true);
            if (patterns[i] != null) {
                tf.setPattern(patterns[i]);
            }
            tf.setHorizontalTextAlign(rightAlign[i] ? HorizontalTextAlignEnum.RIGHT : HorizontalTextAlignEnum.LEFT);
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

    private static void addParamTextAligned(JRDesignBand band, String paramName,
                                             int x, int y, int width, int height,
                                             float fontSize, boolean bold,
                                             HorizontalTextAlignEnum align) {
        JRDesignTextField tf = new JRDesignTextField();
        tf.setX(x);
        tf.setY(y);
        tf.setWidth(width);
        tf.setHeight(height);
        tf.setFontSize(fontSize);
        tf.setBold(bold);
        tf.setBlankWhenNull(true);
        tf.setHorizontalTextAlign(align);
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

    private static void addLabeledParamTextAligned(JRDesignBand band, String label, String paramName,
                                                    int x, int y, int width, int height,
                                                    float fontSize, boolean bold,
                                                    HorizontalTextAlignEnum align) {
        JRDesignTextField tf = new JRDesignTextField();
        tf.setX(x); tf.setY(y); tf.setWidth(width); tf.setHeight(height);
        tf.setFontSize(fontSize); tf.setBold(bold); tf.setBlankWhenNull(true);
        tf.setHorizontalTextAlign(align);
        JRDesignExpression expr = new JRDesignExpression();
        expr.setText("\"" + label + "\" + ($P{" + paramName + "} != null ? $P{" + paramName + "} : \"\")");
        tf.setExpression(expr);
        band.addElement(tf);
    }

    private static JRDesignBand buildFakturaSummary(int contentWidth) {
        JRDesignBand band = new JRDesignBand();
        band.setHeight(64);
        addHorizontalLine(band, MARGIN, 2, contentWidth - MARGIN);
        addSummaryRow(band, "Ukupno bez PDV:", "$P{UKUPNO_BEZ_PDV}", 8,  contentWidth);
        addSummaryRow(band, "Ukupno PDV:",     "$P{UKUPNO_PDV}",     26, contentWidth);
        addSummaryRow(band, "UKUPNO:",         "$P{UKUPNO}",         44, contentWidth);
        return band;
    }

    private static JRDesignBand buildNivelacijaSummary(int contentWidth) {
        JRDesignBand band = new JRDesignBand();
        band.setHeight(25);
        addSummaryRow(band, "Ukupno iznos nivelacije:", "$P{UKUPNO_NIVELACIJE}", 0, contentWidth);
        return band;
    }

    private static void addSummaryRow(JRDesignBand band, String label, String valueExpr, int y, int contentWidth) {
        int labelWidth = 160;
        int valueWidth = 110;
        int labelX     = contentWidth - labelWidth - valueWidth;

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
        valueEl.setPattern("#,##0.00");
        JRDesignExpression expr = new JRDesignExpression();
        expr.setText(valueExpr);
        valueEl.setExpression(expr);
        band.addElement(valueEl);
    }
}
