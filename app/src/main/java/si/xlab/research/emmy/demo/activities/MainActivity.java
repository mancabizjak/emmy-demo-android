package si.xlab.research.emmy.demo.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.math.BigInteger;

import compat.Attr;
import compat.Attrs;
import compat.CACertificate;
import compat.CACertificateEC;
import compat.CLAttrs;
import compat.CLClient;
import compat.CLCred;
import compat.CLCredManager;
import compat.CLCredManagerState;
import compat.CLPubKey;
import compat.CLParams;
import compat.CLPublicParams;
import compat.Compat;
import compat.Connection;
import compat.ConnectionConfig;
import compat.Credential;
import compat.CredentialEC;
import compat.ECGroupElement;
import compat.Logger;
import compat.PubKey;
import compat.PubKeyEC;
import compat.Pseudonym;
import compat.PseudonymEC;
import compat.PseudonymsysCAClient;
import compat.PseudonymsysCAClientEC;
import compat.PseudonymsysClient;
import compat.PseudonymsysClientEC;
import compat.SchnorrGroup;
import compat.ServiceInfo;
import si.xlab.research.emmy.demo.R;

import static compat.Compat.restoreCLCredManager;


public class MainActivity extends Activity {

    private static final String verifierURL = "172.16.118.222:7007";
    private static final String caCert = "-----BEGIN CERTIFICATE-----\n" +
            "MIIDezCCAmOgAwIBAgIJALHmT2Ucq7LCMA0GCSqGSIb3DQEBCwUAMFQxCzAJBgNV\n" +
            "BAYTAlNJMREwDwYDVQQIDAhTbG92ZW5pYTENMAsGA1UECgwERW1teTEPMA0GA1UE\n" +
            "CwwGQ3J5cHRvMRIwEAYDVQQDDAlsb2NhbGhvc3QwHhcNMTcwOTIxMDg1ODQxWhcN\n" +
            "MjAwOTIwMDg1ODQxWjBUMQswCQYDVQQGEwJTSTERMA8GA1UECAwIU2xvdmVuaWEx\n" +
            "DTALBgNVBAoMBEVtbXkxDzANBgNVBAsMBkNyeXB0bzESMBAGA1UEAwwJbG9jYWxo\n" +
            "b3N0MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwVQjZWpJgSyHVuEt\n" +
            "v4pgRhMBWezl1+qbIWNbj97WJsIYjYsuXuLedRz87OXuuc3fO9T4bhDOO7W4O/Lj\n" +
            "OcQ8fYX9dtaI0FP5cIgmtHf11KILI3ZSDMgMeudi6P9zQCJpIuAAtwglR1RCokkF\n" +
            "/dgZaC13KHq948gyDrJcSJroRfkds74s8oPAV4+MjwtckRCAedn5fjYBpKDuofqZ\n" +
            "nzRF2T/s0Se9GfqaqaLqOlDURBtMDfWxDVX4DIC9zWcXmyI0ORzFFR7NpSWQF7GY\n" +
            "gYIradbrrutTTokkr65GOm6FqrDdZxeQRIE8d6BS06eNDF7Wi6mDvjxf7v2+rpYQ\n" +
            "2HD+pQIDAQABo1AwTjAdBgNVHQ4EFgQUAtl8LBF7jM90cdloph/U9sR7izgwHwYD\n" +
            "VR0jBBgwFoAUAtl8LBF7jM90cdloph/U9sR7izgwDAYDVR0TBAUwAwEB/zANBgkq\n" +
            "hkiG9w0BAQsFAAOCAQEAq9ySEguM1pSu3pBv6jBbyJQr2/cj4YkEqjg/1VjAOh5D\n" +
            "dK5aFN6DOBNtpXr5GicF7/vRAKs8ykzMtDypdwI41CvSXbEcgnqBV9jP1T7/UrCG\n" +
            "lS5t04oP0zukKOSYsLXYzQQZkxpWXvru0/Sg+bhf4qKUda1xtaZBTkdMqF1qsWIp\n" +
            "BaF1z+Ito+aCQaMyrmIu/2F80vxEhrHg5KC1LX514utLwX0L4ULw0yxN2hbV8S87\n" +
            "KbtT0/SxOErpQMF/LYDzpzWWBXg+BLioR5KidNc0wG0TDwRVfL4HxnaVBQprid42\n" +
            "schXZ5EEtIkYEUYo9gf7XK/5UGmXzIKaIfyMLkRIAg==\n" +
            "-----END CERTIFICATE-----";

