package ba.maloprodaja.dokumenti.importPocetak.service;

import ba.maloprodaja.common.exception.BusinessException;
import ba.maloprodaja.dokumenti.importPocetak.dto.ImportResultDTO;
import ba.maloprodaja.sifarnici.artikalkomp.entity.ArtikalKompanija;
import ba.maloprodaja.sifarnici.artikalkomp.entity.TipMarze;
import ba.maloprodaja.sifarnici.artikalkomp.repository.ArtikalKompanijeRepository;
import ba.maloprodaja.sifarnici.artikalposl.entity.ArtikalPoslovnica;
import ba.maloprodaja.sifarnici.artikalposl.repository.ArtikalPoslovnicaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ImportPocetnogStanjaService implements IImportPocetnogStanjaService {

    private final ArtikalKompanijeRepository artikalKompRepository;
    private final ArtikalPoslovnicaRepository artikalPoslovnicaRepository;

    @Override
    public ImportResultDTO importuj(MultipartFile file, Long idKompanije, Long idPoslovnice) {
        List<String> greske = new ArrayList<>();
        int uspjesnoUvezeno = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            // Red 0 je zaglavlje, počinjemo od reda 1
            for (int rowIndex = 1; rowIndex <= lastRowNum; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || jePrazanRed(row)) {
                    continue;
                }

                int redBroj = rowIndex + 1; // za poruku korisniku (1-based, ali red 1 je header)

                String sifra = citajString(row.getCell(0));
                if (sifra == null || sifra.isBlank()) {
                    greske.add("Red " + redBroj + ": šifra artikla je obavezna");
                    continue;
                }
                sifra = sifra.trim();

                Optional<ArtikalKompanija> artikalOpt = artikalKompRepository.findBySifraAndIdKompanije(sifra, idKompanije);
                if (artikalOpt.isEmpty()) {
                    greske.add("Red " + redBroj + ": artikal sa šifrom '" + sifra + "' nije pronađen");
                    continue;
                }

                ArtikalKompanija artikal = artikalOpt.get();
                BigDecimal kolicina = citajDecimal(row.getCell(2));
                BigDecimal vpc = citajDecimal(row.getCell(3));
                BigDecimal mpc = citajDecimal(row.getCell(4));

                if (kolicina == null) kolicina = BigDecimal.ZERO;
                if (vpc == null) vpc = BigDecimal.ZERO;
                if (mpc == null) mpc = BigDecimal.ZERO;

                azurirajIliKreirajArtikalPoslovnica(artikal.getId(), idKompanije, idPoslovnice, kolicina, vpc, mpc);
                uspjesnoUvezeno++;
            }

            int ukupnoRedova = uspjesnoUvezeno + greske.size();
            log.info("Import početnog stanja završen: ukupno={}, uspješno={}, preskočeno={}, idKompanije={}, idPoslovnice={}",
                    ukupnoRedova, uspjesnoUvezeno, greske.size(), idKompanije, idPoslovnice);

            return new ImportResultDTO(ukupnoRedova, uspjesnoUvezeno, greske.size(), greske);

        } catch (IOException e) {
            if (e.getCause() instanceof org.apache.poi.openxml4j.exceptions.InvalidFormatException) {
                log.error("Neispravan format Excel fajla: {}", e.getMessage());
                throw new BusinessException("Neispravan format Excel fajla");
            }
            log.error("Greška pri čitanju Excel fajla: {}", e.getMessage());
            throw new BusinessException("Neispravan format Excel fajla");
        }
    }

    private void azurirajIliKreirajArtikalPoslovnica(
            Long idArtikla, Long idKompanije, Long idPoslovnice,
            BigDecimal kolicina, BigDecimal vpc, BigDecimal mpc) {

        Optional<ArtikalPoslovnica> postojeciOpt =
                artikalPoslovnicaRepository.findByIdArtiklaAndIdPoslovnice(idArtikla, idPoslovnice);

        if (postojeciOpt.isPresent()) {
            ArtikalPoslovnica postojeci = postojeciOpt.get();
            postojeci.setKolicina(kolicina);
            postojeci.setVpc(vpc);
            postojeci.setMpc(mpc);
            artikalPoslovnicaRepository.save(postojeci);
        } else {
            ArtikalPoslovnica novi = new ArtikalPoslovnica();
            novi.setIdKompanije(idKompanije);
            novi.setIdPoslovnice(idPoslovnice);
            novi.setIdArtikla(idArtikla);
            novi.setKolicina(kolicina);
            novi.setVpc(vpc);
            novi.setMpc(mpc);
            novi.setTipMarze(TipMarze.SLOBODNA);
            novi.setAktivan(true);
            artikalPoslovnicaRepository.save(novi);
        }
    }

    private boolean jePrazanRed(Row row) {
        for (int i = 0; i < 5; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private String citajString(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> null;
        };
    }

    private BigDecimal citajDecimal(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING -> {
                try {
                    yield new BigDecimal(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
            default -> null;
        };
    }
}
