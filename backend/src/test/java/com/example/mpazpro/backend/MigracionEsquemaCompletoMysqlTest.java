package com.example.mpazpro.backend;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.server.ResponseStatusException;

/** Solo se ejecuta con el contenedor desechable de ensayo en el puerto 3310. */
@EnabledIfEnvironmentVariable(named = "MPAZ_ENSAYO_MIGRACION", matches = "si")
class MigracionEsquemaCompletoMysqlTest {
    private static final String H2 = "jdbc:h2:mem:esquema_completo;DB_CLOSE_DELAY=-1";
    private static final String MYSQL = "jdbc:mysql://127.0.0.1:3310/ensayo";
    private static final String CLAVE = "4abf76c2-347b-4b76-9910-fcb0aff16774";
    private static final String RETIRADA = "5abf76c2-347b-4b76-9910-fcb0aff16774";
    private static final String TOKEN = "6abf76c2-347b-4b76-9910-fcb0aff16774";

    @Test void migraEsquemaRealYRecuperaActividadesEIntentosAlReiniciar() throws Exception {
        String identidad;
        long curso;
        long actividad;
        ActividadCompletaRequest pregunta;
        try (var h2 = iniciar(H2, "sa", "")) {
            identidad = h2.getBean(IdentidadCatalogoRepository.class).findById(1L).orElseThrow().getIdentificador();
            curso = h2.getBean(CursoCompartidoRepository.class).findBySeccion("A").orElseThrow().getId();
            pregunta = new ActividadCompletaRequest(curso, "lenguaje", "El pudú", "Lee", "¿Qué necesita el pudú?",
                "Agua", "Piedras", "Zapatos", "Lápices", 0, 3, "Necesita agua", "Piensa en seres vivos");
            var publicaciones = h2.getBean(PublicacionActividadService.class);
            actividad = publicaciones.publicar(CLAVE, pregunta).actividadId();
            publicaciones.publicar(RETIRADA, pregunta);
            publicaciones.retirar(curso, List.of(RETIRADA));
            h2.getBean(RespuestaAulaService.class).responder(new RespuestaAulaRequest(actividad, curso, TOKEN, 1, 1));
        }
        // Simula una versión anterior que todavía no tiene la tabla de identidades de estudiantes.
        try (Connection antigua = DriverManager.getConnection(H2, "sa", "");
             Statement sql = antigua.createStatement()) {
            sql.execute("DROP TABLE identidades_estudiante_aula");
            for (String tabla : List.of("instituciones", "miembros_institucion", "cursos_institucion",
                    "credenciales_institucion", "asignaciones_docente"))
                sql.execute("CREATE TABLE " + tabla + " (id BIGINT PRIMARY KEY)");
        }
        // Hibernate crea el esquema real; sus registros iniciales se retiran solo del contenedor efímero.
        try (var mysql = iniciar(MYSQL, "ensayo", "soloensayo2026")) {
            assertEquals(1L, mysql.getBean(CursoCompartidoRepository.class).count());
        }
        try (Connection origen = DriverManager.getConnection(H2, "sa", "");
             Connection destino = DriverManager.getConnection(MYSQL, "ensayo", "soloensayo2026");
             Statement sql = destino.createStatement()) {
            sql.executeUpdate("DELETE FROM cursos_compartidos");
            sql.executeUpdate("DELETE FROM identidad_catalogo");
            var totales = new MigradorCatalogo().copiar(origen, destino);
            assertEquals(1L, totales.get("intentos_aula"));
            assertEquals(1L, totales.get("publicaciones_retiradas"));
            assertEquals(0L, totales.get("identidades_estudiante_aula"));
            for (String tabla : totales.keySet())
                if (!tabla.equals("identidades_estudiante_aula")) compararRegistros(origen, destino, tabla);
            assertThrows(SQLException.class, () -> new MigradorCatalogo().copiar(origen, destino));
        }
        try (var mysql = iniciar(MYSQL, "ensayo", "soloensayo2026")) {
            assertEquals(identidad, mysql.getBean(IdentidadCatalogoRepository.class).findById(1L).orElseThrow().getIdentificador());
            var publicaciones = mysql.getBean(PublicacionActividadService.class);
            assertFalse(publicaciones.publicar(CLAVE, pregunta).creada());
            assertThrows(ResponseStatusException.class, () -> publicaciones.publicar(RETIRADA, pregunta));
            var estado = mysql.getBean(RespuestaAulaService.class).estado(new EstadoRespuestaAulaRequest(actividad, curso, TOKEN)).orElseThrow();
            assertEquals(1, estado.numero());
            assertFalse(estado.correcta());
            assertEquals("El pudú", mysql.getBean(ActividadCompletaRepository.class).findById(actividad).orElseThrow().getTitulo());
            assertTrue(mysql.getBean(CursoCompartidoRepository.class).saveAndFlush(new CursoCompartido("B")).getId() > curso);
        }
    }

    private static ConfigurableApplicationContext iniciar(String url, String usuario, String clave) {
        return new SpringApplicationBuilder(MpazBackendApplication.class).web(WebApplicationType.NONE)
            .run("--spring.datasource.url=" + url, "--spring.datasource.username=" + usuario,
                 "--spring.datasource.password=" + clave, "--spring.profiles.active=aula-local",
                 "--spring.jpa.hibernate.ddl-auto=update", "--spring.main.banner-mode=off");
    }

    private static void compararRegistros(Connection h2, Connection mysql, String tabla) throws Exception {
        assertEquals(filas(h2, tabla), filas(mysql, tabla), "Difieren los registros de " + tabla);
    }

    private static List<List<String>> filas(Connection c, String tabla) throws Exception {
        List<List<String>> resultado = new ArrayList<>();
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM " + tabla)) {
            ResultSetMetaData md = rs.getMetaData();
            List<Integer> orden = new ArrayList<>();
            for (int i = 1; i <= md.getColumnCount(); i++) orden.add(i);
            orden.sort(Comparator.comparing(i -> {
                try { return md.getColumnLabel(i).toLowerCase(Locale.ROOT); }
                catch (SQLException ex) { throw new IllegalStateException(ex); }
            }));
            while (rs.next()) {
                List<String> fila = new ArrayList<>();
                for (int i : orden) {
                    Object valor = rs.getObject(i);
                    fila.add(valor == null ? null : valor instanceof Boolean b ? (b ? "1" : "0") : valor.toString());
                }
                resultado.add(fila);
            }
        }
        resultado.sort(Comparator.comparing(Object::toString));
        return resultado;
    }
}
