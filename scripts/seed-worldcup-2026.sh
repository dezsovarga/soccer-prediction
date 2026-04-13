#!/usr/bin/env bash
# Seed script: FIFA World Cup 2026 — league, 48 teams (12 groups), 72 group stage fixtures, standings
# Usage: ./scripts/seed-worldcup-2026.sh
# Requires: Docker container 'soccer-prediction-db' running with PostgreSQL

set -euo pipefail

CONTAINER="soccer-prediction-db"
DB="soccer_prediction"
DB_USER="postgres"

# Check container is running
if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER}$"; then
  echo "Error: Docker container '${CONTAINER}' is not running."
  echo "Start it with: docker compose up -d --wait"
  exit 1
fi

echo "Seeding FIFA World Cup 2026 data..."

docker exec -i "${CONTAINER}" psql -U "${DB_USER}" -d "${DB}" <<'SQL'
BEGIN;

-- ============================================================
-- 1. CREATE LEAGUE
-- ============================================================
INSERT INTO leagues (id, name, mode, season, join_code, exact_score_points, correct_outcome_points, wrong_prediction_points, top_scorer_bonus, league_winner_bonus, created_at)
VALUES (gen_random_uuid(), 'FIFA World Cup 2026', 'MANUAL', 2026, 'WC2026', 3, 1, 0, 10, 10, NOW())
ON CONFLICT DO NOTHING;

-- ============================================================
-- 2. INSERT 48 TEAMS (12 groups x 4 teams)
-- ============================================================
INSERT INTO teams (id, league_id, name, country_code, logo_url, group_name) VALUES
-- Group A
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Mexico',           'mx',     'https://flagcdn.com/w80/mx.png', 'A'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'South Africa',     'za',     'https://flagcdn.com/w80/za.png', 'A'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'South Korea',      'kr',     'https://flagcdn.com/w80/kr.png', 'A'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Czech Republic',   'cz',     'https://flagcdn.com/w80/cz.png', 'A'),
-- Group B
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Canada',                 'ca', 'https://flagcdn.com/w80/ca.png', 'B'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Bosnia and Herzegovina', 'ba', 'https://flagcdn.com/w80/ba.png', 'B'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Qatar',                  'qa', 'https://flagcdn.com/w80/qa.png', 'B'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Switzerland',            'ch', 'https://flagcdn.com/w80/ch.png', 'B'),
-- Group C
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Brazil',    'br',     'https://flagcdn.com/w80/br.png', 'C'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Morocco',   'ma',     'https://flagcdn.com/w80/ma.png', 'C'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Haiti',     'ht',     'https://flagcdn.com/w80/ht.png', 'C'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Scotland',  'gb-sct', 'https://flagcdn.com/w80/gb-sct.png', 'C'),
-- Group D
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'United States', 'us', 'https://flagcdn.com/w80/us.png', 'D'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Paraguay',      'py', 'https://flagcdn.com/w80/py.png', 'D'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Australia',     'au', 'https://flagcdn.com/w80/au.png', 'D'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Turkey',        'tr', 'https://flagcdn.com/w80/tr.png', 'D'),
-- Group E
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Germany',     'de', 'https://flagcdn.com/w80/de.png', 'E'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Curaçao',     'cw', 'https://flagcdn.com/w80/cw.png', 'E'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Ivory Coast', 'ci', 'https://flagcdn.com/w80/ci.png', 'E'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Ecuador',     'ec', 'https://flagcdn.com/w80/ec.png', 'E'),
-- Group F
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Netherlands', 'nl', 'https://flagcdn.com/w80/nl.png', 'F'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Japan',       'jp', 'https://flagcdn.com/w80/jp.png', 'F'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Sweden',      'se', 'https://flagcdn.com/w80/se.png', 'F'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Tunisia',     'tn', 'https://flagcdn.com/w80/tn.png', 'F'),
-- Group G
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Belgium',     'be', 'https://flagcdn.com/w80/be.png', 'G'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Egypt',       'eg', 'https://flagcdn.com/w80/eg.png', 'G'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Iran',        'ir', 'https://flagcdn.com/w80/ir.png', 'G'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'New Zealand', 'nz', 'https://flagcdn.com/w80/nz.png', 'G'),
-- Group H
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Spain',        'es', 'https://flagcdn.com/w80/es.png', 'H'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Cape Verde',   'cv', 'https://flagcdn.com/w80/cv.png', 'H'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Saudi Arabia', 'sa', 'https://flagcdn.com/w80/sa.png', 'H'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Uruguay',      'uy', 'https://flagcdn.com/w80/uy.png', 'H'),
-- Group I
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'France',  'fr', 'https://flagcdn.com/w80/fr.png', 'I'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Senegal', 'sn', 'https://flagcdn.com/w80/sn.png', 'I'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Iraq',    'iq', 'https://flagcdn.com/w80/iq.png', 'I'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Norway',  'no', 'https://flagcdn.com/w80/no.png', 'I'),
-- Group J
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Argentina', 'ar', 'https://flagcdn.com/w80/ar.png', 'J'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Algeria',   'dz', 'https://flagcdn.com/w80/dz.png', 'J'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Austria',   'at', 'https://flagcdn.com/w80/at.png', 'J'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Jordan',    'jo', 'https://flagcdn.com/w80/jo.png', 'J'),
-- Group K
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Portugal',   'pt', 'https://flagcdn.com/w80/pt.png', 'K'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Congo DR',   'cd', 'https://flagcdn.com/w80/cd.png', 'K'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Uzbekistan', 'uz', 'https://flagcdn.com/w80/uz.png', 'K'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Colombia',   'co', 'https://flagcdn.com/w80/co.png', 'K'),
-- Group L
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'England', 'gb-eng', 'https://flagcdn.com/w80/gb-eng.png', 'L'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Croatia', 'hr',     'https://flagcdn.com/w80/hr.png',     'L'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Ghana',   'gh',     'https://flagcdn.com/w80/gh.png',     'L'),
(gen_random_uuid(), (SELECT id FROM leagues WHERE join_code='WC2026'), 'Panama',  'pa',     'https://flagcdn.com/w80/pa.png',     'L');

