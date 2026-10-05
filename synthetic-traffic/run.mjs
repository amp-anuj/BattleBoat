// Synthetic traffic generator for the Battleboat web app.
//
// Drives the real page in Chromium so every event goes through the Amplitude
// Browser SDK exactly as a human session would (autocapture, Session Replay,
// Guides & Surveys, Experiment all fire). Each session is a fresh browser
// context, so it gets its own Amplitude session. Daily runs reuse a saved
// user id and device id for people who have played before.
//
// Usage:
//   node run.mjs [--url URL] [--sessions N] [--concurrency N] [--headed]
//                [--prefix synth] [--seed N] [--port 3000]
//   node run.mjs --daily [--users 10] [--sessions 15] [--date YYYY-MM-DD]
//                [--dry-run]
//
// With no --url, the repo root is served on localhost:<port> for the run.
//
// --daily plays 15 sessions across 10 users (some play twice). About 60% of
// the users are returning once the roster has history; the rest are new.
// Identities live in roster.json so the same user id and device id come back
// on a later day. A second run the same day finishes anything the first run
// didn't, and does not add more users.
//
// Every synthetic user gets the user property `traffic_source = synthetic`.
// Daily runs also set `synth_cohort` to `new` or `returning`.

import { chromium } from 'playwright';
import http from 'node:http';
import { randomUUID } from 'node:crypto';
import { createReadStream, existsSync, readFileSync, statSync, writeFileSync } from 'node:fs';
import { dirname, extname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const HERE = dirname(fileURLToPath(import.meta.url));

// ---------------------------------------------------------------- CLI args
const args = parseArgs(process.argv.slice(2));
const CONFIG = {
	url: args.url ?? null, // null => serve the repo root locally
	port: int(args.port, 3000),
	daily: Boolean(args.daily),
	users: int(args.users, 10),
	sessions: int(args.sessions, args.daily ? 15 : 20),
	concurrency: int(args.concurrency, 3),
	headed: Boolean(args.headed),
	prefix: args.prefix ?? 'synth',
	date: args.date ?? new Date().toLocaleDateString('en-CA', { timeZone: 'America/New_York' }),
	dryRun: Boolean(args['dry-run']),
	rosterPath: args.roster ? resolve(args.roster) : join(HERE, 'roster.json'),
	seed: args.seed ? int(args.seed, 1) : (args.daily ? Number(String(args.date ?? '').replaceAll('-', '')) || dateSeed() : Date.now()),
};

function dateSeed() {
	return Number(new Date().toLocaleDateString('en-CA', { timeZone: 'America/New_York' }).replaceAll('-', ''));
}

// Weighted mix of behaviours. Tune to shape your funnels.
const PERSONAS = [
	{ name: 'quick', weight: 45 },     // place randomly -> start -> finish game
	{ name: 'manual', weight: 20 },    // place a ship by hand (maybe rotate), random for the rest
	{ name: 'stuck', weight: 10 },     // clicks grid before picking a ship -> rescue prompt
	{ name: 'abandon', weight: 15 },   // starts a game, fires a few shots, leaves
	{ name: 'bounce', weight: 10 },    // loads page, pokes around, never starts
];

const SELECTORS = {
	enemyCells: '.grid.computer-player .grid-cell',
	humanCells: '.grid.human-player .grid-cell',
	humanGrid: '.grid.human-player',
	placeRandomly: '#place-randomly',
	startGame: '#start-game',
	restartGame: '#restart-game',
	restartSidebar: '#restart-sidebar',
	rotate: '#rotate-button',
	rescuePrompt: '#placement-rescue',
	rescuePlaceRandomly: '#placement-rescue-action',
	userIdInput: '#user-id-input',
	setUserId: '#set-user-id',
	ships: ['patrolboat', 'submarine', 'destroyer', 'battleship', 'carrier'],
};

const USER_AGENTS = [
	'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36',
	'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36',
	'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36',
	'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.6 Safari/605.1.15',
];

const key = (x, y) => `${x},${y}`;

// ---------------------------------------------------------------- main
const rng = mulberry32(CONFIG.seed || dateSeed());
const roster = CONFIG.daily ? loadRoster(CONFIG.rosterPath) : null;
if (CONFIG.daily && roster.lastRunDate !== CONFIG.date) {
	buildDailyPlan(roster, rng);
	if (!CONFIG.dryRun) saveRoster(roster);
}
const jobs = CONFIG.daily ? jobsFromRoster(roster) : adHocJobs(rng);

const newUsers = CONFIG.daily ? roster.plan.filter((p) => !p.returning).length : 0;
const returningUsers = CONFIG.daily ? roster.plan.filter((p) => p.returning).length : 0;
console.log(`Battleboat synthetic traffic`);
console.log(`  url         ${CONFIG.url ?? '(local)'}`);
console.log(`  mode        ${CONFIG.daily ? `daily ${CONFIG.date}` : 'ad hoc'}`);
if (CONFIG.daily) {
	console.log(`  users       ${roster.plan.length} (${newUsers} new, ${returningUsers} returning)`);
	console.log(`  sessions    ${jobs.length} remaining of ${roster.plan.reduce((s, p) => s + p.sessionsPlanned, 0)}`);
} else {
	console.log(`  sessions    ${jobs.length}`);
}
console.log(`  concurrency ${CONFIG.concurrency}`);
console.log(`  seed        ${CONFIG.seed}`);
if (CONFIG.daily) console.log(`  roster      ${CONFIG.rosterPath}`);
console.log('');

if (CONFIG.dryRun) {
	if (CONFIG.daily) {
		for (const row of roster.plan) {
			const user = roster.users.find((u) => u.userId === row.userId);
			console.log(`  ${row.returning ? 'returning' : 'new      '} ${row.userId}  sessions=${row.sessionsPlanned}  device=${user.deviceId}`);
		}
	}
	console.log('Dry run; nothing was played.');
	process.exit(0);
}
if (jobs.length === 0) {
	console.log(CONFIG.daily ? `Nothing left to play for ${CONFIG.date}.` : 'No sessions requested.');
	process.exit(0);
}

let localServer = null;
if (!CONFIG.url) {
	const root = resolve(HERE, '..');
	localServer = await serveStatic(root, CONFIG.port);
	CONFIG.url = `http://localhost:${CONFIG.port}/`;
}

const browser = await chromium.launch({ headless: !CONFIG.headed });
const summary = { done: 0, failed: 0, games: 0, wins: 0, personas: {} };

let next = 0;
async function worker(workerId) {
	while (next < jobs.length) {
		const job = jobs[next++];
		summary.personas[job.persona] = (summary.personas[job.persona] ?? 0) + 1;
		try {
			const result = await runSession(browser, job);
			if (CONFIG.daily) markSessionDone(roster, job.userId);
			summary.done++;
			summary.games += result.games;
			summary.wins += result.wins;
			const cohort = job.returning == null ? '' : job.returning ? ' returning' : ' new';
			console.log(`[w${workerId}] ${job.persona.padEnd(8)} user=${result.userId ?? 'anon'}${cohort} games=${result.games} wins=${result.wins} shots=${result.shots}`);
		} catch (err) {
			summary.failed++;
			console.error(`[w${workerId}] ${job.persona} ${job.userId ?? ''} FAILED: ${err.message.split('\n')[0]}`);
		}
	}
}

await Promise.all(Array.from({ length: Math.min(CONFIG.concurrency, jobs.length) }, (_, i) => worker(i)));
await browser.close();
localServer?.close();

console.log('');
console.log(`Done. sessions=${summary.done} failed=${summary.failed} games=${summary.games} wins=${summary.wins}`);
console.log(`Personas: ${JSON.stringify(summary.personas)}`);

// ---------------------------------------------------------------- daily roster
// Users who have actually sent events (sessions > 0) can come back. A user
// who was planned but never successfully played is not treated as returning.
function loadRoster(path) {
	if (!existsSync(path)) return { lastRunDate: null, nextId: 1, users: [], plan: [] };
	return JSON.parse(readFileSync(path, 'utf8'));
}

function saveRoster(roster) {
	writeFileSync(CONFIG.rosterPath, JSON.stringify(roster, null, 2) + '\n');
}

function buildDailyPlan(roster, r) {
	if (CONFIG.sessions < CONFIG.users) {
		throw new Error(`--sessions (${CONFIG.sessions}) must be at least --users (${CONFIG.users})`);
	}
	const prior = roster.users.filter((u) => u.sessions > 0 && u.lastSeen < CONFIG.date);
	const returningTarget = Math.round(CONFIG.users * 0.6);
	const returning = pickSpread(prior, Math.min(returningTarget, prior.length), r);

	const created = [];
	for (let i = 0; i < CONFIG.users - returning.length; i++) {
		const user = {
			userId: `${CONFIG.prefix}-${String(roster.nextId).padStart(4, '0')}`,
			deviceId: randomUUID(),
			firstSeen: CONFIG.date,
			lastSeen: null,
			sessions: 0,
		};
		roster.nextId += 1;
		roster.users.push(user);
		created.push(user);
	}

	const chosen = [
		...returning.map((user) => ({ user, returning: true })),
		...created.map((user) => ({ user, returning: false })),
	];
	const extras = CONFIG.sessions - CONFIG.users;
	const extraIds = new Set(shuffle(chosen, r).slice(0, extras).map((c) => c.user.userId));

	roster.lastRunDate = CONFIG.date;
	roster.plan = chosen.map((c) => {
		const n = 1 + (extraIds.has(c.user.userId) ? 1 : 0);
		return {
			userId: c.user.userId,
			returning: c.returning,
			sessionsPlanned: n,
			sessionsDone: 0,
			personas: Array.from({ length: n }, () => pickWeighted(PERSONAS, r).name),
		};
	});
}

// Half from people seen most recently, the rest from people who have been away,
// so it isn't the same six users every day.
function pickSpread(prior, count, r) {
	if (count <= 0) return [];
	const sorted = [...prior].sort((a, b) => b.lastSeen.localeCompare(a.lastSeen) || a.userId.localeCompare(b.userId));
	const mid = Math.ceil(sorted.length / 2);
	const recent = sorted.slice(0, mid);
	const older = sorted.slice(mid);
	const picked = sample(recent, Math.min(recent.length, Math.ceil(count / 2)), r);
	picked.push(...sample(older, Math.min(older.length, count - picked.length), r));
	if (picked.length < count) {
		const pickedIds = new Set(picked.map((u) => u.userId));
		picked.push(...sample(sorted.filter((u) => !pickedIds.has(u.userId)), count - picked.length, r));
	}
	return picked;
}

function jobsFromRoster(roster) {
	const jobs = [];
	for (const row of roster.plan) {
		const user = roster.users.find((u) => u.userId === row.userId);
		for (let i = row.sessionsDone; i < row.sessionsPlanned; i++) {
			jobs.push({
				userId: user.userId,
				deviceId: user.deviceId,
				returning: row.returning,
				persona: row.personas[i],
			});
		}
	}
	return jobs;
}

function markSessionDone(roster, userId) {
	const row = roster.plan.find((p) => p.userId === userId);
	row.sessionsDone += 1;
	const user = roster.users.find((u) => u.userId === userId);
	user.sessions += 1;
	user.lastSeen = CONFIG.date;
	saveRoster(roster);
}

function adHocJobs(r) {
	return Array.from({ length: CONFIG.sessions }, () => ({
		userId: null,
		deviceId: null,
		returning: null,
		persona: pickWeighted(PERSONAS, r).name,
	}));
}

function sample(list, count, r) {
	const copy = [...list];
	const out = [];
	while (out.length < count && copy.length) {
		out.push(copy.splice(Math.floor(r() * copy.length), 1)[0]);
	}
	return out;
}

function shuffle(list, r) {
	const copy = [...list];
	for (let i = copy.length - 1; i > 0; i--) {
		const j = Math.floor(r() * (i + 1));
		[copy[i], copy[j]] = [copy[j], copy[i]];
	}
	return copy;
}

// ---------------------------------------------------------------- session
async function runSession(browser, job) {
	const { persona } = job;
	const context = await browser.newContext({
		// Spans both layouts: stacked grids (<=1300px) and side-by-side (>1300px).
		viewport: { width: 1100 + Math.floor(rng() * 700), height: 900 + Math.floor(rng() * 200) },
		userAgent: pick(USER_AGENTS, rng),
		locale: pick(['en-US', 'en-GB', 'en-CA', 'en-AU'], rng),
	});
	const page = await context.newPage();
	// The game ends with window.alert(); dismiss it or the page stalls.
	page.on('dialog', (d) => d.accept().catch(() => {}));

	const result = { games: 0, wins: 0, shots: 0, userId: null };
	try {
		await page.goto(sessionUrl(job), { waitUntil: 'load' });
		await page.waitForFunction(
			(sel) => document.querySelectorAll(sel).length === 100 && window.amplitude?.getDeviceId?.(),
			SELECTORS.enemyCells,
			{ timeout: 20000 },
		);
		await pause(page, 600, 1800); // "reading the page"

		// Daily runs set identity from the URL before the first event. Ad hoc
		// runs still tag the user here, and most of them log in through the panel.
		if (job.userId) {
			result.userId = job.userId;
			const actual = await page.evaluate(() => ({
				userId: window.amplitude.getUserId(),
				deviceId: window.amplitude.getDeviceId(),
			}));
			if (actual.userId !== job.userId || actual.deviceId !== job.deviceId) {
				throw new Error(`identity mismatch: expected ${job.userId} / ${job.deviceId}, got ${actual.userId} / ${actual.deviceId}`);
			}
		} else {
			await page.evaluate(() => {
				const id = new window.amplitude.Identify();
				id.set('traffic_source', 'synthetic');
				window.amplitude.identify(id);
			});
			if (rng() < 0.65) {
				result.userId = `${CONFIG.prefix}-${randomUUID().slice(0, 8)}`;
				await handleEngagement(page);
				await page.fill(SELECTORS.userIdInput, result.userId);
				await pause(page, 200, 600);
				if (rng() < 0.5) await page.press(SELECTORS.userIdInput, 'Enter');
				else await click(page, SELECTORS.setUserId);
			}
		}

		switch (persona) {
			case 'bounce':
				await bounce(page);
				break;
			case 'abandon':
				await placeFleet(page, 'quick');
				await startGame(page);
				result.shots += (await playGame(page, { maxShots: 3 + Math.floor(rng() * 10) })).shots;
				break;
			default: {
				const games = 1 + (rng() < 0.35 ? 1 : 0) + (rng() < 0.1 ? 1 : 0);
				for (let g = 0; g < games; g++) {
					await placeFleet(page, g === 0 ? persona : 'quick');
					await startGame(page);
					const outcome = await playGame(page, { maxShots: 100 });
					result.shots += outcome.shots;
					result.games++;
					if (outcome.won) result.wins++;
					if (g < games - 1) {
						await pause(page, 800, 2500);
						await click(page, SELECTORS.restartGame);
						await page.waitForSelector(`${SELECTORS.placeRandomly}:not(.hidden)`);
					}
				}
			}
		}

		// Let the SDK (and Session Replay) drain its queue before teardown.
		await page.evaluate(() => window.amplitude.flush?.());
		await page.waitForTimeout(2000);
	} finally {
		await context.close();
	}
	return result;
}

// ---------------------------------------------------------------- behaviours
async function bounce(page) {
	if (rng() < 0.6) {
		await click(page, `#${pick(SELECTORS.ships, rng)}`);
		await pause(page, 400, 1200);
	}
	if (rng() < 0.3) {
		await click(page, SELECTORS.rotate);
		await pause(page, 300, 900);
	}
	await page.mouse.move(300 + rng() * 600, 300 + rng() * 300);
	await pause(page, 1500, 4000);
}

async function placeFleet(page, mode) {
	if (mode === 'manual') {
		// Place one or two ships by hand (some attempts will be illegal).
		const count = 1 + (rng() < 0.5 ? 1 : 0);
		for (let i = 0; i < count; i++) {
			const ship = SELECTORS.ships[i];
			await click(page, `#${ship}`);
			await pause(page, 300, 900);
			if (rng() < 0.5) {
				await click(page, SELECTORS.rotate);
				await pause(page, 200, 600);
			}
			// Up to 3 tries per ship; the last try is aimed at a safe cell.
			for (let attempt = 0; attempt < 3; attempt++) {
				const safe = attempt === 2;
				const x = safe ? i : Math.floor(rng() * 10);
				const y = safe ? 0 : Math.floor(rng() * 10);
				await clickCell(page, SELECTORS.humanCells, x, y);
				await pause(page, 300, 800);
				const placed = await page.$eval(`#${ship}`, (el) => el.classList.contains('placed'));
				if (placed) break;
			}
		}
	} else if (mode === 'stuck') {
		// Click the grid with no ship selected until the rescue prompt shows.
		for (let i = 0; i < 3; i++) {
			await clickCell(page, SELECTORS.humanCells, Math.floor(rng() * 10), Math.floor(rng() * 10));
			await pause(page, 500, 1500);
			if (await isVisible(page, SELECTORS.rescuePrompt)) break;
		}
		if (await isVisible(page, SELECTORS.rescuePrompt)) {
			await pause(page, 800, 2000);
			await click(page, SELECTORS.rescuePlaceRandomly);
			await pause(page, 400, 1000);
		}
	}

	// Fill in whatever is left.
	if (await isVisible(page, SELECTORS.placeRandomly)) {
		await click(page, SELECTORS.placeRandomly);
	}
	await page.waitForSelector(`${SELECTORS.startGame}:not(.hidden)`, { timeout: 5000 });
	await pause(page, 400, 1200);
}

async function startGame(page) {
	await click(page, SELECTORS.startGame);
	await pause(page, 500, 1500);
}

// Hunt/target shooting: random until a hit, then probe neighbours. Enough to
// win sometimes against the probability AI, so the Win property varies.
async function playGame(page, { maxShots }) {
	const fired = new Set();
	const hits = new Set();
	const targets = []; // stack: last element is highest priority
	let shots = 0;

	while (shots < maxShots) {
		if (await gameOver(page)) break;

		let x, y;
		while (targets.length) {
			const t = targets.pop();
			if (!fired.has(key(t.x, t.y))) { ({ x, y } = t); break; }
		}
		if (x === undefined) {
			// Parity hunt: only even (x+y) cells, since every ship is >= 2 long.
			do {
				x = Math.floor(rng() * 10);
				y = Math.floor(rng() * 10);
			} while (fired.has(key(x, y)) || (x + y) % 2 !== 0 && fired.size < 50);
		}

		fired.add(key(x, y));
		await clickCell(page, SELECTORS.enemyCells, x, y);
		shots++;

		const cls = await page.$eval(`.grid.computer-player .grid-cell-${x}-${y}`, (el) => el.className);
		if (/\bgrid-hit\b/.test(cls)) {
			hits.add(key(x, y));
			const inBounds = (a, b) => a >= 0 && a < 10 && b >= 0 && b < 10;
			const neighbours = [[1, 0], [-1, 0], [0, 1], [0, -1]];
			// Low priority: all unexplored neighbours.
			for (const [dx, dy] of neighbours) {
				if (inBounds(x + dx, y + dy) && !fired.has(key(x + dx, y + dy))) {
					targets.unshift({ x: x + dx, y: y + dy });
				}
			}
			// High priority: extend any line of hits through this cell.
			for (const [dx, dy] of neighbours) {
				if (hits.has(key(x - dx, y - dy))) {
					let ex = x + dx, ey = y + dy;
					while (inBounds(ex, ey) && hits.has(key(ex, ey))) { ex += dx; ey += dy; }
					if (inBounds(ex, ey) && !fired.has(key(ex, ey))) targets.push({ x: ex, y: ey });
					let bx = x - dx, by = y - dy;
					while (inBounds(bx, by) && hits.has(key(bx, by))) { bx -= dx; by -= dy; }
					if (inBounds(bx, by) && !fired.has(key(bx, by))) targets.push({ x: bx, y: by });
				}
			}
		} else if (/\bgrid-sunk\b/.test(cls)) {
			// Ship finished; drop queued targets and go back to hunting.
			targets.length = 0;
		}
		await pause(page, 250, 1100);
	}

	const won = await page.evaluate(() => {
		const cells = document.querySelectorAll('.grid.computer-player .grid-cell');
		let sunk = 0;
		cells.forEach((c) => { if (c.classList.contains('grid-sunk')) sunk++; });
		return sunk === 17; // 5+4+3+3+2
	});
	return { shots, won };
}

// ---------------------------------------------------------------- helpers
// Guides & Surveys can pop a modal at any point (it intercepts pointer
// events). Behave like a user: answer or close it, then continue.
async function handleEngagement(page, { forceCloseChecklist = false } = {}) {
	for (let i = 0; i < 3; i++) {
		const modal = page.locator('#engagement-wrapper .rc-dialog-wrap:visible');
		if ((await modal.count()) === 0) break;
		await pause(page, 600, 1800); // "reading the nudge"

		const stars = modal.locator('button[aria-label^="Rate "]');
		const starCount = await stars.count();
		const close = modal.locator('button[aria-label="Close modal"]');
		if (starCount > 0 && rng() < 0.6) {
			await stars.nth(Math.floor(rng() * starCount)).click({ timeout: 3000 });
			await pause(page, 300, 900);
			const done = modal.locator('button', { hasText: /done|submit|next/i });
			if (await done.count()) await done.first().click({ timeout: 3000 });
			else if (await close.count()) await close.first().click({ timeout: 3000 });
		} else if (await close.count()) {
			await close.first().click({ timeout: 3000 });
		} else {
			await page.keyboard.press('Escape');
		}
		await pause(page, 300, 800);
	}

	// Occasionally close the checklist widget too.
	const checklistClose = page.locator('#engagement-wrapper button[aria-label="Close checklist"]:visible');
	if ((await checklistClose.count()) > 0 && (forceCloseChecklist || rng() < 0.25)) {
		await checklistClose.first().click({ timeout: 3000 }).catch(() => {});
		await pause(page, 200, 600);
	}
}

async function click(page, selector, options = {}) {
	await handleEngagement(page);
	try {
		await page.click(selector, { timeout: 8000, ...options });
	} catch (err) {
		if (!/engagement-wrapper/.test(err.message)) throw err;
		// A nudge appeared mid-click or the checklist is covering the target.
		await handleEngagement(page, { forceCloseChecklist: true });
		await page.click(selector, { timeout: 8000, ...options });
	}
}

async function clickCell(page, gridSelector, x, y) {
	const scope = gridSelector === SELECTORS.enemyCells ? '.grid.computer-player' : '.grid.human-player';
	await click(page, `${scope} .grid-cell-${x}-${y}`);
}

async function gameOver(page) {
	return page.$eval(SELECTORS.restartSidebar, (el) => !el.classList.contains('hidden'));
}

async function isVisible(page, selector) {
	return page.$eval(selector, (el) => !el.classList.contains('hidden') && el.offsetParent !== null).catch(() => false);
}

function sessionUrl(job) {
	if (!job.userId) return CONFIG.url;
	const u = new URL(CONFIG.url);
	u.searchParams.set('synth_user_id', job.userId);
	u.searchParams.set('synth_device_id', job.deviceId);
	u.searchParams.set('synth_cohort', job.returning ? 'returning' : 'new');
	return u.toString();
}

function pause(page, minMs, maxMs) {
	return page.waitForTimeout(minMs + rng() * (maxMs - minMs));
}

function pick(list, r) {
	return list[Math.floor(r() * list.length)];
}

function pickWeighted(items, r) {
	const total = items.reduce((s, i) => s + i.weight, 0);
	let roll = r() * total;
	for (const item of items) {
		roll -= item.weight;
		if (roll <= 0) return item;
	}
	return items[items.length - 1];
}

function int(v, fallback) {
	const n = parseInt(v, 10);
	return Number.isFinite(n) ? n : fallback;
}

function parseArgs(argv) {
	const out = {};
	for (let i = 0; i < argv.length; i++) {
		const a = argv[i];
		if (!a.startsWith('--')) continue;
		const k = a.slice(2);
		const nextArg = argv[i + 1];
		if (nextArg === undefined || nextArg.startsWith('--')) out[k] = true;
		else { out[k] = nextArg; i++; }
	}
	return out;
}

// Minimal static file server for the repo root (so no extra tooling is needed).
function serveStatic(root, port) {
	const types = {
		'.html': 'text/html', '.js': 'text/javascript', '.css': 'text/css',
		'.png': 'image/png', '.jpg': 'image/jpeg', '.svg': 'image/svg+xml', '.ico': 'image/x-icon',
	};
	const server = http.createServer((req, res) => {
		let path = decodeURIComponent(new URL(req.url, 'http://x').pathname);
		if (path.endsWith('/')) path += 'index.html';
		const file = join(root, path);
		if (!file.startsWith(root) || !existsSync(file) || !statSync(file).isFile()) {
			res.writeHead(404); res.end(); return;
		}
		res.writeHead(200, { 'Content-Type': types[extname(file)] ?? 'application/octet-stream' });
		createReadStream(file).pipe(res);
	});
	return new Promise((ok, fail) => {
		server.once('error', (err) => {
			if (err.code === 'EADDRINUSE') {
				fail(new Error(`Port ${port} is in use. Pass --port <n>, or --url http://localhost:${port}/ if that is already Battleboat.`));
			} else fail(err);
		});
		server.listen(port, () => ok(server));
	});
}

// Deterministic PRNG so a run can be reproduced with --seed.
function mulberry32(a) {
	return function () {
		a |= 0; a = (a + 0x6D2B79F5) | 0;
		let t = Math.imul(a ^ (a >>> 15), 1 | a);
		t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
		return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
	};
}