BUYER_1="$1"
BUYER_2="$2"
BUYER_3="$3"
curl -X POST \
  http://localhost:8080/shop \
  -H 'Content-Type: application/json' \
  -d '{ "buyerIdentifier": "'"$BUYER_1"'", "items": [ { "productIdentifier": "123456789", "amount": "10", "price": "1000" } ] }'
curl -X POST \
  http://localhost:8080/shop \
  -H 'Content-Type: application/json' \
  -d '{ "buyerIdentifier": "'"$BUYER_2"'", "items": [ { "productIdentifier": "123456789", "amount": "12", "price": "1000" } ] }'
curl -X POST \
  http://localhost:8080/shop \
  -H 'Content-Type: application/json' \
  -d '{ "buyerIdentifier": "'"$BUYER_3"'", "items": [ { "productIdentifier": "123456789", "amount": "13", "price": "1000" } ] }'
