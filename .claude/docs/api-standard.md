# REST API standard

Obavezujuće konvencije za sve nove i izmijenjene endpointe u `ba.maloprodaja`.
Postojeći kod djelomično odstupa — odstupanja su označena i evidentirana u `TASKS.md`.

---

## 1. Imenovanje i rute

- Base path: `/api`
- Resursi u **množini, kebab-case, bosanski** (dosljedno sa domenom):
  `/api/artikli`, `/api/tipovi-velicina`, `/api/definicije-atributa`
- Ugniježđeni resursi samo jedan nivo duboko:
  `/api/dokumenti/{id}/stavke` ✅
  `/api/dokumenti/{id}/stavke/{sid}/atributi` ❌ → izdvoji u vlastiti resurs
- Akcije koje nisu CRUD → pod-resurs u imperativu:
  `POST /api/dokumenti/{id}/potvrdi`, `POST /api/dokumenti/{id}/storniraj`
- Nikad glagol u imenu resursa: `/api/getArtikli` ❌

## 2. HTTP verbi i status kodovi

| Operacija | Verb | Uspjeh | Napomena |
|---|---|---|---|
| Lista | `GET` | `200` | uvijek paginirana (v. §4) |
| Jedan zapis | `GET` | `200` / `404` | |
| Kreiranje | `POST` | `201` + `Location` header | |
| Puna izmjena | `PUT` | `200` | svi obavezni field-ovi |
| Djelomična izmjena | `PATCH` | `200` | nullable field = "ne mijenjaj" |
| Deaktivacija | `PATCH .../deaktiviraj` | `204` | **soft delete — nikad `DELETE`** |
| Domain akcija | `POST` | `200` | vraća novo stanje resursa |

- `400` — validacijska greška (Bean Validation)
- `401` — nema/nevaljan token
- `403` — token valjan, nema prava (RBAC)
- `409` — poslovni konflikt (`BusinessException`): duplikat, nedovoljna zaliha, pogrešan status
- `422` se **ne koristi** — sve validacijske greške su `400`

> **Soft delete je pravilo.** Nijedan poslovni zapis se fizički ne briše;
> `aktivan = false`. Fizički `DELETE` samo za nepotvrđene nacrte i many-to-many veze.

## 3. Response envelope

Svaki odgovor je omotan u `ApiResponse<T>`:

```json
{
  "success": true,
  "data": {},
  "message": null,
  "errors": null,
  "correlationId": "b1f2...",
  "timestamp": "2026-09-17T10:04:11Z"
}
```

Greška:

```json
{
  "success": false,
  "data": null,
  "message": "Artikal je već dodan u ovu poslovnicu.",
  "errors": [
    { "field": "sifra", "code": "DUPLICATE", "message": "Šifra već postoji" }
  ],
  "correlationId": "b1f2...",
  "timestamp": "2026-09-17T10:04:11Z"
}
```

- `message` — jedna poruka za korisnika, **na bosanskom** (UI je bosanski)
- `errors` — po-field greške za mapiranje na formu; `null` kad nema field konteksta
- `code` — stabilan mašinski enum; frontend nikad ne parsira `message`
- `correlationId` — iz MDC-a, ide u log i u odgovor za support

> ⚠️ **Trenutno odstupanje:** backend nikad ne popunjava `errors` a frontend ga čita;
> `correlationId` ne postoji. `TASKS.md` P0.7 i P2.6.

## 4. Paginacija, sortiranje, filtriranje

- Koristi Spring `Pageable` — **nikad ručni `page`/`size` parametri**
- Query params: `?page=0&size=20&sort=naziv,asc`
- Default `size = 20`, hard max `100` (`spring.data.web.pageable.max-page-size`)
- Filteri su eksplicitni named parametri, ne generički query DSL:
  `?idPoslovnice=3&aktivan=true&naziv=maj`
- `data` za listu:

```json
{ "content": [], "page": 0, "size": 20, "totalElements": 0, "totalPages": 0 }
```

> ⚠️ **Trenutno odstupanje:** `PageResponseDTO` oblik se ne poklapa sa frontendom.

## 5. DTO pravila

- **Nikad ne izlaži entitet.** Svaki endpoint prima i vraća DTO.
- DTO su **Java `record`**, grupirani u nested holder po resursu:
  `ArtikalPoslovnicaDTO.CreateDTO`, `.UpdateDTO`, `.ListItemDTO`, `.DetailDTO`
