package ba.maloprodaja.dokumenti.nivelacija.service;

import ba.maloprodaja.dokumenti.enums.VrstaNivelacije;
import ba.maloprodaja.dokumenti.nivelacija.dto.NivelacijaDTO;

import java.math.BigDecimal;
import java.util.List;

public interface INivelacijaService {

    record NivelacijaStavkaInfo(Long idArtikla, BigDecimal kolicina, BigDecimal vpc) {}

    List<NivelacijaDTO.ListItemDTO> listAll(Long idKompanije, Long idPoslovnice);

    NivelacijaDTO.DetailDTO findById(Long id);

    /**
     * Kreira automatsku nivelaciju na osnovu potvrđenog prometnog dokumenta (UF, MSU...).
     * Poziva se iz DokumentService.potvrdi() kada tip dokumenta ima smjer +1 (ulaz robe).
     *
     * @param idDokumenta ID prometnog dokumenta (sprema se kao referenca u nivelacija.id_fakture ili id_otpremnice)
     * @param vrsta       AUTOMATSKA_FAKTURA ili AUTOMATSKA_OTPREMNICA
     * @param napomena    napomena sa originalnog dokumenta
     * @param stavkeInfo  lista stavki sa idArtikla, kolicinom i vpc
     */
    void kreirajAutomatskuZaDokument(Long idDokumenta, VrstaNivelacije vrsta,
                                     Long idKompanije, Long idPoslovnice,
                                     String napomena, List<NivelacijaStavkaInfo> stavkeInfo);

    NivelacijaDTO.DetailDTO kreirajRucnu(NivelacijaDTO.CreateRucnaDTO dto, Long idKompanije, Long idPoslovnice);
}
