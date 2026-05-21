package io.github.mykhailokulakov.genericspringservice.support.fixtures;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

@Target(TYPE)
@Retention(RUNTIME)
@ExtendWith(SeededExamplesExtension.class)
public @interface WithSeededExamples {

  int count() default 10;

  String[] tags() default {};

  boolean truncate() default true;
}
