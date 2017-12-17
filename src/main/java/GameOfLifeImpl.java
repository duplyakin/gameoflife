import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Vasyukevich Andrey
 * @since 12.12.2017
 */
public class GameOfLifeImpl implements GameOfLife {
    int N;
    int M;
    int[][] currentField;


    void parseData(String inputFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String header = reader.readLine();
            String[] headerdata = header.split("\\s");
            if (headerdata.length != 2) {
                throw new RuntimeException("bad header");
            }
            N = Integer.parseInt(headerdata[0]);
            M = Integer.parseInt(headerdata[1]);
            currentField = new int[N][N];
            for (int i = 0; i < N; i++) {
                String row = reader.readLine();
                for (int j = 0; j < N; j++) {
                    currentField[i][j] = row.charAt(j) - 48;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    List<String> reportResult(int[][] data) {
        List<String> result = new ArrayList<>(data.length);
        for (int i = 0; i < data.length; i++) {
            StringBuilder sb = new StringBuilder(data[i].length);
            for (int j = 0; j < data[i].length; j++) {
                sb.append(data[i][j]);
            }
            result.add(sb.toString());
        }
        return result;
    }

    int calcNeighboursSum(int i, int j, int[][] current) {
        int top = (i > 0) ? (i - 1) : (N - 1);
        int bottom;
        int left, right;

        if (i < N - 1) {
            bottom = i + 1;
        } else {
            bottom = 0;
        }
        if (j > 0) {
            left = j - 1;
        } else {
            left = N - 1;
        }
        if (j < N - 1) {
            right = j + 1;
        } else {
            right = 0;
        }

        return current[top][left] +
                current[top][j] +
                current[top][right] +
                current[i][left] +
                current[i][right] +
                current[bottom][left] +
                current[bottom][j] +
                current[bottom][right];
    }

    static int doLife(int curval, int neighbours) {
        if (curval == 0 && neighbours == 3) {
            return 1;
        }
        if (curval == 1 && (neighbours > 3 || neighbours < 2)) {
            return 0;
        }
        return curval;

    }

    private CountDownLatch latch = new CountDownLatch(4);
    private ExecutorService es = Executors.newFixedThreadPool(4);


    private class RowCallable implements Callable<int[]> {
        final int[][] current;
        final int row;
        final int N;

        private RowCallable(final int[][] current,int[] buffer, int row, int N) {
            this.current = current;
            this.row = row;
            this.buffer=buffer;
            this.N = N;
            top = row > 0 ? row - 1 : N - 1;
            bottom = row < N - 1 ? row + 1 : 0;
        }

        private final int top;
        private final int bottom;
        private final int[] buffer;
        @Override
        public int[] call() throws Exception {
            //int[] buffer = new int[N];
            int left;
            int right;
            int sum;
            for (int j = 1; j < N-1; j++) {
                left = j - 1;
                right = j + 1;
                sum = current[top][left] +
                        current[top][j] +
                        current[top][right] +
                        current[row][left] +
                        current[row][right] +
                        current[bottom][left] +
                        current[bottom][j] +
                        current[bottom][right];
                buffer[j] = doLife(current[row][j], sum);
            }
            left = N - 1;
            right = 1;
            sum = current[top][left] +
                    current[top][0] +
                    current[top][right] +
                    current[row][left] +
                    current[row][right] +
                    current[bottom][left] +
                    current[bottom][0] +
                    current[bottom][right];
            buffer[0]=doLife(current[row][0],sum);
            left = N - 2;
            right = 0;
            sum = current[top][left] +
                    current[top][N-1] +
                    current[top][right] +
                    current[row][left] +
                    current[row][right] +
                    current[bottom][left] +
                    current[bottom][N-1] +
                    current[bottom][right];
            buffer[N-1]=doLife(current[row][N-1],sum);

            return buffer;
        }
    }


    int[][] play(int[][] start, int iter) {
        int[][] current = start;
        int[][] next = new int[N][N];
        int[][] tmp;
        Future<int[]> futures[] = (Future<int[]>[]) new Future[N];
        for (int turn = 0; turn < iter; turn++) {

            for (int i = 0; i < N; i++) {
                futures[i] = es.submit(new RowCallable(current,next[i], i, N));
            }
            for (int i = 0; i < N; i++) {
                try {
                    next[i] = futures[i].get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }


            tmp = current;
            current = next;
            next = tmp;
        }
        return current;
    }

    @Override
    public List<String> play(String inputFile) {
        // your solution
        parseData(inputFile);
        long start = System.nanoTime();
        int[][] result = play(currentField, M);
        long finish = System.nanoTime();
        long timeConsumedNanos = finish - start;
        System.out.println("time: " + (double) timeConsumedNanos / (double) 10E9);
        return reportResult(result);
    }
}
