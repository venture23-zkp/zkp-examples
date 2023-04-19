pragma circom 2.0.0;

include "../node_modules/circomlib/circuits/bitify.circom";
include "../node_modules/circomlib/circuits/sha256/sha256.circom";
include "./puzzle.circom";


template SHABoardHasher() {
    signal input board[9][9];
    signal output out;

    component board_n2b[9][9];
    component sha256 = Sha256(328); // 81 * 4 bits + 4 bits for byte alignment
    sha256.in[0] <== 0;
    sha256.in[1] <== 0;
    sha256.in[2] <== 0;
    sha256.in[3] <== 0;
    for (var i = 0; i < 9; i++) {
        for (var j = 0; j < 9; j++) {
            board_n2b[i][j] = Num2Bits(4);
            board_n2b[i][j].in <== board[i][j];
            var index = 4 + 4 * (9 * i + j);
            sha256.in[index] <== board_n2b[i][j].out[3];
            sha256.in[index + 1] <== board_n2b[i][j].out[2];
            sha256.in[index + 2] <== board_n2b[i][j].out[1];
            sha256.in[index + 3] <== board_n2b[i][j].out[0];
        }
    }
    component boardHash = Bits2Num(256);
    for (var i = 0; i < 256; i++) {
        boardHash.in[i] <== sha256.out[255 - i];
    }

    boardHash.out ==> out;
}

template SudokuSHA256() {
    signal input boardId;
    signal input board[9][9];
    signal input solved[9][9];

    component boardHasher = SHABoardHasher();
    boardHasher.in <== board;

    boardHasher.out === boardId;

    component puzzle = Puzzle();
    puzzle.board <== board;
    puzzle.solved <== solved;
}


component main {public [boardId]} = SudokuSHA256();