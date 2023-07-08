package workplugins;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface PluginAnnotation {

    /**
     * 组件名称
     * @return
     */
    String name() default "";

    /**
     * 组件输入参数对象名称
     * @return
     */
    String  input() default "";

    /**
     * 组件输出参数名称
     * @return
     */
    String  output() default "";


    String value() default "";

    /**
     * 组件唯一标识
     * @return
     */
    String flage() default "";
}
