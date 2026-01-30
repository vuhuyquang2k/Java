#!/bin/bash
# ============================================
# Vault Initialization Script
# Run this after `docker-compose up -d vault`
# ============================================

set -e

export VAULT_ADDR='http://127.0.0.1:8200'
export VAULT_TOKEN='dev-root-token'

echo "🔐 Initializing Vault secrets..."

# Wait for Vault to be ready
until vault status > /dev/null 2>&1; do
    echo "⏳ Waiting for Vault to start..."
    sleep 2
done

echo "✅ Vault is ready!"

# Enable KV secrets engine v2
vault secrets enable -path=secret kv-v2 2>/dev/null || echo "ℹ️  KV secrets engine already enabled"

# Store application secrets
echo "📝 Storing secrets in Vault..."

vault kv put secret/base-project \
    database.password="" \
    google.client-id="${GOOGLE_CLIENT_ID:-your_google_client_id}" \
    google.client-secret="${GOOGLE_CLIENT_SECRET:-your_google_client_secret}"

echo ""
echo "✅ Vault initialization complete!"
echo ""
echo "📋 Stored secrets:"
vault kv get secret/base-project

echo ""
echo "🌐 Vault UI: http://localhost:8200"
echo "🔑 Token: dev-root-token"
