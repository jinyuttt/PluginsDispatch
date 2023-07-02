package workplugins;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface PluginAnnotation {
    String name() default "";

    String  input() default "";

    String value() default "";
    String flage() default "";
}
