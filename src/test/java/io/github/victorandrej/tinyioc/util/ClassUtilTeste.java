package io.github.victorandrej.tinyioc.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClassUtilTeste {
    @Test
    public void deve_retornar_a_classe_do_arquivo() {
        assertDoesNotThrow(() -> {
                    var classes = ClassUtil.findAllClasses(ClassUtilTeste.class);
                    assertEquals(2, classes.size());
                }
        );

    }

    public static class ClasseTest {
    }
}
