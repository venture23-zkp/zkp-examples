package io.venture23zkp.sudoku;

import java.math.BigInteger;

import score.ObjectReader;
import score.ObjectWriter;

public class Board {

    private final BigInteger sha256Id;
    private final BigInteger pedersenId;
    private final int[][] data;

    Board(BigInteger sha256Id, BigInteger pedersenId, int[][] data) {
        this.sha256Id = sha256Id;
        this.pedersenId = pedersenId;
        this.data = data;
    }

    public BigInteger getSha256Id() {
        return sha256Id;
    }

    public BigInteger getPedersenId() {
        return pedersenId;
    }

    public int[][] getData() {
        return this.data;
    }

    public static void writeObject(ObjectWriter w, Board b) {
        b.writeObject(w);
    }

    public void writeObject(ObjectWriter w) {
        w.beginList(81+2);
        w.write(sha256Id);
        w.write(pedersenId);
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                w.write(this.data[i][j]);
            }
        }
        w.end();
    }

    public static Board readObject(ObjectReader r) {
        r.beginList();
        BigInteger sha256Id = r.readBigInteger();
        BigInteger pedersenId = r.readBigInteger();
        int[][] data = new int[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                data[i][j] = r.readInt();
            }
        }
        r.end();
        return new Board(sha256Id, pedersenId, data);
    }
}
