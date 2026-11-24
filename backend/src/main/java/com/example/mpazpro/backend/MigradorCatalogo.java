package com.example.mpazpro.backend;

import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Copia interna entre conexiones ya abiertas. No expuesta por HTTP.
 * Solo para ensayos aislados: requiere origen H2 cerrado y destino MySQL vacío.
 */
public final class MigradorCatalogo {
    private static final List<String> ORDEN = List.of(
        "identidad_catalogo", "cursos_compartidos", "actividades",
        "actividades_completas", "publicaciones_actividad", "publicaciones_retiradas",
        "intentos_aula", "identidades_estudiante_aula"
    );
    // Las tablas antiguas solo se omiten si están vacías; nunca se eliminan.
    private static final Set<String> TABLAS_ANTIGUAS = Set.of(
        "instituciones", "miembros_institucion", "cursos_institucion",
        "credenciales_institucion", "asignaciones_docente"
    );
    private static final Pattern NOMBRE = Pattern.compile("[a-z][a-z0-9_]*");

    public Map<String, Long> copiar(Connection origen, Connection destino) throws SQLException {
        if (!origen.getMetaData().getDatabaseProductName().equals("H2") ||
            !destino.getMetaData().getDatabaseProductName().equals("MySQL"))
            throw new SQLException("Se requiere origen H2 y destino MySQL");
        if (!destino.getAutoCommit()) throw new SQLException("El destino debe tener autocommit activo");
        Map<String, String> origenTablas = tablas(origen);
        Map<String, String> destinoTablas = tablas(destino);
        revisarTablasAntiguas(origen, origenTablas);
        revisarTablasAntiguas(destino, destinoTablas);
        Set<String> requeridas = new HashSet<>(ORDEN);
        requeridas.remove("identidades_estudiante_aula");
        if (!origenTablas.keySet().containsAll(requeridas) ||
            !new HashSet<>(ORDEN).containsAll(origenTablas.keySet()) ||
            !destinoTablas.keySet().equals(new HashSet<>(ORDEN)))
            throw new SQLException("Esquema incompleto o contiene tablas desconocidas");

        Map<String, List<String>> columnas = new LinkedHashMap<>();
        for (String tabla : ORDEN) {
            if (!origenTablas.containsKey(tabla)) continue;
            List<String> desde = columnas(origen, origenTablas.get(tabla));
            List<String> hacia = columnas(destino, destinoTablas.get(tabla));
            if (desde.isEmpty() || !new HashSet<>(desde).equals(new HashSet<>(hacia)))
                throw new SQLException("Columnas incompatibles en " + tabla);
            columnas.put(tabla, desde);
        }

        try (Statement consulta = destino.createStatement();
             ResultSet rs = consulta.executeQuery("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE' AND (engine IS NULL OR UPPER(engine) <> 'INNODB')")) {
            rs.next();
            if (rs.getLong(1) != 0) throw new SQLException("El destino debe usar tablas InnoDB");
        }
        try (Statement consulta = origen.createStatement();
             ResultSet rs = consulta.executeQuery("SELECT identificador FROM identidad_catalogo")) {
            if (!rs.next()) throw new SQLException("Falta la identidad del catálogo");
            String identidad = rs.getString(1);
            try {
                if (!UUID.fromString(identidad).toString().equals(identidad))
                    throw new IllegalArgumentException();
            } catch (RuntimeException e) {
                throw new SQLException("Identidad del catálogo inválida", e);
            }
            if (rs.next()) throw new SQLException("Más de una identidad de catálogo");
        }
        destino.setAutoCommit(false);
        try {
            for (String tabla : ORDEN) {
                if (contar(destino, tabla) != 0) throw new SQLException("Destino no vacío: " + tabla);
            }
            Map<String, Long> totales = new LinkedHashMap<>();
            for (String tabla : ORDEN) {
                List<String> campos = columnas.get(tabla);
                if (campos == null) {
                    totales.put(tabla, 0L);
                    continue;
                }
                String lista = String.join(", ", campos.stream().map(c -> "`" + c + "`").toList());
                String marcas = String.join(", ", Collections.nCopies(campos.size(), "?"));
                String select = "SELECT " + String.join(", ", campos) + " FROM " + tabla;
                String insert = "INSERT INTO `" + tabla + "` (" + lista + ") VALUES (" + marcas + ")";
                List<String> claves = clavesPrimarias(origen, origenTablas.get(tabla));
                if (claves.isEmpty() || !campos.containsAll(claves))
                    throw new SQLException("Falta clave primaria en " + tabla);
                String condicion = String.join(" AND ", claves.stream().map(c -> "`" + c + "` = ?").toList());
                String verificar = "SELECT " + lista + " FROM `" + tabla + "` WHERE " + condicion;
                long cantidad = 0;
                try (Statement lector = origen.createStatement();
                     ResultSet registros = lector.executeQuery(select);
                     PreparedStatement escritor = destino.prepareStatement(insert);
                     PreparedStatement comprobador = destino.prepareStatement(verificar)) {
                    while (registros.next()) {
                        for (int i = 1; i <= campos.size(); i++) escritor.setObject(i, registros.getObject(i));
                        escritor.executeUpdate();
                        for (int i = 0; i < claves.size(); i++)
                            comprobador.setObject(i + 1, registros.getObject(claves.get(i)));
                        try (ResultSet copia = comprobador.executeQuery()) {
                            if (!copia.next()) throw new SQLException("Registro no encontrado en " + tabla);
                            for (int i = 1; i <= campos.size(); i++)
                                if (!valoresIguales(registros.getObject(i), copia.getObject(i)))
                                    throw new SQLException("Dato distinto en " + tabla + "." + campos.get(i - 1));
                            if (copia.next()) throw new SQLException("Clave duplicada en " + tabla);
                        }
                        cantidad++;
                    }
                }
                if (contar(destino, tabla) != cantidad) throw new SQLException("Conteo incorrecto: " + tabla);
                totales.put(tabla, cantidad);
            }
            destino.commit();
            return Collections.unmodifiableMap(totales);
        } catch (SQLException | RuntimeException fallo) {
            try {
                destino.rollback();
            } catch (SQLException rollback) {
                fallo.addSuppressed(rollback);
                try { destino.close(); } catch (SQLException cierre) { fallo.addSuppressed(cierre); }
            }
            throw fallo;
        } finally {
            if (!destino.isClosed()) destino.setAutoCommit(true);
        }
    }

