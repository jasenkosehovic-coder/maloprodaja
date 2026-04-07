package ba.maloprodaja.promet.tipdokumenta.service;

import ba.maloprodaja.promet.tipdokumenta.dto.TipDokumentaDTO;

import java.util.List;

public interface ITipDokumentaService {

    List<TipDokumentaDTO> findAll(Long idKompanije);
}
