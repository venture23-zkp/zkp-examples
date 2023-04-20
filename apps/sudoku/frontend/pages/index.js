import React, { useState, useCallback, useEffect } from 'react';
import { IconService, CallBuilder } from 'icon-sdk-js';

import "./index.module.css";

import network from './config/network.json';
import addresses from "./config/addresses.json";

// NOTE: crypto could not be imported for frontend, so used "require"
const { createHash } = require('crypto');

const snarkjs = require("snarkjs");

import useCustomToast from '../hooks/useCustomToast';

const service = new IconService(
    new IconService.HttpProvider(network['39'].rpcUrls[0])
);
const IconConverter = IconService.IconConverter;


async function generateGroth16Proof(input, wasmPath, zkeyPath) {
    const {
        proof: { pi_a, pi_b, pi_c },
    } = await snarkjs.groth16.fullProve(input, wasmPath, zkeyPath);
    return {
        a: [pi_a[0], pi_a[1]],
        b: [
            [pi_b[0][0], pi_b[0][1]],
            [pi_b[1][0], pi_b[1][1]],
        ],
        c: [pi_c[0], pi_c[1]],
    }
}

async function fetchRandomBoardId() {
    const call = new CallBuilder()
        .to(addresses.sha256Sudoku)
        .method("getRandomBoardId")
        .build();
    return await service.call(call).execute();
}

async function fetchBoardData(boardId) {
    const call = new CallBuilder()
        .to(addresses.sha256Sudoku)
        .method("getBoardData")
        .params({ id: `0x${boardId.toString(16)}` })
        .build();
    return await service.call(call).execute();
}

// function mirror_bits(b) {
//     let sum = 0
//     for (let i = 0; i < 8; i++) {
//         sum += ((b & (1 << i)) >> i) << (7 - i)
//     }
//     return sum
// }

// async function getBoardHash(board) {
//     const pedersen = await buildPedersenHash();
//     board = board.flat()
//     let e1, e2;
//     const data = new Uint8Array(41); // 328 / 8
//     for (let i = 0; i < 41; i++) {
//         e1 = i === 0 ? 0 : board[2 * i - 1]
//         e2 = board[2 * i];
//         data[i] = mirror_bits(e1 * 16 + e2);
//     }
//     const packed = pedersen.hash(data)
//     const res = pedersen.babyJub.unpackPoint(packed)
//     return pedersen.babyJub.F.toObject(res[0])
// }

function sha256(data) {
    return createHash('sha256').update(data).digest('hex');
}

function sha256HashBoard(board) {
    board = board.flat()
    let e1, e2;
    const data = new Uint8Array(41); // 328 / 8
    for (let i = 0; i < 41; i++) {
        e1 = i === 0 ? 0 : board[2 * i - 1]
        e2 = board[2 * i];
        data[i] = e1 * 16 + e2;
    }
    return BigInt("0x" + sha256(data))
}

// async function getBoardHash(board) {
//     const pedersen = await buildPedersenHash();
//     board = board.flat()
//     let e1, e2;
//     const data = new Uint8Array(41); // 328 / 8
//     for (let i = 0; i < 41; i++) {
//         e1 = i === 0 ? 0 : board[2 * i - 1]
//         e2 = board[2 * i];
//         data[i] = mirror_bits(e1 * 16 + e2);
//     }
//     const packed = pedersen.hash(data)
//     const res = pedersen.babyJub.unpackPoint(packed)
//     return pedersen.babyJub.F.toObject(res[0])
// }

