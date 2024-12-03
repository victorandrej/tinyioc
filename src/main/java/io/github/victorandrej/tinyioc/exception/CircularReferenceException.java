package io.github.victorandrej.tinyioc.exception;

import io.github.victorandrej.tinyioc.config.BeanInfo;

import java.util.*;
import java.util.stream.Stream;

/**
 * Ocorre quando ha uma referencia circular nos beans
 */
public class CircularReferenceException extends RuntimeException {
    private static final String INIT_ARROW = "->";
    private static final String FINAL_ARROW = "<-";
    private static final String PIPE = "|";
    private static final char SPACE = ' ';
    private static final char TRACE = '-';

    public static CircularReferenceException newInstance(Set<Class<?>> classSet, Class clazz) {
        return new CircularReferenceException(createMessage(filterNonReference(classSet, clazz)));
    }

    /**
     * retira as classes que nao fazem parte da referencia circular
     *
     * @param classQueue
     * @param clazz
     * @return
     */
    private static LinkedList<Class<?>>  filterNonReference(Set<Class<?>>  classSet, Class<?> clazz) {
        LinkedList<Class<?>> queue = new LinkedList<>();

        Boolean finded = false;

        for (var currClazz : classSet) {

            finded = finded ? finded : Stream.of(currClazz.getConstructors()).anyMatch(c-> Stream.of(c.getParameters()).anyMatch(p->p.getType().equals(clazz)))   ;


            if (finded)
                queue.add(currClazz);
        }
        return queue;

    }

    private static String createMessage(LinkedList<Class<?>>  queueClass) {
        List<String> messages = new ArrayList();
        var highestMessage = 0;
        for (var i = 0; i < queueClass.size(); i++) {
            var clazz = queueClass.get(i);
            String message = "";
            if (i == 0) {
                message += INIT_ARROW;
            } else if (i == queueClass.size() - 1) {
                message += insertChar(TRACE, INIT_ARROW.length());
            } else {
                message += PIPE + insertChar(SPACE, INIT_ARROW.length() - 1);
            }
            message += clazz.getName();

            highestMessage = highestMessage > message.length() ? highestMessage : message.length();
            messages.add(message);
        }


        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator());
        for (var i = 0; i < messages.size(); i++) {
            String message = messages.get(i);

            if (i == 0) {
                message += insertChar(TRACE, Math.abs(message.length() - highestMessage));
                message += insertChar(TRACE, FINAL_ARROW.length());
            } else if (i != messages.size()-1) {
                message += insertChar(SPACE, Math.abs(message.length() - highestMessage));
                message += insertChar(SPACE, FINAL_ARROW.length() - 1) + PIPE;
            }
            sb.append(message);
            if (i == messages.size() - 1) {
                sb.append(FINAL_ARROW);
                sb.append(insertChar(TRACE, Math.abs(message.length() - highestMessage)));
            }
            sb.append(System.lineSeparator());
        }


        return sb.toString();
    }

    private static String insertChar(char c, int size) {
        String s = "";

        for (var i = 0; i < size; i++)
            s += c;
        return s;
    }

    public CircularReferenceException(String message) {
        super(message);

    }


}
