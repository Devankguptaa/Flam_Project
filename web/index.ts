const overlay = document.getElementById('overlay') as HTMLDivElement;
const img = document.getElementById('frame') as HTMLImageElement;

async function load() {
  const resp = await fetch('sample-frame-base64.txt');
  const b64 = await resp.text();
  img.src = 'data:image/png;base64,' + b64.trim();
}

let last = performance.now();
let frames = 0;
function loop() {
  const now = performance.now();
  frames++;
  const dt = now - last;
  if (dt >= 1000) {
    const fps = (frames * 1000) / dt;
    const w = img.naturalWidth || 0;
    const h = img.naturalHeight || 0;
    overlay.textContent = `${w}x${h} | ${fps.toFixed(1)} FPS`;
    last = now;
    frames = 0;
  }
  requestAnimationFrame(loop);
}

load().then(() => requestAnimationFrame(loop));