    private static void revisarTablasAntiguas(Connection conexion, Map<String, String> tablas) throws SQLException {
        for (String tabla : TABLAS_ANTIGUAS) {
            if (tablas.containsKey(tabla)) {
                if (contar(conexion, tabla) != 0)
                    throw new SQLException("Hay datos en una tabla antigua; se requiere revisión: " + tabla);
                tablas.remove(tabla);
            }
        }
    }

    private static Map<String, String> tablas(Connection conexion) throws SQLException {
        Map<String, String> tablas = new HashMap<>();
        try (ResultSet rs = conexion.getMetaData().getTables(
                conexion.getCatalog(), conexion.getSchema(), "%", new String[] {"TABLE", "BASE TABLE"})) {
            while (rs.next()) {
                String real = rs.getString("TABLE_NAME");
                String nombre = real.toLowerCase(Locale.ROOT);
                if (!NOMBRE.matcher(nombre).matches() || tablas.put(nombre, real) != null)
                    throw new SQLException("Nombre de tabla inválido o duplicado");
            }
        }
        return tablas;
    }

    private static List<String> columnas(Connection conexion, String tabla) throws SQLException {
        List<String> columnas = new ArrayList<>();
        try (ResultSet rs = conexion.getMetaData().getColumns(
                conexion.getCatalog(), conexion.getSchema(), tabla, "%")) {
            while (rs.next()) {
                String nombre = rs.getString("COLUMN_NAME").toLowerCase(Locale.ROOT);
                if (!NOMBRE.matcher(nombre).matches() || columnas.contains(nombre))
                    throw new SQLException("Nombre de columna inválido o duplicado");
                columnas.add(nombre);
            }
        }
        return columnas;
    }

    private static List<String> clavesPrimarias(Connection conexion, String tabla) throws SQLException {
        Map<Integer, String> ordenadas = new TreeMap<>();
        try (ResultSet rs = conexion.getMetaData().getPrimaryKeys(conexion.getCatalog(), conexion.getSchema(), tabla)) {
            while (rs.next()) {
                String nombre = rs.getString("COLUMN_NAME").toLowerCase(Locale.ROOT);
                if (!NOMBRE.matcher(nombre).matches()) throw new SQLException("Clave primaria inválida");
                ordenadas.put(rs.getInt("KEY_SEQ"), nombre);
            }
        }
        return new ArrayList<>(ordenadas.values());
    }

    private static boolean valoresIguales(Object primero, Object segundo) {
        if (primero == null || segundo == null) return primero == segundo;
        if (primero instanceof byte[] a && segundo instanceof byte[] b) return Arrays.equals(a, b);
        if (primero instanceof Boolean a && segundo instanceof Number b) return (a ? 1 : 0) == b.intValue();
        if (segundo instanceof Boolean b && primero instanceof Number a) return (b ? 1 : 0) == a.intValue();
        if (primero instanceof Number a && segundo instanceof Number b)
            return new java.math.BigDecimal(a.toString()).compareTo(new java.math.BigDecimal(b.toString())) == 0;
        return primero.equals(segundo);
    }

    private static long contar(Connection conexion, String tabla) throws SQLException {
        try (Statement consulta = conexion.createStatement();
             ResultSet rs = consulta.executeQuery("SELECT COUNT(*) FROM `" + tabla + "`")) {
            rs.next();
            return rs.getLong(1);
        }
    }
}
