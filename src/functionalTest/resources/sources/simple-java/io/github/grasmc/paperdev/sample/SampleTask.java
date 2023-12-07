package io.github.grassmc.paperdev.sample;

public class SampleTask implements Runnable {
    protected void call() {
        System.out.println("Hello, Paper!");
    }

    @Override
    public void run() {
        call();
    }
}
