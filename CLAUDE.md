# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status: greenfield (no code yet)

This repository contains **no application code, build tooling, or package manifest yet**. It is not a git repository. The only inputs are two specification documents under `project_materials/`. Do not look for source files — there are none; architecture, language, and framework choices are still open.

## Project overview

**智能充电桩运营服务平台** (Smart EV Charging Station Operation Service Platform).

The intended surface areas, fixed by the spec (`智能充电桩运营服务平台_01.pdf`), are:

- **H5** — mobile web frontend
- **Web** — web frontend
- **AI Agent** — AI agent capabilities

The second document (`分级容量目标选择和部署要求_02.pdf`) defines graded capacity targets and deployment requirements.

## Source documents

- `project_materials/智能充电桩运营服务平台_01.pdf` — platform spec, organized into H5 / Web / AI Agent sections, each with a numbered list of requirements.
- `project_materials/分级容量目标选择和部署要求_02.pdf` — capacity target selection and deployment requirements.

**These PDFs are image-based (rendered/scanned).** `pdftotext` only extracts headings and bare numbers, not the body text. To read the actual requirements, open them in a PDF viewer or OCR/convert pages to images — do not rely on text extraction (`pdftotext` is available at `D:\git\Git\mingw64\bin\pdftotext.exe`).

## Capacity / deployment tiers (from PDF 02)

The deployment spec defines four tiers, **L1 → L4**, with scaling targets (raw numbers extracted from the sparse text layer; exact column semantics must be confirmed against the visual PDF):

| Tier | Capacity figures (extracted) | QPS figures (extracted) |
|------|------------------------------|-------------------------|
| L1   | 10,000 · 1,000 · 10 · 100 · 100 | 20 · 50 · 30 · 10 · 10 · 5 QPS |
| L2   | 100,000 · 10,000 · 100 · 1,000 · 1,000 | 100 · 500 · 300 · 50 · 50 · 30 QPS |
| L3   | 1,000,000 · 100,000 · 1,000 · 10,000 · 10,000 | 500 · 2,000 · 1,000 · 300 · 300 · 200 QPS |
| L4   | 10,000,000 · 1,000,000 · 10,000 · 100,000 · 100,000 | 2,000 · 10,000 · 5,000 · 1,000 · 1,000 · 1,000 QPS |

Each tier also references an **Agent** (AI Agent) component. Additional figures appear in the raw text (e.g. L3 `5`, `2,000/…`; L4 `100,000`; a `2–5` figure and `20,000/…` near the end) whose meaning is only clear from the visual content. Treat these tables as a rough scale reference, not a precise specification, until the PDFs are visually reviewed.
