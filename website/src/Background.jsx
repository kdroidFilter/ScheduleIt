import { useEffect, useRef } from 'react'

const VERT_FULL = `
attribute vec2 a_position;
varying vec2 v_uv;
void main() {
  v_uv = a_position * 0.5 + 0.5;
  gl_Position = vec4(a_position, 0.0, 1.0);
}
`

const FRAG_BG = `
precision highp float;
varying vec2 v_uv;
uniform float u_time;
uniform vec2 u_res;

float blob(vec2 uv, vec2 c, float r) {
  float d = distance(uv, c);
  return smoothstep(r, 0.0, d);
}

void main() {
  vec2 uv = v_uv;
  uv.x *= u_res.x / u_res.y;
  float t = u_time * 0.06;

  vec2 c1 = vec2(0.40 + 0.30 * sin(t * 0.9), 0.30 + 0.25 * cos(t * 1.1));
  vec2 c2 = vec2(1.10 + 0.25 * cos(t * 0.7), 0.55 + 0.20 * sin(t * 1.3));
  vec2 c3 = vec2(0.70 + 0.35 * sin(t * 0.5), 1.00 + 0.20 * cos(t * 0.8));

  vec3 col = vec3(0.043, 0.051, 0.071);
  col += vec3(0.30, 0.55, 1.00) * blob(uv, c1, 0.55) * 0.55;
  col += vec3(0.56, 0.35, 1.00) * blob(uv, c2, 0.65) * 0.45;
  col += vec3(0.10, 0.65, 1.00) * blob(uv, c3, 0.50) * 0.35;

  // Vignette toward bottom
  float v = smoothstep(1.4, 0.2, length(v_uv - vec2(0.5, 0.0)));
  col *= mix(0.85, 1.0, v);

  // Subtle grain
  float n = fract(sin(dot(v_uv * 1024.0 + u_time, vec2(12.9898, 78.233))) * 43758.5453);
  col += (n - 0.5) * 0.015;

  gl_FragColor = vec4(col, 1.0);
}
`

const VERT_PARTICLES = `
attribute vec3 a_seed; // x, y, phase
uniform float u_time;
uniform vec2 u_res;
varying float v_alpha;
varying float v_hue;

void main() {
  float t = u_time * 0.08;
  float drift = sin(a_seed.z + t * 0.6) * 0.04;
  float fx = fract(a_seed.x + t * 0.05) * 2.0 - 1.0;
  float fy = fract(a_seed.y - t * 0.03 + drift) * 2.0 - 1.0;
  gl_Position = vec4(fx, fy, 0.0, 1.0);

  float dpr = clamp(u_res.x / 800.0, 1.0, 2.5);
  gl_PointSize = (4.0 + 8.0 * fract(a_seed.z * 7.0)) * dpr;

  v_alpha = 0.10 + 0.20 * (0.5 + 0.5 * sin(a_seed.z * 9.0 + u_time * 0.9));
  v_hue = fract(a_seed.z * 3.13);
}
`

const FRAG_PARTICLES = `
precision highp float;
varying float v_alpha;
varying float v_hue;
void main() {
  vec2 p = gl_PointCoord - 0.5;
  float d = length(p);
  if (d > 0.5) discard;
  // Soft halo: bright core with smooth falloff
  float core = smoothstep(0.18, 0.0, d);
  float halo = smoothstep(0.5, 0.05, d);
  vec3 cool = vec3(0.70, 0.85, 1.00);
  vec3 warm = vec3(0.85, 0.75, 1.00);
  vec3 col = mix(cool, warm, v_hue);
  float a = (halo * 0.55 + core) * v_alpha;
  gl_FragColor = vec4(col, a);
}
`

function compile(gl, type, src) {
  const sh = gl.createShader(type)
  gl.shaderSource(sh, src)
  gl.compileShader(sh)
  if (!gl.getShaderParameter(sh, gl.COMPILE_STATUS)) {
    console.error(gl.getShaderInfoLog(sh))
    gl.deleteShader(sh)
    return null
  }
  return sh
}

function program(gl, vsSrc, fsSrc) {
  const vs = compile(gl, gl.VERTEX_SHADER, vsSrc)
  const fs = compile(gl, gl.FRAGMENT_SHADER, fsSrc)
  if (!vs || !fs) return null
  const p = gl.createProgram()
  gl.attachShader(p, vs)
  gl.attachShader(p, fs)
  gl.linkProgram(p)
  if (!gl.getProgramParameter(p, gl.LINK_STATUS)) {
    console.error(gl.getProgramInfoLog(p))
    return null
  }
  return p
}