    // Create a single instance of Connection and ConnectionConfig that will be re-used
    // for several clients
    private static ConnectionConfig cfg;
    private static Connection conn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cfg = new ConnectionConfig(verifierURL, "localhost", caCert.getBytes(), 500);
        try {
            Logger debugLogger = new Logger(Compat.DEBUG);
            Compat.setLogger(debugLogger);

            conn = new Connection(cfg);
            doCL();
        } catch (Exception e) {
            Toast.makeText(this, "Error establishing connection to emmy server", Toast.LENGTH_LONG).show();
            Log.e("getConnection", "Error getting connection");
            e.printStackTrace();
        }

        findViewById(R.id.button_example_cl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    doCL();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.button_example_psys).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                //introduceVerifier();
                doPseudonymsys();
            } catch (Exception e) {
                e.printStackTrace();
            }
            }
        });

        findViewById(R.id.button_example_psys_ec).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                doPseudonymsysEC();
            } catch (Exception e) {
                e.printStackTrace();
            }
            }
        });

    }

    void doCL() throws Exception {
        String TAG = "CL";

        if (conn != null) {
            Log.d(TAG, "Connection to emmy server is established, continuting...");
            // For communication with emmy server via gRPC
            CLClient c = new CLClient(conn);

            // ------------------------------------
            // Part 1 - obtain public configuration
            // ------------------------------------
            // server announces its public parameters, client retrieves them via RPC call
            CLPublicParams pp = c.getPublicParams();
            CLPubKey pubKey = pp.getPubKey(); // organization's public key
            CLParams schemeParams = pp.getConfig(); // crypto parameters for the scheme

            // ----------------------------------------------
            // Part 2 - derive & securely store master secret
            // ----------------------------------------------
            // This is done only once, before starting registration
            // The same secret is used for all subsequent communication with emmy server
            byte[] secret = pubKey.generateMasterSecret();
            // TODO store secret

            // ---------------------------------------------------------------------
            // Part 3 - fill in the attributes to be encoded in anonymous credential
            // ---------------------------------------------------------------------
            // TODO attributes here will be int or string type
            // TODO add StringAttr, BigIntAttr, IntAttr, etc...if required
            // Currently we only store big integer types passed accross the language boundary
            // as byte arrays
            Attr a1 = new Attr("time_from", new BigInteger("1234567790").toByteArray());
            Attr a2 = new Attr("time_to", new BigInteger("9999999999999").toByteArray());

            // Compose known attributes
            // TODO add check for length, to prevent panics bc. of misconfiguration
            Attrs known = new Attrs();
            known.add(a1);
            known.add(a2);

            CLAttrs attrs = new CLAttrs(known, null, null);


            // ---------------------------------------
            // Part 3 - instantiate credential manager
            // ---------------------------------------
            // Credential manager is used for all crypto protocols (issue credential in registration
            // and prove credential in validation)
            CLCredManager cm = new CLCredManager(schemeParams, pubKey, secret, attrs);

            // Registration (done once)
            Log.d(TAG, "Starting issue credential...");
            CLCred cred = c.issueCred(cm, "abc");
            Log.d(TAG, "Issue credential done.");

            // Validation (multiple times with the same secret, credential)
            Log.d(TAG, "Starting proof of credential...");
            String sessionKey = c.proveCred(cm, cred, known);
            Log.d(TAG, "Proof of credential done. [SessionKey] = " + sessionKey);

            Log.i(TAG, "Saving the state of credential manager");
            CLCredManagerState cmState = cm.getState();
            //TODO record the state in DB

            Log.i(TAG, "Restoring credential manager from a previous state");
            CLCredManager restored = restoreCLCredManager(cmState, secret, attrs);

            Log.d(TAG, "Starting proof of credential from the restored credential manager...");
            sessionKey = c.proveCred(cm, cred, known);
            Log.d(TAG, "Proof of credential done. [SessionKey] = " + sessionKey);
        } else {
            Log.e(TAG, "Connection could not be established");
        }
    }

    /*void introduceVerifier() throws Exception {
        ServiceInfo info = compat.getServiceInfo(conn);
        Log.i("introduceVerifier", info.getName() + " " + info.getDescription() + " " + info.getProvider());
    }*/

    void doPseudonymsys() throws Exception {
        // Construct Schnorr group
        String p = "16714772973240639959372252262788596420406994288943442724185217359247384753656472309049760952976644136858333233015922583099687128195321947212684779063190875332970679291085543110146729439665070418750765330192961290161474133279960593149307037455272278582955789954847238104228800942225108143276152223829168166008095539967222363070565697796008563529948374781419181195126018918350805639881625937503224895840081959848677868603567824611344898153185576740445411565094067875133968946677861528581074542082733743513314354002186235230287355796577107626422168586230066573268163712626444511811717579062108697723640288393001520781671";
        String g = "13435884250597730820988673213378477726569723275417649800394889054421903151074346851880546685189913185057745735207225301201852559405644051816872014272331570072588339952516472247887067226166870605704408444976351128304008060633104261817510492686675023829741899954314711345836179919335915048014505501663400445038922206852759960184725596503593479528001139942112019453197903890937374833630960726290426188275709258277826157649744326468681842975049888851018287222105796254410594654201885455104992968766625052811929321868035475972753772676518635683328238658266898993508045858598874318887564488464648635977972724303652243855656";
        String q = "98208916160055856584884864196345443685461747768186057136819930381973920107591";
        SchnorrGroup group = new SchnorrGroup(p, g, q);

        // Generate master secret and master pseudonym
        PseudonymsysClient org1 = new PseudonymsysClient(conn, group);
        String secret = org1.generateMasterKey();

        // Get a CA certificate
        PseudonymsysCAClient ca = new PseudonymsysCAClient(group);
        Pseudonym masterNym = ca.generateMasterNym(secret);
        CACertificate caCertA = ca.generateCertificate(secret, masterNym);
        Pseudonym nym = org1.generateNym(secret, caCertA, "mock");

        // Register with org1 and obtain credentials for logging in with org1
        String orgH1 = "11253748020267515701977135421640400742511414782332660443524776235731592618314865082641495270379529602832564697632543178140373575666207325449816651443326295587329200580969897900340682863137274403743213121482058992744156278265298975875832815615008349379091580640663544863825594755871212120449589876097254391036951735135790415340694042060640287135597503154554767593490141558733646631257590898412097094878970047567251318564175378758713497120310233239160479122314980866111775954564694480706227862890375180173977176588970220883117212300621045744043530072238840577201003052170999723878986905807102656657527667244456412473985";
        String orgH2 = "76168773256070905782197510623595125058465077612447809025568517977679494145178174622864958684725961070073576803345724904501942931513809178875449022568661712955904784104680061168715431907736821341951579763867969478146743783132963349845621343504647834967006527983684679901491401571352045358450346417143743546169924539113192750473927517206655311791719866371386836092309758541857984471638917674114075906273800379335165008797874367104743232737728633294061064784890416168238586934819945486226202990710177343797354424869474259809902990704930592533690341526792158132580375587182781640673464871125845158432761445006356929132";
        PubKey orgPubKeys = new PubKey(orgH1, orgH2);
        Credential credential = org1.obtainCredential(secret, nym, orgPubKeys);
        Log.i("doPseudonymSys", "Obtained Anonymous Credential for authentication with organization A " + credential.toString());

        // Transfer credentials of org1 to org2
        PseudonymsysClient org2 = new PseudonymsysClient(conn, group);
        String sessionKey = org2.transferCredential("org1", secret, nym, credential);
        Log.i("doPseudonymSys", "Transferred Anonymous Credential for authentication with organization A, authenticated with organization B: " + sessionKey);
    }

    void doPseudonymsysEC() throws Exception {
        Log.d("doPseudonymSysEC", "Starting...");

        // Only this curve will work by default, because it is the one used by emmy server
        long curve = Compat.P256;

        // Generate master secret and master PseudonymsysClientEC
        PseudonymsysClientEC org1 = new PseudonymsysClientEC(conn, curve);
        String secret = org1.generateMasterKey();

        // Get a CA certificate
        PseudonymsysCAClientEC ca = new PseudonymsysCAClientEC(curve);
        PseudonymEC masterNym = ca.generateMasterNym(secret, curve);
        CACertificateEC cert = ca.generateCertificate(secret, masterNym);

        // Register with org1 and obtain credentials for logging in with org1
        PseudonymEC nym = org1.generateNym(secret, cert, "mock");
        ECGroupElement a = new ECGroupElement("111843344654618029419055700569023289100199029635186896671499163057944727230", "63726701293868334061084235330967878003056898720773299094696019482924813137111");
        ECGroupElement b = new ECGroupElement("3836882559946612606724713122432195411371871189052450829349314418954131635804", "87187568403836989661029612226711448246955830180833597642485083706252921915098");
        PubKeyEC orgPubKeys = new PubKeyEC(a, b);
        CredentialEC credential = org1.obtainCredential(secret, nym, orgPubKeys);

        // Transfer credentials of org1 to org2
        PseudonymsysClientEC org2 = new PseudonymsysClientEC(conn, curve);
        String sessionKey = org2.transferCredential("org1", secret, nym, credential);
        Log.i("doPseudonymsysEC", "Transfer credential successful, got sessionKey" + sessionKey);
}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
