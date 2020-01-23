#!/bin/sh

helm upgrade cmc-claim-store-pr-1390 cmc-claim-store \
     -f cmc-claim-store/values.yaml \
     -f cmc-claim-store/values.pr1390.yaml \
     --install --wait --timeout 600s \
     --set global.subscriptionId=1c4f0704-a29e-403d-b719-b9 \
     --namespace money-claims
