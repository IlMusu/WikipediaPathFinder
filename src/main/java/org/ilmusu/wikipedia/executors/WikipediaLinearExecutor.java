package org.ilmusu.wikipedia.executors;

public class WikipediaLinearExecutor extends _WikipediaExecutor
{
    @Override
    public void executeStep(int step, Runnable defaultExecutor)
    {
        defaultExecutor.run();
    }
}
