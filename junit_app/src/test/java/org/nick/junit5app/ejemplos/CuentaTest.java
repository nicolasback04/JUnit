package org.nick.junit5app.ejemplos;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.api.parallel.Resources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.nick.junit5app.ejemplos.Models.Banco;
import org.nick.junit5app.ejemplos.Models.Cuenta;
import org.nick.junit5app.ejemplos.exceptions.DineroInsuficienteException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

class CuentaTest {
    Cuenta cuenta;
    private TestInfo testInfo;
    private TestReporter testReporter;

    @BeforeEach
    void initMetodoTest(TestInfo testInfo, TestReporter testReporter){
        this.cuenta = new Cuenta("Andres", new BigDecimal("1000.12345"));
        this.testInfo= testInfo;
        this.testReporter= testReporter;
        System.out.println("iniciando el método.");
        testReporter.publishEntry("Ejecutando: " + testInfo.getDisplayName() + " " + testInfo.getTestMethod().orElse(null).getName()
                + " con las etiquetas "+ testInfo.getTags());
    }

    @AfterEach
    void tearDown() {
        System.out.println("Finalizando el método de prueba.");
    }

    @BeforeAll
    static void beforeAll() {
        System.out.println("Inicializando el test");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("Finalizando el test");
    }

    @Tag("cuenta")
    @Nested
    @DisplayName("Probando atributos de la cuenta corriente")
    class CuentaTestNombreSaldo {
        @Test
        @DisplayName("El nombre")
        void testNombreCuenta() {
            testReporter.publishEntry(testInfo.getTags().toString());
            if (testInfo.getTags().contains("cuenta")){
                testReporter.publishEntry("hacer algo con la etiqueta de la cuenta");
            }
            //cuenta.setPersona("Andres");
            String esperado = "Andres";
            String real = cuenta.getPersona();
            assertNotNull(real, "la cuenta no puede ser nula");
            assertEquals(esperado, real, ()-> "el nombre de la cuenta no es el que se esperaba. Se esperaba "+ esperado
                    + " sin embargo fue " + real);
            assertTrue(real.equals("Andres"), ()-> "nombre ceunta eperada debe ser igual a la real");
        }

        @Test
        @DisplayName("el saldo,que no sea null, mayor a cero, valor esperado")
        void testSaldoCuenta() {
            assertNotNull(cuenta.getSaldo());
            assertEquals(1000.12345, cuenta.getSaldo().doubleValue());
            assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @Test
        @DisplayName("testenado referencias que sean iguales con el método equals.")
        void testReferenciaCuenta() {
            cuenta = new Cuenta("John Doe", new BigDecimal("8900.9997"));
            Cuenta cuenta2 = new Cuenta("John Doe", new BigDecimal("8900.9997"));

            //assertNotEquals(cuenta2, cuenta);
            assertEquals(cuenta2, cuenta);
        }
    }

    @Nested
    class CuentaOperacionesTest {
        @Tag("cuenta")
        @Test
        void testDebitoCuneta() {
            cuenta.debito(new BigDecimal(100));
            assertNotNull(cuenta.getSaldo());
            assertEquals(900, cuenta.getSaldo().intValue());
            assertEquals("900.12345", cuenta.getSaldo().toPlainString());
        }

        @Tag("cuenta")
        @Test
        void testCreditoCuneta() {
            cuenta.credito(new BigDecimal(100));
            assertNotNull(cuenta.getSaldo());
            assertEquals(1100, cuenta.getSaldo().intValue());
            assertEquals("1100.12345", cuenta.getSaldo().toPlainString());
        }

        @Tag("cuenta")
        @Tag("banco")
        @Test
        void testTransferirDineroCuentas() {
            Cuenta cuenta1 = new Cuenta("John Doe", new BigDecimal("2500"));
            Cuenta cuenta2 = new Cuenta("Andres", new BigDecimal("1500.9898"));

            Banco banco = new Banco();
            banco.setNombre("Banco del Estado");
            banco.transferir(cuenta2, cuenta1, new BigDecimal(500));
            assertEquals("1000.9898", cuenta2.getSaldo().toPlainString());
            assertEquals("3000", cuenta1.getSaldo().toPlainString());
        }
    }

