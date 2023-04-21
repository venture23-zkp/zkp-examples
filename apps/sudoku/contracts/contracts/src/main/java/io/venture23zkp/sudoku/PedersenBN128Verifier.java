package io.venture23zkp.sudoku;

import score.Context;
import score.annotation.External;

import java.math.BigInteger;
import java.util.Arrays;


public class PedersenBN128Verifier {
    static final String protocol = "groth16";
    static final String curve = "bn128";
    static final int nPublic = 1;

    static final String curveName = "bn128";
    static final String g1CurveName = "bn128-g1";
    static final String g2CurveName = "bn128-g2";
    static final BigInteger BASE_FIELD = new BigInteger(
            "21888242871839275222246405745257275088696311157297823662689037894645226208583");
    static final BigInteger SCALAR_FIELD = new BigInteger(
            "21888242871839275222246405745257275088548364400416034343698204186575808495617");
    static final int BASE_FIELD_SIZE = 32; // bytes
    

    static byte[] concat(byte[]... args) {
        int length = 0;
        for (int i = 0; i < args.length; i++) {
            length += args[i].length;
        }
        byte[] out = new byte[length];
        int offset = 0;
        for (int i = 0; i < args.length; i++) {
            System.arraycopy(args[i], 0, out, offset, args[i].length);
            offset += args[i].length;
        }
        return out;
    }

    static class P1 {
        BigInteger x;
        BigInteger y;

        public P1 generator() {
            return new P1(new BigInteger("1"), new BigInteger("2"));
        }

        P1() {
            this.x = BigInteger.ZERO;
            this.y = BigInteger.ZERO;
        }
        
        P1(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }

        P1(byte[] data) {
            if (data.length != 2 * BASE_FIELD_SIZE) {
                throw new IllegalArgumentException("P1(byte[] data): invalid data layout!");
            }
            this.x = new BigInteger(Arrays.copyOfRange(data, 0, BASE_FIELD_SIZE));
            this.y = new BigInteger(Arrays.copyOfRange(data, BASE_FIELD_SIZE, 2 * BASE_FIELD_SIZE));
        }

        public byte[] bytes() {
            byte[] buf = new byte[2 * BASE_FIELD_SIZE];
            byte[] xb = this.x.toByteArray();
            byte[] yb = this.y.toByteArray();
            System.arraycopy(xb, 0, buf, BASE_FIELD_SIZE - xb.length, xb.length);
            System.arraycopy(yb, 0, buf, 2 * BASE_FIELD_SIZE - yb.length, yb.length);
            return buf;
        }

        public boolean equals(P1 p) {
            return this.x.equals(p.x) && this.y.equals(p.y);
        }

        public P1 neg() {
            P1 zero = new P1();
            return this.equals(zero) ? zero : new P1(this.x, BASE_FIELD.subtract(this.y.mod(BASE_FIELD)));
        }

        public P1 add(P1 other) {
            byte[] res = Context.ecAdd(g1CurveName, concat(this.bytes(), other.bytes()), false);
            return new P1(res);
        }

        public P1 scalarMul(BigInteger scalar) {
            byte[] res = Context.ecScalarMul(g1CurveName, scalar.toByteArray(), this.bytes(), false);
            return new P1(res);
        }

    }

    static class P2 {
        // (x0 * i + x1, y0 * i + y1)
        BigInteger x0;
        BigInteger x1;
        BigInteger y0;
        BigInteger y1;

        public P2 generator() {
            return new P2(
                    new BigInteger("11559732032986387107991004021392285783925812861821192530917403151452391805634"),
                    new BigInteger("10857046999023057135944570762232829481370756359578518086990519993285655852781"),
                    new BigInteger("4082367875863433681332203403145435568316851327593401208105741076214120093531"),
                    new BigInteger("8495653923123431417604973247489272438418190587263600148770280649306958101930"));
        }

        P2() {
            this.x0 = BigInteger.ZERO;
            this.x1 = BigInteger.ZERO;
            this.y0 = BigInteger.ZERO;
            this.y1 = BigInteger.ZERO;
        }
        
        P2(BigInteger x0, BigInteger x1, BigInteger y0, BigInteger y1) {
            this.x0 = x0;
            this.x1 = x1;
            this.y0 = y0;
            this.y1 = y1;
        }

        P2(byte[] data) {
            if (data.length != 4 * BASE_FIELD_SIZE) {
                throw new IllegalArgumentException("P2(byte[] data): invalid data layout!");
            }
            this.x0 = new BigInteger(Arrays.copyOfRange(data, 0, BASE_FIELD_SIZE));
            this.x1 = new BigInteger(Arrays.copyOfRange(data, BASE_FIELD_SIZE, 2 * BASE_FIELD_SIZE));
            this.y0 = new BigInteger(Arrays.copyOfRange(data, 2 * BASE_FIELD_SIZE, 3 * BASE_FIELD_SIZE));
            this.y1 = new BigInteger(Arrays.copyOfRange(data, 3 * BASE_FIELD_SIZE, 4 * BASE_FIELD_SIZE));
        }

