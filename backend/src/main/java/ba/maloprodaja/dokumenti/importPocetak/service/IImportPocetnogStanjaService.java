package ba.maloprodaja.dokumenti.importPocetak.service;

import ba.maloprodaja.dokumenti.importPocetak.dto.ImportResultDTO;
import org.springframework.web.multipart.MultipartFile;

public interface IImportPocetnogStanjaService {

    ImportResultDTO importuj(MultipartFile file, Long idKompanije, Long idPoslovnice);
}
