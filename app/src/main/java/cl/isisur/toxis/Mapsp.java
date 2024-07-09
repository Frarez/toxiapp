package cl.isisur.toxis;

public class Mapsp {
    private Double latitud;
    private Double longitud;
    private String usuario;

    public Mapsp() {
        // Default constructor required for calls to DataSnapshot.getValue(Mapsp.class)
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
}
