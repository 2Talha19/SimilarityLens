// ========== 3D CARD CONFIGURATION ==========
const IMAGES = [
    { src: 'https://picsum.photos/seed/arch1/500/650', w: 220, h: 290, x: 8, y: 12, z: -80, rx: -4, ry: 14, delay: 0 },
    { src: 'https://picsum.photos/seed/city5/400/560', w: 180, h: 250, x: 78, y: 55, z: -200, rx: 5, ry: -8, delay: 0.3 },
    { src: 'https://picsum.photos/seed/nature9/480/600', w: 200, h: 270, x: 72, y: 8, z: -120, rx: -3, ry: -18, delay: 0.6 },
    { src: 'https://picsum.photos/seed/port2/420/540', w: 190, h: 260, x: 22, y: 60, z: -160, rx: 6, ry: 12, delay: 0.9 },
    { src: 'https://picsum.photos/seed/face3/360/480', w: 165, h: 225, x: 50, y: 70, z: -240, rx: -6, ry: 6, delay: 1.2 },
    { src: 'https://picsum.photos/seed/sky7/440/580', w: 195, h: 265, x: 85, y: 20, z: -100, rx: 4, ry: -14, delay: 1.5 },
    { src: 'https://picsum.photos/seed/forest1/380/500', w: 170, h: 230, x: 3, y: 40, z: -300, rx: -2, ry: 10, delay: 1.8 },
    { src: 'https://picsum.photos/seed/ocean4/460/580', w: 185, h: 255, x: 65, y: 78, z: -180, rx: 7, ry: -4, delay: 2.1 },
    { src: 'https://picsum.photos/seed/art3/420/580', w: 185, h: 255, x: 93, y: 85, z: -220, rx: 8, ry: -12, delay: 2.7 },
    { src: 'https://picsum.photos/seed/urban9/400/550', w: 175, h: 240, x: 8, y: 80, z: -110, rx: -3, ry: 5, delay: 3.0 },
];

const scene = document.getElementById('scene');
const cards = [];

// Create 3D cards
IMAGES.forEach((cfg) => {
    const card = document.createElement('div');
    card.className = 'card';
    card.style.cssText = `
        width:${cfg.w}px; height:${cfg.h}px;
        left:${cfg.x}%; top:${cfg.y}%;
        opacity:0;
        transform: translateX(-50%) translateY(-50%) translateZ(${cfg.z}px) rotateX(${cfg.rx}deg) rotateY(${cfg.ry}deg);
    `;

    const img = document.createElement('img');
    img.src = cfg.src;
    img.loading = 'lazy';
    img.draggable = false;

    const gloss = document.createElement('div');
    gloss.className = 'card-gloss';

    card.appendChild(img);
    card.appendChild(gloss);
    scene.appendChild(card);

    setTimeout(() => {
        card.style.transition = 'opacity 1.2s ease, transform 0.05s linear';
        card.style.opacity = '1';
    }, 600 + cfg.delay * 400);

    cards.push({
        el: card, cfg, baseX: cfg.x, baseY: cfg.y, baseZ: cfg.z, baseRX: cfg.rx, baseRY: cfg.ry,
        floatOffset: Math.random() * Math.PI * 2,
        floatSpeed: 0.4 + Math.random() * 0.4
    });
});

// ========== CURSOR TRACKING ==========
let mx = 0.5, my = 0.5;
let lx = 0.5, ly = 0.5;
let cx = 0, cy = 0, rx2 = 0, ry2 = 0;

const cur = document.getElementById('cursor');
const ring = document.getElementById('cursor-ring');

document.addEventListener('mousemove', e => {
    mx = e.clientX / window.innerWidth;
    my = e.clientY / window.innerHeight;
    cx = e.clientX;
    cy = e.clientY;
});

// Hover effects for buttons
document.querySelectorAll('.btn-primary, .btn-ghost, .nav-item').forEach(btn => {
    btn.addEventListener('mouseenter', () => {
        cur.style.width = '20px';
        cur.style.height = '20px';
        ring.style.width = '60px';
        ring.style.height = '60px';
        ring.style.opacity = '0.4';
    });
    btn.addEventListener('mouseleave', () => {
        cur.style.width = '12px';
        cur.style.height = '12px';
        ring.style.width = '40px';
        ring.style.height = '40px';
        ring.style.opacity = '1';
    });
});

// ========== PARTICLE BACKGROUND ==========
const canvas = document.getElementById('bgCanvas');
const ctx = canvas.getContext('2d');
const PARTICLE_COUNT = 120;
let particles = [];

function initCanvas() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    particles = Array.from({ length: PARTICLE_COUNT }, () => ({
        x: Math.random() * canvas.width,
        y: Math.random() * canvas.height,
        r: Math.random() * 1.5 + 0.3,
        vx: (Math.random() - 0.5) * 0.2,
        vy: (Math.random() - 0.5) * 0.2,
        alpha: Math.random() * 0.5 + 0.1,
        color: Math.random() > 0.5 ? '167,139,250' : '236,72,153',
    }));
}

window.addEventListener('resize', initCanvas);
initCanvas();

let t = 0;
function tick() {
    t += 0.012;
    lx += (mx - lx) * 0.04;
    ly += (my - ly) * 0.04;

    // Cursor Follow
    rx2 += (cx - rx2) * 0.14;
    ry2 += (cy - ry2) * 0.14;
    cur.style.left = cx + 'px';
    cur.style.top = cy + 'px';
    ring.style.left = rx2 + 'px';
    ring.style.top = ry2 + 'px';

    // Draw particles
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    particles.forEach(p => {
        p.x += p.vx;
        p.y += p.vy;
        if (p.x < 0) p.x = canvas.width;
        if (p.x > canvas.width) p.x = 0;
        if (p.y < 0) p.y = canvas.height;
        if (p.y > canvas.height) p.y = 0;
        ctx.beginPath();
        ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
        ctx.fillStyle = `rgba(${p.color},${p.alpha})`;
        ctx.fill();
    });

    // Update 3D cards based on mouse position
    const dx = (lx - 0.5) * 2;
    const dy = (ly - 0.5) * 2;

    cards.forEach(c => {
        const float = Math.sin(t * c.floatSpeed + c.floatOffset) * 18;
        const floatX = Math.cos(t * c.floatSpeed * 0.7 + c.floatOffset) * 10;
        const px = c.baseX + dx * (Math.abs(c.baseZ) / 8);
        const py = c.baseY + dy * (Math.abs(c.baseZ) / 10);
        const rX = c.baseRX + dy * 6;
        const rY = c.baseRY - dx * 10;

        c.el.style.transform = `
            translateX(calc(-50% + ${floatX}px))
            translateY(calc(-50% + ${float}px))
            translateZ(${c.baseZ}px)
            rotateX(${rX}deg)
            rotateY(${rY}deg)
        `;
        c.el.style.left = px + '%';
        c.el.style.top = py + '%';
    });

    requestAnimationFrame(tick);
}

tick();

// ========== ACTIVE NAVBAR LINK ==========
document.querySelectorAll('.nav-item').forEach(link => {
    const href = link.getAttribute('href');
    if (href === window.location.pathname.split('/').pop() || 
        (window.location.pathname.endsWith('/') && href === 'home.html')) {
        link.classList.add('active');
    }
});