    @Tag("cuenta")
    @Tag("error")
    @Test
    void testDineroInsuficienteExceptionCuneta() {
        Exception exception = assertThrows(DineroInsuficienteException.class, () -> {
            cuenta.debito(new BigDecimal(1500));
        });
        String real = exception.getMessage();
        String esperado = "Dinero Insuficiente";
        assertEquals(esperado, real);
    }


    @Test
    @Tag("cuenta")
    @Tag("banco")
    @DisplayName("probando relacines entre las cuentas y el banco con assertAll")
    void testRelacionBancoCuentas() {
        Cuenta cuenta1 = new Cuenta("John Doe", new BigDecimal("2500"));
        Cuenta cuenta2 = new Cuenta("Andres", new BigDecimal("1500.9898"));

        Banco banco = new Banco();
        banco.addCuenta(cuenta1);
        banco.addCuenta(cuenta2);

        banco.setNombre("Banco del Estado");
        banco.transferir(cuenta2, cuenta1, new BigDecimal(500));
        assertAll(() -> assertEquals("1000.9898", cuenta2.getSaldo().toPlainString(),
                        ()-> "el valor del saldo de la cuenta2 no es el esperado."),
                () -> assertEquals("3000", cuenta1.getSaldo().toPlainString(),
                        ()-> "el valor del saldo de la cuenta1 no es el esperado"),
                () -> assertEquals(2, banco.getCuentas().size(), ()-> "el banco no tiene las cuentas esperadas"),
                () -> assertEquals("Banco del Estado", cuenta1.getBanco().getNombre()),
                () -> assertEquals("Andres", banco.getCuentas().stream()
                            .filter(c -> c.getPersona().equals("Andres"))
                            .findFirst()
                            .get().getPersona()),
                () -> assertTrue(banco.getCuentas().stream()
                            .anyMatch(c -> c.getPersona().equals("John Doe"))));
    }

    @Nested
    class SistemaOperativoTest {
        @Test
        @EnabledOnOs(OS.WINDOWS)
        void testSoloWindows() {
        }

        @Test
        @EnabledOnOs({OS.LINUX, OS.MAC})
        void testSoloLinuxMac() {
        }

        @Test
        @DisabledOnOs(OS.WINDOWS)
        void testNoWindows() {
        }
    }

    @Nested
    class JavaVersionTest {
        @Test
        @DisabledOnJre(JRE.JAVA_17)
        void soloJdk17() {
        }
    }

    @Nested
    class SistemPropertisTest {
        @Test
        void imprimirSystemProperties() {
            Properties properties = System.getProperties();
            properties.forEach((k, v)-> System.out.println(k + ":" + v));
        }

        @Test
        @EnabledIfSystemProperty(named= "Java.version", matches = "17.0.6")
        void testJavaVersion() {
        }

        @Test
        @DisabledIfSystemProperty(named= "os,arch", matches = ".*32*.")
        void testSolo64() {
        }

        @Test
        @EnabledIfSystemProperty(named= "os,arch", matches = ".*32*.")
        void testNo64() {
        }

        @Test
        @EnabledIfSystemProperty(named= "os,arch", matches = "nick")
        void testUserName() {
        }

        @Test
        @EnabledIfSystemProperty(named = "os,arch", matches = "dev")
        void testDev() {
        }
    }

    @Nested
    class VariableAmbienteTest {
        @Test
        void imprimirVariablesAmbiente() {
            Map<String, String> getenv = System.getenv();
            getenv.forEach((k,v)-> System.out.println(k + " = "+ v));
        }

        @Test
        @EnabledIfEnvironmentVariable(named= "JAVA_HOME", matches = ".*jdk-17.0.6.*")
        void testJavaHome() {
        }

        @Test
        @EnabledIfEnvironmentVariable(named= "NUMBER_OF_PROCESSORS", matches = "12")
        void testProcesadores() {
        }

        @Test
        @EnabledIfEnvironmentVariable(named= "ENVIRONMENT", matches = "dev")
        void testEnv() {
        }

        @Test
        @DisabledIfEnvironmentVariable(named= "ENVIRONMENT", matches = "prod")
        void testEnvProdDisabled() {
        }
    }


