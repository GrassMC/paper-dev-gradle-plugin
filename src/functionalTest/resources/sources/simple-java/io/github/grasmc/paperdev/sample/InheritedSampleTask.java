package io.github.grassmc.paperdev.sample;

public class InheritedSampleTask extends SampleTask {
    @Override
    protected void call() {
        System.out.println("Hello!");
    }
}
