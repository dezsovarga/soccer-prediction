#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Load environment variables
if [ -f "$SCRIPT_DIR/.env" ]; then
  source "$SCRIPT_DIR/.env"
else
  echo "Warning: .env file not found. Copy .env.example to .env and fill in your values."
  exit 1
fi

cleanup() {
  echo ""
  echo "Shutting down..."
  kill $BACKEND_PID $FRONTEND_PID 2>/dev/null || true
  docker compose -f "$SCRIPT_DIR/docker-compose.yml" down
  echo "Done."
}
trap cleanup EXIT INT TERM

# Start PostgreSQL
echo "Starting PostgreSQL..."
docker compose -f "$SCRIPT_DIR/docker-compose.yml" up -d --wait

# Start backend
echo "Starting backend..."
cd "$SCRIPT_DIR/backend"
./mvnw spring-boot:run &
BACKEND_PID=$!

# Start frontend
echo "Starting frontend..."
cd "$SCRIPT_DIR/frontend"
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"
nvm use 22
npm install --silent
npm run dev &
FRONTEND_PID=$!

echo ""
echo "All services running:"
echo "  PostgreSQL  -> localhost:5432"
echo "  Backend     -> http://localhost:8080"
echo "  Frontend    -> http://localhost:5173"
echo ""
echo "Press Ctrl+C to stop all services."

wait
