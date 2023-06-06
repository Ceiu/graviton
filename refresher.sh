#!/bin/bash

curl -k -X POST -H 'content-type:application/json' -d @org_block-0.json "http://localhost:8080/poc/sync/orgs" &
curl -k -X POST -H 'content-type:application/json' -d @org_block-1.json "http://localhost:8080/poc/sync/orgs" &
curl -k -X POST -H 'content-type:application/json' -d @org_block-2.json "http://localhost:8080/poc/sync/orgs" &
curl -k -X POST -H 'content-type:application/json' -d @org_block-3.json "http://localhost:8080/poc/sync/orgs" &
curl -k -X POST -H 'content-type:application/json' -d @org_block-4.json "http://localhost:8080/poc/sync/orgs" &
curl -k -X POST -H 'content-type:application/json' -d @org_block-5.json "http://localhost:8080/poc/sync/orgs" &
curl -k -X POST -H 'content-type:application/json' -d @org_block-6.json "http://localhost:8080/poc/sync/orgs" &
curl -k -X POST -H 'content-type:application/json' -d @org_block-7.json "http://localhost:8080/poc/sync/orgs" &
curl -k -X POST -H 'content-type:application/json' -d @org_block-8.json "http://localhost:8080/poc/sync/orgs" &
curl -k -X POST -H 'content-type:application/json' -d @org_block-9.json "http://localhost:8080/poc/sync/orgs" &
wait
