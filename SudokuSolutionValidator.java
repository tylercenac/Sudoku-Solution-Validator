package sudokusolutionvalidator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

/**
 *
 * Name:        Cenac, Tyler
 * Email:       tcenac1@lsu.edu
 * Project:     PA-1 (Multithreading)
 * Instructor:  Feng Chen
 * Class:       cs4103-sp20
 * Login Id:    cs410314
 *
 */
public class SudokuSolutionValidator {

    // global constant for number of threads
    private static final int nThreads = 27;

    // placeholder puzzle solution
    private static final int[][] puzzle = {
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0},};

    // array that will be updated by worker threads
    private static boolean[] valid;

    // object that contains relevant row and column
    public static class RowCol {

        int row, col;

        RowCol(int row, int column) {
            this.row = row;
            this.col = column;
        }
    }

    // runnable object that checks for row validity
    public static class checkRow extends RowCol implements Runnable {

        checkRow(int row, int column) {
            super(row, column);
        }

        @Override
        public void run() {
            if (col != 0 || row > 8) {
                return;
            }

            boolean[] validA = new boolean[9];

            for (int i = 0; i < 9; i++) {

                int num = puzzle[row][i];

                if (num < 1 || num > 9 || validA[num - 1]) {

                    return;

                } else if (!validA[num - 1]) {

                    validA[num - 1] = true;
                }
            }
            // execute if row subsection is valid
            valid[row + 9] = true;
        }

    }

    // runnable object that checks for column validity
    public static class checkCol extends RowCol implements Runnable {

        checkCol(int row, int column) {
            super(row, column);
        }

        @Override
        public void run() {
            if (row != 0 || col > 8) {
                return;
            }

            boolean[] validA = new boolean[9];
            for (int i = 0; i < 9; i++) {

                int num = puzzle[i][col];

                if (num < 1 || num > 9 || validA[num - 1]) {

                    return;

                } else if (!validA[num - 1]) {

                    validA[num - 1] = true;
                }
            }
            // execute if col subsection is valid
            valid[col + 18] = true;
        }

    }

    // runnable object that checks for 3x3 grid validity
    public static class check3x3 extends RowCol implements Runnable {

        check3x3(int row, int column) {
            super(row, column);
        }

        @Override
        public void run() {
            // verify parameters
            if (col % 3 != 0 || col > 6 || row > 6 || row % 3 != 0) {
                return;
            }

            boolean[] validA = new boolean[9];

            for (int i = row; i < row + 3; i++) {

                for (int j = col; j < col + 3; j++) {

                    int num = puzzle[i][j];

                    if (num < 1 || num > 9 || validA[num - 1]) {

                        return;

                    } else {

                        validA[num - 1] = true;

                    }
                }
            }
            // execute if 3x3 is valid
            valid[col / 3 + row] = true;
        }

    }

    public static void main(String[] args) throws FileNotFoundException {

        // get file location
        Scanner input = new Scanner(System.in);
        System.out.print("Enter the location of the file: "); // "C:\\Users\\..."
        String fileName = input.nextLine();
        File file = new File(fileName);

        // input solution from file
        Scanner inputStream = null;
        inputStream = new Scanner(file);

        int row = 0;

        while (inputStream.hasNextLine()) {

            String[] line = inputStream.nextLine().split(" ");

            for (int i = 0; i < 9; i++) {

                puzzle[row][i] = Integer.parseInt(line[i]);

            }

            row++;

        }

        //print solution
        for (int i = 0; i < 9; i++) {
            System.out.println(Arrays.toString(puzzle[i]));
        }

        valid = new boolean[nThreads];
        Thread[] threads = new Thread[nThreads];
        int index = 0;

        // create a thread for each 3x3, row, and col = 27 threads total
        for (int i = 0; i < 9; i++) {

            for (int j = 0; j < 9; j++) {

                if (j % 3 == 0 && i % 3 == 0) {

                    threads[index++] = new Thread(new check3x3(i, j));

                }

                if (j == 0) {

                    threads[index++] = new Thread(new checkRow(i, j));

                }

                if (i == 0) {

                    threads[index++] = new Thread(new checkCol(i, j));

                }
            }
        }

        // start threads
        for (int i = 0; i < threads.length; i++) {

            threads[i].start();

        }

        // wait for threads to finish
        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println();

        // convert validity from boolean values to string values
        String[] validStr = new String[valid.length];
        for (int i = 0; i < valid.length; i++) {
            if (valid[i]) {
                validStr[i] = "valid";
            } else {
                validStr[i] = "invalid";
            }
        }

        String[] subgrids = {"123", "456", "789"};

        int index2 = 0;
        
        // prints thread information for each 3x3 grid
        for(int i=0; i<3; i++){
            for(int j=0; j<3; j++){
                System.out.println("Thread " + (index2 + 1) + ", Subgrid R"+subgrids[i]+"xC" + subgrids[j] + ", " + validStr[index2]);
                index2++;
            }
        }
        
        // prints thread information for each row
        for (int i = 0; i < 9; i++) {
            
            System.out.println("Thread " + (index2 + 1) + ", Row " + (i + 1) + ", " + validStr[index2]);
            index2++;
            
        }
        
        // prints thread information for each column
        for (int i = 0; i < 9; i++) {
            
            System.out.println("Thread " + (index2 + 1) + ", Column " + (i + 1) + ", " + validStr[index2]);
            index2++;
            
        }

        System.out.println();

        // sudoku solution is invalid if there are any 0s in the valid array
        for (int i = 0; i < valid.length; i++) {
            if (!valid[i]) {
                System.out.println("Solution is invalid!");
                return;
            }
        }
        System.out.println("Solution is valid!");

    }

}