        public byte[] bytes() {
            byte[] buf = new byte[4 * BASE_FIELD_SIZE];
            byte[] x0b = this.x0.toByteArray();
            byte[] x1b = this.x1.toByteArray();
            byte[] y0b = this.y0.toByteArray();
            byte[] y1b = this.y1.toByteArray();
            System.arraycopy(x0b, 0, buf, BASE_FIELD_SIZE - x0b.length, x0b.length);
            System.arraycopy(x1b, 0, buf, 2 * BASE_FIELD_SIZE - x1b.length, x1b.length);
            System.arraycopy(y0b, 0, buf, 3 * BASE_FIELD_SIZE - y0b.length, y0b.length);
            System.arraycopy(y1b, 0, buf, 4 * BASE_FIELD_SIZE - y1b.length, y1b.length);
            return buf;
        }

        public boolean equals(P2 p) {
            return this.x0.equals(p.x0) && this.x1.equals(p.x1) && this.y0.equals(p.y0) && this.y1.equals(p.y1);
        }

        public P2 neg() {
            P2 zero = new P2();
            return this.equals(zero) ? zero
                    : new P2(this.x0, this.x1, BASE_FIELD.subtract(this.y0.mod(BASE_FIELD)),
                            BASE_FIELD.subtract(this.y1.mod(BASE_FIELD)));
        }

        public P2 add(P2 other) {
            byte[] res = Context.ecAdd(g2CurveName, concat(this.bytes(), other.bytes()), false);
            return new P2(res);
        }

        public P2 scalarMul(BigInteger scalar) {
            byte[] res = Context.ecScalarMul(g2CurveName, scalar.toByteArray(), this.bytes(), false);
            return new P2(res);
        }

    }

    class Proof {
        P1 A;
        P2 B;
        P1 C;
    }

    class VerifyingKey {
        P1 alfa1;
        P2 beta2;
        P2 gamma2;
        P2 delta2;
        P1[] IC;
    }

    public VerifyingKey verifyingKey() {
        VerifyingKey vk = new VerifyingKey();

        vk.alfa1 = new P1(
                new BigInteger("12596912544348948381177599699953615278020976179029265987408871732383665336853"),
                new BigInteger("20971415676300623855464115183417853499748935771885198372657976671608356049664"));

        vk.beta2 = new P2(
                new BigInteger("9495569972937442844508456196632146912646524494643964509354343676750642761765"),
                new BigInteger("10132426612553504810764891613446869754967553196525874898634509363113975775041"),
                new BigInteger("6403943446711432300991299638684217597269944346723554544491310779182898761178"),
                new BigInteger("10944123948010144085478681989953063447760672121571281306698699327435453874900"));

        vk.gamma2 = new P2(
                new BigInteger("11559732032986387107991004021392285783925812861821192530917403151452391805634"),
                new BigInteger("10857046999023057135944570762232829481370756359578518086990519993285655852781"),
                new BigInteger("4082367875863433681332203403145435568316851327593401208105741076214120093531"),
                new BigInteger("8495653923123431417604973247489272438418190587263600148770280649306958101930"));

        vk.delta2 = new P2(
                new BigInteger("21799340242586924495479468657822269391859656730964044907844789668905036656546"),
                new BigInteger("4524015714112901377197964195547958988378269569824473107614452051098127615003"),
                new BigInteger("13391886395034990724285657348628162869586946663758062881665703468692496158445"),
                new BigInteger("15006896737345366088129674481232742137865640932055679832940850595272564243339"));

        vk.IC = new P1[] {
                new P1( 
                        new BigInteger("2046783015712313852155433002062002668835660095507840393667199357813533143088"),
                        new BigInteger("16729581745080300084280423530901388375382095783882516086191629761025057939839")),
                new P1( 
                        new BigInteger("6858916636740191812251104527190914202248766460857630202331101711385341271693"),
                        new BigInteger("17766339349720656107004941379731911275001708147287015830394733219521402198029")),
        };

        return vk;
    }

    public boolean verify(BigInteger[] input, Proof proof) {
        VerifyingKey vk = verifyingKey();
        Context.require(input.length + 1 == vk.IC.length, "verifier-bad-input");
        // Compute the linear combination vk_x
        P1 vk_x = new P1();
        for (int i = 0; i < input.length; i++) {
            Context.require(input[i].compareTo(SCALAR_FIELD) < 0, "verifier-gte-snark-scalar-field");
            vk_x = vk_x.add(vk.IC[i + 1].scalarMul(input[i]));
        }
        vk_x = vk_x.add(vk.IC[0]);
        byte[] data = concat(
                proof.A.neg().bytes(),
                proof.B.bytes(),
                vk.alfa1.bytes(),
                vk.beta2.bytes(),
                vk_x.bytes(),
                vk.gamma2.bytes(),
                proof.C.bytes(),
                vk.delta2.bytes());
        return Context.ecPairingCheck(curveName, data, false);
    }

    @External(readonly = true)
    public boolean verifyProof(BigInteger[] a, BigInteger[][] b, BigInteger[] c, BigInteger[] input) {
        Proof proof = new Proof();
        proof.A = new P1(a[0], a[1]);
        proof.B = new P2(b[0][0], b[0][1], b[1][0], b[1][1]);
        proof.C = new P1(c[0], c[1]);
        return verify(input, proof);
    }

}