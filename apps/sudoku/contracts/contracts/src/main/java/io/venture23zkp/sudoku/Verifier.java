package io.venture23zkp.sudoku;

import score.Context;
import score.annotation.External;

import java.math.BigInteger;
import java.util.Arrays;


public class Verifier {
    static final String protocol = "groth16";
    static final String curve = "bls12381";
    static final int nPublic = 1;

    static final String curveName = "bls12-381";
    static final String g1CurveName = "bls12-381-g1";
    static final String g2CurveName = "bls12-381-g2";
    static final BigInteger BASE_FIELD = new BigInteger(
            "4002409555221667393417789825735904156556882819939007885332058136124031650490837864442687629129015664037894272559787");
    static final BigInteger SCALAR_FIELD = new BigInteger(
            "52435875175126190479447740508185965837690552500527637822603658699938581184513");
    static final int BASE_FIELD_SIZE = 48; // bytes
    

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
            return new P1(
                new BigInteger(
                        "3685416753713387016781088315183077757961620795782546409894578378688607592378376318836054947676345821548104185464507"),
                new BigInteger(
                        "1339506544944476473020471379941921221584933875938349620426543736416511423956333506472724655353366534992391756441569")
            );
        }

        P1() {
            this.x = new BigInteger(
                    "9850501549098619803069760025035903451269934817616361666987073351061430442874302652853566563721228910201656997576704");
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
        BigInteger x0;
        BigInteger x1;
        BigInteger y0;
        BigInteger y1;

        public P2 generator() {
            return new P2(
                    new BigInteger(
                            "352701069587466618187139116011060144890029952792775240219908644239793785735715026873347600343865175952761926303160"),
                    new BigInteger(
                            "3059144344244213709971259814753781636986470325476647558659373206291635324768958432433509563104347017837885763365758"),
                    new BigInteger(
                            "1985150602287291935568054521177171638300868978215655730859378665066344726373823718423869104263333984641494340347905"),
                    new BigInteger(
                            "927553665492332455747201965776037880757740193453592970025027978793976877002675564980949289727957565575433344219582"));
        }

        P2() {
            this.x0 = BigInteger.ZERO;
            this.x1 = new BigInteger(
                    "9850501549098619803069760025035903451269934817616361666987073351061430442874302652853566563721228910201656997576704");
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
                new BigInteger("3531543147828956906586800815302149414901280548985003660230334864598813281860958188389158354211966256079661604440046"),
                new BigInteger("2588096302190527861042063283636574665846068520973070260838314313408434582549416511442650798931017438804640762803807"));

        vk.beta2 = new P2(
                new BigInteger("1550141899850323401561812759167130346528401877189938753457691925315872247106669878733952315984175036287535628383091"),
                new BigInteger("934553146397916500960652759662336028395070722216642770170740154672299558999407843892594787757116335754719489873096"),
                new BigInteger("3758865137542005847852846624425630685100959421979229364780890624532097208512028323561907936321363711030625780331"),
                new BigInteger("2045685466806580984704235391102762779580463959782438007321493821651569005840330353597599430509889473576001152210610"));

        vk.gamma2 = new P2(
                new BigInteger("352701069587466618187139116011060144890029952792775240219908644239793785735715026873347600343865175952761926303160"),
                new BigInteger("3059144344244213709971259814753781636986470325476647558659373206291635324768958432433509563104347017837885763365758"),
                new BigInteger("1985150602287291935568054521177171638300868978215655730859378665066344726373823718423869104263333984641494340347905"),
                new BigInteger("927553665492332455747201965776037880757740193453592970025027978793976877002675564980949289727957565575433344219582"));

        vk.delta2 = new P2(
                new BigInteger("2159141419485858493955954684403974050177945694589093982056550717978847898550033454246724665056021183873330141359881"),
                new BigInteger("2392482241893825944512429783634780000115597534153075199578566047920763027366305095868398185200419907465327483755483"),
                new BigInteger("3383121952870232483666123086071919930789573676485292835514970532563136090315770107260658991737584365394871997115510"),
                new BigInteger("1879391342063180326273635542124648223239471138650677503241382292265302496477175222885775926060709007331394601133251"));

        vk.IC = new P1[] {
                new P1( 
                        new BigInteger("1523842727303251998743745091408002786215171030870232914013258054299295286729609816198208109982059310752570483922048"),
                        new BigInteger("2027712410081245772862262175445598303415155102507723921215897882262885915001174832194115285055732359615562750472617")),
                new P1( 
                        new BigInteger("1756548331855786335600573697014477358574390646264226500973524868105165134272861474386420013199724878953143649464115"),
                        new BigInteger("3538875227158591298752051888022063329414516630201394889729928991376938443595260199561103646085255775225465859695073")),
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
        proof.B = new P2(b[0][1], b[0][0], b[1][1], b[1][0]);
        proof.C = new P1(c[0], c[1]);
        return verify(input, proof);
    }

}