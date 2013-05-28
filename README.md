COUCHBASE_META_OPERATIONS implementation:

    Specify cluster-load details in test.properties
                               
    source/destination:             ...source/destination-node(s), seperated by ","
    source-port/destination-port:   ...couchbase port
    bucket-name:                    ...name of bucket
    bucket-password:                ...bucket's password
    bucket-memQuota:                ...bucket's memQuota (automation not yet) 
    json=false:                     ...doesn't spawn JSON values
    item-count:                     ...no. of sets
    item-size:                      ...size of each item (approximation) 
    prefix:                         ...prefix of generated keys 
    exp-ratio:                      ...ratio of item-count to be expired 
    expiration-time:                ...expirtaion time
    del-ratio:                      ...ratio of item-count to be deleted (disjoint with exp-ratio)
    add-count:                      ...items to add
    upd-ratio:                      ...ratio of item-count + add-count to be updated (with expiration time)
    replication-starts-first:       ...false => setm immediately after setrm
                                       true => setm's run after all setrm's + timed_wait (10s)
    biXDCR=false:			        ...set to true if biXDCR replication and front end needed on
    				                   destination as well
    parallelFrontEnds=false:	    ...leave at false for now, to_be_implemented: Front end to
    				                   run parallely on source and destination if set to true
    doVerify=true:                  ...to run or to not run verification module
    write-date-to-file=true:        ...to write or to not write all data content to log files in 
                                       verification module

ORDER OF EXECUTION:

    - Program prompts user to setup cluster, the bucket, replication
    - All the sets
    - All the deletes
    - All the adds
    - Verification (if flag set to true)

NOTE:

    - Multiple instances of this project can be used to run against multiple buckets

COMPILE / EXECUTION Instructions:

    To compile:
    make compile

    To execute:
    make run

    To remove .class files:
    make clean

    To remove log.txt files:
    make removelogs

FILES:

    Helper                          ...Parent class that orchestrates all the operations
    Setrunner                       ...Class that runs sets, be it with expires or not, will populate
                                       hashtables used in verification
    Addrunner                       ...Class that runs adds, be it with expires or not, will populate
                                       hashtables used in verification
    Delrunner                       ...Class that runs deletes, will populate hashtables used in 
                                       verification, del-ratio works on item-count for now
    Verification                    ...To be implemented, to retrieve MetaData of all items from
                                       destination, repopulate destination's hashtable, and compare
                                       the source's and destination's hashtables
    
    Stronghold                      ...Stores attributes retrieved from test.properties, hashtables
                                       used in verification declared here
    Hashstructure                   ...Hashtables (used in verification) will have key as the item's
                                       key, and value as an instance of this class (which contains 
                                       the document and metadata)
    DelayedOps                      ...In case of replication-starts-first flag set to true, an arraylist
                                       with data being an instance of this class (which contains key, 
                                       value, metadata) is populated for setWithMeta commands to run once
                                       all the setReturnMetas complete + a timeout threshold of 10s
    
    Spawner                         ...Creates JSON documents if json set to true in test.properties
    ClusterSetup                    ...To setup the cluster based on specifications (NOT IN USE)
