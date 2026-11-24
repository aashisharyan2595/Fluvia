#!/usr/bin/env node
/**
 * Generates androidApp/src/main/kotlin/com/metrolist/android/ui/fluvia/FluviaIconPaths.kt.
 *
 * Every icon of every pack is reduced to one SVG path-data string (all shapes merged, a leading relative `m`
 * made absolute) so the app can build ImageVectors with PathParser at runtime.
 *
 * Needs the icon packages, installed in a scratch directory outside the repository:
 *
 *   mkdir /tmp/fluvia-icons && cd /tmp/fluvia-icons
 *   npm install lucide-static feather-icons @tabler/icons @phosphor-icons/core @material-symbols/svg-400
 *
 * Then, from the repository root:
 *
 *   node scripts/gen-icon-packs.mjs /tmp/fluvia-icons/node_modules
 *
 * The node_modules directory may also come from the FLUVIA_ICON_MODULES environment variable. The Fluvia pack's
 * own icons are the inner SVG markup in scripts/fluvia-icons.json. Package licences are read from each package.json.
 */
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const modules = process.argv[2] ?? process.env.FLUVIA_ICON_MODULES;
if (!modules) {
  console.error('usage: node scripts/gen-icon-packs.mjs <path to node_modules> (or set FLUVIA_ICON_MODULES)');
  process.exit(1);
}
const OUT = path.join(root, 'androidApp/src/main/kotlin/com/metrolist/android/ui/fluvia/FluviaIconPaths.kt');
const R = (p) => path.join(modules, p);
const FLUVIA_LICENCE = 'GPL-3.0';

const num = (s, d = 0) => (s == null || s === '' ? d : parseFloat(s));
function attr(tag, n) {
  const m = tag.match(new RegExp('(?:^|\\s)' + n + '="([^"]*)"'));
  return m ? m[1] : null;
}
const f = (n) => +n.toFixed(3);

function toPath(svg) {
  const out = [];
  const re = /<(path|circle|ellipse|rect|line|polyline|polygon)\b([^>]*?)\/?>/g;
  let m;
  while ((m = re.exec(svg))) {
    const t = m[1];
    const a = ' ' + m[2];
    if (attr(a, 'stroke') === 'none' && attr(a, 'fill') === 'none') continue;
    if (attr(a, 'fill') === 'none' && attr(a, 'stroke') == null && t === 'rect' && !attr(a, 'rx')) continue;
    if (t === 'rect' && attr(a, 'fill') === 'none' && attr(a, 'stroke') == null) continue;
    if (t === 'path') {
      if (attr(a, 'd') === 'M0 0h24v24H0z') continue;
      out.push(attr(a, 'd'));
    } else if (t === 'circle') {
      const cx = num(attr(a, 'cx')), cy = num(attr(a, 'cy')), r = num(attr(a, 'r'));
      out.push(`M${f(cx - r)} ${f(cy)}a${r} ${r} 0 1 0 ${f(2 * r)} 0a${r} ${r} 0 1 0 ${f(-2 * r)} 0z`);
    } else if (t === 'ellipse') {
      const cx = num(attr(a, 'cx')), cy = num(attr(a, 'cy')), rx = num(attr(a, 'rx')), ry = num(attr(a, 'ry'));
      out.push(`M${f(cx - rx)} ${f(cy)}a${rx} ${ry} 0 1 0 ${f(2 * rx)} 0a${rx} ${ry} 0 1 0 ${f(-2 * rx)} 0z`);
    } else if (t === 'rect') {
      const x = num(attr(a, 'x')), y = num(attr(a, 'y')), w = num(attr(a, 'width')), h = num(attr(a, 'height'));
      let rx = num(attr(a, 'rx')), ry = num(attr(a, 'ry'), rx);
      if (!attr(a, 'rx') && attr(a, 'ry')) rx = ry;
      rx = Math.min(rx, w / 2);
      ry = Math.min(ry, h / 2);
      out.push(
        rx || ry
          ? `M${f(x + rx)} ${f(y)}h${f(w - 2 * rx)}a${rx} ${ry} 0 0 1 ${rx} ${ry}v${f(h - 2 * ry)}a${rx} ${ry} 0 0 1 ${-rx} ${ry}h${f(-(w - 2 * rx))}a${rx} ${ry} 0 0 1 ${-rx} ${-ry}v${f(-(h - 2 * ry))}a${rx} ${ry} 0 0 1 ${rx} ${-ry}z`
          : `M${x} ${y}h${w}v${h}h${-w}z`,
      );
    } else if (t === 'line') {
      out.push(`M${attr(a, 'x1')} ${attr(a, 'y1')}L${attr(a, 'x2')} ${attr(a, 'y2')}`);
    } else {
      const pts = attr(a, 'points').trim().split(/[\s,]+/);
      let d = 'M' + pts[0] + ' ' + pts[1];
      for (let i = 2; i < pts.length; i += 2) d += 'L' + pts[i] + ' ' + pts[i + 1];
      out.push(d + (t === 'polygon' ? 'z' : ''));
    }
  }
  const N = /-?(?:\d+\.?\d*|\.\d+)(?:e[-+]?\d+)?/g;
  return out
    .map((d) => {
      const m = d.match(/^\s*m\s*/);
      if (!m) return d;
      const rest = d.slice(m[0].length);
      const nums = rest.match(N);
      if (!nums || nums.length < 2) return d;
      let cnt = 0, pos = 0, mm;
      N.lastIndex = 0;
      while ((mm = N.exec(rest)) && cnt < 2) {
        cnt++;
        pos = N.lastIndex;
      }
      const tail = rest.slice(pos);
      return 'M' + nums[0] + ' ' + nums[1] + (/^\s*[-.\d]/.test(tail) ? 'l' : '') + tail;
    })
    .join('');
}

