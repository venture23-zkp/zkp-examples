#!/bin/bash
set -e

curveName=bls12381
maxConstraints=16

circuitName=sudoku_pedersen

snarkjsCmd="node /home/bishal/works/venture23-zkp/icon-snarkjs/build/cli.cjs"
# snarkjsCmd="snarkjs"

ptauDir="../ptau"
buildDir="build_${circuitName}"
inputFileName="../input.json"
witnessFileName="witness.wtns"
finalPotFileName="$ptauDir/pot${maxConstraints}_${curveName}.ptau"


mkdir -p $ptauDir
mkdir -p $buildDir

circom ${circuitName}.circom --r1cs --wasm --prime ${curveName} -o ${buildDir}

cd ${buildDir}

# phase 1 powers of tau
if [ ! -f ${finalPotFileName} ]; then
    tmpPotFileName0="pot_${curveName}_0000.ptau"
    tmpPotFileName1="pot_${curveName}_0001.ptau"
    tmpPotFileNameBeacon="pot_${curveName}_0001.ptau"
    $snarkjsCmd powersoftau new ${curveName} $maxConstraints $tmpPotFileName0 -v
    $snarkjsCmd powersoftau contribute $tmpPotFileName0 $tmpPotFileName1 --name="First contribution" -v
    $snarkjsCmd powersoftau beacon $tmpPotFileName1 $tmpPotFileNameBeacon 0102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f 10 -n="Final Beacon"
    $snarkjsCmd powersoftau prepare phase2 $tmpPotFileNameBeacon $finalPotFileName -v
fi

$snarkjsCmd groth16 setup ${circuitName}.r1cs $finalPotFileName ${circuitName}_0000.zkey

$snarkjsCmd zkey contribute ${circuitName}_0000.zkey ${circuitName}_0001.zkey --name="1st contribution" -v

$snarkjsCmd zkey export verificationkey ${circuitName}_0001.zkey verification_key.json

$snarkjsCmd zkey export javaverifier ${circuitName}_0001.zkey SnarkJSVerifier.java

node ${circuitName}_js/generate_witness.js ${circuitName}_js/${circuitName}.wasm $inputFileName ${witnessFileName}

$snarkjsCmd groth16 prove ${circuitName}_0001.zkey witness.wtns proof.json public.json

$snarkjsCmd zkey export javacalldata
