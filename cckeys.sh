#!/bin/sh

# 1. Make sure that you are logged in to Azure `az login`
# 2. Make sure that you are logged in to Confluent Cloud `confluent login`

# Azure configuration parameters
az_subscrition="a23ea812-1d8a-41f1-84b2-806938e8e2e6"
az_keyvault_name="kirill-kulikov-keyvault"
az_keyvault_apikey_name=$1 #"confluent-cloud-api-key"
az_keyvault_apisecret_name=$2 #"confluent-cloud-api-secret"

# Confluent Cloud configuration parameters
resource=$3 #"lkc-57d13z"

echo "creating a new API Key and Secret"
json=$(confluent api-key create --resource "$resource" \
--description "Metro $(date +%Y-%m-%d-%H-%M-%S)" --output json)

echo $json | jq '.'

key=$(echo $json | jq -r '.key')
secret=$(echo $json | jq -r '.secret')

az keyvault secret set --vault-name $az_keyvault_name \
--name $az_keyvault_apikey_name --value $key --subscription $az_subscrition

az keyvault secret set --vault-name $az_keyvault_name \
--name $az_keyvault_apisecret_name --value $secret --subscription $az_subscrition

# Script flow
# 1. Creates a new key/secret pair in Confluent Cloud
# 2. The script does not update/delete old keys/secrets in Confluent Cloud
# 3. So all applications still using the old key/secret can continue working without discruption
# 4. Finally, the script UPDATES the key/secret pair in Azure keyvault
# 5. So if you restart services they can pick up a new key/secret
# 6. P.S. at some moment all the services need to be restarted (to pick up new keys) and old keys can be deleted from Confluent Cloud