export default function Sudoku() {

    const [board, setBoard] = useState();
    const [boardId, setBoardId] = useState();
    const [solved, setSolved] = useState();
    const [circuitStats, setCircuitStats] = useState(null);
    const { Toaster, toast } = useCustomToast();

    // load boardId
    useEffect(() => {
        fetchRandomBoardId()
            .then(boardIdHex => {
                // setBoardId(BigInt(boardIdHex))
                setBoardId(12946702913587076100588339357837874244737833722059782620840121604018902625880n)
            })
            .catch(error => {
                alert(`Failed to load boardId: error = ${error}`)
            })
    }, [])

    // load board
    useEffect(() => {
        if (boardId === undefined) return;

        fetchBoardData(boardId)
            .then(boardHex => {

                const board = boardHex.map(row => {
                    return row.map(col => parseInt(col, 16));
                });

                const boardHash = sha256HashBoard(board)
                console.log(boardHash)
                if (boardId !== boardHash) {
                    alert(`Invalid board: Hash does not match id = ${boardId}`)
                    return
                }

                setBoard(board)
                setSolved(board)
            })
            .catch(error => {
                alert(`Failed to load board: id = ${boardId}, error = ${error}`)
            })

    }, [boardId])


    const updateSolution = useCallback((i, j, value) => {
        const newSolution = JSON.parse(JSON.stringify(solved));
        newSolution[i][j] = value;
        setSolved(newSolution);
    })

    const getParams = ({ a, b, c, boardId }) => {
        return {
            a: a.map(IconConverter.toHexNumber),
            b: b.map(row => row.map(IconConverter.toHexNumber).reverse()),
            c: c.map(IconConverter.toHexNumber),
            boardId: IconConverter.toHexNumber(boardId)
        }
    };


    const verifySudoku = async () => {
        // load circuit stats
        setCircuitStats(null)
        const cs = await snarkjs.r1cs.info("/circuit/sudoku.r1cs");
        setCircuitStats({
            curve: cs.curve.name,
            nConstraints: cs.nConstraints,
            nLabels: cs.nLabels,
            nOutputs: cs.nOutputs,
            nPrvInputs: cs.nPrvInputs,
            nPubInputs: cs.nPubInputs,
            nVars: cs.nVars,
        });

        // generate proof
        const input = { boardId, board, solved }
        let params;
        try {
            const proof = await generateGroth16Proof(
                input,
                "/circuit/sudoku.wasm",
                "/circuit/sudoku_final.zkey"
            )
            params = getParams({ ...proof, boardId })
        } catch (error) {
            toast.error("Failed to generate proof! error=" + error)
            return
        }

        const call = new CallBuilder()
            .to(addresses.sha256Sudoku)
            .method("verify")
            .params(params).build();

        try {
            const res = await service.call(call).execute();
            if (!res) {
                toast.error("Wrong solution!")
            } else {
                toast.success("Correct solution!")
            }
        } catch (error) {
            alert("Failed to verify using RPC! error=" + error);
        }
    };

    return board === undefined ? null : (
        <div align="center" style={Styles.board}>
            <Toaster />
            {
                solved.map((row, rowIndex) => (
                    <div key={rowIndex}>
                        {
                            row.map((col, colIndex) => (
                                <input
                                    key={colIndex}
                                    style={Styles.input}
                                    value={col}
                                    onChange={event => {
                                        if (board[rowIndex][colIndex] === 0) {
                                            updateSolution(rowIndex, colIndex, event.target.value);
                                        }
                                    }}
                                />
                            ))
                        }
                    </div>
                ))
            }
            <button onClick={verifySudoku} style={Styles.verifyButton}>Verify</button>
            {
                circuitStats !== null ? (
                    <div style={Styles.circuitStatsContainer}>
                        <h3>Circuit Statistics</h3>
                        <pre>
                            Curve: {circuitStats.curve},
                            Constraints: {circuitStats.nConstraints},
                            Labels: {circuitStats.nLabels},
                            Outputs: {circuitStats.nOutputs},
                            PrvInputs: {circuitStats.nPrvInputs},
                            PubInputs: {circuitStats.nPubInputs},
                            Vars: {circuitStats.nVars}
                        </pre>
                    </div>
                ) : null
            }
        </div>
    );
}

const Styles = {
    board: {
        minHeight: "100vh",
        width: "100vw",
        backgroundColor: "rgb(17 1 66)",
        display: "flex",
        flexDirection: "column",
        justifyContent: "center",
        alignItems: "center",
        padding: "20px 0"
    },
    input: {
        height: "3rem",
        width: "3rem",
        borderWidth: "2px",
        backgroundColor: "transparent",
        color: "rgb(175 132 248)",
        fontSize: "1.5rem",
        lineHeight: "2rem",
        textAlign: "center",
        borderStyle: "solid",
        borderColor: "#cbd5e1"
    },
    verifyButton: {
        fontWeight: "500",
        fontSize: "1.125rem",
        linHeight: "1.75rem",
        padding: "0.75em 1.25em",
        backgroundImage: "linear-gradient(to right,#9333ea, #4F46ED)",
        borderStyle: "none",
        width: "150px",
        borderRadius: "4px",
        margin: "10px 0",
        color: "#cbd5e1",
        cursor: "pointer"
    },
    circuitStatsContainer: {
        color: "#cbd5e1",
        //   display:inline-block;
        // webkitMask: "linear-gradient(-60deg,#000 30%,#0005,#000 70%) right/300% 100%",
        // backgroundRepeat: "no-repeat",
        // animation: "shimmer 2.5s infinite",
        fontSize: "1rem",
        // maxWidth: "200px",
    }
}
