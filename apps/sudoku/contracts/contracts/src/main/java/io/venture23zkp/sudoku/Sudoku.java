package io.venture23zkp.sudoku;

import java.math.BigInteger;

import score.ArrayDB;
import score.Context;
import score.annotation.External;

public class Sudoku {

    private final PedersenBN128Verifier pedersenBN128Verifier;
    private final Sha256BLS12381Verifier sha256BLS12381Verifier;

    private final ArrayDB<Board> boards = Context.newArrayDB("boards", Board.class);


    public Sudoku() {
        this.pedersenBN128Verifier = new PedersenBN128Verifier();
        this.sha256BLS12381Verifier = new Sha256BLS12381Verifier();

        boards.add(new Board(
                new BigInteger("12946702913587076100588339357837874244737833722059782620840121604018902625880"),
                new BigInteger("20508280487233337829117787098579484117180647220128235675540805714795266829330"),
                new int[][] {
                        { 1, 2, 7, 5, 8, 4, 6, 9, 3 },
                        { 8, 5, 6, 3, 7, 9, 1, 2, 4 },
                        { 3, 4, 9, 6, 2, 1, 8, 7, 5 },
                        { 4, 7, 1, 9, 5, 8, 2, 3, 6 },
                        { 2, 6, 8, 7, 1, 3, 5, 4, 9 },
                        { 9, 3, 5, 4, 6, 2, 7, 1, 8 },
                        { 5, 8, 3, 2, 9, 7, 4, 6, 1 },
                        { 7, 1, 4, 8, 3, 6, 9, 5, 2 },
                        { 6, 9, 2, 1, 4, 5, 3, 0, 7 }
                }
        ));

        boards.add(new Board(
                new BigInteger("107892420655660906236019058477082710171022090417415360920355362860546940302805"),
                new BigInteger("4734607875457860383511038714947529190779064469715356965082230943676408289143"),
                new int[][] {
                        { 0, 2, 7, 5, 0, 4, 0, 0, 0 },
                        { 0, 0, 0, 3, 7, 0, 0, 0, 4 },
                        { 3, 0, 0, 0, 0, 0, 8, 0, 0 },
                        { 4, 7, 0, 9, 5, 8, 0, 3, 6 },
                        { 2, 6, 8, 7, 1, 0, 0, 4, 9 },
                        { 0, 0, 0, 0, 0, 2, 0, 1, 8 },
                        { 0, 8, 3, 0, 9, 0, 4, 0, 0 },
                        { 7, 1, 0, 0, 0, 0, 9, 0, 2 },
                        { 0, 0, 0, 0, 0, 5, 0, 0, 7 }
                }
        ));

        boards.add(new Board(
            new BigInteger("50213289574608008120354010172600564686142848648930442262797942174114227273539"),
            new BigInteger("8152119989451001230856454209031619574707505319220427314377706000948347561614"),
            new int[][] {
                    { 0, 0, 0, 0, 0, 6, 0, 0, 0 },
                    { 0, 0, 7, 2, 0, 0, 8, 0, 0 },
                    { 9, 0, 6, 8, 0, 0, 0, 1, 0 },
                    { 3, 0, 0, 7, 0, 0, 0, 2, 9 },
                    { 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                    { 4, 0, 0, 5, 0, 0, 0, 7, 0 },
                    { 6, 5, 0, 1, 0, 0, 0, 0, 0 },
                    { 8, 0, 1, 0, 5, 0, 3, 0, 0 },
                    { 7, 9, 2, 0, 0, 0, 0, 0, 4 }
            }
        ));

    }

    @External(readonly = true)
    public Board getRandomBoard() {
        int i = (int) (Context.getBlockTimestamp() % boards.size());
        return boards.get(i);
    }

    @External(readonly = true)
    public boolean verify(BigInteger boardId, BigInteger[] a, BigInteger[][] b, BigInteger[] c) {
        for (int i = 0; i < boards.size(); i++) {
            Board board = boards.get(i);
            if (board.getSha256Id().equals(boardId)) {
                return this.sha256BLS12381Verifier.verifyProof(a, b, c, new BigInteger[]{boardId});
            }
            if (board.getPedersenId().equals(boardId)) {
                return this.pedersenBN128Verifier.verifyProof(a, b, c, new BigInteger[]{boardId});
            }
        }
        throw new IllegalArgumentException("Board with id = " + boardId + " does not exist!");
    }

}
