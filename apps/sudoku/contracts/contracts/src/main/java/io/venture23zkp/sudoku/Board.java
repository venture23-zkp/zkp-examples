package io.venture23zkp.sudoku;

import score.Context;
import score.ObjectReader;
import score.ObjectWriter;

public class Board {

    private final int[][] data;

    Board(int[][] data) {
        this.data = data;
    }


    public int[][] getData() {
        return this.data;
    }

    public static void writeObject(ObjectWriter w, Board b) {
        b.writeObject(w);
    }

    public void writeObject(ObjectWriter w) {
        w.beginList(81);
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                w.write(this.data[i][j]);
            }
        }
        w.end();
    }

    public static Board readObject(ObjectReader r) {
        r.beginList();
        int[][] data = new int[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                data[i][j] = r.readInt();
            }
        }
        r.end();
        return new Board(data);
    }
}
