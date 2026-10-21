import os
import math
from PIL import Image, ImageDraw, ImageFilter, ImageFont

os.makedirs('launcher/app/src/main/res/drawable-nodpi', exist_ok=True)
W, H = 1920, 1080

def create_gradient(top_color, bottom_color, w=W, h=H):
    base = Image.new('RGB', (w, h), top_color)
    top_r, top_g, top_b = top_color
    bot_r, bot_g, bot_b = bottom_color
    draw = ImageDraw.Draw(base)
    for y in range(h):
        t = y / float(h)
        r = int(top_r + (bot_r - top_r) * t)
        g = int(top_g + (bot_g - top_g) * t)
        b = int(top_b + (bot_b - top_b) * t)
        draw.line([(0, y), (w, y)], fill=(r, g, b))
    return base

def add_vignette(img):
    overlay = Image.new('RGBA', img.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(overlay)
    w, h = img.size
    for y in range(h):
        alpha = 0
        if y > int(h * 0.40):
            t = (y - int(h * 0.40)) / float(h * 0.60)
            alpha = int(255 * (t ** 1.5))
        # Left side darkening (for text readability)
        draw.line([(0, y), (w, y)], fill=(11, 15, 25, alpha))
    
    # Left vertical scrim (so hero text on the left is always crisp)
    for x in range(int(w * 0.5)):
        t = 1.0 - (x / float(w * 0.5))
        alpha = int(140 * (t ** 1.8))
        draw.line([(x, 0), (x, h)], fill=(11, 15, 25, alpha))

    return Image.alpha_composite(img.convert('RGBA'), overlay)

# 1. YouTube
def make_youtube():
    base = create_gradient((28, 12, 18), (8, 10, 16))
    glow = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    draw = ImageDraw.Draw(glow)
    
    # Dramatic red stage spotlight
    for r in range(600, 0, -15):
        alpha = int(140 * (1 - r / 600.0))
        draw.ellipse([W * 0.72 - r, H * 0.28 - r * 0.55, W * 0.72 + r, H * 0.28 + r * 0.55], fill=(235, 30, 40, alpha))
    
    # Subtle secondary cyan/blue fill light
    for r in range(400, 0, -20):
        alpha = int(50 * (1 - r / 400.0))
        draw.ellipse([W * 0.85 - r, H * 0.4 - r * 0.5, W * 0.85 + r, H * 0.4 + r * 0.5], fill=(30, 100, 220, alpha))

    glow = glow.filter(ImageFilter.GaussianBlur(80))
    combined = Image.alpha_composite(base.convert('RGBA'), glow)

    # Floating YouTube Play Button Symbol in the spotlight (Right quadrant)
    icon_layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    i_draw = ImageDraw.Draw(icon_layer)
    cx, cy = int(W * 0.72), int(H * 0.28)
    pw, ph = 180, 126
    # Red rounded rect
    i_draw.rounded_rectangle([cx - pw, cy - ph, cx + pw, cy + ph], radius=44, fill=(255, 0, 0, 180))
    # White play triangle
    tri_w, tri_h = 50, 60
    i_draw.polygon([(cx - tri_w // 2, cy - tri_h), (cx - tri_w // 2, cy + tri_h), (cx + tri_w, cy)], fill=(255, 255, 255, 220))
    icon_layer = icon_layer.filter(ImageFilter.GaussianBlur(2))

    combined = Image.alpha_composite(combined, icon_layer)
    res = add_vignette(combined)
    res.convert('RGB').save('launcher/app/src/main/res/drawable-nodpi/backdrop_youtube.webp', 'WEBP', quality=85)
    print("YouTube backdrop ready")

# 2. Durioo+
def make_durioo():
    durioo_img = Image.open('backup/durioo_onboarding.png').convert('RGBA')
    durioo_img = durioo_img.resize((W, H), Image.Resampling.LANCZOS)
    
    # Warm amber glow
    glow = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    draw = ImageDraw.Draw(glow)
    for r in range(500, 0, -15):
        alpha = int(90 * (1 - r / 500.0))
        draw.ellipse([W * 0.65 - r, H * 0.25 - r * 0.5, W * 0.65 + r, H * 0.25 + r * 0.5], fill=(245, 158, 11, alpha))
    glow = glow.filter(ImageFilter.GaussianBlur(90))
    combined = Image.alpha_composite(durioo_img, glow)

    res = add_vignette(combined)
    res.convert('RGB').save('launcher/app/src/main/res/drawable-nodpi/backdrop_durioo.webp', 'WEBP', quality=85)
    print("Durioo backdrop ready")

# 3. Netflix
def make_netflix():
    base = create_gradient((20, 10, 14), (8, 9, 12))
    glow = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    draw = ImageDraw.Draw(glow)
    
    # Crimson volumetric beam
    for r in range(550, 0, -15):
        alpha = int(130 * (1 - r / 550.0))
        draw.ellipse([W * 0.72 - r, H * 0.3 - r * 0.6, W * 0.72 + r, H * 0.3 + r * 0.6], fill=(229, 9, 20, alpha))
    glow = glow.filter(ImageFilter.GaussianBlur(85))
    combined = Image.alpha_composite(base.convert('RGBA'), glow)

    # Stylized Netflix ribbon 'N' in the glow
    n_layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    n_draw = ImageDraw.Draw(n_layer)
    cx, cy = int(W * 0.72), int(H * 0.3)
    nw, nh = 70, 160
    # Left bar
    n_draw.rectangle([cx - nw, cy - nh, cx - nw + 35, cy + nh], fill=(184, 29, 36, 210))
    # Right bar
    n_draw.rectangle([cx + nw - 35, cy - nh, cx + nw, cy + nh], fill=(184, 29, 36, 210))
    # Diagonal ribbon
    n_draw.polygon([
        (cx - nw, cy - nh),
        (cx - nw + 35, cy - nh),
        (cx + nw, cy + nh),
        (cx + nw - 35, cy + nh)
    ], fill=(229, 9, 20, 240))
    n_layer = n_layer.filter(ImageFilter.GaussianBlur(2))
    combined = Image.alpha_composite(combined, n_layer)

    res = add_vignette(combined)
    res.convert('RGB').save('launcher/app/src/main/res/drawable-nodpi/backdrop_netflix.webp', 'WEBP', quality=85)
    print("Netflix backdrop ready")

# 4. Disney+
def make_disney():
    base = create_gradient((6, 16, 40), (5, 8, 14))
    glow = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    draw = ImageDraw.Draw(glow)
    
    # Deep cyan & royal blue cosmic aura
    for r in range(600, 0, -15):
        alpha = int(120 * (1 - r / 600.0))
        draw.ellipse([W * 0.72 - r, H * 0.28 - r * 0.55, W * 0.72 + r, H * 0.28 + r * 0.55], fill=(0, 210, 211, alpha))
    for r in range(400, 0, -15):
        alpha = int(80 * (1 - r / 400.0))
        draw.ellipse([W * 0.65 - r, H * 0.35 - r * 0.5, W * 0.65 + r, H * 0.35 + r * 0.5], fill=(20, 80, 220, alpha))
    glow = glow.filter(ImageFilter.GaussianBlur(90))
    combined = Image.alpha_composite(base.convert('RGBA'), glow)

    # Iconic starlight arc
    arc_layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    a_draw = ImageDraw.Draw(arc_layer)
    cx, cy = int(W * 0.72), int(H * 0.28)
    a_draw.arc([cx - 200, cy - 140, cx + 200, cy + 180], start=190, end=350, fill=(255, 255, 255, 180), width=6)
    # Starburst sparkle at tip
    sx, sy = cx + 185, cy + 20
    a_draw.ellipse([sx - 15, sy - 15, sx + 15, sy + 15], fill=(0, 210, 211, 230))
    a_draw.ellipse([sx - 6, sy - 6, sx + 6, sy + 6], fill=(255, 255, 255, 255))
    arc_layer = arc_layer.filter(ImageFilter.GaussianBlur(2))
    combined = Image.alpha_composite(combined, arc_layer)

    res = add_vignette(combined)
    res.convert('RGB').save('launcher/app/src/main/res/drawable-nodpi/backdrop_disney.webp', 'WEBP', quality=85)
    print("Disney backdrop ready")

# 5. Apple TV
def make_appletv():
    base = create_gradient((20, 16, 32), (8, 9, 14))
    glow = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    draw = ImageDraw.Draw(glow)
    
    # Premium silver-violet spotlight
    for r in range(550, 0, -15):
        alpha = int(120 * (1 - r / 550.0))
        draw.ellipse([W * 0.72 - r, H * 0.28 - r * 0.55, W * 0.72 + r, H * 0.28 + r * 0.55], fill=(168, 85, 247, alpha))
    for r in range(350, 0, -15):
        alpha = int(60 * (1 - r / 350.0))
        draw.ellipse([W * 0.78 - r, H * 0.35 - r * 0.5, W * 0.78 + r, H * 0.35 + r * 0.5], fill=(220, 220, 240, alpha))
    glow = glow.filter(ImageFilter.GaussianBlur(85))
    combined = Image.alpha_composite(base.convert('RGBA'), glow)

    # Sleek Apple silhouette + "tv" in the spotlight
    app_layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    ap_draw = ImageDraw.Draw(app_layer)
    cx, cy = int(W * 0.72), int(H * 0.28)
    # Apple body
    ap_draw.ellipse([cx - 50, cy - 40, cx + 10, cy + 50], fill=(255, 255, 255, 200))
    ap_draw.ellipse([cx - 10, cy - 40, cx + 50, cy + 50], fill=(255, 255, 255, 200))
    # Leaf
    ap_draw.ellipse([cx - 10, cy - 70, cx + 25, cy - 45], fill=(255, 255, 255, 200))
    app_layer = app_layer.filter(ImageFilter.GaussianBlur(2))
    combined = Image.alpha_composite(combined, app_layer)

    res = add_vignette(combined)
    res.convert('RGB').save('launcher/app/src/main/res/drawable-nodpi/backdrop_appletv.webp', 'WEBP', quality=85)
    print("Apple TV backdrop ready")

make_youtube()
make_durioo()
make_netflix()
make_disney()
make_appletv()
