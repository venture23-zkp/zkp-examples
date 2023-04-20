package io.venture23zkp.sudoku;

import score.Context;
import score.annotation.External;

import java.math.BigInteger;
import java.util.Arrays;


public class Sha256_Verifier {
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
                new BigInteger("3968728501640457921232254987451480667888539867302908288669570438819482869863198297825341808381278901479255995562335"),
                new BigInteger("2363863879228145167760414574993918770078838705722011931227532849138247488139420318937768503112187760323071740205482"));

        vk.beta2 = new P2(
                new BigInteger("2344073726606089984858510004602202127462227617691339526090290651642995130771195150001454327593039152317916462538609"),
                new BigInteger("1529755711149237722810997643702286152441471885902666026269900582024193193770021755274880618572329197068692622326796"),
                new BigInteger("3670724499152371027318044871773533668007858320534542988826243622707601093353613874999795953307807803379321618119189"),
                new BigInteger("1963261358996659663923577322489565243581087848586890564494700386092069785510810267743382798597606567125558627616796"));

        vk.gamma2 = new P2(
                new BigInteger("352701069587466618187139116011060144890029952792775240219908644239793785735715026873347600343865175952761926303160"),
                new BigInteger("3059144344244213709971259814753781636986470325476647558659373206291635324768958432433509563104347017837885763365758"),
                new BigInteger("1985150602287291935568054521177171638300868978215655730859378665066344726373823718423869104263333984641494340347905"),
                new BigInteger("927553665492332455747201965776037880757740193453592970025027978793976877002675564980949289727957565575433344219582"));

        vk.delta2 = new P2(
                new BigInteger("525416796144848053827116445144668461845950111391886519628189908495610878533646512330625958705889136575005973151128"),
                new BigInteger("884131884441588396541022663535828964792250925124831599941842162741429111341519116596801358943151182733959482101601"),
                new BigInteger("2572663853983945662108867609585405553429257942759723123690392608089343713719497263230874578874604986261065073750413"),
                new BigInteger("587051576656941144188730451817453908354650965858045507608987913400531607946928621829003767848079108588153868010706"));

        vk.IC = new P1[] {
                new P1( 
                        new BigInteger("1697144801492969540507504067744444423209723950530130223079257651500834720012080291097668234038401478969758494723962"),
                        new BigInteger("1853080674185963120904752773318086208367699463031269789508179734238703560002881450034898540987950476365433804463773")),
                new P1( 
                        new BigInteger("803583882184453002986826315938922750852802081237213304499314217412263836675965640992231715078860127898408388356364"),
                        new BigInteger("2537101071475535387262184231532169362914012168197773303836593367208799945694715919639212858069063632585097369498015")),
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