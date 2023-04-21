pragma circom 2.0.0;

include "./node_modules/circomlib/circuits/comparators.circom";

template Puzzle() {
    signal input board[9][9];
    signal input solved[9][9];

    // Check if the numbers of the solved sudoku are >=1 and <=9 matches
    // Each number in the solved sudoku is checked to see if it is >=1 and <=9

    component getone[9][9];
    component letnine[9][9];


    for (var i = 0; i < 9; i++) {
       for (var j = 0; j < 9; j++) {
           letnine[i][j] = LessEqThan(32);
           letnine[i][j].in[0] <== solved[i][j];
           letnine[i][j].in[1] <== 9;

           getone[i][j] = GreaterEqThan(32);
           getone[i][j].in[0] <== solved[i][j];
           getone[i][j].in[1] <== 1;

           letnine[i][j].out ===  getone[i][j].out;
        }
    }


    // Check if board is the initial state of solved
    // If board[i][j] is not zero, it means that solved[i][j] is equal to board[i][j]
    // If board[i][j] is zero, it means that solved [i][j] is different from board[i][j]

    component ieBoard[9][9];
    component izBoard[9][9];

    for (var i = 0; i < 9; i++) {
       for (var j = 0; j < 9; j++) {
            ieBoard[i][j] = IsEqual();
            ieBoard[i][j].in[0] <== solved[i][j];
            ieBoard[i][j].in[1] <== board[i][j];

            izBoard[i][j] = IsZero();
            izBoard[i][j].in <== board[i][j];

            ieBoard[i][j].out === 1 - izBoard[i][j].out;
        }
    }


    // Check if each row in solved has all the numbers from 1 to 9, both included
    // For each element in solved, check that this element is not equal
    // to previous elements in the same row

    component ieRow[324];

    var indexRow = 0;


    for (var i = 0; i < 9; i++) {
       for (var j = 0; j < 9; j++) {
            for (var k = 0; k < j; k++) {
                ieRow[indexRow] = IsEqual();
                ieRow[indexRow].in[0] <== solved[i][k];
                ieRow[indexRow].in[1] <== solved[i][j];
                ieRow[indexRow].out === 0;
                indexRow ++;
            }
        }
    }


    // Check if each column in solved has all the numbers from 1 to 9, both included
    // For each element in solved, check that this element is not equal
    // to previous elements in the same column

    component ieCol[324];

    var indexCol = 0;


    for (var i = 0; i < 9; i++) {
       for (var j = 0; j < 9; j++) {
            for (var k = 0; k < i; k++) {
                ieCol[indexCol] = IsEqual();
                ieCol[indexCol].in[0] <== solved[k][j];
                ieCol[indexCol].in[1] <== solved[i][j];
                ieCol[indexCol].out === 0;
                indexCol ++;
            }
        }
    }


    // Check if each square in solved has all the numbers from 1 to 9, both included
    // For each square and for each element in each square, check that the
    // element is not equal to previous elements in the same square

    component ieSquare[324];

    var indexSquare = 0;

    for (var i = 0; i < 9; i+=3) {
       for (var j = 0; j < 9; j+=3) {
            for (var k = i; k < i+3; k++) {
                for (var l = j; l < j+3; l++) {
                    for (var m = i; m <= k; m++) {
                        for (var n = j; n < l; n++){
                            ieSquare[indexSquare] = IsEqual();
                            ieSquare[indexSquare].in[0] <== solved[m][n];
                            ieSquare[indexSquare].in[1] <== solved[k][l];
                            ieSquare[indexSquare].out === 0;
                            indexSquare ++;
                        }
                    }
                }
            }
        }
    }

}