package ba.maloprodaja.common.tenant;

public class TenantContext {

    private static final ThreadLocal<Long> CURRENT_KOMPANIJA = new ThreadLocal<>();
    private static final ThreadLocal<Long> CURRENT_POSLOVNICA = new ThreadLocal<>();

    private TenantContext() {}

    public static Long getIdKompanije() {
        return CURRENT_KOMPANIJA.get();
    }

    public static Long getIdPoslovnice() {
        return CURRENT_POSLOVNICA.get();
    }

    public static void setIdKompanije(Long idKompanije) {
        CURRENT_KOMPANIJA.set(idKompanije);
    }

    public static void setIdPoslovnice(Long idPoslovnice) {
        CURRENT_POSLOVNICA.set(idPoslovnice);
    }

    public static void clear() {
        CURRENT_KOMPANIJA.remove();
        CURRENT_POSLOVNICA.remove();
    }
}
