package si.xlab.research.emmy.demo.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import compatibility.CACertificate;
import compatibility.CACertificateEC;
import compatibility.Compatibility;
import compatibility.Connection;
import compatibility.ConnectionConfig;
import compatibility.Credential;
import compatibility.CredentialEC;
import compatibility.ECGroupElement;
import compatibility.Logger;
import compatibility.OrgPubKeys;
import compatibility.OrgPubKeysEC;
import compatibility.Pseudonym;
import compatibility.PseudonymEC;
import compatibility.PseudonymsysCAClient;
import compatibility.PseudonymsysCAClientEC;
import compatibility.PseudonymsysClient;
import compatibility.PseudonymsysClientEC;
import compatibility.ServiceInfo;
import si.xlab.research.emmy.demo.R;


public class MainActivity extends AppCompatActivity {

    private static final String verifierURL = "<emmyServerIp>:7007";
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

        cfg = new ConnectionConfig(verifierURL, "localhost", caCert.getBytes());
        try {
            Logger debugLogger = new Logger(Compatibility.DEBUG);
            Compatibility.setLogger(debugLogger);

            conn = new Connection(cfg);
        } catch (Exception e) {
            Log.d("getConnection", "Error getting connection");
            e.printStackTrace();
        }

        findViewById(R.id.button_example_psys).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                introduceVerifier();
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

    void introduceVerifier() throws Exception {
        ServiceInfo info = Compatibility.getServiceInfo(conn);
        Log.i("introduceVerifier", info.getName() + " " + info.getDescription() + " " + info.getProvider());
    }

    void doPseudonymsys() throws Exception {
        // Generate master secret and master pseudonym
        PseudonymsysClient org1 = new PseudonymsysClient(conn);
        String secret = org1.generateMasterKey();

        // Get a CA certificate
        PseudonymsysCAClient ca = new PseudonymsysCAClient(conn);
        Pseudonym masterNym = ca.generateMasterNym(secret);
        CACertificate caCertA = ca.generateCertificate(secret, masterNym);
        Pseudonym nym = org1.generateNym(secret, caCertA, "mock");

        // Register with org1 and obtain credentials for logging in with org1
        String orgH1 = "11253748020267515701977135421640400742511414782332660443524776235731592618314865082641495270379529602832564697632543178140373575666207325449816651443326295587329200580969897900340682863137274403743213121482058992744156278265298975875832815615008349379091580640663544863825594755871212120449589876097254391036951735135790415340694042060640287135597503154554767593490141558733646631257590898412097094878970047567251318564175378758713497120310233239160479122314980866111775954564694480706227862890375180173977176588970220883117212300621045744043530072238840577201003052170999723878986905807102656657527667244456412473985";
        String orgH2 = "76168773256070905782197510623595125058465077612447809025568517977679494145178174622864958684725961070073576803345724904501942931513809178875449022568661712955904784104680061168715431907736821341951579763867969478146743783132963349845621343504647834967006527983684679901491401571352045358450346417143743546169924539113192750473927517206655311791719866371386836092309758541857984471638917674114075906273800379335165008797874367104743232737728633294061064784890416168238586934819945486226202990710177343797354424869474259809902990704930592533690341526792158132580375587182781640673464871125845158432761445006356929132";
        OrgPubKeys orgPubKeys = new OrgPubKeys(orgH1, orgH2);
        Credential credential = org1.obtainCredential(secret, nym, orgPubKeys);
        Log.i("doPseudonymSys", "Obtained Anonymous Credential for authentication with organization A " + credential.toString());

        // Transfer credentials of org1 to org2
        PseudonymsysClient org2 = new PseudonymsysClient(conn);
        String sessionKey = org2.transferCredential("org1", secret, nym, credential);
        Log.i("doPseudonymSys", "Transferred Anonymous Credential for authentication with organization A, authenticated with organization B: " + sessionKey);
    }

    void doPseudonymsysEC() throws Exception {
        Log.d("doPseudonymSysEC", "Starting...");

        // Only this curve will work by default, because it is the one used by emmy server
        long curve = Compatibility.P256;

        // Generate master secret and master PseudonymsysClientEC
        PseudonymsysClientEC org1 = new PseudonymsysClientEC(conn, curve);
        String secret = org1.generateMasterKey();

        // Get a CA certificate
        PseudonymsysCAClientEC ca = new PseudonymsysCAClientEC(conn, curve);
        PseudonymEC masterNym = ca.generateMasterNym(secret, curve);
        CACertificateEC cert = ca.generateCertificate(secret, masterNym);

        // Register with org1 and obtain credentials for logging in with org1
        PseudonymEC nym = org1.generateNym(secret, cert, "mock");
        ECGroupElement a = new ECGroupElement("111843344654618029419055700569023289100199029635186896671499163057944727230", "63726701293868334061084235330967878003056898720773299094696019482924813137111");
        ECGroupElement b = new ECGroupElement("3836882559946612606724713122432195411371871189052450829349314418954131635804", "87187568403836989661029612226711448246955830180833597642485083706252921915098");
        OrgPubKeysEC orgPubKeys = new OrgPubKeysEC(a, b);
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
