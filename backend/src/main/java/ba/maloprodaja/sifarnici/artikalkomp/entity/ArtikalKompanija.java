package ba.maloprodaja.sifarnici.artikalkomp.entity;

import ba.maloprodaja.common.entity.KompanijaBaseEntity;
import ba.maloprodaja.sifarnici.dobavljac.entity.Dobavljac;
import ba.maloprodaja.sifarnici.grupaartikala.entity.GrupaArtikala;
import ba.maloprodaja.sifarnici.proizvodjac.entity.Proizvodjac;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "artikli_kompanije")
public class ArtikalKompanija extends KompanijaBaseEntity {

    @Column(name = "naziv", nullable = false, length = 300)
    private String naziv;

    @Column(name = "sifra", nullable = false, length = 100)
    private String sifra;

    @Column(name = "opis", length = 1000)
    private String opis;

    @Column(name = "jedin", length = 20)
    private String jedin;

    @Column(name = "pdv", nullable = false, precision = 10, scale = 4)
    private BigDecimal pdv = new BigDecimal("17.00");

    @Column(name = "aktivan", nullable = false)
    private boolean aktivan = true;

    @Column(name = "id_grupe")
    private Long idGrupe;

    @Column(name = "id_proizvodjaca")
    private Long idProizvodjaca;

    @Column(name = "id_dobavljaca")
    private Long idDobavljaca;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_grupe", insertable = false, updatable = false)
    private GrupaArtikala grupaArtikala;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proizvodjaca", insertable = false, updatable = false)
    private Proizvodjac proizvodjac;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dobavljaca", insertable = false, updatable = false)
    private Dobavljac dobavljac;
}
