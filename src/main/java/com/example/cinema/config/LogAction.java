package com.example.cinema.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogAction {
    String action(); // Ví dụ: "CREATE", "UPDATE", "DELETE"
    String target(); // Ví dụ: "MOVIE", "ROOM", "SHOWTIME"
}
