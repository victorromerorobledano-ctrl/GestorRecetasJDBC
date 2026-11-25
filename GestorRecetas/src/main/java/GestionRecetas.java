import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GestionRecetas {

    // ---------- Utilidad ----------

    private static Receta mapearReceta(ResultSet rs) throws SQLException {
        return new Receta(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("categoria"),
                rs.getInt("tiempo_preparacion"),
                rs.getString("dificultad"),
                rs.getInt("raciones"));
    }

    // ---------- 1. Gestión del catálogo ----------

    public static List<Receta> obtenerRecetas() {
        List<Receta> recetas = new ArrayList<>();
        String sql = "SELECT id, nombre, categoria, tiempo_preparacion, dificultad, raciones FROM recetas";

        try (Connection con = ConexionBD.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                recetas.add(mapearReceta(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar las recetas: " + e.getMessage());
        }
        return recetas;
    }

    public static void mostrarRecetas() {
        List<Receta> recetas = obtenerRecetas();
        if (recetas.isEmpty()) {
            System.out.println("No hay recetas en el catálogo.");
        } else {
            for (Receta r : recetas) {
                System.out.println(r);
            }
        }
    }

    public static Receta buscarPorId(int id) {
        Receta receta = null;
        String sql = "SELECT id, nombre, categoria, tiempo_preparacion, dificultad, raciones FROM recetas WHERE id = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    receta = mapearReceta(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar la receta: " + e.getMessage());
        }
        return receta;
    }

    public static void anadirReceta(Receta r) {
        String sql = "INSERT INTO recetas (nombre, categoria, tiempo_preparacion, dificultad, raciones) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, r.getNombre());
            ps.setString(2, r.getCategoria());
            ps.setInt(3, r.getTiempoPreparacion());
            ps.setString(4, r.getDificultad());
            ps.setInt(5, r.getRaciones());

            int filas = ps.executeUpdate();
            System.out.println("Receta añadida. Filas afectadas: " + filas);

        } catch (SQLException e) {
            System.out.println("Error al añadir la receta: " + e.getMessage());
        }
    }

    public static void modificarReceta(int id, int tiempo, int raciones) {
        String sql = "UPDATE recetas SET tiempo_preparacion = ?, raciones = ? WHERE id = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, tiempo);
            ps.setInt(2, raciones);
            ps.setInt(3, id);

            int filas = ps.executeUpdate();
            if (filas > 0) {
                System.out.println("Receta modificada correctamente.");
            } else {
                System.out.println("No existe ninguna receta con id " + id);
            }

        } catch (SQLException e) {
            System.out.println("Error al modificar la receta: " + e.getMessage());
        }
    }

    public static void eliminarReceta(int id) {
        String sql = "DELETE FROM recetas WHERE id = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int filas = ps.executeUpdate();
            if (filas > 0) {
                System.out.println("Receta eliminada correctamente.");
            } else {
                System.out.println("No existe ninguna receta con id " + id);
            }

        } catch (SQLException e) {
            System.out.println("Error al eliminar la receta: " + e.getMessage());
        }
    }

    // ---------- 3. Transacción ----------

    public static void anadirRecetaConTransaccion(Receta r) {
        String sqlInsert = "INSERT INTO recetas (nombre, categoria, tiempo_preparacion, dificultad, raciones) VALUES (?, ?, ?, ?, ?)";
        String sqlUpdate = "UPDATE estadisticas SET total_recetas = (SELECT COUNT(*) FROM recetas) WHERE id = 1";

        Connection con = null;

        try {
            con = ConexionBD.getConexion();
            con.setAutoCommit(false);

            try (PreparedStatement psInsert = con.prepareStatement(sqlInsert);
                 PreparedStatement psUpdate = con.prepareStatement(sqlUpdate)) {

                psInsert.setString(1, r.getNombre());
                psInsert.setString(2, r.getCategoria());
                psInsert.setInt(3, r.getTiempoPreparacion());
                psInsert.setString(4, r.getDificultad());
                psInsert.setInt(5, r.getRaciones());
                psInsert.executeUpdate();

                int filas = psUpdate.executeUpdate();
                if (filas == 0) {
                    throw new SQLException("No se encontró la fila de estadísticas.");
                }

                con.commit();
                System.out.println("Transacción completada: receta añadida y estadísticas actualizadas.");
            }

        } catch (SQLException e) {
            System.out.println("Error en la transacción: " + e.getMessage());
            if (con != null) {
                try {
                    con.rollback();
                    System.out.println("Se han deshecho todos los cambios (rollback).");
                } catch (SQLException ex) {
                    System.out.println("Error al hacer rollback: " + ex.getMessage());
                }
            }
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    System.out.println("Error al cerrar la conexión: " + e.getMessage());
                }
            }
        }
    }

    // ---------- 4. Procedimiento almacenado ----------

    public static void buscarRecetaProcedimiento(String nombre) {
        String sql = "{CALL buscarReceta(?)}";

        try (Connection con = ConexionBD.getConexion();
             CallableStatement cs = con.prepareCall(sql)) {

            cs.setString(1, nombre);

            try (ResultSet rs = cs.executeQuery()) {
                boolean encontrada = false;
                while (rs.next()) {
                    System.out.println(mapearReceta(rs));
                    encontrada = true;
                }
                if (!encontrada) {
                    System.out.println("No se ha encontrado ninguna receta con el nombre \"" + nombre + "\".");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al ejecutar el procedimiento: " + e.getMessage());
        }
    }

    // ---------- Menú ----------

    private static int leerEntero(Scanner sc, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Introduce un número válido.");
            }
        }
    }

    private static String leerTexto(Scanner sc, String mensaje) {
        System.out.print(mensaje);
        return sc.nextLine().trim();
    }

    private static Receta leerReceta(Scanner sc) {
        String nombre = leerTexto(sc, "Nombre: ");
        String categoria = leerTexto(sc, "Categoría: ");
        int tiempo = leerEntero(sc, "Tiempo de preparación (min): ");
        String dificultad = leerTexto(sc, "Dificultad (Fácil/Media/Difícil): ");
        int raciones = leerEntero(sc, "Raciones: ");
        return new Receta(nombre, categoria, tiempo, dificultad, raciones);
    }

    private static void mostrarMenu() {
        System.out.println("\n===== GESTOR DE RECETAS =====");
        System.out.println("1. Mostrar todas las recetas");
        System.out.println("2. Buscar receta por id");
        System.out.println("3. Añadir receta");
        System.out.println("4. Modificar tiempo y raciones");
        System.out.println("5. Eliminar receta");
        System.out.println("6. Añadir receta con transacción (actualiza estadísticas)");
        System.out.println("7. Buscar receta por nombre (procedimiento almacenado)");
        System.out.println("0. Salir");
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int opcion;

        do {
            mostrarMenu();
            opcion = leerEntero(sc, "Opción: ");

            switch (opcion) {
                case 1:
                    mostrarRecetas();
                    break;
                case 2:
                    int idBuscar = leerEntero(sc, "Id de la receta: ");
                    Receta r = buscarPorId(idBuscar);
                    if (r != null) {
                        System.out.println(r);
                    } else {
                        System.out.println("No existe ninguna receta con id " + idBuscar);
                    }
                    break;
                case 3:
                    anadirReceta(leerReceta(sc));
                    break;
                case 4:
                    int idMod = leerEntero(sc, "Id de la receta: ");
                    int tiempo = leerEntero(sc, "Nuevo tiempo de preparación (min): ");
                    int raciones = leerEntero(sc, "Nuevas raciones: ");
                    modificarReceta(idMod, tiempo, raciones);
                    break;
                case 5:
                    eliminarReceta(leerEntero(sc, "Id de la receta a eliminar: "));
                    break;
                case 6:
                    anadirRecetaConTransaccion(leerReceta(sc));
                    break;
                case 7:
                    buscarRecetaProcedimiento(leerTexto(sc, "Nombre exacto de la receta: "));
                    break;
                case 0:
                    System.out.println("Saliendo...");
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        } while (opcion != 0);

        sc.close();
    }
}
