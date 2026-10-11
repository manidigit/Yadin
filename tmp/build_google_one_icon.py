import os
import subprocess

# Generate high-resolution SVG of official Google One logo
svg_content = """<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="0 0 512 512">
  <rect width="512" height="512" fill="#FFFFFF"/>
  <g transform="translate(15, 10)">
    <!-- Blue top-left diagonal beak -->
    <path d="M 160,230 L 250,140 C 270,120 295,120 315,140" 
          fill="none" stroke="#4285F4" stroke-width="56" stroke-linecap="round" stroke-linejoin="round"/>
    <!-- Red top-right curve -->
    <path d="M 295,120 L 330,155 C 350,175 350,205 330,225 L 295,260" 
          fill="none" stroke="#EA4335" stroke-width="56" stroke-linecap="round" stroke-linejoin="round"/>
    <!-- Yellow vertical stem -->
    <path d="M 315,220 L 315,330" 
          fill="none" stroke="#FBBC04" stroke-width="56" stroke-linecap="round"/>
    <!-- Green bottom base -->
    <path d="M 315,330 L 235,330 C 205,330 185,350 185,375 C 185,400 205,420 235,420 L 325,420" 
          fill="none" stroke="#34A853" stroke-width="56" stroke-linecap="round" stroke-linejoin="round"/>
  </g>
</svg>
"""

svg_path = "/tmp/google_one_master.svg"
png_path = "app/src/main/res/drawable/ic_launcher_foreground_asset.png"

with open(svg_path, "w") as f:
    f.write(svg_content)

# Render high-res PNG using ImageMagick convert
cmd = [
    "convert",
    "-size", "512x512",
    "xc:white",
    # Blue beak
    "-stroke", "#4285F4", "-strokewidth", "56", "-fill", "none",
    "-draw", "path 'M 165,235 L 255,145 A 35,35 0 0,1 305,145'",
    # Red curve
    "-stroke", "#EA4335",
    "-draw", "path 'M 305,145 L 335,175 A 35,35 0 0,1 335,225 L 305,255'",
    # Yellow stem
    "-stroke", "#FBBC04",
    "-draw", "path 'M 305,225 L 305,335'",
    # Green foot
    "-stroke", "#34A853",
    "-draw", "path 'M 305,335 L 230,335 A 30,30 0 0,0 200,365 A 30,30 0 0,0 230,395 L 325,395'",
    # Round caps
    "-fill", "#4285F4", "-draw", "circle 165,235 193,235",
    "-fill", "#34A853", "-draw", "circle 325,395 353,395",
    png_path
]

subprocess.run(cmd, check=True)

# Generate density mipmaps
base_dir = "app/src/main/res"
densities = [("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192)]

for name, size in densities:
    target_dir = os.path.join(base_dir, f"mipmap-{name}")
    os.makedirs(target_dir, exist_ok=True)
    
    # Remove old webp files to prevent conflicts
    for old_file in ["ic_launcher.webp", "ic_launcher_round.webp"]:
        fpath = os.path.join(target_dir, old_file)
        if os.path.exists(fpath):
            os.remove(fpath)
            
    # Resize square launcher
    sq_path = os.path.join(target_dir, "ic_launcher.png")
    subprocess.run(["convert", png_path, "-resize", f"{size}x{size}!", f"PNG32:{sq_path}"], check=True)
    
    # Create circular launcher
    rd_path = os.path.join(target_dir, "ic_launcher_round.png")
    radius = size // 2
    circle_cmd = [
        "convert", png_path, "-resize", f"{size}x{size}!",
        "(", "-size", f"{size}x{size}", "xc:none", "-fill", "white", "-draw", f"circle {radius},{radius} {radius},0", ")",
        "-alpha", "set", "-compose", "DstIn", "-composite", f"PNG32:{rd_path}"
    ]
    subprocess.run(circle_cmd, check=True)

print("Google One icon generated successfully!")
