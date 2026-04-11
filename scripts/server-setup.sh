#!/usr/bin/env bash
# One-time setup script for the DigitalOcean droplet.
# Run as root: bash server-setup.sh
set -euo pipefail

echo "==> Installing Docker..."
curl -fsSL https://get.docker.com | sh

echo "==> Creating app directory..."
mkdir -p /opt/soccer-prediction/caddy

echo "==> Cloning Caddyfile and compose file..."
# These will be overwritten on first deploy, but we need them for initial setup
cat > /opt/soccer-prediction/caddy/Caddyfile << 'CADDYEOF'
{$DOMAIN} {
    reverse_proxy frontend:80
}
CADDYEOF

echo "==> Creating .env.production (fill in your values)..."
cat > /opt/soccer-prediction/.env.production << 'ENVEOF'
# Database
DB_USERNAME=postgres
DB_PASSWORD=CHANGE_ME

# Google OAuth
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
ADMIN_EMAIL=

# Domain
DOMAIN=
FRONTEND_URL=

# API-Football (optional)
API_FOOTBALL_KEY=
ENVEOF

echo ""
echo "==> Done! Next steps:"
echo "  1. Edit /opt/soccer-prediction/.env.production with your real values"
echo "  2. Copy docker-compose.prod.yml to /opt/soccer-prediction/"
echo "  3. Push to main — GitHub Actions will build, push, and deploy"
echo ""
echo "  Or for first manual deploy:"
echo "    cd /opt/soccer-prediction"
echo "    docker compose -f docker-compose.prod.yml --env-file .env.production up -d --build"
