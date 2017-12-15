import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

    int doLife(int curval, int neighbours) {
        if (curval == 0 && neighbours == 3) {
            return 1;
        }
        if (curval == 1 && (neighbours > 3 || neighbours < 2)) {
            return 0;
        }
        return curval;

    }

    int[][] play(int[][] start, int iter) {
        int[][] current = start;

        for (int turn = 0; turn < iter; turn++) {
            int[][] next = new int[N][N];
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    int sum = calcNeighboursSum(i, j, current);
                    next[i][j] = doLife(current[i][j], sum);
                }
            }
            current = next;
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
        System.out.println("time: " + (double)timeConsumedNanos/(double)10E9);
        return reportResult(result);
    }
}
