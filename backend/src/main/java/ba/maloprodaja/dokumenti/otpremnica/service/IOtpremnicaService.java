package ba.maloprodaja.dokumenti.otpremnica.service;

import ba.maloprodaja.dokumenti.otpremnica.dto.OtpremnicaDTO;

import java.util.List;

public interface IOtpremnicaService {

    List<OtpremnicaDTO.ListItemDTO> listAll(Long idKompanije, Long idPoslovnice, Integer godina);

    OtpremnicaDTO.DetailDTO findById(Long id);

    OtpremnicaDTO.DetailDTO create(OtpremnicaDTO.CreateDTO dto, Long idKompanije, Long idPoslovnicePosiljaoca);

    void posalji(Long id);

    void potvrdiPrijem(Long id, OtpremnicaDTO.PotvrdiPrijemDTO dto, Long idPoslovnicePrimaoca);

    void storno(Long id, Long idKompanije, Long idPoslovnice);
}
