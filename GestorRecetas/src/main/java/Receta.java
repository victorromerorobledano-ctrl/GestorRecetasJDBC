public class Receta {

    private int id;
    private String nombre;
    private String categoria;
    private int tiempoPreparacion;
    private String dificultad;
    private int raciones;

    public Receta() {
    }

    public Receta(String nombre, String categoria, int tiempoPreparacion, String dificultad, int raciones) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.tiempoPreparacion = tiempoPreparacion;
        this.dificultad = dificultad;
        this.raciones = raciones;
    }

    public Receta(int id, String nombre, String categoria, int tiempoPreparacion, String dificultad, int raciones) {
        this(nombre, categoria, tiempoPreparacion, dificultad, raciones);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public int getTiempoPreparacion() {
        return tiempoPreparacion;
    }

    public void setTiempoPreparacion(int tiempoPreparacion) {
        this.tiempoPreparacion = tiempoPreparacion;
    }

    public String getDificultad() {
        return dificultad;
    }

    public void setDificultad(String dificultad) {
        this.dificultad = dificultad;
    }

    public int getRaciones() {
        return raciones;
    }

    public void setRaciones(int raciones) {
        this.raciones = raciones;
    }

    @Override
    public String toString() {
        return "Receta [id=" + id + ", nombre=" + nombre + ", categoria=" + categoria
                + ", tiempo=" + tiempoPreparacion + " min, dificultad=" + dificultad
                + ", raciones=" + raciones + "]";
    }
}
