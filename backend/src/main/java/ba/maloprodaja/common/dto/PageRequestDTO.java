package ba.maloprodaja.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageRequestDTO {

    private int page = 0;
    private int size = 20;
    private String sortBy = "id";
    private String sortDir = "asc";
}
