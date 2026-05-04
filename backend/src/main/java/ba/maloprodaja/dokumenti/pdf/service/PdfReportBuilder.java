package ba.maloprodaja.dokumenti.pdf.service;

import ba.maloprodaja.common.exception.BusinessException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignImage;
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
import net.sf.jasperreports.engine.type.ScaleImageEnum;

import java.awt.Color;
import java.awt.image.BufferedImage;
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

    // ---------------------------------------------------------- promet dokument

    /**
     * Shared A4 Portrait template for all promet document types (UF, IF, PD, MSI, MSU).
     * All rows share the same structure: rb, artikal, varijanta, kolicina, cijena, popust, ukupno.
     *
     * Parameters:
     *   KOMPANIJA_NAZIV, KOMPANIJA_ADRESA, KOMPANIJA_GRAD,
     *   POSLOVNICA_NAZIV, POSLOVNICA_ADRESA,
     *   TIP_NAZIV, BROJ_DOKUMENTA, DATUM, PARTNER, NAPOMENA,
     *   UKUPNO (BigDecimal), LOGO_IMAGE (BufferedImage — nullable)
     */
    static JasperReport buildPrometDokumentReport() {
        try {
            JasperDesign design = portraitDesign("promet-dokument");

            addParameter(design, "KOMPANIJA_NAZIV",   String.class);
            addParameter(design, "KOMPANIJA_ADRESA",  String.class);
            addParameter(design, "KOMPANIJA_GRAD",    String.class);
            addParameter(design, "POSLOVNICA_NAZIV",  String.class);
            addParameter(design, "TIP_NAZIV",         String.class);
            addParameter(design, "BROJ_DOKUMENTA",    String.class);
            addParameter(design, "DATUM",             String.class);
            addParameter(design, "PARTNER",           String.class);
            addParameter(design, "NAPOMENA",          String.class);
            addParameter(design, "UKUPNO",            BigDecimal.class);
            addParameter(design, "LOGO_IMAGE",        BufferedImage.class);

            addField(design, "rb",       Integer.class);
            addField(design, "artikal",  String.class);
            addField(design, "varijanta",String.class);
            addField(design, "kolicina", BigDecimal.class);
            addField(design, "cijena",   BigDecimal.class);
            addField(design, "popust",   BigDecimal.class);
            addField(design, "ukupno",   BigDecimal.class);

            design.setTitle(buildPrometTitleBand());
            design.setColumnHeader(buildPrometColumnHeader());

            JRDesignBand detail = new JRDesignBand();
            detail.setHeight(DETAIL_HEIGHT);
            // Column widths: rb=25, artikal=155, varijanta=115, kolicina=50, cijena=65, popust=45, ukupno=80 => 535 total = PORTRAIT_CONTENT_WIDTH - MARGIN
            int[]     wP  = {25, 155, 115, 50, 65, 45, 80};
            String[]  fP  = {"rb", "artikal", "varijanta", "kolicina", "cijena", "popust", "ukupno"};
            String[]  pP  = {null, null, null, "#,##0.###", "#,##0.00", "#,##0.##", "#,##0.00"};
            boolean[] rP  = {true, false, false, true, true, true, true};
            buildDetailBandFull(detail, wP, fP, pP, rP);
            // Thin separator line at bottom of each row
            addHorizontalLine(detail, MARGIN, DETAIL_HEIGHT - 1, PORTRAIT_CONTENT_WIDTH - MARGIN, new Color(230, 230, 230));
            ((JRDesignSection) design.getDetailSection()).addBand(detail);

            design.setSummary(buildPrometSummary(PORTRAIT_CONTENT_WIDTH));

            return JasperCompileManager.compileReport(design);

        } catch (JRException e) {
            throw new BusinessException("Greška pri inicijalizaciji promet dokument PDF predloška");
        }
    }

    // --------------------------------------------------------- ulazna faktura (landscape, all columns)

    /**
     * A4 Landscape PDF for ulazne fakture — matches the UI table columns exactly:
     * rb, artikal, varijanta, kolicina, vpc, marzaProcenat, iznosMarze,
     * pdvProcenat, iznosPdv, popustProcenat, iznosPopusta, mpc, iznosMpc.
     *
     * Usable width: 802pt.
     * Column widths sum: 25+175+75+45+55+42+62+38+62+42+62+55+64 = 802.
     */
    static JasperReport buildUlaznaFakturaReport() {
        try {
            JasperDesign design = baseDesign("ulazna-faktura");

            addParameter(design, "KOMPANIJA_NAZIV",   String.class);
            addParameter(design, "KOMPANIJA_ADRESA",  String.class);
            addParameter(design, "KOMPANIJA_GRAD",    String.class);
            addParameter(design, "POSLOVNICA_NAZIV",  String.class);
            addParameter(design, "TIP_NAZIV",         String.class);
            addParameter(design, "BROJ_DOKUMENTA",    String.class);
            addParameter(design, "DATUM",             String.class);
            addParameter(design, "PARTNER",           String.class);
            addParameter(design, "NAPOMENA",          String.class);
            addParameter(design, "BROJ_FAKTURE",      String.class);
            addParameter(design, "LOGO_IMAGE",        BufferedImage.class);
            addParameter(design, "UKUPNO_VPC",        BigDecimal.class);
            addParameter(design, "UKUPNO_POPUSTA",    BigDecimal.class);
            addParameter(design, "UKUPNO_MARZE",      BigDecimal.class);
            addParameter(design, "UKUPNO_PDV",        BigDecimal.class);
            addParameter(design, "UKUPNO_MPC",        BigDecimal.class);

            addField(design, "rb",             Integer.class);
            addField(design, "artikal",        String.class);
            addField(design, "varijanta",      String.class);
            addField(design, "kolicina",       BigDecimal.class);
            addField(design, "vpc",            BigDecimal.class);
            addField(design, "popustProcenat", BigDecimal.class);
            addField(design, "iznosPopusta",   BigDecimal.class);
            addField(design, "pdvProcenat",    BigDecimal.class);
            addField(design, "iznosPdv",       BigDecimal.class);
            addField(design, "marzaProcenat",  BigDecimal.class);
            addField(design, "mpc",            BigDecimal.class);
            addField(design, "iznosMpc",       BigDecimal.class);

            design.setTitle(buildUlaznaFakturaTitleBand());
            design.setColumnHeader(buildUlaznaFakturaColumnHeader());

            JRDesignBand detail = buildUlaznaFakturaDetailBand();
            ((JRDesignSection) design.getDetailSection()).addBand(detail);

            design.setSummary(buildUlaznaFakturaSummary(CONTENT_WIDTH));
            design.setPageFooter(buildUlaznaFakturaPageFooter(CONTENT_WIDTH));

            return JasperCompileManager.compileReport(design);

        } catch (JRException e) {
            throw new BusinessException("Greška pri inicijalizaciji ulazna faktura PDF predloška");
        }
    }

    private static JRDesignBand buildUlaznaFakturaDetailBand() {
        JRDesignBand detail = new JRDesignBand();
        detail.setHeight(DETAIL_HEIGHT);
        // Widths sum: 180+140+45+55+42+62+38+62+42+72+54=792 (10pt right gap)
        int[]     wU   = {180, 140, 45, 55, 42, 62, 38, 62, 42, 72, 54};
        String[]  exps = {
            "String.valueOf($F{rb}) + \" \" + $F{artikal}",
            "$F{varijanta}",
            "$F{kolicina}",
            "$F{vpc}",
            "$F{popustProcenat}",
            "$F{iznosPopusta}",
            "$F{pdvProcenat}",
            "$F{iznosPdv}",
            "$F{marzaProcenat}",
            "$F{mpc}",
            "$F{iznosMpc}"
        };
        String[]  pU  = {null, null, "#,##0.###",
                         "#,##0.00", "#,##0.##", "#,##0.00",
                         "#,##0.##", "#,##0.00", "#,##0.##",
                         "#,##0.00", "#,##0.00"};
        boolean[] rU  = {false, false, true,
                         true, true, true,
                         true, true, true,
                         true, true};
        buildDetailBandWithExpressions(detail, wU, exps, pU, rU);
        addHorizontalLine(detail, MARGIN, DETAIL_HEIGHT - 1, CONTENT_WIDTH, new Color(230, 230, 230));
        return detail;
    }

    private static void buildDetailBandWithExpressions(JRDesignBand band, int[] widths,
                                                        String[] expressions,
                                                        String[] patterns, boolean[] rightAlign) {
        int x = MARGIN;
        for (int i = 0; i < widths.length; i++) {
            JRDesignTextField tf = new JRDesignTextField();
            tf.setX(x); tf.setY(0); tf.setWidth(widths[i]); tf.setHeight(DETAIL_HEIGHT);
            tf.setFontSize(8f); tf.setBlankWhenNull(true);
            if (patterns[i] != null) tf.setPattern(patterns[i]);
            tf.setHorizontalTextAlign(rightAlign[i] ? HorizontalTextAlignEnum.RIGHT : HorizontalTextAlignEnum.LEFT);
            JRDesignExpression expr = new JRDesignExpression();
            expr.setText(expressions[i]);
            tf.setExpression(expr);
            band.addElement(tf);
            x += widths[i];
        }
    }

    private static JRDesignBand buildUlaznaFakturaTitleBand() {
        final int DOC_W = CONTENT_WIDTH;

        JRDesignBand band = new JRDesignBand();
        band.setHeight(129);

        addParamTextAligned(band, "KOMPANIJA_NAZIV",  MARGIN, 4,  DOC_W, 16, 12, true,  HorizontalTextAlignEnum.LEFT);
        addParamTextAligned(band, "KOMPANIJA_ADRESA", MARGIN, 21, DOC_W, 12, 9,  false, HorizontalTextAlignEnum.LEFT);
        addParamTextAligned(band, "KOMPANIJA_GRAD",   MARGIN, 33, DOC_W, 12, 9,  false, HorizontalTextAlignEnum.LEFT);
        addLabeledParamText(band, "Poslovnica: ", "POSLOVNICA_NAZIV", MARGIN, 45, DOC_W, 12, 9, false);

        addHorizontalLine(band, MARGIN, 60, DOC_W, Color.BLACK);

        // Title + document number on one left-aligned line
        JRDesignTextField titleLine = new JRDesignTextField();
        titleLine.setX(MARGIN); titleLine.setY(63);
        titleLine.setWidth(DOC_W); titleLine.setHeight(13);
        titleLine.setFontSize(11f); titleLine.setBold(true); titleLine.setBlankWhenNull(true);
        titleLine.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
        JRDesignExpression titleExpr = new JRDesignExpression();
        titleExpr.setText("$P{TIP_NAZIV} + ($P{BROJ_DOKUMENTA} != null && !$P{BROJ_DOKUMENTA}.isEmpty() ? \"  br. \" + $P{BROJ_DOKUMENTA} : \"\")");
        titleLine.setExpression(titleExpr);
        band.addElement(titleLine);

        addLabeledParamText(band, "Partner: ",    "PARTNER",      MARGIN, 77,  DOC_W, 12, 9, false);
        addLabeledParamText(band, "Br. fakture: ","BROJ_FAKTURE", MARGIN, 90,  DOC_W, 12, 9, false);
        addLabeledParamText(band, "Datum: ",      "DATUM",        MARGIN, 103, 220,   12, 9, false);
        addLabeledParamText(band, "Napomena: ",   "NAPOMENA",     MARGIN, 116, DOC_W, 12, 9, false);

        return band;
    }

    private static JRDesignBand buildUlaznaFakturaColumnHeader() {
        JRDesignBand band = new JRDesignBand();
        band.setHeight(COL_HEADER_HEIGHT);
        // Widths sum: 180+140+45+55+42+62+38+62+42+72+54=792 (10pt right gap)
        int[]     wU    = {180, 140, 45, 55, 42, 62, 38, 62, 42, 72, 54};
        String[]  hdrs  = {"#Artikal", "Varijanta", "Kol.",
                           "Fakt.VPC", "Pop.%", "Iznos pop.",
                           "PDV%", "Iznos PDV", "Marža%",
                           "MPC", "Ukupno"};
        boolean[] right = {false, false, true,
                           true, true, true,
                           true, true, true,
                           true, true};
        buildColumnHeaderAligned(band, wU, hdrs, right);
        return band;
    }

    private static JRDesignBand buildUlaznaFakturaSummary(int contentWidth) {
        JRDesignBand band = new JRDesignBand();
        band.setHeight(96);
        addHorizontalLine(band, MARGIN, 2, contentWidth, Color.BLACK);
        addSummaryRow(band, "Ukupno VPC neto:",  "$P{UKUPNO_VPC}",     8,  contentWidth);
        addSummaryRow(band, "Ukupno popust:",     "$P{UKUPNO_POPUSTA}", 26, contentWidth);
        addSummaryRow(band, "Ukupno marža:",      "$P{UKUPNO_MARZE}",   44, contentWidth);
        addSummaryRow(band, "Ukupno PDV:",        "$P{UKUPNO_PDV}",     62, contentWidth);
        addSummaryRow(band, "UKUPNO MPC:",        "$P{UKUPNO_MPC}",     80, contentWidth);
        return band;
    }

    private static JRDesignBand buildUlaznaFakturaPageFooter(int contentWidth) {
        JRDesignBand band = new JRDesignBand();
        band.setHeight(18);

        JRDesignTextField genDate = new JRDesignTextField();
        genDate.setX(MARGIN); genDate.setY(2);
        genDate.setWidth(200); genDate.setHeight(14);
        genDate.setFontSize(8f); genDate.setBlankWhenNull(true);
        JRDesignExpression genExpr = new JRDesignExpression();
        genExpr.setText("\"Generirano: \" + new java.text.SimpleDateFormat(\"dd.MM.yyyy\").format(new java.util.Date())");
        genDate.setExpression(genExpr);
        band.addElement(genDate);

        JRDesignTextField pageInfo = new JRDesignTextField();
        pageInfo.setX(contentWidth - 110); pageInfo.setY(2);
        pageInfo.setWidth(110); pageInfo.setHeight(14);
        pageInfo.setFontSize(8f);
        pageInfo.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        JRDesignExpression pageExpr = new JRDesignExpression();
        pageExpr.setText("\"Stranica \" + $V{PAGE_NUMBER} + \"/\" + $V{PAGE_NUMBER}");
        pageInfo.setExpression(pageExpr);
        band.addElement(pageInfo);

        return band;
    }

    private static JRDesignBand buildPrometTitleBand() {
        // Logo area: x=MARGIN, y=4, 60x52
        // Company info starts at x=MARGIN+66 (to the right of logo)
        // Total title band height = 140 (logo block + doc info block + separator)
        final int LOGO_W    = 60;
        final int LOGO_H    = 52;
        final int INFO_X    = MARGIN + LOGO_W + 6;
        // Available width for text to the right of logo
        final int INFO_W    = PORTRAIT_CONTENT_WIDTH - MARGIN - LOGO_W - 6;

        JRDesignBand band = new JRDesignBand();
        band.setHeight(140);

        // --- Logo image (nullable — blank when null via null expression guard) ---
        JRDesignImage logoImg = new JRDesignImage(null);
        logoImg.setX(MARGIN);
        logoImg.setY(4);
        logoImg.setWidth(LOGO_W);
        logoImg.setHeight(LOGO_H);
        logoImg.setScaleImage(ScaleImageEnum.RETAIN_SHAPE);
        logoImg.setMode(ModeEnum.TRANSPARENT);
        JRDesignExpression logoExpr = new JRDesignExpression();
        logoExpr.setText("$P{LOGO_IMAGE}");
        logoImg.setExpression(logoExpr);
        band.addElement(logoImg);

        // --- Company name (bold, 13pt) ---
        addParamTextAligned(band, "KOMPANIJA_NAZIV",  INFO_X, 4,  INFO_W, 18, 13, true,  HorizontalTextAlignEnum.LEFT);
        // --- Company address ---
        addParamTextAligned(band, "KOMPANIJA_ADRESA", INFO_X, 23, INFO_W, 14, 9,  false, HorizontalTextAlignEnum.LEFT);
        // --- Company city ---
        addParamTextAligned(band, "KOMPANIJA_GRAD",   INFO_X, 37, INFO_W, 14, 9,  false, HorizontalTextAlignEnum.LEFT);
        // --- Branch name (label + value) ---
        addLabeledParamText(band, "Poslovnica: ", "POSLOVNICA_NAZIV",  INFO_X, 51, INFO_W, 13, 9, false);

        // --- Horizontal separator ---
        addHorizontalLine(band, MARGIN, 62, PORTRAIT_CONTENT_WIDTH - MARGIN, Color.BLACK);

        // --- Document type (bold) + number on same line ---
        final int DOC_W = PORTRAIT_CONTENT_WIDTH - MARGIN;
        addParamTextAligned(band, "TIP_NAZIV",      MARGIN,             68, DOC_W / 2, 16, 12, true,  HorizontalTextAlignEnum.LEFT);
        addLabeledParamText(band, "br. ", "BROJ_DOKUMENTA", MARGIN + DOC_W / 2, 68, DOC_W / 2, 16, 10, false);

        // --- Date + partner on same line ---
        addLabeledParamText(band, "Datum: ",   "DATUM",   MARGIN,              86, 160, 13, 9, false);
        addLabeledParamText(band, "Partner: ", "PARTNER", MARGIN + 165,        86, DOC_W - 165, 13, 9, false);

        // --- Optional note ---
        addLabeledParamText(band, "Napomena: ", "NAPOMENA", MARGIN, 101, DOC_W, 13, 9, false);

        // --- Table header separator ---
        addHorizontalLine(band, MARGIN, 118, PORTRAIT_CONTENT_WIDTH - MARGIN, Color.BLACK);

        return band;
    }

    private static JRDesignBand buildPrometColumnHeader() {
        JRDesignBand band = new JRDesignBand();
        band.setHeight(COL_HEADER_HEIGHT);
        int[]     wP    = {25, 155, 115, 50, 65, 45, 80};
        String[]  hdrs  = {"Rb", "Artikal", "Varijanta", "Kol.", "Cijena", "Pop.%", "Ukupno"};
        boolean[] right = {true, false, false, true, true, true, true};
        buildColumnHeaderAligned(band, wP, hdrs, right);
        return band;
    }

    private static JRDesignBand buildPrometSummary(int contentWidth) {
        JRDesignBand band = new JRDesignBand();
        band.setHeight(44);
        addHorizontalLine(band, MARGIN, 2, contentWidth - MARGIN, Color.BLACK);
        addSummaryRow(band, "UKUPNO:", "$P{UKUPNO}", 8, contentWidth);

        // Page footer: generated date (left) + page/total (right)
        JRDesignTextField pageInfo = new JRDesignTextField();
        pageInfo.setX(contentWidth - 110);
        pageInfo.setY(28);
        pageInfo.setWidth(110);
        pageInfo.setHeight(14);
        pageInfo.setFontSize(8f);
        pageInfo.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        JRDesignExpression pageExpr = new JRDesignExpression();
        pageExpr.setText("\"Stranica \" + $V{PAGE_NUMBER} + \"/\" + $V{PAGE_NUMBER}");
        pageInfo.setExpression(pageExpr);
        band.addElement(pageInfo);

        JRDesignTextField genDate = new JRDesignTextField();
        genDate.setX(MARGIN);
        genDate.setY(28);
        genDate.setWidth(200);
        genDate.setHeight(14);
        genDate.setFontSize(8f);
        JRDesignExpression genExpr = new JRDesignExpression();
        genExpr.setText("\"Generirano: \" + new java.text.SimpleDateFormat(\"dd.MM.yyyy\").format(new java.util.Date())");
        genDate.setExpression(genExpr);
        band.addElement(genDate);

        return band;
    }

    // --------------------------------------------------------- povrat dobavljacu

    /**
     * A4 Portrait PDF for povrat dobavljacu.
     * Columns: rb, artikal, varijanta, kolicina, vpc, pdvProcenat, iznosPdv
     * Usable width: 535pt (PORTRAIT_CONTENT_WIDTH - MARGIN).
     * Column widths: 25+180+115+50+70+45+50 = 535.
     */
    static JasperReport buildPovratDobavljacuReport() {
        try {
            JasperDesign design = portraitDesign("povrat-dobavljacu");

            addParameter(design, "KOMPANIJA_NAZIV",  String.class);
            addParameter(design, "KOMPANIJA_ADRESA", String.class);
            addParameter(design, "KOMPANIJA_GRAD",   String.class);
            addParameter(design, "POSLOVNICA_NAZIV", String.class);
            addParameter(design, "TIP_NAZIV",        String.class);
            addParameter(design, "BROJ_DOKUMENTA",   String.class);
            addParameter(design, "DATUM",            String.class);
            addParameter(design, "PARTNER",          String.class);
            addParameter(design, "NAPOMENA",         String.class);
            addParameter(design, "LOGO_IMAGE",       BufferedImage.class);
            addParameter(design, "UKUPNO_VPC",       BigDecimal.class);
            addParameter(design, "UKUPNO_PDV",       BigDecimal.class);
            addParameter(design, "UKUPNO",           BigDecimal.class);

            addField(design, "rb",          Integer.class);
            addField(design, "artikal",     String.class);
            addField(design, "varijanta",   String.class);
            addField(design, "kolicina",    BigDecimal.class);
            addField(design, "vpc",         BigDecimal.class);
            addField(design, "pdvProcenat", BigDecimal.class);
            addField(design, "iznosPdv",    BigDecimal.class);
            addField(design, "iznosVpc",    BigDecimal.class);
            addField(design, "ukupno",      BigDecimal.class);

            design.setTitle(buildPovratTitleBand());
            design.setColumnHeader(buildPovratColumnHeader());

            JRDesignBand detail = new JRDesignBand();
            detail.setHeight(DETAIL_HEIGHT);
            // Combined #Artikal=175, Varijanta=85, Kol.=40, Nab.VPC=60, PDV%=35, Iznos PDV=50, Uk.VPC=45, Ukupno=45 => 535
            int[]     wPov  = {175, 85, 40, 60, 35, 50, 45, 45};
            String[]  exps  = {
                "String.valueOf($F{rb}) + \" \" + $F{artikal}",
                "$F{varijanta}",
                "$F{kolicina}",
                "$F{vpc}",
                "$F{pdvProcenat}",
                "$F{iznosPdv}",
                "$F{iznosVpc}",
                "$F{ukupno}"
            };
            String[]  pPov  = {null, null, "#,##0.###", "#,##0.00", "#,##0.##", "#,##0.00", "#,##0.00", "#,##0.00"};
            boolean[] rPov  = {false, false, true, true, true, true, true, true};
            buildDetailBandWithExpressions(detail, wPov, exps, pPov, rPov);
            addHorizontalLine(detail, MARGIN, DETAIL_HEIGHT - 1, PORTRAIT_CONTENT_WIDTH - MARGIN, new Color(230, 230, 230));
            ((JRDesignSection) design.getDetailSection()).addBand(detail);

            design.setSummary(buildPovratSummary(PORTRAIT_CONTENT_WIDTH));
            design.setPageFooter(buildPovratPageFooter(PORTRAIT_CONTENT_WIDTH));

            return JasperCompileManager.compileReport(design);

        } catch (JRException e) {
            throw new BusinessException("Greška pri inicijalizaciji povrat dobavljacu PDF predloška");
        }
    }

    private static JRDesignBand buildPovratTitleBand() {
        final int DOC_W = PORTRAIT_CONTENT_WIDTH - MARGIN;

        JRDesignBand band = new JRDesignBand();
        band.setHeight(128);

        // Company block — all at x=MARGIN, same alignment as doc info below
        addParamTextAligned(band, "KOMPANIJA_NAZIV",  MARGIN, 4,  DOC_W, 16, 12, true,  HorizontalTextAlignEnum.LEFT);
        addParamTextAligned(band, "KOMPANIJA_ADRESA", MARGIN, 21, DOC_W, 13, 9,  false, HorizontalTextAlignEnum.LEFT);
        addParamTextAligned(band, "KOMPANIJA_GRAD",   MARGIN, 34, DOC_W, 13, 9,  false, HorizontalTextAlignEnum.LEFT);
        addLabeledParamText(band, "Poslovnica: ", "POSLOVNICA_NAZIV", MARGIN, 47, DOC_W, 13, 9, false);

        addHorizontalLine(band, MARGIN, 62, DOC_W, Color.BLACK);

        // Document info — same x=MARGIN as company block above
        JRDesignTextField titleLine = new JRDesignTextField();
        titleLine.setX(MARGIN); titleLine.setY(66);
        titleLine.setWidth(DOC_W); titleLine.setHeight(14);
        titleLine.setFontSize(11f); titleLine.setBold(true); titleLine.setBlankWhenNull(true);
        titleLine.setHorizontalTextAlign(HorizontalTextAlignEnum.LEFT);
        JRDesignExpression titleExpr = new JRDesignExpression();
        titleExpr.setText("$P{TIP_NAZIV} + ($P{BROJ_DOKUMENTA} != null && !$P{BROJ_DOKUMENTA}.isEmpty() ? \"  br. \" + $P{BROJ_DOKUMENTA} : \"\")");
        titleLine.setExpression(titleExpr);
        band.addElement(titleLine);

        addLabeledParamText(band, "Datum: ",    "DATUM",    MARGIN, 82,  DOC_W, 13, 9, false);
        addLabeledParamText(band, "Partner: ",  "PARTNER",  MARGIN, 95,  DOC_W, 13, 9, false);
        addLabeledParamText(band, "Napomena: ", "NAPOMENA", MARGIN, 108, DOC_W, 13, 9, false);

        addHorizontalLine(band, MARGIN, 122, DOC_W, Color.BLACK);

        return band;
    }

    private static JRDesignBand buildPovratColumnHeader() {
        JRDesignBand band = new JRDesignBand();
        band.setHeight(COL_HEADER_HEIGHT);
        // Combined #Artikal=175, Varijanta=85, Kol.=40, Nab.VPC=60, PDV%=35, Iznos PDV=50, Uk.VPC=45, Ukupno=45 => 535
        int[]     wPov  = {175, 85, 40, 60, 35, 50, 45, 45};
        String[]  hdrs  = {"#Artikal", "Varijanta", "Kol.", "Nab.VPC", "PDV%", "Iznos PDV", "Uk.VPC", "Ukupno"};
        boolean[] right = {false, false, true, true, true, true, true, true};
        buildColumnHeaderAligned(band, wPov, hdrs, right);
        return band;
    }

    private static JRDesignBand buildPovratSummary(int contentWidth) {
        JRDesignBand band = new JRDesignBand();
        band.setHeight(62);
        addHorizontalLine(band, MARGIN, 2, contentWidth - MARGIN, Color.BLACK);
        addSummaryRow(band, "Ukupno VPC neto:", "$P{UKUPNO_VPC}", 8,  contentWidth);
        addSummaryRow(band, "Ukupno PDV:",      "$P{UKUPNO_PDV}", 26, contentWidth);
        addSummaryRow(band, "UKUPNO:",          "$P{UKUPNO}",     44, contentWidth);
        return band;
    }

    private static JRDesignBand buildPovratPageFooter(int contentWidth) {
        JRDesignBand band = new JRDesignBand();
        band.setHeight(18);

        JRDesignTextField genDate = new JRDesignTextField();
        genDate.setX(MARGIN); genDate.setY(2);
        genDate.setWidth(200); genDate.setHeight(14);
        genDate.setFontSize(8f); genDate.setBlankWhenNull(true);
        JRDesignExpression genExpr = new JRDesignExpression();
        genExpr.setText("\"Generirano: \" + new java.text.SimpleDateFormat(\"dd.MM.yyyy\").format(new java.util.Date())");
        genDate.setExpression(genExpr);
        band.addElement(genDate);

        JRDesignTextField pageInfo = new JRDesignTextField();
        pageInfo.setX(contentWidth - 110); pageInfo.setY(2);
        pageInfo.setWidth(110); pageInfo.setHeight(14);
        pageInfo.setFontSize(8f);
        pageInfo.setHorizontalTextAlign(HorizontalTextAlignEnum.RIGHT);
        JRDesignExpression pageExpr = new JRDesignExpression();
        pageExpr.setText("\"Stranica \" + $V{PAGE_NUMBER} + \"/\" + $V{PAGE_COUNT}");
        pageInfo.setExpression(pageExpr);
        band.addElement(pageInfo);

        return band;
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
