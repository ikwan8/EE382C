/**
 * Created by Ian on 9/11/2016.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Frequency{

    public static ExecutorService threadPool;


    public static int parallelFreq(int x, int[] A, int numThreads) {
        // your implementation goes here.

        threadPool = Executors.newFixedThreadPool(numThreads);

        //convert int[] to ArrayList<Integer>
        ArrayList<Integer> masterList = new ArrayList<Integer>();
        for(int num : A){
            masterList.add(num);
        }

        //split list into fragments
        int arrFragSize = masterList.size()/numThreads;

        ArrayList<ArrayList<Integer>> subLists = new ArrayList<ArrayList<Integer>>();
        for(int i = 0; i < masterList.size(); i+=arrFragSize){
            int begin = i;
            int end  = i+arrFragSize;
            List<Integer> next = new ArrayList<Integer>();
            if (end > masterList.size() || begin==masterList.size()){
                next = masterList.subList(begin, masterList.size());
                subLists.add(new ArrayList<Integer>(next));
                break;
            }
            next = masterList.subList(begin, end);

            subLists.add(new ArrayList<Integer>(next));
        }
        if(subLists.size() > numThreads) {
            int diff = subLists.size() - numThreads;
            for(int i = 0; i < diff; i++) {
                subLists.get(subLists.size() - 2).addAll(subLists.get(subLists.size() - 1));
                subLists.remove(subLists.size() - 1);
            }
        }

        System.out.println(subLists);

        //parallel count
        ArrayList<Future<Integer>> outputList = new ArrayList<>();
        for(ArrayList<Integer> inputList : subLists) {
            Future<Integer> subListOutput = threadPool.submit(new PFreq(x, inputList));
            outputList.add(subListOutput);
        }

        int result = 0;
        for(Future<Integer> output : outputList){
            try {
                result += output.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return result;
    }





    public static class PFreq implements Callable{

        int x;
        ArrayList<Integer> list;

        public PFreq(int x, ArrayList<Integer> list) {
            this.x = x;
            this.list = list;
        }

        @Override
        public Integer call() throws Exception {
            Integer count = 0;
            for(int num : list){
                if(x == num){
                    count++;
                }
            }
            return count;
        }
    }

    public static void main(String[] args){
        int[] A = new int[] {1, 1, 3, 2, 5, 1, 4};
        System.out.println(parallelFreq(1, A, 2));
    }
}