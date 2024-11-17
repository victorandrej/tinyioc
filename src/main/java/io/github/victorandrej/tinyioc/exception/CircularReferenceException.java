package io.github.victorandrej.tinyioc.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CircularReferenceException extends RuntimeException {
    private static final String INIT_ARROW = "->";
    private static final String FINAL_ARROW = "<-";
    private static final String PIPE = "|";
    private static final char SPACE = ' ';
    private  static  final  char TRACE ='-';

    public static CircularReferenceException newInstance(Stack<Class<?>> classStack, Class<?> errorClass) {
        return new CircularReferenceException(createMessage(classStack, errorClass));
    }

    private static String createMessage(Stack<Class<?>> classStack, Class<?> errorClass) {
        List<String> messages = new ArrayList();
        var highestMessage = 0;
        for (var i = 0; i < classStack.size(); i++) {
            var clazz = classStack.get(i);
            String message = "";
            if (i == 0) {
                message += INIT_ARROW;
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
            message += insertChar(SPACE, Math.abs(message.length() - highestMessage));
            message += insertChar(SPACE, FINAL_ARROW.length() - 1) + PIPE;
            sb.append(message);
            sb.append(System.lineSeparator());
        }

        String message =   insertChar(TRACE,INIT_ARROW.length()) +  errorClass.getName() +FINAL_ARROW;
        sb.append(message);
        sb.append(insertChar(TRACE, Math.abs(message.length() - highestMessage) + FINAL_ARROW.length()));


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
