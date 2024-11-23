package io.github.victorandrej.tinyioc;


import io.github.victorandrej.tinyioc.config.Configuration;
import io.github.victorandrej.tinyioc.exception.*;
import io.github.victorandrej.tinyioc.steriotypes.Bean;
import io.github.victorandrej.tinyioc.steriotypes.BeanFactory;
import io.github.victorandrej.tinyioc.util.ClassUtilTeste;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

public class IOCTest {
    static final String NOME_BEAN = "BEAN_NOME";
    static IOC instance;

    @BeforeAll
    public static void beforeAll() {
        instance =
                IOCBuilder.configure().bean(ClasseTeste.class)
                        .bean(ClasseTeste2.class)
                        .bean(ClasseComInterface.class).build();


    }



    @Test
    public void deve_retornar_a_instancia() {
        assertNotNull(instance.getInstance(ClasseTeste.class), "Retornou a instancia");
    }

    @Test
    public void classes_nao_estaticas_devem_retornar_erro() {

        assertThrows(InvalidClassException.class, () -> {
            IOCBuilder.configure().bean(NonStaticClass.class).build();
        }, "não aceitou classe nao estatica");

    }

    @Test
    public void classes_nao_publicas_devem_retornar_erro() {

        assertThrows(InvalidClassException.class, () -> {
            IOCBuilder.configure().bean(NonPublicClass.class).build();
        }, "não aceitou classe nao publica");

    }

    @Test
    public void deve_retornar_implementacao_de_interface() {
        assertDoesNotThrow(() -> {
            var o = instance.getInstance(BeanInterface.class);
            assertEquals(ClasseComInterface.class, o.getClass());
        });


    }

    @Test
    public void deve_retornar_erro_caso_bean_nao_exista() {
        assertThrows(NoSuchBeanException.class, () -> instance.getInstance(ClasseNaoCadastrada.class));
    }

    @Test
    public void deve_retornar_erro_caso_existam_mais_de_uma_instancia_para_o_bean() {
        IOC ioc = IOCBuilder.configure().bean(ClasseComInterface.class).bean(ClasseDuplicadaComInterface.class).build();
        assertThrows(RuntimeException.class, () -> ioc.getInstance(BeanInterface.class));
    }

    @Test
    public void deve_retornar_o_bean_pelo_nome_especificado() {
        IOC ioc = IOCBuilder.configure().bean(NamedBean.class).build();

        assertDoesNotThrow(() -> {
            assertNotNull(ioc.getInstance(NamedBean.class, NOME_BEAN));
        });
    }

    @Test
    public void deve_retornar_o_bean_pelo_nome_da_classe_com_a_primeira_letra_em_minusculo() {
        IOC ioc = IOCBuilder.configure().bean(NonNamedBean.class).build();
        assertDoesNotThrow(() -> {
            assertNotNull(ioc.getInstance(NonNamedBean.class, "nonNamedBean"));
        });

    }


    @Test
    public void deve_retornar_um_erro_se_nao_houver_um_bean_com_o_nome() {

        IOC ioc = IOCBuilder.configure().bean(NamedBean.class).build();

        assertThrows(RuntimeException.class, () -> {
            assertNotNull(ioc.getInstance(NamedBean.class, "qualquerNome"));
        });

    }

    @Test
    public void deve_retornar_erro_caso_classe_nao_anotada() {
        assertThrows(InvalidClassException.class, () -> {
            IOCBuilder.configure().bean(ClasseNaoAnotada.class).build();
        });
    }


    @Test
    public void deve_retornar_erro_caso_houver_cadastro_duplicado() {

        assertThrows(DuplicatedBeanException.class, () -> {
            IOCBuilder.configure().bean(new NamedBean(), NOME_BEAN).bean(new NamedBean(), NOME_BEAN).build();
        });

    }


