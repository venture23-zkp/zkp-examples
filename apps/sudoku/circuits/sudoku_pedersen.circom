pragma circom 2.0.0;

include "../node_modules/circomlib/circuits/bitify.circom";
include "../node_modules/circomlib/circuits/pedersen.circom";
include "./puzzle.circom";


template PedersenBoardHasher() {
    signal input board[9][9];
    signal output out;

    component board_n2b[9][9];
    component pedersen = Pedersen(328); // 81 * 4 bits + 4 bits for byte alignment
    pedersen.in[0] <== 0;
    pedersen.in[1] <== 0;
    pedersen.in[2] <== 0;
    pedersen.in[3] <== 0;
    for (var i = 0; i < 9; i++) {
        for (var j = 0; j < 9; j++) {
            log(board[i][j]);
            board_n2b[i][j] = Num2Bits(4);
            board_n2b[i][j].in <== board[i][j];
            var index = 4 + 4 * (9 * i + j);
            pedersen.in[index] <== board_n2b[i][j].out[3];
            pedersen.in[index + 1] <== board_n2b[i][j].out[2];
            pedersen.in[index + 2] <== board_n2b[i][j].out[1];
            pedersen.in[index + 3] <== board_n2b[i][j].out[0];
        }
    }

    pedersen.out[0] ==> out;
}

template SudokuPedersen() {
    signal input boardId;
    signal input board[9][9];
    signal input solved[9][9];

    component boardHasher = PedersenBoardHasher();
    boardHasher.in <== board;

    boardHasher.out === boardId;

    component puzzle = Puzzle();
    puzzle.board <== board;
    puzzle.solved <== solved;
}


component main {public [boardId]} = SudokuPedersen();
