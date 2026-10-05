# Sistema de Gestión de Biblioteca Escolar

Evaluación Final Transversal — Desarrollo Orientado a Objetos II (DUOC UC)
Autor: Javier Gazmuri

Aplicación de escritorio en Java (Swing + JDBC + MySQL) para controlar el registro, préstamo y devolución de libros de una biblioteca escolar, con acceso diferenciado para bibliotecarios y estudiantes.

## Tecnologías

- Java 17 o superior
- Swing (interfaz gráfica con JFrame)
- JDBC con MySQL Connector/J 9.1.0
- MySQL 8
- Maven

## Arquitectura

El proyecto aplica los patrones **MVC**, **DAO** y **Singleton**.

| Paquete | Responsabilidad |
|---|---|
| `modelo` | Clases del dominio: `Persona` (abstracta), `Usuario`, `Estudiante`, `Libro`, `Categoria`, `Prestamo`, `Rol` (enum), `Prestable` (interfaz), `LibroPrestado` (record) |
| `vista` | Ventanas Swing: login, menú principal, libros, categorías, estudiantes, préstamos y reportes |
| `controlador` | Validaciones y coordinación entre vistas y DAOs; hilo de préstamos |
| `dao` | Acceso a datos con JDBC: un DAO por entidad |
| `util` | `DatabaseConnection` (Singleton) y `ValidacionException` |
| `main` | Clase `Main`, punto de entrada |

**Programación orientada a objetos:** herencia (`Usuario` y `Estudiante` extienden de la clase abstracta `Persona`), polimorfismo (`getDescripcion()` se comporta distinto en cada subclase), interfaz (`Libro` implementa `Prestable`).

**Concurrencia y consistencia:** el registro de un préstamo se ejecuta en un hilo independiente (`HiloPrestamo`) para no congelar la interfaz. La operación en `PrestamoDAO` está sincronizada con `synchronized` sobre un candado estático, usa una transacción JDBC y descuenta el stock con `UPDATE ... WHERE stock > 0`, lo que impide que el stock quede negativo ante préstamos simultáneos.

## Funcionalidades

- **Autenticación** por correo o RUT, con roles de bibliotecario y estudiante.
- **Gestión de libros, categorías y estudiantes** (CRUD completo, solo bibliotecario). Al registrar un estudiante se crea también su usuario de acceso.
- **Préstamos y devoluciones:** vencimiento calculado automáticamente (7 días) y detección de atrasos.
- **Reportes:** libros más prestados, historial por estudiante y libros actualmente en préstamo.
- **Rol estudiante:** puede consultar libros y gestionar sus propios préstamos y devoluciones.

## Instalación y ejecución

1. Tener MySQL en ejecución.
2. Crear la base de datos ejecutando, en este orden, los scripts de la carpeta `sql/`:
```
   mysql -u root -p < sql/PRY2203_EFT_S9_Script_crea_tablas_biblioteca.sql
   mysql -u root -p < sql/PRY2203_EFT_S9_Script_poblado_tablas_biblioteca.sql
```
3. Copiar `config.example.properties` como `config.properties` y completar el usuario y la contraseña de su MySQL.
4. Ejecutar la aplicación desde la carpeta donde esté `config.properties`:
```
   java -jar BibliotecaEscolar.jar
```
Para generar el ejecutable desde el código fuente: `mvn clean package` (queda en `target/BibliotecaEscolar.jar`).

## Usuarios de prueba

Todos usan la contraseña `clave123`.

| Rol | Usuario |
|---|---|
| Bibliotecario | `antonia@correo.cl` |
| Estudiante | `carlos@correo.cl` o RUT `98765432-1` |
| Estudiante | `maria@correo.cl` |

## Estructura del repositorio

```
├── sql/                 Scripts de creación y poblado de la base de datos
├── src/main/java/       Código fuente (modelo, vista, controlador, dao, util, main)
├── config.example.properties
├── pom.xml
└── README.md
```