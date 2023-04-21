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
                new BigInteger("14427904526692955994135266148105199547295021354486923775128949875071160625652"),
                new BigInteger("373855976796049236433883987904534948948811045245608573695344175932367347171"));

        vk.beta2 = new P2(
                new BigInteger("3000807251306673372158526433175426219099277663646573865840671816109598336909"),
                new BigInteger("14416384699578667833845019555137076393182445438960563541322908752399790097927"),
                new BigInteger("7385433749112508043928220809275981375973702566850879661634842222945310994578"),
                new BigInteger("19121391068013639153040244991792809570355743213616513836245712914655366351396"));

        vk.gamma2 = new P2(
                new BigInteger("11559732032986387107991004021392285783925812861821192530917403151452391805634"),
                new BigInteger("10857046999023057135944570762232829481370756359578518086990519993285655852781"),
                new BigInteger("4082367875863433681332203403145435568316851327593401208105741076214120093531"),
                new BigInteger("8495653923123431417604973247489272438418190587263600148770280649306958101930"));

        vk.delta2 = new P2(
                new BigInteger("8904182482202439118421492539363069315943627397535320011776206280670563800440"),
                new BigInteger("4889667985066502390112153789187819268768743818553115806542370218402250098351"),
                new BigInteger("12934548675128638183548778599048113669988901959427832502196098880996020804343"),
                new BigInteger("19086793658928098009716377620126382932447591411527548076733208915340840713910"));

        vk.IC = new P1[] {
                new P1( 
                        new BigInteger("18781640236477944752114115085336643421314987820313108966006876497113657065319"),
                        new BigInteger("9914532269582715715664096261023379157739256317652462928279267557617996909293")),
                new P1( 
                        new BigInteger("2583981020790058471114071110241384614698409664832101336789091330444629232928"),
                        new BigInteger("1562280625446668672327032821854886972722955538967630004248666319407100606740")),
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