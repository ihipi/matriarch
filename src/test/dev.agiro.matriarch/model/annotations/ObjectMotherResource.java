package dev.agiro.matriarch.model.annotations;


import dev.agiro.matriarch.application.MotherFactoryResourceProviders;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ArgumentsSource(MotherFactoryResourceProviders.class)
public @interface ObjectMotherResource {
    RandomArg[] args() ;
}
