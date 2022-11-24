#!/bin/sh

# 1. Make sure that you are logged in to Azure `az login`
# 2. Make sure that you are logged in to Confluent Cloud `confluent login`

# Azure configuration parameters
az_vault_name="kirill-kulikov-keyvault"
az_vault_apikey_name="confluent-cloud-api-key"
az_vault_apisecret_name="confluent-cloud-api-secret"

# Confluent Cloud configuration parameters
resource="lkc-57d13z"

timeback=10
today=$(date)
before="$(date -d "$today - $timeback hours" +%s)"

# counts a number of API keys which are near to expire
count=$(confluent api-key list --output json --resource $resource \
| jq "map(select(.created | fromdateiso8601 < $before)) | length")

if [ $count -gt 0 ];
then
    echo "creating a new API Key and Secret"
    json=$(confluent api-key create --resource "$resource" \
    --description "Metro $(date +%Y-%m-%d-%H-%M-%S)" --output json)

    echo $json | jq '.'

    key=$(echo $json | jq -r '.key')
    secret=$(echo $json | jq -r '.secret')

    az keyvault secret set --vault-name $az_vault_name --name $az_vault_apikey_name --value $key
    az keyvault secret set --vault-name $az_vault_name --name $az_vault_apisecret_name --value $secret
fi

# az account set -s a23ea812-1d8a-41f1-84b2-806938e8e2e6
# az group list --subscription a23ea812-1d8a-41f1-84b2-806938e8e2e6
# az keyvault list --resource-group kirill-kulikov-resourcegroup