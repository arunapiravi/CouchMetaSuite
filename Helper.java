import src.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import org.codehaus.jettison.json.JSONException;

import com.couchbase.client.CouchbaseConnectionFactoryBuilder;
import com.couchbase.client.CouchbaseMetaClient;


public class Helper {

    private static String[] _sourceNodes = {};
    private static String _sourcePort = "";
    private static String[] _destinationNodes = {};
    private static String _destinationPort = "";
    private static String _bucketName = "default";
    private static String _bucketPasswd = "";
    private static String _prefix1 = "";
    private static String _prefix2 = "";

    public static void main(String args[]) throws MalformedURLException, IOException, JSONException, InterruptedException, ExecutionException {

        Stronghold sh = new Stronghold();

        try {
            File file = new File("test.properties");
            FileInputStream fileInput = new FileInputStream(file);
            Properties properties = new Properties();
            properties.load(fileInput);
            fileInput.close();

            parse_input(properties, sh);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(" --> \n Cluster Setup pending automation, \n Please setup clusters, buckets, replications ." +
                "\n Once DONE, hit ENTER to continue ..\n <--\n");
        Scanner scan = new Scanner(System.in);
        scan.nextLine();

        /*
        // Creating a bucket and stuff at source and destination
        ClusterSetup._createBucket(sh, _sourceNodes[0], _sourcePort, _bucketName, _bucketPasswd);
        Thread.sleep(3000);
        ClusterSetup._setupCluster(_sourceNodes, _sourcePort);
        ClusterSetup._waitforrebalance(_sourceNodes[0], _sourcePort);

        ClusterSetup._createBucket(sh, _destinationNodes[0], _destinationPort, _bucketName, _bucketPasswd);
        Thread.sleep(3000);
        ClusterSetup._setupCluster(_destinationNodes, _destinationPort);
        ClusterSetup._waitforrebalance(_destinationNodes[0], _destinationPort);

        Thread.sleep(10000);
        ClusterSetup._setupReplication(_sourceNodes[0], _destinationNodes[0], _sourcePort, _bucketName);
        */

        // Connection to source's and destination's bucket
        final CouchbaseMetaClient source_client = connect(_sourceNodes[0], _sourcePort);
        final CouchbaseMetaClient destination_client = connect(_destinationNodes[0], _destinationPort);

        final Stronghold stho = sh;

        if (sh.getReplicationFlag()) {
            System.out.println("\n--> OperationsWithMetas sent once all the OperationReturnMetas are complete.");
        } else {
            System.out.println("\n--> OperationsWithMetas sent immediately after every OperationReturnMeta completes.");
        }

        if (!sh.getparallel()) {
            // Operation that setrm's on source, and setwithMeta's on destination with the meta from setrm
            System.out.println("Front end on source ..");
            System.out.println(">> Launching Sets .. ( " + sh.getItemcount() + " items )");
            Setrunner.sets(sh, source_client, destination_client, _prefix1);
            System.out.println(">> Completed Sets ..");
            Thread.sleep(5000);

            if (sh.getbiXDCR()) {
                System.out.println("biXDCR: Front end on destination ..");
                System.out.println(">> Launching Sets .. ( " + sh.getItemcount() + " items )");
                Setrunner.sets(sh, destination_client, source_client, _prefix2);
                System.out.println(">> Completed Sets ..");
                Thread.sleep(5000);
            }

            // Operation that delrm's on source, and delwithMeta's on destination with the meta from delrm
            System.out.println("Front end on source ..");
            System.out.println(">> Launching Deletes .. ( " + Math.round(sh.getItemcount() * sh.getDelRatio()) + " items )");
            Delrunner.dels(sh, source_client, destination_client, _prefix1);
            System.out.println(">> Completed Deletes ..");
            Thread.sleep(5000);

            if (sh.getbiXDCR()) {
                System.out.println("biXDCR: Front end on destination ..");
                System.out.println(">> Launching Deletes .. ( " + Math.round(sh.getItemcount() * sh.getDelRatio()) + " items )");
                Delrunner.dels(sh, destination_client, source_client, _prefix2);
                System.out.println(">> Completed Deletes ..");
                Thread.sleep(5000);
            }

            // Operation that addrm's on source, and addwithMeta's on destination with the meta from addrm
            System.out.println("Front end on source ..");
            System.out.println(">> Launching Adds .. ( " + sh.getAddCount() + " items )");
            Addrunner.adds(sh, source_client, destination_client, _prefix1);
            System.out.println(">> Completed Adds ..");
            Thread.sleep(5000);

            if (sh.getbiXDCR()) {
                System.out.println("biXDCR: Front end on destination ..");
                System.out.println(">> Launching Adds .. ( " + sh.getAddCount() + " items )");
                Addrunner.adds(sh, destination_client, source_client, _prefix2);
                System.out.println(">> Completed Adds ..");
                Thread.sleep(5000);
            }

            // Operation that updrm's on source, and setwithMeta's on destination with the meta from updrm
            //System.out.println("Front end on source ..");
            //System.out.println(">> Launching Updates .. ( " + Math.round((sh.getItemcount() + sh.getAddCount()) * sh.getUpdRatio()) + " items )");
            //Updrunner.upds(sh, source_client, destination_client, _prefix1);
            //System.out.println(">> Completed Updates ..");
            //Thread.sleep(5000);

            if (sh.getbiXDCR()) {
                System.out.println("biXDCR: Front end on destination ..");
                System.out.println(">> Launching Updates .. ( " + Math.round((sh.getItemcount() + sh.getAddCount()) * sh.getUpdRatio()) + " items )");
                Updrunner.upds(sh, destination_client, source_client, _prefix2);
                System.out.println(">> Completed Updates ..");
                Thread.sleep(5000);
            }

        } else {

            // Source control
            Runnable _source_control_ = new Runnable() {
                public void run() {
                    try {
                        // Operation that setrm's on source, and setwithMeta's on destination with the meta from setrm
                        System.out.println("Front end on source ..\n>> Launching Sets .. ( " + stho.getItemcount() + " items )");
                        Setrunner.sets(stho, source_client, destination_client, _prefix1);
                        System.out.println(">> Completed Sets on source..");
                        Thread.sleep(5000);
                        // Operation that delrm's on source, and delwithMeta's on destination with the meta from delrm
                        System.out.println("Front end on source ..\n>> Launching Deletes .. ( " + Math.round(stho.getItemcount() * stho.getDelRatio()) + " items )");
                        Delrunner.dels(stho, source_client, destination_client, _prefix1);
                        System.out.println(">> Completed Deletes on source..");
                        Thread.sleep(5000);
                        // Operation that addrm's on source, and addwithMeta's on destination with the meta from addrm
                        System.out.println("Front end on source ..\n>> Launching Adds .. ( " + stho.getAddCount() + " items )");
                        Addrunner.adds(stho, source_client, destination_client, _prefix1);
                        System.out.println(">> Completed Adds on source..");
                        Thread.sleep(5000);
                        // Operation that updrm's on source, and setwithMeta's on destination with the meta from updrm
                        //System.out.println("Front end on source ..\n>> Launching Updates .. ( " + Math.round((stho.getItemcount() + stho.getAddCount()) * stho.getUpdRatio()) + " items )");
                       // Updrunner.upds(stho, source_client, destination_client, _prefix1);
                        //System.out.println(">> Completed Updates on source..");
                        //Thread.sleep(5000);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            //Destination control
            Runnable _destination_control_ = new Runnable() {
                public void run() {
                    try {
                        System.out.println("biXDCR: Front end on destination ..\n>> Launching Sets .. ( " + stho.getItemcount() + " items )");
                        Setrunner.sets(stho, destination_client, source_client, _prefix2);
                        System.out.println(">> Completed Sets on destination..");
                        Thread.sleep(5000);
                        System.out.println("biXDCR: Front end on destination ..\n>> Launching Deletes .. ( " + Math.round(stho.getItemcount() * stho.getDelRatio()) + " items )");
                        Delrunner.dels(stho, destination_client, source_client, _prefix2);
                        System.out.println(">> Completed Deletes on destination..");
                        Thread.sleep(5000);
                        System.out.println("biXDCR: Front end on destination ..\n>> Launching Adds .. ( " + stho.getAddCount() + " items )");
                        Addrunner.adds(stho, destination_client, source_client, _prefix2);
                        System.out.println(">> Completed Adds on destination..");
                        Thread.sleep(5000);
                        System.out.println("biXDCR: Front end on destination ..>> Launching Updates .. ( " + Math.round((stho.getItemcount() + stho.getAddCount()) * stho.getUpdRatio()) + " items )");
                        Updrunner.upds(stho, destination_client, source_client, _prefix2);
                        System.out.println(">> Completed Updates on destination..");
                        Thread.sleep(5000);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            Thread _srcThread = new Thread(_source_control_);
            Thread _destThread = new Thread(_destination_control_);

            _srcThread.start();
            if (sh.getbiXDCR()) {
                _destThread.start();
            }
            _srcThread.join();
            if (sh.getbiXDCR()) {
                _destThread.join();
            }

        }

        // VERIFICATION
        if (sh.getdoVerify()) {
            System.out.println("Starting the verification stage .. ");
            Verification.comparison(sh, source_client, destination_client);
            if (sh.getbiXDCR()) {
                Verification.comparison(sh, destination_client, source_client);
            }
        } else
            System.out.println("Skipping the verification stage\n");

        System.exit(0);
    }

    private static CouchbaseMetaClient connect(String _addr, String _port) {
        /*
         * CouchbaseMetaClient Connection to bucket at the specified server
         */
        List<URI> uris = new LinkedList<URI>();
        uris.add(URI.create(String.format("http://" + _addr + ":" + _port + "/pools")));
        CouchbaseConnectionFactoryBuilder cfb = new CouchbaseConnectionFactoryBuilder();
        try {
            return new CouchbaseMetaClient(cfb.buildCouchbaseConnection(uris, _bucketName, _bucketPasswd));
        } catch (Exception e) {
            System.err.println("Error connecting to Couchbase: "
                    + e.getMessage());
            System.exit(0);
        }
        return null;
    }

    private static void parse_input(Properties properties, Stronghold sh) {
        /*
         * Read test variables from test.properties file
         */
        Enumeration<Object> enuKeys = properties.keys();
        while(enuKeys.hasMoreElements()){
            String key = (String) enuKeys.nextElement();
            if (key.equals("source"))
                _sourceNodes = properties.getProperty(key).split(",");
            if (key.equals("source-port"))
                _sourcePort = properties.getProperty(key);
            if (key.equals("destination"))
                _destinationNodes = properties.getProperty(key).split(",");
            if (key.equals("destination-port"))
                _destinationPort = properties.getProperty(key);
            if (key.equals("bucket-name"))
                _bucketName = properties.getProperty(key);
            if (key.equals("bucket-password"))
                _bucketPasswd = properties.getProperty(key);
            if (key.equals("prefix1"))
                _prefix1 = properties.getProperty(key);
            if (key.equals("prefix2"))
                _prefix2 = properties.getProperty(key);

            if (key.equals("bucket-memQuota"))
                sh.setMemquota(Integer.parseInt(properties.getProperty(key)));
            if (key.equals("json"))
                sh.setJson(Boolean.parseBoolean(properties.getProperty(key)));
            if (key.equals("item-count"))
                sh.setItemcount(Integer.parseInt(properties.getProperty(key)));
            if (key.equals("item-size"))
                sh.setSize(Integer.parseInt(properties.getProperty(key)));
            if (key.equals("exp-ratio"))
                sh.setExpRatio(Float.parseFloat(properties.getProperty(key)));
            if (key.equals("expiration-time"))
                sh.setExpiration(Integer.parseInt(properties.getProperty(key)));
            if (key.equals("del-ratio"))
                sh.setDelRatio(Float.parseFloat(properties.getProperty(key)));
            if (key.equals("add-count"))
                sh.setAddCount(Integer.parseInt(properties.getProperty(key)));
            if (key.equals("upd-ratio"))
                sh.setUpdRatio(Float.parseFloat(properties.getProperty(key)));
            if (key.equals("replication-starts-first"))
                sh.setReplicationFlag(Boolean.parseBoolean(properties.getProperty(key)));
            if (key.equals("biXDCR"))
                sh.setbiXDCR(Boolean.parseBoolean(properties.getProperty(key)));
            if (key.equals("parallelFrontEnds"))
                sh.setparallel(Boolean.parseBoolean(properties.getProperty(key)));
            if (key.equals("doVerify"))
                sh.setdoVerify(Boolean.parseBoolean(properties.getProperty(key)));
            if (key.equals("write-data-to-file"))
                sh.setaboutwrite(Boolean.parseBoolean(properties.getProperty(key)));
        }

    }
}