-- ============================================================
-- 3. INSERT 72 GROUP STAGE FIXTURES
--    All kickoff times are in UTC.
-- ============================================================
DO $$
DECLARE
  lid UUID;
BEGIN
  SELECT id INTO lid FROM leagues WHERE join_code = 'WC2026';

  INSERT INTO fixtures (id, league_id, home_team, away_team, home_team_id, away_team_id, kickoff, status, round, matchday, updated_at) VALUES
  -- ===================== GROUP A =====================
  -- Matchday 1
  (gen_random_uuid(), lid, 'Mexico', 'South Africa',
    (SELECT id FROM teams WHERE name='Mexico' AND league_id=lid),
    (SELECT id FROM teams WHERE name='South Africa' AND league_id=lid),
    '2026-06-11 19:00:00+00', 'SCHEDULED', 'Group A', 1, NOW()),
  (gen_random_uuid(), lid, 'South Korea', 'Czech Republic',
    (SELECT id FROM teams WHERE name='South Korea' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Czech Republic' AND league_id=lid),
    '2026-06-12 02:00:00+00', 'SCHEDULED', 'Group A', 1, NOW()),
  -- Matchday 2
  (gen_random_uuid(), lid, 'Czech Republic', 'South Africa',
    (SELECT id FROM teams WHERE name='Czech Republic' AND league_id=lid),
    (SELECT id FROM teams WHERE name='South Africa' AND league_id=lid),
    '2026-06-18 16:00:00+00', 'SCHEDULED', 'Group A', 2, NOW()),
  (gen_random_uuid(), lid, 'Mexico', 'South Korea',
    (SELECT id FROM teams WHERE name='Mexico' AND league_id=lid),
    (SELECT id FROM teams WHERE name='South Korea' AND league_id=lid),
    '2026-06-19 03:00:00+00', 'SCHEDULED', 'Group A', 2, NOW()),
  -- Matchday 3
  (gen_random_uuid(), lid, 'South Korea', 'South Africa',
    (SELECT id FROM teams WHERE name='South Korea' AND league_id=lid),
    (SELECT id FROM teams WHERE name='South Africa' AND league_id=lid),
    '2026-06-25 03:00:00+00', 'SCHEDULED', 'Group A', 3, NOW()),
  (gen_random_uuid(), lid, 'Czech Republic', 'Mexico',
    (SELECT id FROM teams WHERE name='Czech Republic' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Mexico' AND league_id=lid),
    '2026-06-25 03:00:00+00', 'SCHEDULED', 'Group A', 3, NOW()),

  -- ===================== GROUP B =====================
  -- Matchday 1
  (gen_random_uuid(), lid, 'Canada', 'Bosnia and Herzegovina',
    (SELECT id FROM teams WHERE name='Canada' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Bosnia and Herzegovina' AND league_id=lid),
    '2026-06-12 19:00:00+00', 'SCHEDULED', 'Group B', 1, NOW()),
  (gen_random_uuid(), lid, 'Qatar', 'Switzerland',
    (SELECT id FROM teams WHERE name='Qatar' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Switzerland' AND league_id=lid),
    '2026-06-13 19:00:00+00', 'SCHEDULED', 'Group B', 1, NOW()),
  -- Matchday 2
  (gen_random_uuid(), lid, 'Switzerland', 'Bosnia and Herzegovina',
    (SELECT id FROM teams WHERE name='Switzerland' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Bosnia and Herzegovina' AND league_id=lid),
    '2026-06-18 19:00:00+00', 'SCHEDULED', 'Group B', 2, NOW()),
  (gen_random_uuid(), lid, 'Canada', 'Qatar',
    (SELECT id FROM teams WHERE name='Canada' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Qatar' AND league_id=lid),
    '2026-06-18 22:00:00+00', 'SCHEDULED', 'Group B', 2, NOW()),
  -- Matchday 3
  (gen_random_uuid(), lid, 'Switzerland', 'Canada',
    (SELECT id FROM teams WHERE name='Switzerland' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Canada' AND league_id=lid),
    '2026-06-24 19:00:00+00', 'SCHEDULED', 'Group B', 3, NOW()),
  (gen_random_uuid(), lid, 'Bosnia and Herzegovina', 'Qatar',
    (SELECT id FROM teams WHERE name='Bosnia and Herzegovina' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Qatar' AND league_id=lid),
    '2026-06-24 19:00:00+00', 'SCHEDULED', 'Group B', 3, NOW()),

  -- ===================== GROUP C =====================
  -- Matchday 1
  (gen_random_uuid(), lid, 'Brazil', 'Morocco',
    (SELECT id FROM teams WHERE name='Brazil' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Morocco' AND league_id=lid),
    '2026-06-13 22:00:00+00', 'SCHEDULED', 'Group C', 1, NOW()),
  (gen_random_uuid(), lid, 'Haiti', 'Scotland',
    (SELECT id FROM teams WHERE name='Haiti' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Scotland' AND league_id=lid),
    '2026-06-14 01:00:00+00', 'SCHEDULED', 'Group C', 1, NOW()),
  -- Matchday 2
  (gen_random_uuid(), lid, 'Scotland', 'Morocco',
    (SELECT id FROM teams WHERE name='Scotland' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Morocco' AND league_id=lid),
    '2026-06-19 22:00:00+00', 'SCHEDULED', 'Group C', 2, NOW()),
  (gen_random_uuid(), lid, 'Brazil', 'Haiti',
    (SELECT id FROM teams WHERE name='Brazil' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Haiti' AND league_id=lid),
    '2026-06-20 01:00:00+00', 'SCHEDULED', 'Group C', 2, NOW()),
  -- Matchday 3
  (gen_random_uuid(), lid, 'Scotland', 'Brazil',
    (SELECT id FROM teams WHERE name='Scotland' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Brazil' AND league_id=lid),
    '2026-06-24 22:00:00+00', 'SCHEDULED', 'Group C', 3, NOW()),
  (gen_random_uuid(), lid, 'Morocco', 'Haiti',
    (SELECT id FROM teams WHERE name='Morocco' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Haiti' AND league_id=lid),
    '2026-06-24 22:00:00+00', 'SCHEDULED', 'Group C', 3, NOW()),

  -- ===================== GROUP D =====================
  -- Matchday 1
  (gen_random_uuid(), lid, 'United States', 'Paraguay',
    (SELECT id FROM teams WHERE name='United States' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Paraguay' AND league_id=lid),
    '2026-06-13 01:00:00+00', 'SCHEDULED', 'Group D', 1, NOW()),
  (gen_random_uuid(), lid, 'Australia', 'Turkey',
    (SELECT id FROM teams WHERE name='Australia' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Turkey' AND league_id=lid),
    '2026-06-14 04:00:00+00', 'SCHEDULED', 'Group D', 1, NOW()),
  -- Matchday 2
  (gen_random_uuid(), lid, 'United States', 'Australia',
    (SELECT id FROM teams WHERE name='United States' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Australia' AND league_id=lid),
    '2026-06-19 19:00:00+00', 'SCHEDULED', 'Group D', 2, NOW()),
  (gen_random_uuid(), lid, 'Turkey', 'Paraguay',
    (SELECT id FROM teams WHERE name='Turkey' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Paraguay' AND league_id=lid),
    '2026-06-20 03:00:00+00', 'SCHEDULED', 'Group D', 2, NOW()),
  -- Matchday 3
  (gen_random_uuid(), lid, 'Turkey', 'United States',
    (SELECT id FROM teams WHERE name='Turkey' AND league_id=lid),
    (SELECT id FROM teams WHERE name='United States' AND league_id=lid),
    '2026-06-26 02:00:00+00', 'SCHEDULED', 'Group D', 3, NOW()),
  (gen_random_uuid(), lid, 'Paraguay', 'Australia',
    (SELECT id FROM teams WHERE name='Paraguay' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Australia' AND league_id=lid),
    '2026-06-26 02:00:00+00', 'SCHEDULED', 'Group D', 3, NOW()),

  -- ===================== GROUP E =====================
  -- Matchday 1
  (gen_random_uuid(), lid, 'Germany', 'Curaçao',
    (SELECT id FROM teams WHERE name='Germany' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Curaçao' AND league_id=lid),
    '2026-06-14 17:00:00+00', 'SCHEDULED', 'Group E', 1, NOW()),
  (gen_random_uuid(), lid, 'Ivory Coast', 'Ecuador',
    (SELECT id FROM teams WHERE name='Ivory Coast' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Ecuador' AND league_id=lid),
    '2026-06-14 23:00:00+00', 'SCHEDULED', 'Group E', 1, NOW()),
  -- Matchday 2
  (gen_random_uuid(), lid, 'Germany', 'Ivory Coast',
    (SELECT id FROM teams WHERE name='Germany' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Ivory Coast' AND league_id=lid),
    '2026-06-20 20:00:00+00', 'SCHEDULED', 'Group E', 2, NOW()),
  (gen_random_uuid(), lid, 'Ecuador', 'Curaçao',
    (SELECT id FROM teams WHERE name='Ecuador' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Curaçao' AND league_id=lid),
    '2026-06-21 00:00:00+00', 'SCHEDULED', 'Group E', 2, NOW()),
  -- Matchday 3
  (gen_random_uuid(), lid, 'Ecuador', 'Germany',
    (SELECT id FROM teams WHERE name='Ecuador' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Germany' AND league_id=lid),
    '2026-06-25 20:00:00+00', 'SCHEDULED', 'Group E', 3, NOW()),
  (gen_random_uuid(), lid, 'Curaçao', 'Ivory Coast',
    (SELECT id FROM teams WHERE name='Curaçao' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Ivory Coast' AND league_id=lid),
    '2026-06-25 20:00:00+00', 'SCHEDULED', 'Group E', 3, NOW()),

  -- ===================== GROUP F =====================
  -- Matchday 1
  (gen_random_uuid(), lid, 'Netherlands', 'Japan',
    (SELECT id FROM teams WHERE name='Netherlands' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Japan' AND league_id=lid),
    '2026-06-14 20:00:00+00', 'SCHEDULED', 'Group F', 1, NOW()),
  (gen_random_uuid(), lid, 'Sweden', 'Tunisia',
    (SELECT id FROM teams WHERE name='Sweden' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Tunisia' AND league_id=lid),
    '2026-06-15 02:00:00+00', 'SCHEDULED', 'Group F', 1, NOW()),
  -- Matchday 2
  (gen_random_uuid(), lid, 'Netherlands', 'Sweden',
    (SELECT id FROM teams WHERE name='Netherlands' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Sweden' AND league_id=lid),
    '2026-06-20 17:00:00+00', 'SCHEDULED', 'Group F', 2, NOW()),
  (gen_random_uuid(), lid, 'Tunisia', 'Japan',
    (SELECT id FROM teams WHERE name='Tunisia' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Japan' AND league_id=lid),
    '2026-06-21 04:00:00+00', 'SCHEDULED', 'Group F', 2, NOW()),
  -- Matchday 3
  (gen_random_uuid(), lid, 'Japan', 'Sweden',
    (SELECT id FROM teams WHERE name='Japan' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Sweden' AND league_id=lid),
    '2026-06-25 23:00:00+00', 'SCHEDULED', 'Group F', 3, NOW()),
  (gen_random_uuid(), lid, 'Tunisia', 'Netherlands',
    (SELECT id FROM teams WHERE name='Tunisia' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Netherlands' AND league_id=lid),
    '2026-06-25 23:00:00+00', 'SCHEDULED', 'Group F', 3, NOW()),

  -- ===================== GROUP G =====================
  -- Matchday 1
  (gen_random_uuid(), lid, 'Belgium', 'Egypt',
    (SELECT id FROM teams WHERE name='Belgium' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Egypt' AND league_id=lid),
    '2026-06-15 22:00:00+00', 'SCHEDULED', 'Group G', 1, NOW()),
  (gen_random_uuid(), lid, 'Iran', 'New Zealand',
    (SELECT id FROM teams WHERE name='Iran' AND league_id=lid),
    (SELECT id FROM teams WHERE name='New Zealand' AND league_id=lid),
    '2026-06-16 04:00:00+00', 'SCHEDULED', 'Group G', 1, NOW()),
  -- Matchday 2
  (gen_random_uuid(), lid, 'Belgium', 'Iran',
    (SELECT id FROM teams WHERE name='Belgium' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Iran' AND league_id=lid),
    '2026-06-21 19:00:00+00', 'SCHEDULED', 'Group G', 2, NOW()),
  (gen_random_uuid(), lid, 'New Zealand', 'Egypt',
    (SELECT id FROM teams WHERE name='New Zealand' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Egypt' AND league_id=lid),
    '2026-06-22 01:00:00+00', 'SCHEDULED', 'Group G', 2, NOW()),
  -- Matchday 3
  (gen_random_uuid(), lid, 'Egypt', 'Iran',
    (SELECT id FROM teams WHERE name='Egypt' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Iran' AND league_id=lid),
    '2026-06-27 03:00:00+00', 'SCHEDULED', 'Group G', 3, NOW()),
  (gen_random_uuid(), lid, 'New Zealand', 'Belgium',
    (SELECT id FROM teams WHERE name='New Zealand' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Belgium' AND league_id=lid),
    '2026-06-27 03:00:00+00', 'SCHEDULED', 'Group G', 3, NOW()),

  -- ===================== GROUP H =====================
  -- Matchday 1
  (gen_random_uuid(), lid, 'Spain', 'Cape Verde',
    (SELECT id FROM teams WHERE name='Spain' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Cape Verde' AND league_id=lid),
    '2026-06-15 16:00:00+00', 'SCHEDULED', 'Group H', 1, NOW()),
  (gen_random_uuid(), lid, 'Saudi Arabia', 'Uruguay',
    (SELECT id FROM teams WHERE name='Saudi Arabia' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Uruguay' AND league_id=lid),
    '2026-06-15 22:00:00+00', 'SCHEDULED', 'Group H', 1, NOW()),
  -- Matchday 2
  (gen_random_uuid(), lid, 'Spain', 'Saudi Arabia',
    (SELECT id FROM teams WHERE name='Spain' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Saudi Arabia' AND league_id=lid),
    '2026-06-21 16:00:00+00', 'SCHEDULED', 'Group H', 2, NOW()),
  (gen_random_uuid(), lid, 'Uruguay', 'Cape Verde',
    (SELECT id FROM teams WHERE name='Uruguay' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Cape Verde' AND league_id=lid),
    '2026-06-21 22:00:00+00', 'SCHEDULED', 'Group H', 2, NOW()),
  -- Matchday 3
  (gen_random_uuid(), lid, 'Cape Verde', 'Saudi Arabia',
    (SELECT id FROM teams WHERE name='Cape Verde' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Saudi Arabia' AND league_id=lid),
    '2026-06-27 00:00:00+00', 'SCHEDULED', 'Group H', 3, NOW()),
  (gen_random_uuid(), lid, 'Uruguay', 'Spain',
    (SELECT id FROM teams WHERE name='Uruguay' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Spain' AND league_id=lid),
    '2026-06-27 00:00:00+00', 'SCHEDULED', 'Group H', 3, NOW()),

  -- ===================== GROUP I =====================
  -- Matchday 1
  (gen_random_uuid(), lid, 'France', 'Senegal',
    (SELECT id FROM teams WHERE name='France' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Senegal' AND league_id=lid),
    '2026-06-16 19:00:00+00', 'SCHEDULED', 'Group I', 1, NOW()),
  (gen_random_uuid(), lid, 'Iraq', 'Norway',
    (SELECT id FROM teams WHERE name='Iraq' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Norway' AND league_id=lid),
    '2026-06-16 22:00:00+00', 'SCHEDULED', 'Group I', 1, NOW()),
  -- Matchday 2
  (gen_random_uuid(), lid, 'France', 'Iraq',
    (SELECT id FROM teams WHERE name='France' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Iraq' AND league_id=lid),
    '2026-06-22 21:00:00+00', 'SCHEDULED', 'Group I', 2, NOW()),
  (gen_random_uuid(), lid, 'Norway', 'Senegal',
    (SELECT id FROM teams WHERE name='Norway' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Senegal' AND league_id=lid),
    '2026-06-23 00:00:00+00', 'SCHEDULED', 'Group I', 2, NOW()),
  -- Matchday 3
  (gen_random_uuid(), lid, 'Norway', 'France',
    (SELECT id FROM teams WHERE name='Norway' AND league_id=lid),
    (SELECT id FROM teams WHERE name='France' AND league_id=lid),
    '2026-06-26 19:00:00+00', 'SCHEDULED', 'Group I', 3, NOW()),
  (gen_random_uuid(), lid, 'Senegal', 'Iraq',
    (SELECT id FROM teams WHERE name='Senegal' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Iraq' AND league_id=lid),
    '2026-06-26 19:00:00+00', 'SCHEDULED', 'Group I', 3, NOW()),

  -- ===================== GROUP J =====================
  -- Matchday 1
  (gen_random_uuid(), lid, 'Argentina', 'Algeria',
    (SELECT id FROM teams WHERE name='Argentina' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Algeria' AND league_id=lid),
    '2026-06-17 01:00:00+00', 'SCHEDULED', 'Group J', 1, NOW()),
  (gen_random_uuid(), lid, 'Austria', 'Jordan',
    (SELECT id FROM teams WHERE name='Austria' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Jordan' AND league_id=lid),
    '2026-06-17 04:00:00+00', 'SCHEDULED', 'Group J', 1, NOW()),
  -- Matchday 2
  (gen_random_uuid(), lid, 'Argentina', 'Austria',
    (SELECT id FROM teams WHERE name='Argentina' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Austria' AND league_id=lid),
    '2026-06-22 17:00:00+00', 'SCHEDULED', 'Group J', 2, NOW()),
  (gen_random_uuid(), lid, 'Jordan', 'Algeria',
    (SELECT id FROM teams WHERE name='Jordan' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Algeria' AND league_id=lid),
    '2026-06-23 03:00:00+00', 'SCHEDULED', 'Group J', 2, NOW()),
  -- Matchday 3
  (gen_random_uuid(), lid, 'Algeria', 'Austria',
    (SELECT id FROM teams WHERE name='Algeria' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Austria' AND league_id=lid),
    '2026-06-28 02:00:00+00', 'SCHEDULED', 'Group J', 3, NOW()),
  (gen_random_uuid(), lid, 'Jordan', 'Argentina',
    (SELECT id FROM teams WHERE name='Jordan' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Argentina' AND league_id=lid),
    '2026-06-28 02:00:00+00', 'SCHEDULED', 'Group J', 3, NOW()),

  -- ===================== GROUP K =====================
  -- Matchday 1
  (gen_random_uuid(), lid, 'Portugal', 'Congo DR',
    (SELECT id FROM teams WHERE name='Portugal' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Congo DR' AND league_id=lid),
    '2026-06-17 17:00:00+00', 'SCHEDULED', 'Group K', 1, NOW()),
  (gen_random_uuid(), lid, 'Uzbekistan', 'Colombia',
    (SELECT id FROM teams WHERE name='Uzbekistan' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Colombia' AND league_id=lid),
    '2026-06-18 02:00:00+00', 'SCHEDULED', 'Group K', 1, NOW()),
  -- Matchday 2
  (gen_random_uuid(), lid, 'Portugal', 'Uzbekistan',
    (SELECT id FROM teams WHERE name='Portugal' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Uzbekistan' AND league_id=lid),
    '2026-06-23 17:00:00+00', 'SCHEDULED', 'Group K', 2, NOW()),
  (gen_random_uuid(), lid, 'Colombia', 'Congo DR',
    (SELECT id FROM teams WHERE name='Colombia' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Congo DR' AND league_id=lid),
    '2026-06-24 02:00:00+00', 'SCHEDULED', 'Group K', 2, NOW()),
  -- Matchday 3
  (gen_random_uuid(), lid, 'Colombia', 'Portugal',
    (SELECT id FROM teams WHERE name='Colombia' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Portugal' AND league_id=lid),
    '2026-06-27 23:30:00+00', 'SCHEDULED', 'Group K', 3, NOW()),
  (gen_random_uuid(), lid, 'Congo DR', 'Uzbekistan',
    (SELECT id FROM teams WHERE name='Congo DR' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Uzbekistan' AND league_id=lid),
    '2026-06-27 23:30:00+00', 'SCHEDULED', 'Group K', 3, NOW()),

  -- ===================== GROUP L =====================
  -- Matchday 1
  (gen_random_uuid(), lid, 'England', 'Croatia',
    (SELECT id FROM teams WHERE name='England' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Croatia' AND league_id=lid),
    '2026-06-17 20:00:00+00', 'SCHEDULED', 'Group L', 1, NOW()),
  (gen_random_uuid(), lid, 'Ghana', 'Panama',
    (SELECT id FROM teams WHERE name='Ghana' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Panama' AND league_id=lid),
    '2026-06-17 23:00:00+00', 'SCHEDULED', 'Group L', 1, NOW()),
  -- Matchday 2
  (gen_random_uuid(), lid, 'England', 'Ghana',
    (SELECT id FROM teams WHERE name='England' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Ghana' AND league_id=lid),
    '2026-06-23 20:00:00+00', 'SCHEDULED', 'Group L', 2, NOW()),
  (gen_random_uuid(), lid, 'Panama', 'Croatia',
    (SELECT id FROM teams WHERE name='Panama' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Croatia' AND league_id=lid),
    '2026-06-23 23:00:00+00', 'SCHEDULED', 'Group L', 2, NOW()),
  -- Matchday 3
  (gen_random_uuid(), lid, 'Panama', 'England',
    (SELECT id FROM teams WHERE name='Panama' AND league_id=lid),
    (SELECT id FROM teams WHERE name='England' AND league_id=lid),
    '2026-06-27 21:00:00+00', 'SCHEDULED', 'Group L', 3, NOW()),
  (gen_random_uuid(), lid, 'Croatia', 'Ghana',
    (SELECT id FROM teams WHERE name='Croatia' AND league_id=lid),
    (SELECT id FROM teams WHERE name='Ghana' AND league_id=lid),
    '2026-06-27 21:00:00+00', 'SCHEDULED', 'Group L', 3, NOW());
END $$;

-- ============================================================
-- 4. SET LOGO URLs ON FIXTURES (from team references)
-- ============================================================
UPDATE fixtures f
SET home_team_logo = t.logo_url
FROM teams t
WHERE f.home_team_id = t.id
  AND f.league_id = (SELECT id FROM leagues WHERE join_code = 'WC2026');

UPDATE fixtures f
SET away_team_logo = t.logo_url
FROM teams t
WHERE f.away_team_id = t.id
  AND f.league_id = (SELECT id FROM leagues WHERE join_code = 'WC2026');

-- ============================================================
-- 5. INSERT STANDINGS (all zeroed, 4 teams per group, with logos)
-- ============================================================
INSERT INTO standings (id, league_id, team_id, team_name, team_logo, group_name, rank, points, played, won, drawn, lost, goals_for, goals_against, goal_diff, updated_at)
SELECT
  gen_random_uuid(),
  t.league_id,
  t.id,
  t.name,
  t.logo_url,
  t.group_name,
  ROW_NUMBER() OVER (PARTITION BY t.group_name ORDER BY t.name),
  0, 0, 0, 0, 0, 0, 0, 0,
  NOW()
FROM teams t
WHERE t.league_id = (SELECT id FROM leagues WHERE join_code = 'WC2026');

COMMIT;
SQL

echo ""
echo "Done! Seeded:"
echo "  - 1 league: FIFA World Cup 2026 (join code: WC2026)"
echo "  - 48 teams in 12 groups (A-L)"
echo "  - 72 group stage fixtures"
echo "  - 48 standings entries"
