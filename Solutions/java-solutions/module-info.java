module JavaAdvanced {
    requires info.kgeorgiy.java.advanced.implementor;
    requires info.kgeorgiy.java.advanced.implementor.tools;
    requires info.kgeorgiy.java.advanced.lambda;
    requires info.kgeorgiy.java.advanced.student;
    requires java.compiler;
    requires info.kgeorgiy.java.advanced.iterative;
    requires info.kgeorgiy.java.advanced.mapper;
    requires info.kgeorgiy.java.advanced.crawler;
    requires info.kgeorgiy.java.advanced.hello;
    requires java.rmi;
    requires jdk.httpserver;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;

    requires org.junit.platform.commons;
    requires org.junit.platform.engine;
    requires org.junit.platform.launcher;

    exports info.kgeorgiy.ja.morozov.bank to java.rmi, org.junit.platform.commons;
    exports info.kgeorgiy.ja.morozov.bank.account to java.rmi;
    exports info.kgeorgiy.ja.morozov.bank.person to java.rmi;
    exports info.kgeorgiy.ja.morozov.bank.bank to java.rmi, org.junit.platform.commons;
}