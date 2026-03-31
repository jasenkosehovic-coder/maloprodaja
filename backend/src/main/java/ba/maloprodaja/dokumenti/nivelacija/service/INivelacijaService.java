package ba.maloprodaja.dokumenti.nivelacija.service;

import ba.maloprodaja.dokumenti.nivelacija.dto.NivelacijaDTO;

import java.math.BigDecimal;
import java.util.List;

public interface INivelacijaService {

    record NivelacijaStavkaInfo(Long idArtikla, BigDecimal kolicina, BigDecimal vpc) {}

    List<NivelacijaDTO.ListItemDTO> listAll(Long idKompanije, Long idPoslovnice);

    NivelacijaDTO.DetailDTO findById(Long id);

    void kreirajAutomatskuZaFakturu(Long idFakture, Long idKompanije, Long idPoslovnice);

    void kreirajAutomatskuZaOtpremnicu(Long idOtpremnice, Long idKompanije, Long idPoslovnicePrimaoca,
                                       List<NivelacijaStavkaInfo> stavkeInfo);

    NivelacijaDTO.DetailDTO kreirajRucnu(NivelacijaDTO.CreateRucnaDTO dto, Long idKompanije, Long idPoslovnice);
}