export default function Background({ particleCount = 35 }) {
  const canvasRef = useRef(null)

  useEffect(() => {
    const canvas = canvasRef.current
    const gl = canvas.getContext('webgl', { antialias: true, premultipliedAlpha: false })
    if (!gl) return

    const bgProg = program(gl, VERT_FULL, FRAG_BG)
    const partProg = program(gl, VERT_PARTICLES, FRAG_PARTICLES)
    if (!bgProg || !partProg) return

    // Fullscreen triangle
    const quad = gl.createBuffer()
    gl.bindBuffer(gl.ARRAY_BUFFER, quad)
    gl.bufferData(gl.ARRAY_BUFFER, new Float32Array([-1, -1, 3, -1, -1, 3]), gl.STATIC_DRAW)

    // Particles
    const seeds = new Float32Array(particleCount * 3)
    for (let i = 0; i < particleCount; i++) {
      seeds[i * 3] = Math.random()
      seeds[i * 3 + 1] = Math.random()
      seeds[i * 3 + 2] = Math.random() * Math.PI * 2
    }
    const partBuf = gl.createBuffer()
    gl.bindBuffer(gl.ARRAY_BUFFER, partBuf)
    gl.bufferData(gl.ARRAY_BUFFER, seeds, gl.STATIC_DRAW)

    const bgLoc = {
      pos: gl.getAttribLocation(bgProg, 'a_position'),
      time: gl.getUniformLocation(bgProg, 'u_time'),
      res: gl.getUniformLocation(bgProg, 'u_res'),
    }
    const partLoc = {
      seed: gl.getAttribLocation(partProg, 'a_seed'),
      time: gl.getUniformLocation(partProg, 'u_time'),
      res: gl.getUniformLocation(partProg, 'u_res'),
    }

    const resize = () => {
      const dpr = Math.min(window.devicePixelRatio || 1, 2)
      const w = Math.floor(canvas.clientWidth * dpr)
      const h = Math.floor(canvas.clientHeight * dpr)
      if (canvas.width !== w || canvas.height !== h) {
        canvas.width = w
        canvas.height = h
      }
      gl.viewport(0, 0, canvas.width, canvas.height)
    }
    resize()
    window.addEventListener('resize', resize)

    const start = performance.now()
    let raf = 0
    let lastFrame = 0

    const render = (now) => {
      raf = requestAnimationFrame(render)
      // Frame-rate cap ~60 fps + skip when tab is hidden
      if (document.hidden) return
      if (now - lastFrame < 16) return
      lastFrame = now

      const t = (now - start) / 1000
      gl.clearColor(0.043, 0.051, 0.071, 1)
      gl.clear(gl.COLOR_BUFFER_BIT)

      // Background pass
      gl.useProgram(bgProg)
      gl.bindBuffer(gl.ARRAY_BUFFER, quad)
      gl.enableVertexAttribArray(bgLoc.pos)
      gl.vertexAttribPointer(bgLoc.pos, 2, gl.FLOAT, false, 0, 0)
      gl.uniform1f(bgLoc.time, t)
      gl.uniform2f(bgLoc.res, canvas.width, canvas.height)
      gl.drawArrays(gl.TRIANGLES, 0, 3)

      // Particles pass with additive blending
      gl.useProgram(partProg)
      gl.enable(gl.BLEND)
      gl.blendFunc(gl.SRC_ALPHA, gl.ONE)
      gl.bindBuffer(gl.ARRAY_BUFFER, partBuf)
      gl.enableVertexAttribArray(partLoc.seed)
      gl.vertexAttribPointer(partLoc.seed, 3, gl.FLOAT, false, 0, 0)
      gl.uniform1f(partLoc.time, t)
      gl.uniform2f(partLoc.res, canvas.width, canvas.height)
      gl.drawArrays(gl.POINTS, 0, particleCount)
      gl.disable(gl.BLEND)
    }
    raf = requestAnimationFrame(render)

    return () => {
      cancelAnimationFrame(raf)
      window.removeEventListener('resize', resize)
      gl.deleteBuffer(quad)
      gl.deleteBuffer(partBuf)
      gl.deleteProgram(bgProg)
      gl.deleteProgram(partProg)
    }
  }, [particleCount])

  return <canvas ref={canvasRef} className="bg-canvas" aria-hidden="true" />
}
