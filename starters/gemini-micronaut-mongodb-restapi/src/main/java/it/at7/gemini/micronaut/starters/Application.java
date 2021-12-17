package it.at7.gemini.micronaut.starters;

import io.micronaut.runtime.Micronaut;

public class Application {

    public static void main(String[] args) {

        Micronaut.build(args)
                .banner(false)
                .start();
    }
}