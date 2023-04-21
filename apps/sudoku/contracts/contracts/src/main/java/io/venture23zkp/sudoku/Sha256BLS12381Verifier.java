package io.venture23zkp.sudoku;

import score.Context;
import score.annotation.External;

import java.math.BigInteger;
import java.util.Arrays;


public class Sha256BLS12381Verifier {
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
        // (x0 * i + x1, y0 * i + y1)
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
                new BigInteger("2547180133157117885214704588961084455094093381524377483084936273275465332726625452459023338008080417173310644220947"),
                new BigInteger("1988802296826419097428684579166378851312072066987426545358334494377313213758596831750534208004828906966029083392109"));

        vk.beta2 = new P2(
                new BigInteger("984927267370204712883148021305699652032464007685443489391771489420695179094661929409208580843803159710162601121029"),
                new BigInteger("3933650818499964720857180501912214303153673051326015740289059421053550519752951660642657620621932320725788971105126"),
                new BigInteger("2605073384601439913811225073145627607286110032095361326656632161882466626869215249994596305599995541740644250546127"),
                new BigInteger("3608861563337122655071571438005695859029250967854130027111758642353190490438904235501757366818543057955020925256026"));

        vk.gamma2 = new P2(
                new BigInteger("3059144344244213709971259814753781636986470325476647558659373206291635324768958432433509563104347017837885763365758"),
                new BigInteger("352701069587466618187139116011060144890029952792775240219908644239793785735715026873347600343865175952761926303160"),
                new BigInteger("927553665492332455747201965776037880757740193453592970025027978793976877002675564980949289727957565575433344219582"),
                new BigInteger("1985150602287291935568054521177171638300868978215655730859378665066344726373823718423869104263333984641494340347905"));

        vk.delta2 = new P2(
                new BigInteger("2022958382143437902616756125794459342883179787026251121588897724833506544146388539578566963665338678580562998694727"),
                new BigInteger("772166042053939727245238843716279366085135416182953493465799212379760139478164714620893120678098476646919644828908"),
                new BigInteger("2130130987025132459722442582616551179542601651280360347648818738025124076101279290981724527466727720890113032746506"),
                new BigInteger("351391559595101444840812972013845783050122823404625678005065762838964043169808719219597831451259145993204771660310"));

        vk.IC = new P1[] {
                new P1( 
                        new BigInteger("3546128514312378296913710687493224069990792615844164474076541518963573985088102705159028153904460075704200422164232"),
                        new BigInteger("2317671914564379858589276305321394655468435807929764472176374078918300206281009405880889716127110447825240680054738")),
                new P1( 
                        new BigInteger("1409887610359236842102235861642606660333817000425598609751966539883118900355144142935692682381972715938270574295967"),
                        new BigInteger("987296747544633464026365367475841620632955340386663146663758181176005384784017771703757510692934281735986652210560")),
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