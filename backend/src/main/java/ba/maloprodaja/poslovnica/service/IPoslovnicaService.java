package ba.maloprodaja.poslovnica.service;

import ba.maloprodaja.poslovnica.dto.PoslovnicaDTO;

import java.util.List;

public interface IPoslovnicaService {

    List<PoslovnicaDTO> listByKompanija(Long idKompanije);
}
