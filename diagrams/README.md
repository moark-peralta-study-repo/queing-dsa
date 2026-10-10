# Paper-ready diagrams (IEEE-style)

Rendered from mermaid sources in `sources/`, verified against the live code (2026-10-10).

## PNGs (`png/`)

| File | Source | Use |
| --- | --- | --- |
| `light-architecture.png` | `sources/architecture-light.mmd` | System architecture — paper (white bg, black-on-white, 2× print scale) |
| `light-join-flow.png` | `sources/join-flow.mmd` | Join/track/serve data flow (sequence) — paper |
| `light-state-machine.png` | `sources/state-machine.mmd` | Queue ticket state machine — paper |
| `light-erd.png` | `sources/erd.mmd` | Database ERD (15 tables) — paper (2× print scale) |
| `dark-architecture.png` | `sources/architecture.mmd` | Same diagrams, HTML-look (dark bg, cyan/emerald/violet accents) — website/blog |
| `dark-join-flow.png` | `sources/join-flow.mmd` | |
| `dark-state-machine.png` | `sources/state-machine.mmd` | |
| `dark-erd.png` | `sources/erd.mmd` | |

`light-*.png` are the ones to drop into the IEEE paper (`\includegraphics`). They are
black-on-white, monochrome, DejaVu Sans (embeds fine in LaTeX PDFs).

## Re-rendering

Sources + theme configs live in `sources/`. Toolchain used: `@mermaid-js/mermaid-cli` (mmdc 11.x)
with puppeteer (needs `--no-sandbox` as root; config in `/root/diagrams-queing/pup.json`).

```bash
cd /root/diagrams-queing   # npm project with mmdc + theme json files
mmdc -i /root/dev/enyu/queing-dsa/diagrams/sources/architecture-light.mmd \
     -o /root/dev/enyu/queing-dsa/diagrams/png/light-architecture.png \
     -c theme-light.json -b white -w 1150 -s 2 -p pup.json
# repeat per diagram; ERD uses -w 1700 -s 1.5, dark set uses theme-dark.json -b transparent
```

Notes:
- `architecture.mmd` (dark) and `architecture-light.mmd` (paper) differ only in the
  `classDef`/`class`/`linkStyle` lines (colored nodes vs. monochrome).
- The mermaid sources mirror the blocks in the Obsidian notes
  (`DSA-QUEING SYSTEM/Architecture.md`, `Database ERD.md`, `Queue Lifecycle.md`).
- Facts baked in (verify before re-using): WebServer is embedded in the Swing UI at **:8080**,
  toggled from the menu bar (not started by `Main` at boot); `Main` = init tables →
  `SeedDemoData.seedIfEmpty` → Swing. Swing calls services **and** DAOs directly (no controllers in the
  request path — the `controller/` layer is unwired). Status vocabulary = `model/QueueStatus.java`
  (Waiting / Checked In / In Consultation / For Laboratory / For Pharmacy / Discharged / Completed /
  No Show). `skip()` → `No Show` + `SKIPPED` audit event.
