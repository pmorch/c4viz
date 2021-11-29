package com.morch.c4viz;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {
    private static class Foobar {
        public String foo="FOO";
        public int bar = 234;
    }

    @GetMapping("/")
    public Foobar index() {
        return new Foobar();
    }
}