    @Test
    @DisplayName("test Saldo Cuenta Dev")
    void testSaldoCuentaDev() {
        boolean esDev = "dev".equals(System.getProperty("ENV"));
        assumeTrue(esDev);
        assertNotNull(cuenta.getSaldo());
        assertEquals(1000.12345, cuenta.getSaldo().doubleValue());
        assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("test Saldo Cuenta Dev 2")
    void testSaldoCuentaDev2() {
        boolean esDev = "dev".equals(System.getProperty("ENV"));
        assumingThat(esDev, ()-> {
            assertNotNull(cuenta.getSaldo());
            assertEquals(1000.12345, cuenta.getSaldo().doubleValue());
            assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        });
    }

    @DisplayName("Probando Debito Cuenta Repetir!")
    @RepeatedTest(value=5, name="{displayName} - Repetición número{curretRepetitions} de {totalRepetitions}")
    void testDebitoCunetaRepetir(RepetitionInfo info) {
        if (info.getCurrentRepetition()==3) {
            System.out.println("Estamos en la repetición " + info.getCurrentRepetition());
        }
        cuenta = new Cuenta("Andres", new BigDecimal("1000.12345"));
        cuenta.debito(new BigDecimal(100));
        assertNotNull(cuenta.getSaldo());
        assertEquals(900, cuenta.getSaldo().intValue());
        assertEquals("900.12345", cuenta.getSaldo().toPlainString());
    }

    @Tag("param")
    @Nested
    class PruebasParametrizadasTest{

        @ParameterizedTest(name = "numero {index} ejecutando con el valor {0} - {argumentsWithNames}")
        @ValueSource(strings = {"100", "200", "300", "500", "700", "1000"})
        void testDebitoCunetaValueSource(String monto) {
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO)>0);
        }

        @ParameterizedTest(name = "número {index} ejecutando con el valor {0} - {argumentsWithNames}")
        @CsvSource({"1,100", "2,200", "3,300", "4,500", "5,700", "6,1000.12345"})
        void testDebitoCunetaCsvSource(String index, String monto) {
            System.out.println(index + " -> "+ monto);
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO)>0);
        }

        @ParameterizedTest(name = "número {index} ejecutando con el valor {0} - {argumentsWithNames}")
        @CsvSource({"200,100", "250,200", "300,300", "510,500", "750,700", "1000.12345,1000.12345"})
        void testDebitoCunetaCsvSource2(String saldo, String monto) {
            System.out.println(saldo + " -> "+ monto);
            cuenta.setSaldo(new BigDecimal(saldo));
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO)>0);
        }

        @ParameterizedTest(name = "número {index} ejecutando con el valor {0} - {argumentsWithNames}")
        @CsvFileSource (resources = "/data.csv")
        void testDebitoCunetaCsvFileSource(String monto) {
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO)>0);
        }

        @ParameterizedTest(name = "número {index} ejecutando con el valor {0} - {argumentsWithNames}")
        @CsvFileSource (resources = "/data2.csv")
        void testDebitoCunetaCsvFileSource2(String monto) {
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO)>0);
        }

    }

    @Tag("param")
    @ParameterizedTest(name = "número {index} ejecutando con el valor {0} - {argumentsWithNames}")
    @MethodSource ("montoList")
    void testDebitoCunetaMethodSource(String monto) {
        cuenta.debito(new BigDecimal(monto));
        assertNotNull(cuenta.getSaldo());
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO)>0);
    }

    static List<String> montoList(){
        return Arrays.asList("100", "200", "300", "500", "700", "1000.12345");
    }

    @Nested
    @Tag("timeout")
    class EjemploTimeOut{
        @Test
        @Timeout(1)
        void pruebaTimeOut() throws InterruptedException{
            TimeUnit.SECONDS.sleep(1);
        }

        @Test
        @Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
        void pruebaTimeOut2() throws InterruptedException{
            TimeUnit.MILLISECONDS.sleep(900);
        }

        @Test
        void testtimeOutAssertions() {
            assertTimeout(Duration.ofSeconds(5), ()->{
                TimeUnit.MILLISECONDS.sleep(4000);
            });
        }
    }

}