package fr.piwithy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.*;

public class PiMonteCarlo {

    private static Logger LOGGER = LogManager.getLogger(PiMonteCarlo.class);


    public static void main(String[] args){
        int niter = 2147483647;
        int nThread=8;
        int count=0;
        double pi;
        Instant st = Instant.now();
        ExecutorService executor = Executors.newFixedThreadPool(nThread);
        ArrayList<Callable<Integer>> callables = new ArrayList<>();
        for(int i=0; i<nThread; i++){
            callables.add(new PiThread(niter/nThread, i+1));
        }
        try {
            count = executor.invokeAll(callables).stream().map(integerFuture -> {
                try {
                    return integerFuture.get();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }).mapToInt(Integer::intValue).sum();
            pi = (double) count / niter * 4;
            Instant end = Instant.now();
            long timeMs = Duration.between(st, end).toMillis();
            LOGGER.info("estimate of pi: " + pi + " | Duration " + timeMs + "ms");
        }catch (Exception e) {
            e.printStackTrace();
        }
        try{
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }catch (InterruptedException e){
            e.printStackTrace();
        }finally {
            if(!executor.isTerminated()){
                LOGGER.fatal("Canceling non-finished task!");
            }
            executor.shutdownNow();
        }
    }
}