    @Test
    public void deve_retornar_uma_execao_de_referencia_circular() {


        assertThrows(CircularReferenceException.class, () -> {
            IOCBuilder.configure().bean(CircularReference.class)
                    .bean(CircularReference2.class)
                    .bean(CircularReference3.class).build();

        });


    }

    @Test
    public void deve_retornar_uma_execao_de_varios_constutores() {

        assertThrows(TooManyConstructorsException.class, () -> {
            IOCBuilder.configure().bean(ClasseComVariosConstrutores.class).build();
        });

    }

    @Test
    public void deve_retornar_uma_execao_de_sem_constutores() {

        assertThrows(NoSuchConstructorException.class, () -> {
            IOCBuilder.configure().bean(ClasseSemConstrutores.class).build();


        });

    }


    @Test
    public void deve_printar_classes_anotadas_como_bean_mas_nao_registradas() {
        var err = System.err;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            System.setErr(new PrintStream(outputStream));


            IOCBuilder.configure()
                    .scanNonUsedBean(IOCTest.class).build();

            String s = new String(outputStream.toByteArray(), Charset.forName("UTF-8"));

            assertTrue(s.contains(ClasseNaoRegistrada.class.getName()));

        } finally {
            System.setErr(err);
        }

    }


    @Test
    public void deve_criar_bean_teste_apartir_da_factory() {
        assertDoesNotThrow(()->{
            var ioc = IOCBuilder.configure().bean(BeanFactoryTest.class).build();
            var instance = ioc.getInstance(ClasseTeste.class);
            assertNotNull(instance);
            assertEquals(ClasseTeste.class,instance.getClass());

        });

    }


    @Test
    public  void deve_retornar_erro_se_nao_encontrar_bean_para_injecao(){
        assertThrows(NoSuchBeanException.class,()->{
            IOCBuilder.configure().bean(ClasseRequerBeanInexistente.class).build();
        });


    }

    @Test
    public void deve_criar_o_bean_com_referencia_requerida(){
        assertDoesNotThrow(()-> IOCBuilder.configure().bean(ClasseTeste.class).bean(ClasseComDependencia.class).build());
    }

    @Bean
    public  static class ClasseComDependencia{
       public   ClasseComDependencia(ClasseTeste t){}
    }


    @Bean
    public  static class ClasseRequerBeanInexistente{
            public ClasseRequerBeanInexistente(ClasseNaoRegistrada cl){}
    }

        @Bean
    public static class  BeanFactoryTest implements BeanFactory {

        @Override
        public void create(Configuration configuration) {
            configuration.bean(new ClasseTeste());
        }
    }


    @Bean
    public static class ClasseNaoRegistrada {
    }

    @Bean
    public static class ClasseSemConstrutores {
        private ClasseSemConstrutores() {
        }
    }

    @Bean
    public static class ClasseComVariosConstrutores {
        public ClasseComVariosConstrutores() {
        }

        public ClasseComVariosConstrutores(ClasseTeste t1) {
        }
    }

    @Bean
    public static class CircularReference {
        public CircularReference(CircularReference2 c2) {
        }
    }

    @Bean
    public static class CircularReference2 {
        public CircularReference2(CircularReference3 c3) {
        }
    }

    @Bean
    public static class CircularReference3 {
        public CircularReference3(CircularReference c1) {
        }
    }


    @Bean(beanName = NOME_BEAN)
    public static class NamedBean {

    }

    @Bean
    public static class NonNamedBean {
    }

    public static interface BeanInterface {

    }


    public static class ClasseNaoCadastrada {
    }

    public static class ClasseNaoAnotada {
    }

    ;

    @Bean
    public static class ClasseComInterface implements BeanInterface {

    }

    @Bean
    public static class ClasseDuplicadaComInterface implements BeanInterface {

    }

    @Bean
    static class NonPublicClass {

    }

    @Bean
    class NonStaticClass {

    }

    @Bean()
    public static class ClasseTeste {

    }

    @Bean()
    public static class ClasseTeste2 {

    }
}
