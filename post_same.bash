#!/bin/bash

BUYER="$1"
ITERATIONS="$2"

for ((i=1; i<=$ITERATIONS; i++))
do
    curl -X POST \
      http://localhost:8080/shop \
      -H 'Content-Type: application/json' \
      -d '{ "buyerIdentifier": "'"$BUYER"'", "items": [ { "productIdentifier": "123456789", "amount": "10", "price": "1000" } ] }'
done

