
package io.venture23zkp.sudoku;

import java.math.BigInteger;
import java.util.Map;
import scorex.util.HashMap;

import score.ArrayDB;
import score.Context;
import score.DictDB;
import score.annotation.External;

public class Sudoku {

    private final Pedersen_Verifier pedersen_verifier;
    private final Sha256_Verifier sha_verifier;


    private final DictDB<BigInteger, Board> boards = Context.newDictDB("boards", Board.class);
    public final ArrayDB<BigInteger> boardIds = Context.newArrayDB("boardIds", BigInteger.class);

    public Sudoku() {
        this.pedersen_verifier = new Pedersen_Verifier();
        this.sha_verifier = new Sha256_Verifier();

        addBoard(
                new BigInteger("12946702913587076100588339357837874244737833722059782620840121604018902625880"),
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
                });

        addBoard(
                new BigInteger("107892420655660906236019058477082710171022090417415360920355362860546940302805"),
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
                });

        addBoard(
                new BigInteger("50213289574608008120354010172600564686142848648930442262797942174114227273539"),
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
                });

    }

    private void addBoard(BigInteger id, int[][] data) {
        boardIds.add(id);
        boards.set(id, new Board(data));
    }

    @External(readonly = true)
    public BigInteger getRandomBoardId() {
        int i = (int) (Context.getBlockTimestamp() % boardIds.size());
        return boardIds.get(i);
    }

    @External(readonly = true)
    public int[][] getBoardData(BigInteger id) {
        return boards.get(id).getData();
    }

    @External(readonly = true)
    public boolean verify(BigInteger boardId, BigInteger[] a, BigInteger[][] b, BigInteger[] c, String verificationType) {
        Context.require(boards.get(boardId) != null, "Board does not exist!");
        BigInteger[] input = new BigInteger[] { boardId };

        if (verificationType.equals("sha")) {
            return this.sha_verifier.verifyProof(a, b, c, input);
        } else{
            return this.pedersen_verifier.verifyProof(a, b, c, input);
        }
    }

}
