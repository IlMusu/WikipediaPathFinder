package org.ilmusu.wikipedia.executors;

import java.util.ArrayList;
import java.util.List;

public class WikipediaThreadedExecutor extends _WikipediaExecutor
{
    protected final List<Integer> threadsForStep;

    public WikipediaThreadedExecutor(List<Integer> threadsForStep)
    {
        this.threadsForStep = threadsForStep;
    }

    @Override
    public void executeStep(int step, Runnable defaultExecutor)
    {
        int numberOfThreads = this.threadsForStep.get(Math.min(step, this.threadsForStep.size()-1));
        List<Thread> threads = new ArrayList<>();
        for(int i=0; i<numberOfThreads; ++i)
        {
            Thread thread = new Thread(defaultExecutor);
            thread.start();
            threads.add(thread);
        }
        for(Thread thread : threads)
        {
            try{ thread.join(); }
            catch (InterruptedException exception)
            {
                System.out.println("THREAD HAS BEEN INTERRUPTED");
                exception.printStackTrace();
            }
        }
    }
}
