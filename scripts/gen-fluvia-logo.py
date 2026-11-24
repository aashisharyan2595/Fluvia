# Regenerates assets/fluvia/*.svg. Needs fonttools. The wordmark is Outfit Bold (SIL OFL 1.1) converted to outlines.
from fontTools.ttLib import TTFont
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.transformPen import TransformPen

import os
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
FONT = os.path.join(ROOT, 'androidApp/src/main/res/font/outfit_bold.ttf')
OUT = os.path.join(ROOT, 'assets/fluvia')
def text_path(text, size, x=0, y=0, tracking=0):
    f=TTFont(FONT); gs=f.getGlyphSet(); cmap=f.getBestCmap(); upm=f['head'].unitsPerEm
    s=size/upm; pen=SVGPathPen(gs); cx=x
    hmtx=f['hmtx']
    for ch in text:
        g=cmap[ord(ch)]
        tp=TransformPen(pen,(s,0,0,-s,cx,y))
        gs[g].draw(tp)
        cx+=hmtx[g][0]*s+tracking
    return pen.getCommands(), cx

# Mark: a glass drop holding a flowing "F"
MARK_BG='''
  <defs>
    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0" stop-color="#0B0C1B"/><stop offset="1" stop-color="#1B1D3A"/>
    </linearGradient>
    <linearGradient id="flow" gradientUnits="userSpaceOnUse" x1="200" y1="150" x2="340" y2="390">
      <stop offset="0" stop-color="#F0E5D0"/><stop offset="0.55" stop-color="#F6C88E"/><stop offset="1" stop-color="#F1AA55"/>
    </linearGradient>
    <linearGradient id="glass" x1="0.2" y1="0" x2="0.8" y2="1">
      <stop offset="0" stop-color="#FFFFFF" stop-opacity="0.34"/><stop offset="1" stop-color="#FFFFFF" stop-opacity="0.06"/>
    </linearGradient>
    <linearGradient id="rim" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0" stop-color="#FFFFFF" stop-opacity="0.85"/><stop offset="0.5" stop-color="#FFFFFF" stop-opacity="0.12"/><stop offset="1" stop-color="#FFFFFF" stop-opacity="0.45"/>
    </linearGradient>
  </defs>'''
# drop: point at top, round bottom, centered at 256
DROP='M256 74 C 256 74 128 206 128 312 C 128 382 185 438 256 438 C 327 438 384 382 384 312 C 384 206 256 74 256 74 Z'
# flowing F: stem + two wave arms
F_STEM='M222 372 L222 236 C222 190 246 168 292 168 L316 168'
F_ARM ='M222 268 C 254 244, 286 292, 328 262'
def mark(background=True):
    bg='<rect width="512" height="512" rx="116" fill="url(#bg)"/>' if background else ''
    return f'''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" role="img" aria-label="Fluvia">
{MARK_BG}
  {bg}
  <path d="{DROP}" fill="url(#glass)" stroke="url(#rim)" stroke-width="4"/>
  <path d="M176 270 C 180 222 206 184 238 150" fill="none" stroke="#fff" stroke-opacity="0.55" stroke-width="10" stroke-linecap="round"/>
  <g fill="none" stroke="url(#flow)" stroke-width="24" stroke-linecap="round" stroke-linejoin="round">
    <line x1="190" y1="300" x2="190" y2="372"/><line x1="223" y1="250" x2="223" y2="372"/><line x1="256" y1="214" x2="256" y2="372"/><line x1="289" y1="262" x2="289" y2="372"/><line x1="322" y1="312" x2="322" y2="372"/>
    
  </g>
</svg>
'''
open(os.path.join(OUT,'fluvia-mark.svg'),'w').write(mark(True))
open(os.path.join(OUT,'fluvia-mark-transparent.svg'),'w').write(mark(False))

# wordmark lockup: mark (transparent bg, scaled) + outlined text
d,w=text_path('Fluvia',190,0,0,tracking=2)
def lockup(textcolor):
    W=int(560+w)+40
    return f'''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} 512" role="img" aria-label="Fluvia">
{MARK_BG}
  <g transform="translate(0 0)">
    <rect width="512" height="512" rx="116" fill="url(#bg)"/>
    <path d="{DROP}" fill="url(#glass)" stroke="url(#rim)" stroke-width="4"/>
    <path d="M176 270 C 180 222 206 184 238 150" fill="none" stroke="#fff" stroke-opacity="0.55" stroke-width="10" stroke-linecap="round"/>
    <g fill="none" stroke="url(#flow)" stroke-width="24" stroke-linecap="round" stroke-linejoin="round"><line x1="190" y1="300" x2="190" y2="372"/><line x1="223" y1="250" x2="223" y2="372"/><line x1="256" y1="214" x2="256" y2="372"/><line x1="289" y1="262" x2="289" y2="372"/><line x1="322" y1="312" x2="322" y2="372"/></g>
  </g>
  <path transform="translate(560 336)" d="{d}" fill="{textcolor}"/>
</svg>
'''
open(os.path.join(OUT,'fluvia-logo-light.svg'),'w').write(lockup('#0B0C1B'))
open(os.path.join(OUT,'fluvia-logo-dark.svg'),'w').write(lockup('#F0E5D0'))
