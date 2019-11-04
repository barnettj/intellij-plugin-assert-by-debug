package dev.fervento.assertbydebug.sample.entity;

import org.junit.jupiter.api.Test;

public class LoopTest {

    public static class OOref {
        OOref next;

        public OOref getNext() {
            return next;
        }

        public void setNext(OOref next) {
            this.next = next;
        }
    }

    @Test
    public void testLoop() {
        OOref x = new OOref();
        x.setNext(x);
        System.out.println("hello");
    }

}
