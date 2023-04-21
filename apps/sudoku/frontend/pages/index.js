import React, { useState, useCallback, useEffect } from 'react';
import { IconService, CallBuilder } from 'icon-sdk-js';
import { buildPedersenHash } from "circomlibjs"
// NOTE: crypto could not be imported for frontend, so used "require"
const { createHash } = require('crypto');
import GridLoader from "react-spinners/GridLoader";

import style from "./index.module.css";

import network from './config/network.json';
import addresses from "./config/addresses.json";


const snarkjs = require("snarkjs");

import useCustomToast from '../hooks/useCustomToast';

const service = new IconService(
    new IconService.HttpProvider(network['39'].rpcUrls[0])
);
const IconConverter = IconService.IconConverter;

const circuits = [{
    id: 'sha256',
    title: "SHA256",
    staticPath: "/circuit/sha256",
}, {
    id: 'pedersen',
    title: "Pedersen",
    staticPath: "/circuit/pedersen",
}]


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

async function fetchRandomBoard() {
    const call = new CallBuilder()
        .to(addresses.sudoku)
        .method("getRandomBoard")
        .build();
    return await service.call(call).execute();
}

function mirror_bits(b) {
    let sum = 0
    for (let i = 0; i < 8; i++) {
        sum += ((b & (1 << i)) >> i) << (7 - i)
    }
    return sum
}

async function pedersenBoardHash(board) {
    const pedersen = await buildPedersenHash();
    board = board.flat()
    let e1, e2;
    const data = new Uint8Array(41); // 328 / 8
    for (let i = 0; i < 41; i++) {
        e1 = i === 0 ? 0 : board[2 * i - 1]
        e2 = board[2 * i];
        data[i] = mirror_bits(e1 * 16 + e2);
    }
    const packed = pedersen.hash(data)
    const res = pedersen.babyJub.unpackPoint(packed)
    return pedersen.babyJub.F.toObject(res[0])
}

function sha256(data) {
    return createHash('sha256').update(data).digest('hex');
}

