package com.mcmoddev.bot.logging;

import java.io.PrintStream;

import com.mcmoddev.bot.MMDBot;

public class PrintStreamTraced extends PrintStream {
    
    public PrintStreamTraced (PrintStream parent) {
        
        super(parent);
    }
    
    @Override
    public void println (Object o) {
        
        MMDBot.LOG.info("{}{}", getPrefix(), o);
    }
    
    @Override
    public void println (String s) {
        
        MMDBot.LOG.info("{}{}", getPrefix(), s);
    }
    
    private String getPrefix () {
        
        StackTraceElement[] elems = Thread.currentThread().getStackTrace();
        StackTraceElement elem = elems[3];
        return "[" + elem.getClassName() + ":" + elem.getMethodName() + ":" + elem.getLineNumber() + "]: ";
    }
    
}