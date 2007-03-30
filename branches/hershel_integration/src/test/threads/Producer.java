package test.threads;

import java.util.ArrayList;

public class Producer extends Thread
{
    public static void main(String[] argv)
    {
        Producer p = new Producer();
        p.start();
    }
    
    public class Task
    {
        private int id, length;

        public Task(int id, int length)
        {
            this.id = id;
            this.length = length;
        }

        public void doWork() throws InterruptedException
        {
           System.out.printf("Task %d started\n", id);
           Thread.sleep(length * 1000);
           System.out.printf("Task %d finished\n", id);
        }

    }

    public class Worker extends Thread
    {
        private Task currentTask;
        private Consumer consumer;

        public Worker(Consumer consumer)
        {
           this.consumer = consumer;
        }

        public synchronized void run()
        {
            while (true)
            {
                try
                {
                    while (currentTask == null)
                    {                  
                            wait();                   
                    }                    
                    currentTask.doWork();
                    currentTask = null;
                    consumer.workComplete(this);
                }
                catch (InterruptedException e)
                {                   
                    e.printStackTrace();
                }
            }
        }

        public synchronized void handle(Task task)
        {
            currentTask = task;
            notify();
        }

    }

    public class Consumer
    {
        private ArrayList<Worker> threads = new ArrayList<Worker>();

        private ArrayList<Task> items = new ArrayList<Task>();

        public Consumer()
        {
            for (int i = 0; i < 2; i++)
            {
                Worker worker = new Worker(this);
                worker.start();
                threads.add(worker);
            }
        }

        public synchronized void workComplete(Worker worker)
        {
            threads.add(worker);
            if(!items.isEmpty())
            {
                Task t = items.remove(0);
                schedule(t);
            }
        }

        public synchronized void consume(int id, int length)
        {            
            Task task = new Task(id, length);
            if (!threads.isEmpty())
            {
                schedule(task);
            } 
            else
            {
                System.out.printf("Delayed task %d\n", id);
                items.add(task);
            }
        }

        private void schedule(Task task)
        {
            Worker w = threads.get(0);
            w.handle(task);
            threads.remove(w);
        }

    }

    private Consumer consumer = new Consumer();

    public void run()
    {
        int count = 1;
        while (true)
        {
            try
            {
                Thread.sleep((int) (Math.random() * 5 + 1) * 1000);
                consumer.consume(count++, (int) (Math.random() * 10 + 1));
            } catch (InterruptedException e)
            {               
                e.printStackTrace();
            }
        }
    }
}