const read = (p) => (fs.existsSync(p) ? fs.readFileSync(p, 'utf8') : null);

const PACKS = {
  lucide: { object: 'Lucide', label: 'Lucide', pkg: 'lucide-static', stroke: true, viewport: 24, minY: 0,
    file: (n) => R('lucide-static/icons/' + n + '.svg'), fileF: null },
  feather: { object: 'Feather', label: 'Feather', pkg: 'feather-icons', stroke: true, viewport: 24, minY: 0,
    file: (n) => R('feather-icons/dist/icons/' + n + '.svg'), fileF: null },
  tabler: { object: 'Tabler', label: 'Tabler', pkg: '@tabler/icons', stroke: true, viewport: 24, minY: 0,
    file: (n) => R('@tabler/icons/icons/outline/' + n + '.svg'), fileF: (n) => R('@tabler/icons/icons/filled/' + n + '.svg') },
  phosphor: { object: 'Phosphor', label: 'Phosphor', pkg: '@phosphor-icons/core', stroke: false, viewport: 256, minY: 0,
    file: (n) => R('@phosphor-icons/core/assets/regular/' + n + '.svg'), fileF: (n) => R('@phosphor-icons/core/assets/fill/' + n + '-fill.svg') },
  material: { object: 'Material', label: 'Material Symbols', pkg: '@material-symbols/svg-400', stroke: false, viewport: 960, minY: -960,
    file: (n) => R('@material-symbols/svg-400/outlined/' + n + '.svg'), fileF: (n) => R('@material-symbols/svg-400/outlined/' + n + '-fill.svg') },
};

// key: [lucide, feather, tabler, phosphor, material]
const K = {
  user: ['user', 'user', 'user', 'user', 'person'],
  wifiOff: ['wifi-off', 'wifi-off', 'wifi-off', 'wifi-slash', 'wifi_off'],
  lock: ['lock', 'lock', 'lock', 'lock', 'lock'],
  arrowDown: ['arrow-down', 'arrow-down', 'arrow-down', 'arrow-down', 'arrow_downward'],
  download: ['download', 'download', 'download', 'download-simple', 'download'],
  heart: ['heart', 'heart', 'heart', 'heart', 'favorite'],
  search: ['search', 'search', 'search', 'magnifying-glass', 'search'],
  edit: ['pencil', 'edit-2', 'pencil', 'pencil-simple', 'edit'],
  check: ['check', 'check', 'check', 'check', 'check'],
  close: ['x', 'x', 'x', 'x', 'close'],
  chevL: ['chevron-left', 'chevron-left', 'chevron-left', 'caret-left', 'chevron_left'],
  chevR: ['chevron-right', 'chevron-right', 'chevron-right', 'caret-right', 'chevron_right'],
  chevD: ['chevron-down', 'chevron-down', 'chevron-down', 'caret-down', 'keyboard_arrow_down'],
  pause: ['pause', 'pause', 'player-pause', 'pause', 'pause'],
  play: ['play', 'play', 'player-play', 'play', 'play_arrow'],
  next: ['skip-forward', 'skip-forward', 'player-skip-forward', 'skip-forward', 'skip_next'],
  prev: ['skip-back', 'skip-back', 'player-skip-back', 'skip-back', 'skip_previous'],
  shuffle: ['shuffle', 'shuffle', 'arrows-shuffle', 'shuffle', 'shuffle'],
  repeat: ['repeat', 'repeat', 'repeat', 'repeat', 'repeat'],
  palette: ['palette', 'droplet', 'palette', 'palette', 'palette'],
  expand: ['maximize', 'maximize', 'arrows-maximize', 'arrows-out', 'fullscreen'],
  minus: ['minus', 'minus', 'minus', 'minus', 'remove'],
  plus: ['plus', 'plus', 'plus', 'plus', 'add'],
  lyrics: ['align-left', 'align-left', 'align-left', 'text-align-left', 'lyrics'],
  queue: ['list-music', 'list', 'playlist', 'queue', 'queue_music'],
  cast: ['cast', 'cast', 'cast', 'screencast', 'cast'],
  moon: ['moon', 'moon', 'moon', 'moon', 'bedtime'],
  vibrate: ['vibrate', 'smartphone', 'device-mobile-vibration', 'vibrate', 'mobile_2'],
  more: ['ellipsis', 'more-horizontal', 'dots', 'dots-three', 'more_horiz'],
  home: ['house', 'home', 'home', 'house', 'home'],
  library: ['library', 'book', 'books', 'books', 'library_music'],
  settings: ['sliders-horizontal', 'sliders', 'adjustments-horizontal', 'sliders-horizontal', 'tune'],
  alert: ['triangle-alert', 'alert-triangle', 'alert-triangle', 'warning', 'warning'],
  share: ['share', 'share', 'share', 'share-network', 'share'],
  radio: ['radio', 'radio', 'radio', 'radio', 'radio'],
  playlistAdd: ['list-plus', 'list', 'playlist-add', 'playlist', 'playlist_add'],
  queueAdd: ['list-plus', 'list', 'playlist-add', 'queue', 'add_to_queue'],
  queueNext: ['list-start', 'list', 'playlist', 'list-plus', 'queue_play_next'],
  bookmark: ['bookmark', 'bookmark', 'bookmark', 'bookmark-simple', 'bookmark'],
  album: ['disc-3', 'disc', 'disc', 'disc', 'album'],
  mic: ['mic', 'mic', 'microphone', 'microphone', 'mic'],
  users: ['users', 'users', 'users', 'users', 'group'],
  pin: ['pin', 'map-pin', 'pin', 'push-pin', 'keep'],
  queueRemove: ['list-x', 'minus-square', 'playlist-x', 'list-dashes', 'playlist_remove'],
  flag: ['flag', 'flag', 'flag', 'flag', 'flag'],
  phone: ['smartphone', 'smartphone', 'device-mobile', 'device-mobile', 'mobile'],
  speaker: ['speaker', 'speaker', 'device-speaker', 'speaker-hifi', 'speaker'],
  monitor: ['monitor', 'monitor', 'device-desktop', 'monitor', 'monitor'],
  tv: ['tv', 'tv', 'device-tv', 'television', 'tv'],
};
// Keys that also get a filled ("F") variant when the pack ships one.
const FILLED = ['pause', 'play', 'next', 'prev', 'more', 'heart', 'home', 'search', 'library', 'settings'];

