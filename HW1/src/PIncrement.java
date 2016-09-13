/**
 * Created by Ian on 9/11/2016.
 */

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PIncrement{
    public static ExecutorService threadPool;
    public static int m = 1200000;

    public static int parallelIncrement(int c, int numThreads){
        long begin, end;

        begin = System.nanoTime();
        new PetersonIncrement(c, numThreads);
        end = System.nanoTime();

        System.out.println("Execution Time = " + (end - begin) / 1000000 + " ms\n");

        return c + m;
    }

    public static int AtomicIncrement(int c, int numThreads){
        long begin, end;

        begin = System.nanoTime();
        new AtomicIncrement(c, numThreads);
        end = System.nanoTime();

        System.out.println("Execution Time = " + (end - begin) / 1000000 + " ms\n");

        return c + m;
    }

    public static int SynchronizedIncrement(int c, int numThreads){
        long begin, end;

        begin = System.nanoTime();
        new SyncIncrement(c, numThreads);
        end = System.nanoTime();

        System.out.println("Execution Time = " + (end - begin) / 1000000 + " ms\n");

        return c + m;
    }

    public static int ReentrantIncrement(int c, int numThreads){
        long begin, end;

        begin = System.nanoTime();
        new ReentrantIncrement(c, numThreads);
        end = System.nanoTime();

        System.out.println("Execution Time = " + (end - begin) / 1000000 + " ms\n");

        return c + m;
    }


/* Peterson's Tournament Algorithm */
    public static class PetersonIncrement
    {
        static AtomicIntegerArray flag; //= new AtomicIntegerArray(15); // 1 true, 0 false
        static AtomicIntegerArray node; //= new AtomicIntegerArray(15);
        static AtomicIntegerArray victim; //= new AtomicIntegerArray(15);
        static volatile int c;
        static int ArrayLength = 0;

        public PetersonIncrement(int c, int numThreads) {
            // initialize variables
            ArrayLength = numThreads*2-1;
            flag = new AtomicIntegerArray(ArrayLength);
            node = new AtomicIntegerArray(ArrayLength);
            victim = new AtomicIntegerArray(ArrayLength);
            threadPool = Executors.newFixedThreadPool(numThreads);
            this.c = c;

            // initialize array
            flag.set(0, 0);
            node.set(0, 0);
            victim.set(0, 0);
            for (int i = 1; i < ArrayLength; i++) {
                flag.set(i, 0);
                node.set(i, 0);
                victim.set(i, 0);
            }

            // create threads
            for (int i = 0; i < numThreads; i++) {
                threadPool.submit(new PetersonThread(numThreads - 1 + i, numThreads));
            }
            threadPool.shutdown();

            // wait till all threads are done incrementing
            while(!threadPool.isTerminated()); // wait till threads have completed running
            while (this.c < m ) ; // check to see if threads finished correctly
            System.out.println("Peterson's Tournament Algorithm Complete");
        }

        public static class PetersonThread implements Runnable {
            int myThreadNum; // startNode
            int currentNode;
            int pairNodeOp;
            int pairNode;
            int nextNode;
            int totalThreads;

            public PetersonThread(int myNumber, int totalThreads) {
                this.totalThreads = totalThreads;

                // Update information
                myThreadNum = myNumber;
                currentNode = myThreadNum;
                if (currentNode % 2 == 1) {
                    pairNodeOp = 1;
                } else {
                    pairNodeOp = -1;
                }
                pairNode = currentNode + pairNodeOp;
                nextNode = (currentNode - 1) / 2;
                node.set(myNumber,myNumber);
            }

            public void run() {
                for(int j = 0; j < (m / totalThreads); j++) {
                    flag.set(myThreadNum, 1);
                    while (currentNode != 0) {
                        victim.set(nextNode, myThreadNum);
                        while (flag.get(node.get(pairNode)) == 1 && victim.get(nextNode) == myThreadNum) ;
                        while (node.get(nextNode) != 0) ; // there is no one
                        node.set(nextNode, myThreadNum);
                        node.set(currentNode, 0);
                        // Update information
                        currentNode = nextNode;
                        if (currentNode % 2 == 1) {
                            pairNodeOp = 1;
                        } else {
                            pairNodeOp = -1;
                        }
                        pairNode = currentNode + pairNodeOp;
                        nextNode = (currentNode - 1) / 2;
                    }
                    // increment c
                    if (c < m) c++;
                    // update to restart from the bottom
                    flag.set(myThreadNum, 0);
                    node.set(0, 0);
                    node.set(myThreadNum, myThreadNum);
                    currentNode = myThreadNum;
                    if (currentNode % 2 == 1) {
                        pairNodeOp = 1;
                    } else {
                        pairNodeOp = -1;
                    }
                    pairNode = currentNode + pairNodeOp;
                    nextNode = (currentNode - 1) / 2;
                }
                System.out.println("Thread " + (myThreadNum - totalThreads + 2) + " finished @ c = " + c);
            }
        }
    }

/* Atomic Increment */
    public static class AtomicIncrement {
        static volatile AtomicInteger atomicInt;

        public AtomicIncrement(int c, int numThreads)
        {
            threadPool = Executors.newFixedThreadPool(numThreads);

            atomicInt = new AtomicInteger(c);

            for(int i = 0; i < numThreads; i++) {
                threadPool.submit(new AtomicThread(m / numThreads, i + 1));
            }
            threadPool.shutdown();

            // wait till threads have finished incrementing
            while(!threadPool.isTerminated()); // wait till threads have completed
            while (atomicInt.get() < m ) ; // check to see if threads finished correctly
            System.out.println("Java's Atomic Integer Complete");
        }

        public static class AtomicThread implements Runnable {

            int incrementNum;
            int index;

            public AtomicThread(int incrementNum, int index) {
                this.incrementNum = incrementNum;
                this.index = index;
            }

            @Override
            public void run() {
                int i = 0;
                while( i < incrementNum) {
                    int test = atomicInt.get();
                    if(atomicInt.compareAndSet(test, test + 1)) {
                        i++;
                    }
                }
                System.out.println("Thread " + index + " finished @ c = " + atomicInt.get());
            }
        }
    }

/* Java's Synchronized Construct */
    public static class SyncIncrement {

        static volatile int c;
        static int numThreads;

        public SyncIncrement(int c, int numThreads) {
            this.c = c;
            this.numThreads = numThreads;
            threadPool = Executors.newFixedThreadPool(numThreads);

            for(int i = 0; i < numThreads; i++){
                threadPool.submit(new SyncThread(i +1));
            }
            threadPool.shutdown();
            while(!threadPool.isTerminated()){}
            while(this.c < m){}
            System.out.println("Synchronized Construct Complete");
        }

        public static synchronized void increment(){
            c++;
        }

        public static class SyncThread implements Runnable{

            int numThreads;
            public SyncThread(int numThreads){
                this.numThreads = numThreads;
            }

            @Override
            public void run() {
                for(int i = 0; i < m/numThreads; i++){
                    increment();
                }
                System.out.println("Thread " + numThreads + " finished @ c = " + c);
            }
        }
    }

/* Reentrant Lock */
    public static class ReentrantIncrement {

        static volatile int c;
        static int numThreads;
        static Lock lock = new ReentrantLock();

        public ReentrantIncrement(int c, int numThreads) {
            this.c = c;
            this.numThreads = numThreads;
            threadPool = Executors.newFixedThreadPool(numThreads);

            for(int i = 0; i < numThreads; i++){
                threadPool.submit(new ReentrantThread(i + 1));
            }
            threadPool.shutdown();
            while(!threadPool.isTerminated()){}
            while(this.c < m){}
            System.out.println("Reentrant Lock Complete");
        }

        public static void increment(){
            lock.lock();
            try{
                c++;
            } finally{
                lock.unlock();
            }
        }

        public static class ReentrantThread implements Runnable{

            int numThreads;

            public ReentrantThread(int numThreads) {
                this.numThreads = numThreads;
            }

            @Override
            public void run() {
                for(int i = 0; i < m/numThreads; i++){
                    increment();
                }
                System.out.println("Thread " + numThreads + " finished @ c = " + c);
            }
        }
    }

    public static void main(String[] args)
    {
        for(int i = 1; i <= 8; i*=2){
            int result = PIncrement.parallelIncrement(0, i);

            result = PIncrement.AtomicIncrement(0, i);

            result = PIncrement.SynchronizedIncrement(0, i);

            result = PIncrement.ReentrantIncrement(0, i);
        }
    }
}