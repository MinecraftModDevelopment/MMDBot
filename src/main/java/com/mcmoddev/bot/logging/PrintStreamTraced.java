package com.mcmoddev.bot.logging;

import java.io.PrintStream;

import com.mcmoddev.bot.MMDBot;

public class PrintStreamTraced extends PrintStream {

    public PrintStreamTraced (PrintStream parent) {

        super(parent);
    }

    @Override
    public void println (Object o) {

        MMDBot.LOG.info("{}{}", this.getPrefix(), o);
    }

    @Override
    public void println (String s) {

        MMDBot.LOG.info("{}{}", this.getPrefix(), s);
    }

    private String getPrefix () {

        final StackTraceElement[] elems = Thread.currentThread().getStackTrace();
        final StackTraceElement elem = elems[3];
        return "[" + elem.getClassName() + ":" + elem.getMethodName() + ":" + elem.getLineNumber() + "]: ";
    }

}