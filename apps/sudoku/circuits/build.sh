#!/bin/bash
set -e

maxConstraints=16

circuitName=sudoku_sha256

if [ $circuitName == "sudoku_pedersen" ]; then
    curveName=bn128
elif [ $circuitName == "sudoku_sha256" ]; then
    curveName=bls12381
fi

snarkjsCmd="icon-snarkjs"

ptauDir="../ptau"
buildDir="build_${circuitName}"
inputFileName="../inputs/${circuitName}.json"
witnessFileName="witness.wtns"
finalPotFileName="$ptauDir/pot${maxConstraints}_${curveName}.ptau"


mkdir -p ${ptauDir}
mkdir -p ${buildDir}

# compile the citcuit

circom ${circuitName}.circom --r1cs --wasm --prime ${curveName} -o ${buildDir}

cd ${buildDir}

# phase 1 powers of tau
if [ ! -f ${finalPotFileName} ]; then
    tmpPotFileName0="pot_${curveName}_0000.ptau"
    tmpPotFileName1="pot_${curveName}_0001.ptau"
    $snarkjsCmd powersoftau new ${curveName} ${maxConstraints} ${tmpPotFileName0} -v
    $snarkjsCmd powersoftau contribute ${tmpPotFileName0} ${tmpPotFileName1} --name="First contribution" -v
    $snarkjsCmd powersoftau prepare phase2 ${tmpPotFileName1} ${finalPotFileName} -v
fi

$snarkjsCmd groth16 setup ${circuitName}.r1cs ${finalPotFileName} ${circuitName}_0000.zkey

$snarkjsCmd zkey contribute ${circuitName}_0000.zkey ${circuitName}_0001.zkey --name="1st contribution" -v

$snarkjsCmd zkey export verificationkey ${circuitName}_0001.zkey verification_key.json

$snarkjsCmd zkey export javaverifier ${circuitName}_0001.zkey SnarkJSVerifier.java

node ${circuitName}_js/generate_witness.js ${circuitName}_js/${circuitName}.wasm $inputFileName ${witnessFileName}

$snarkjsCmd groth16 prove ${circuitName}_0001.zkey ${witnessFileName} proof.json public.json

$snarkjsCmd groth16 verify verification_key.json public.json proof.json

$snarkjsCmd zkey export javacalldata | tee calldata.json
