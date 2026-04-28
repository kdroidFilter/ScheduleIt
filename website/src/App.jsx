import { useEffect, useMemo, useState } from 'react'
import Background from './Background.jsx'

const REPO = 'kdroidFilter/ScheduleIt'
const API_URL = `https://api.github.com/repos/${REPO}/releases/latest`
const BASE = import.meta.env.BASE_URL

function detectOS() {
  if (typeof navigator === 'undefined') return 'unknown'
  const ua = navigator.userAgent.toLowerCase()
  if (ua.includes('win')) return 'windows'
  if (ua.includes('mac')) return 'mac'
  if (ua.includes('linux')) return 'linux'
  return 'unknown'
}

function classify(name) {
  const n = name.toLowerCase()
  if (n.endsWith('.appx') || n.endsWith('.msixbundle')) return null
  if (n.endsWith('.exe')) return 'windows'
  if (n.includes('mac') || n.endsWith('.dmg') || n.endsWith('.pkg')) return 'mac'
  if (n.includes('linux') || n.endsWith('.deb') || n.endsWith('.rpm') || n.endsWith('.appimage')) return 'linux'
  return 'other'
}

function prettyLabel(name) {
  const lower = name.toLowerCase()
  const arch =
    lower.includes('arm64') ? 'ARM64' :
    lower.includes('amd64') || lower.includes('x64') || lower.includes('x86_64') ? 'x64' :
    ''
  const ext = name.slice(name.lastIndexOf('.') + 1).toUpperCase()
  return [ext, arch].filter(Boolean).join(' · ')
}

function formatSize(bytes) {
  const mb = bytes / (1024 * 1024)
  return `${mb.toFixed(1)} MB`
}

const OS_META = {
  windows: { label: 'Windows', icon: '🪟' },
  mac: { label: 'macOS', icon: '🍎' },
  linux: { label: 'Linux', icon: '🐧' },
  other: { label: 'Other', icon: '📦' },
}

const GALLERY = [
  { src: `${BASE}screens/edit-event.jpg`, caption: 'Edit events with title, time, color and notes' },
  { src: `${BASE}screens/settings.jpg`, caption: 'Tune visible hours and per-day schedules' },
]

const FEATURES = [
  {
    icon: '🎒',
    title: 'Built for student life',
    body: 'From morning math to late-night study sessions, organize every class, lab and lecture in a layout that mirrors your real school week.',
  },
  {
    icon: '📚',
    title: 'Subjects, color-coded',
    body: 'Give each course its own color so you can tell English from Chemistry at a glance — no more mixing up rooms or showing up to the wrong class.',
  },
  {
    icon: '🔔',
    title: 'Never miss a deadline',
    body: 'Track exams, homework and project drop-offs alongside your regular schedule. Your agenda stays the single source of truth from Monday to Friday.',
  },
  {
    icon: '🏫',
    title: 'Like the school planner, but better',
    body: 'All the structure of the paper agenda your teachers told you to keep updated — minus the smudged ink, lost pages and forgotten Mondays.',
  },
  {
    icon: '⏰',
    title: 'Custom hours per day',
    body: 'Half-day on Wednesday? Late start on Friday? Tune visible hours per day so your week looks exactly like your real timetable.',
  },
  {
    icon: '☕',
    title: 'Study breaks included',
    body: 'Block out lunch, recess and free periods so you can actually see when you have time to breathe between two pop quizzes.',
  },
]

export default function App() {
  const [release, setRelease] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)
  const detected = useMemo(detectOS, [])

  useEffect(() => {
    fetch(API_URL)
      .then((r) => {
        if (!r.ok) throw new Error(`GitHub API ${r.status}`)
        return r.json()
      })
      .then(setRelease)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  const grouped = useMemo(() => {
    if (!release?.assets) return {}
    return release.assets.reduce((acc, a) => {
      const os = classify(a.name)
      if (!os) return acc
      if (!acc[os]) acc[os] = []
      acc[os].push(a)
      return acc
    }, {})
  }, [release])

  const order = ['windows', 'mac', 'linux', 'other'].sort((a, b) => {
    if (a === detected) return -1
    if (b === detected) return 1
    return 0
  })

  return (
    <>
      <Background />
      <div className="page">

      <header className="hero">
        <img src={`${BASE}icon.png`} alt="ScheduleIt logo" className="logo" />
        <h1>ScheduleIt</h1>
        <p className="tagline">Plan your week at a glance — fast, native, cross-platform.</p>
        {release && (
          <div className="version">
            <span className="badge">{release.tag_name}</span>
            <a href={release.html_url} target="_blank" rel="noreferrer">Release notes</a>
          </div>
        )}
      </header>

      <section className="showcase">
        <img
          src={`${BASE}screens/light.jpg`}
          alt="ScheduleIt weekly overview"
          className="hero-shot"
        />
      </section>

      <section className="gallery">
        {GALLERY.map((g) => (
          <figure key={g.src}>
            <img src={g.src} alt={g.caption} loading="lazy" />
            <figcaption>{g.caption}</figcaption>
          </figure>
        ))}
      </section>

      <section className="features">
        <h2>Your school week, finally under control</h2>
        <p className="features-lede">
          ScheduleIt was made for the way students actually plan: classes that
          repeat every week, exams that sneak up on you, and a homework pile
          that never sleeps. Drop your timetable in once and let the rest of
          the year run itself.
        </p>
        <div className="feature-grid">
          {FEATURES.map((f) => (
            <article key={f.title} className="feature-card">
              <div className="feature-icon">{f.icon}</div>
              <h3>{f.title}</h3>
              <p>{f.body}</p>
            </article>
          ))}
        </div>
      </section>

      <section className="download-section">
        <h2>Download</h2>
        {loading && <p className="status">Loading latest release…</p>}
        {error && <p className="status error">Could not load release: {error}</p>}

        {release && (
          <div className="downloads">
            {order
              .filter((os) => grouped[os]?.length)
              .map((os) => (
                <section key={os} className={`os-card ${os === detected ? 'highlight' : ''}`}>
                  <header>
                    <span className="os-icon">{OS_META[os].icon}</span>
                    <h3>{OS_META[os].label}</h3>
                    {os === detected && <span className="pill">Detected</span>}
                  </header>
                  <ul>
                    {grouped[os].map((asset) => (
                      <li key={asset.id}>
                        <a className="dl-btn" href={asset.browser_download_url}>
                          <span className="dl-name">{prettyLabel(asset.name)}</span>
                          <span className="dl-size">{formatSize(asset.size)}</span>
                        </a>
                      </li>
                    ))}
                  </ul>
                </section>
              ))}
          </div>
        )}
      </section>

      <footer className="footer">
        <a href={`https://github.com/${REPO}`} target="_blank" rel="noreferrer">
          View source on GitHub
        </a>
      </footer>
      </div>
    </>
  )
}
