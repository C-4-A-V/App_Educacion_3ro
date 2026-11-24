package com.example.mpazpro.backend;

import java.sql.DriverManager;
import java.util.Locale;

/** Herramienta explícita y local: nunca se activa al iniciar el servidor web. */
public final class MigracionCatalogoCli {
    private MigracionCatalogoCli() {}

    public static void main(String[] args) throws Exception {
        String origen = requerida("MPAZ_MIGRACION_H2_URL");
        String mysql = requerida("MPAZ_MYSQL_URL");
        String usuario = requerida("MPAZ_MYSQL_USUARIO");
        String clave = requerida("MPAZ_MYSQL_CLAVE");
        String normalizada = origen.toUpperCase(Locale.ROOT);
        if (!normalizada.startsWith("JDBC:H2:FILE:") ||
            !normalizada.contains(";ACCESS_MODE_DATA=R") || !normalizada.contains(";IFEXISTS=TRUE"))
            throw new IllegalArgumentException("El origen debe ser una copia H2 existente abierta en modo de solo lectura");
        if (!mysql.startsWith("jdbc:mysql://")) throw new IllegalArgumentException("Destino MySQL inválido");
        try (var lectura = DriverManager.getConnection(origen, "sa", "");
             var escritura = DriverManager.getConnection(mysql, usuario, clave)) {
            var resultado = new MigradorCatalogo().copiar(lectura, escritura);
            resultado.forEach((tabla, total) -> System.out.println(tabla + ": " + total));
            System.out.println("Copia verificada. La base de origen no se modificó.");
        }
    }

    private static String requerida(String nombre) {
        String valor = System.getenv(nombre);
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException("Falta variable " + nombre);
        return valor;
    }
}
