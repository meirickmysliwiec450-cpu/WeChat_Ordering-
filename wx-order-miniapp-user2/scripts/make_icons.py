import struct, zlib, os

def create_png(path, r, g, b, shape='circle'):
    size = 81
    pixels = []
    cx, cy, cr = 40, 40, 28
    for y in range(size):
        row = b'\x00'
        for x in range(size):
            dx, dy = x - cx, y - cy
            dist = (dx*dx + dy*dy) ** 0.5
            hit = False
            if shape == 'circle':   hit = dist < cr
            elif shape == 'star':   hit = dist < cr and (x + y) % 22 < 11
            elif shape == 'list':   hit = (8 <= x <= 72 and y % 22 < 10 and 8 <= y <= 72)
            elif shape == 'house':  hit = (10 <= x <= 70 and 32 <= y <= 75) or ((x-40)**2*3 + (y-32)**2 < 1400)
            elif shape == 'person': hit = (dist < cr*0.55 and y < 36) or (18 <= x <= 62 and 42 <= y <= 72)
            if hit: row += bytes([r, g, b, 255])
            else:   row += bytes([0, 0, 0, 0])
        pixels.append(row)
    raw = b''.join(pixels)
    def chunk(ctype, data):
        c = ctype + data
        return struct.pack('>I', len(data)) + c + struct.pack('>I', zlib.crc32(c) & 0xffffffff)
    ihdr = struct.pack('>IIBBBBB', size, size, 8, 6, 0, 0, 0)
    png = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', ihdr) + chunk(b'IDAT', zlib.compress(raw)) + chunk(b'IEND', b'')
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'wb') as f: f.write(png)

base = os.path.join(os.path.dirname(__file__), '..', 'images', 'tab')
GRAY = (140, 140, 140)
RED  = (192, 24, 24)

create_png(os.path.join(base, 'home.png'),        *GRAY, 'house')
create_png(os.path.join(base, 'home-active.png'),  *RED,  'house')
create_png(os.path.join(base, 'menu.png'),         *GRAY, 'list')
create_png(os.path.join(base, 'menu-active.png'),   *RED,  'list')
create_png(os.path.join(base, 'ai.png'),           *GRAY, 'star')
create_png(os.path.join(base, 'ai-active.png'),    *RED,  'star')
create_png(os.path.join(base, 'order.png'),        *GRAY, 'circle')
create_png(os.path.join(base, 'order-active.png'), *RED,  'circle')
create_png(os.path.join(base, 'me.png'),           *GRAY, 'person')
create_png(os.path.join(base, 'me-active.png'),    *RED,  'person')

print('Done! Created 10 icons.')
for f in sorted(os.listdir(base)):
    sz = os.path.getsize(os.path.join(base, f))
    print(f'  {f} ({sz} bytes)')