- `CreateDTO` — obavezna polja `@NotNull`
- `UpdateDTO` — sva polja nullable = "ne mijenjaj"; servis primjenjuje samo non-null
- `ListItemDTO` — flat, denormaliziran (`artikalNaziv`), bez ugniježđenih objekata
- **Klijent nikad ne šalje izračunata polja.** Cijena, iznos, popust, ukupno, status,
  broj dokumenta — servis ih izračunava.
  > ⚠️ Popust se trenutno uzima iz klijentskog DTO-a nevalidirano (`TASKS.md` P1.3).
- **Klijent nikad ne šalje `idKompanije`** — dolazi iz JWT-a.

## 6. Validacija

- Sintaksna: Bean Validation na DTO-u + `@Valid` na kontroleru
- Poslovna: **u servisnom sloju**, `BusinessException` → `409`
- Kontroler ne sadrži logiku osim mapiranja i delegiranja

```java
@PostMapping
public ResponseEntity<ApiResponse<ArtikalPoslovnicaDTO.ListItemDTO>> create(
        @Valid @RequestBody ArtikalPoslovnicaDTO.CreateDTO dto,
        @AuthenticationPrincipal KorisnikPrincipal principal) {
    var created = service.create(dto, principal.idKompanije());
    return ResponseEntity
            .created(URI.create("/api/artikli-poslovnice/" + created.id()))
            .body(ApiResponse.ok(created));
}
```

## 7. Multi-tenancy — obavezno

**Svaki** read i write mora biti ograničen na tenant iz JWT-a.

- `idKompanije` (i gdje treba `idPoslovnice`) **isključivo** iz
  `@AuthenticationPrincipal`, nikad iz body-ja ili query parametra
- **Zabranjen uzorak:** `repository.findById(id)` za tenant-scoped entitet
- **Obavezan uzorak:** `repository.findByIdAndIdKompanije(id, idKompanije)`
  → `ResourceNotFoundException` (ne `403` — ne otkrivaj postojanje tuđeg zapisa)
- Svaki novi tenant-scoped endpoint mora imati **negativni integracijski test** koji
  dokazuje da tenant A ne vidi i ne mijenja podatke tenanta B

> ⚠️ **Trenutno odstupanje:** ~9 servisa koristi bare `findById` → cross-tenant IDOR.
> `TenantContext` je mrtav kod. `TASKS.md` P0.4.

## 8. Autorizacija

- `@PreAuthorize` na kontroler metodi, ne u servisu
- Role: `ADMINISTRATOR`, `KNJIGOVODJA`, `MENADZER`, `BLAGAJNIK`
- Individualni izbornici (`korisnik.izbornici`) kontroliraju **vidljivost UI-a**, ne
  API autorizaciju — API se uvijek štiti rolom

## 9. Obrada greške

`GlobalExceptionHandler` mora pokrivati:

| Exception | Status |
|---|---|
| `MethodArgumentNotValidException` | `400` + `errors[]` po field-u |
| `ConstraintViolationException` | `400` |
| `HttpMessageNotReadableException` | `400` |
| `MethodArgumentTypeMismatchException` | `400` |
| `ResourceNotFoundException` | `404` |
| `BusinessException` | `409` |
| `AccessDeniedException` | `403` |
| `AuthenticationException` | `401` |
| `DataIntegrityViolationException` | `409` — nikad ne propusti SQL detalje |
| `OptimisticLockingFailureException` | `409` — "zapis je izmijenjen, osvježi" |
| `Exception` | `500` — generička poruka, stacktrace samo u log |

**Nikad** ne vraćaj stacktrace, SQL, ime tablice ili constraint-a klijentu.

## 10. Konkurentnost

- Svaki entitet koji se ažurira (posebno zaliha i brojači) mora imati `@Version`
- Brojači dokumenata: pesimistički lock (`@Lock(PESSIMISTIC_WRITE)`) ili DB sekvenca
- Read-modify-write nad zalihom bez locka je zabranjen

> ⚠️ **Trenutno odstupanje:** nema `@Version` nigdje. `TASKS.md` P0.5.

## 11. OpenAPI

- Svaki kontroler `@Tag`; svaka metoda `@Operation(summary=...)` + `@ApiResponses`
- DTO polja `@Schema(description=...)` gdje ime nije samoobjašnjavajuće
- Enum vrijednosti moraju biti u spec-u (frontend generira tipove iz njih)
- **Cilj:** frontend modeli se generiraju iz OpenAPI spec-a, ne pišu ručno — to bi
  spriječilo cijelu klasu bugova tipa `POTVRDEN` vs `'POTVRĐEN'`

## 12. Enum vrijednosti preko API-ja

- Enum se serijalizira kao **ASCII string bez dijakritika**: `POTVRDEN`, ne `POTVRĐEN`
- Prikazni tekst sa dijakriticima je odgovornost frontenda (label mapa)
- Tvrdo pravilo — dijakritici u wire formatu su već jednom slomili FE/BE kontrakt
