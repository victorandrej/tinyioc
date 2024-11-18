package io.github.victorandrej.tinyioc.exception;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
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

    public static CircularReferenceException newInstance(Stack<Class<?>> classStack, Class<?> errorClass) {
        return new CircularReferenceException(createMessage(filterNonReference(classStack, errorClass), errorClass));
    }

    /**
     * retira as classes que nao fazem parte da referencia circular
     *
     * @param classStack
     * @param errorClass
     * @return
     */
    private static Stack<Class<?>> filterNonReference(Stack<Class<?>> classStack, Class<?> errorClass) {
        Stack<Class<?>> stack = new Stack<>();

        Boolean finded = false;

        for (var clazz : classStack) {

            finded = finded ? finded : clazz.equals(errorClass);


            if (finded)
                stack.push(clazz);
        }
        return stack;

    }

    private static String createMessage(Stack<Class<?>> classStack, Class<?> errorClass) {
        List<String> messages = new ArrayList();
        var highestMessage = 0;
        for (var i = 0; i < classStack.size(); i++) {
            var clazz = classStack.get(i);
            String message = "";
            if (i == 0) {
                message += INIT_ARROW;
            } else if (i == classStack.size() - 1) {
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