function sha256BoardHash(board) {
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

export default function Sudoku() {

    const [board, setBoard] = useState();
    const [boardIds, setBoardIds] = useState();
    const [solved, setSolved] = useState();
    const { Toaster, toast } = useCustomToast();
    const [selectedCircuit, setSelectedCircuit] = useState(circuits[0])
    const [circuitStats, setCircuitStats] = useState(circuits.reduce((acc, current) => { acc[current.id] = undefined; return acc }, {}))
    const [circuitStatsLoading, setCircuitStatsLoading] = useState(false);
    const [verificationInProgress, setVerificationInProgress] = useState(false);

    // load board
    useEffect(() => {
        fetchRandomBoard()
            .then(async info => {
                const { sha256Id: hexSha256Id, pedersenId: hexPedersenId, data: hexData } = info;

                const board = hexData.map(row => {
                    return row.map(col => parseInt(col, 16));
                });

                const sha256Id = sha256BoardHash(board)
                const pedersenId = await pedersenBoardHash(board)
                if (BigInt(hexSha256Id) !== sha256Id || BigInt(hexPedersenId) !== pedersenId) {
                    alert(`Invalid board: Board does not match data!`)
                    return
                }

                return { sha256Id, pedersenId, board }
            })
            .then(({ sha256Id, pedersenId, board }) => {
                setBoardIds({ sha256: sha256Id, pedersen: pedersenId })
                setBoard(board)
                setSolved(board)
            })
            .catch(error => {
                alert(`Failed to load a random board! error = ${error}`)
            })

    }, [])

    // load circuit info
    useEffect(() => {

        (async () => {
            setCircuitStatsLoading(true);
            try {
                let cs;
                if (circuitStats[selectedCircuit.id]) {
                    return;
                } else {
                    cs = await snarkjs.r1cs.info(`${selectedCircuit.staticPath}/sudoku.r1cs`);
                }
                setCircuitStats({
                    ...circuitStats,
                    [selectedCircuit.id]: {
                        curve: cs.curve.name,
                        nConstraints: cs.nConstraints,
                        nLabels: cs.nLabels,
                        nOutputs: cs.nOutputs,
                        nPrvInputs: cs.nPrvInputs,
                        nPubInputs: cs.nPubInputs,
                        nVars: cs.nVars,
                        proofTime: 0,
                    }
                });
            } catch (err) {
                console.error(err);
            } finally {
                setCircuitStatsLoading(false);
            }
        })();

    }, [selectedCircuit])


    const updateSolution = useCallback((i, j, value) => {
        const newSolution = JSON.parse(JSON.stringify(solved));
        newSolution[i][j] = value;
        setSolved(newSolution);
    })


    const verifySudoku = useCallback(async () => {
        // generate proof
        let params;
        const boardId = boardIds[selectedCircuit.id]
        try {
            const startTime = Date.now();
            const proof = await generateGroth16Proof(
                { boardId, board, solved },
                `${selectedCircuit.staticPath}/sudoku.wasm`,
                `${selectedCircuit.staticPath}/sudoku_final.zkey`
            )
            setCircuitStats({
                ...circuitStats,
                [selectedCircuit.id]: {
                    ...circuitStats[selectedCircuit.id],
                    proofTime: Date.now() - startTime,
                }
            });
            params = {
                a: proof.a.map(IconConverter.toHexNumber),
                b: proof.b.map(row => row.map(IconConverter.toHexNumber).reverse()),
                c: proof.c.map(IconConverter.toHexNumber),
                boardId: IconConverter.toHexNumber(boardId)
            }
        } catch (error) {
            console.error(error)
            throw Error("Failed to generate proof!")
        }

        const call = new CallBuilder()
            .to(addresses.sudoku)
            .method("verify")
            .params(params).build();

        try {
            const res = await service.call(call).execute();
            if (!res) {
                throw Error("Wrong solution!")
            }
        } catch (error) {
            throw Error("Failed to verify using RPC! error=" + error);
        }
    }, [boardIds, selectedCircuit, circuitStats, board, solved]);

    const handleVerifyClick = async () => {
        const toastId = toast.loading('Verifying...');
        setVerificationInProgress(true);
        try {
            await verifySudoku(selectedCircuit);
            toast.success("Correct solution!");
        } catch (err) {
            toast.error(err.toString());
        } finally {
            toast.remove(toastId);
            setVerificationInProgress(false);
        }
    }

    return board === undefined ? null : (
        <div align="center" className={style.soduku}>
            <Toaster />
            <div className={style.boardContainer}>
                <div className={style.boardWarning}>You can only update the input with value 0</div>
                <div className='board'>
                    {
                        solved.map((row, rowIndex) => (
                            <div key={rowIndex}>
                                {
                                    row.map((col, colIndex) => (
                                        <input
                                            key={colIndex}
                                            className={style.input}
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
                </div>
            </div>
            <div className={style.verificationContainer}>
                {
                    circuitStats !== null ? (
                        <div className={style.circuitStatsContainer}>
                            <div className={style.tabHeadersContainer}>
                                {
                                    circuits.map((circuit, idx) => (
                                        <div className={circuit.id === selectedCircuit.id ?
                                            `${style.tabHeader} ${style.tabHeaderActive}` :
                                            `${style.tabHeader} ${style.tabHeaderInActive}`
                                        } onClick={() => setSelectedCircuit(circuit)} key={idx}>
                                            <h3>{circuit.title}</h3>
                                        </div>
                                    ))
                                }
                            </div>
                            {
                                circuitStatsLoading ?
                                    <div className={style.circuitLoadingContainer}>
                                        <GridLoader
                                            color={"#4F46ED"}
                                            loading={circuitStatsLoading}
                                        />
                                    </div> :
                                    <div className={style.tabBody}>
                                        <div className={style.circuitStats}>
                                            <div className={style.key}>Curve</div> <div className={style.value}>{circuitStats[selectedCircuit.id]?.curve}</div>
                                            <div className={style.key}>Constraints</div> <div className={style.value}>{circuitStats[selectedCircuit.id]?.nConstraints}</div>
                                            <div className={style.key}>Labels</div> <div className={style.value}>{circuitStats[selectedCircuit.id]?.nLabels}</div>
                                            <div className={style.key}>Outputs</div> <div className={style.value}>{circuitStats[selectedCircuit.id]?.nOutputs}</div>
                                            <div className={style.key}>PrvInputs</div> <div className={style.value}>{circuitStats[selectedCircuit.id]?.nPrvInputs}</div>
                                            <div className={style.key}>PubInputs</div> <div className={style.value}>{circuitStats[selectedCircuit.id]?.nPubInputs}</div>
                                            <div className={style.key}>Vars</div> <div className={style.value}>{circuitStats[selectedCircuit.id]?.nVars}</div>
                                            <div className={style.key}>Proof time</div> <div className={style.value}>{circuitStats[selectedCircuit.id]?.proofTime}ms</div>
                                        </div>
                                        <div>
                                            <button
                                                onClick={handleVerifyClick}
                                                className={`${style.verifyButton} ${verificationInProgress ? style.disabledButton : ""}`}
                                                disabled={verificationInProgress}
                                            >
                                                Verify
                                            </button>
                                        </div>
                                    </div>
                            }
                        </div>
                    ) : null
                }
            </div>
        </div >
    );
}
