COUCHBASE_META_OPERATION implementation

Specify cluster-load details in test.properties

source/destination:             ...source-node(s), seperated by ","
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
    replication-starts-first:       ...false => setm immediaetly after setrm
true => setms run after all setrms + timed_wait (10s)

    ORDER OF EXECUTION:
    - All the sets
    - All the deletes
    - All the adds
- Verification (To be implemented)

    COMPILE / EXECUTION Instructions:

    To compile:
    make compile

    To execute:
    make run

    To clean Class files:
    make clean
