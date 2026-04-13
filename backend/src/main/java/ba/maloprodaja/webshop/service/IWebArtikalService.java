package ba.maloprodaja.webshop.service;

import ba.maloprodaja.webshop.dto.WebArtikalDTO;

import java.util.List;

public interface IWebArtikalService {

    List<WebArtikalDTO.ListItemDTO> list(Long idKompanije);

    WebArtikalDTO.ListItemDTO upsert(WebArtikalDTO.CreateDTO dto, Long idKompanije);

    WebArtikalDTO.ListItemDTO update(Long id, WebArtikalDTO.UpdateDTO dto);

    void deactivate(Long id);
}