const missing = [];
const packs = [];

// Fluvia first: it is the fallback for every other pack.
const inner = JSON.parse(fs.readFileSync(path.join(root, 'scripts/fluvia-icons.json'), 'utf8'));
const fluvia = { object: 'Fluvia', label: 'Fluvia', licence: FLUVIA_LICENCE, stroke: true, viewport: 24, minY: 0, paths: {} };
for (const key of Object.keys(K)) {
  if (!inner[key]) {
    missing.push('fluvia:' + key);
    continue;
  }
  fluvia.paths[key] = toPath(inner[key]);
}
packs.push(fluvia);

Object.entries(PACKS).forEach(([id, P], pi) => {
  const licence = JSON.parse(fs.readFileSync(R(P.pkg + '/package.json'), 'utf8')).license;
  const pack = { object: P.object, label: P.label, licence, stroke: P.stroke, viewport: P.viewport, minY: P.minY, paths: {} };
  for (const key of Object.keys(K)) {
    const name = K[key][pi];
    const base = read(P.file(name));
    if (!base) {
      missing.push(id + ':' + key + '=' + name);
      continue;
    }
    pack.paths[key] = toPath(base);
    if (FILLED.includes(key) && P.fileF) {
      const filled = read(P.fileF(name));
      if (filled) pack.paths[key + 'F'] = toPath(filled);
    }
  }
  packs.push(pack);
});

if (missing.length) {
  console.error('missing icons: ' + missing.join(', '));
  process.exit(1);
}

const esc = (s) => s.replace(/\\/g, '\\\\').replace(/"/g, '\\"').replace(/\$/g, '\\$');
let kt = `/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

// Generated by scripts/gen-icon-packs.mjs. Do not edit by hand.

package com.metrolist.android.ui.fluvia

`;
for (const p of packs) {
  kt += `internal object ${p.object}IconPack {
    const val LABEL = "${esc(p.label)}"
    const val LICENCE = "${esc(p.licence)}"
    const val STROKE = ${p.stroke}
    const val VIEWPORT = ${p.viewport}f
    const val MIN_Y = ${p.minY}f

    val PATHS: Map<String, String> =
        mapOf(
${Object.entries(p.paths).map(([k, v]) => `            "${k}" to "${esc(v)}",`).join('\n')}
        )
}

`;
}
kt = kt.replace(/\n+$/, '\n');
fs.writeFileSync(OUT, kt);
console.log('wrote ' + path.relative(root, OUT) + ' (' + kt.length + ' bytes)');
for (const p of packs) {
  const longest = Math.max(...Object.values(p.paths).map((v) => v.length));
  console.log(`${p.object}: ${Object.keys(p.paths).length} paths, licence ${p.licence}, longest path ${longest} chars`);
}
